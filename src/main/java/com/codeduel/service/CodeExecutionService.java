package com.codeduel.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * Sandboxed code execution engine.
 *
 * Flow per submission:
 *   1. Write source file to a temp directory
 *   2. Compile (Java/C++) or skip (Python)
 *   3. Run against each test case, feeding stdin, capturing stdout
 *   4. Compare trimmed output to expected
 *   5. Clean up temp files
 *
 * Security notes for production:
 *   - Run each process as an unprivileged OS user (e.g., "runner")
 *   - Use Linux namespaces / cgroups (Docker or nsjail) for proper isolation
 *   - This ProcessBuilder approach is fine for a portfolio project / demo
 */
@Service
@Slf4j
public class CodeExecutionService {

    @Value("${app.execution.timeout-ms:5000}")
    private long timeoutMs;

    public record TestResult(
            boolean passed,
            String actualOutput,
            String expectedOutput,
            long executionMs,
            String errorMessage
    ) {}

    public record ExecutionResult(
            String status,          // ACCEPTED | WRONG_ANSWER | COMPILE_ERROR | RUNTIME_ERROR | TIME_LIMIT
            int testsPassed,
            int totalTests,
            long executionMs,
            String compilerOutput,
            List<TestResult> testResults
    ) {}

    // ------------------------------------------------------------------ //

    public ExecutionResult execute(String code, String language, List<com.codeduel.entity.TestCase> testCases) {
        Path workDir = null;
        try {
            workDir = Files.createTempDirectory("codeduel_" + UUID.randomUUID());

            // Step 1: Compile (if needed)
            CompileResult compileResult = compile(code, language, workDir);
            if (!compileResult.success()) {
                return new ExecutionResult("COMPILE_ERROR", 0, testCases.size(),
                        0, compileResult.output(), List.of());
            }

            // Step 2: Run each test case
            int passed = 0;
            long totalMs = 0;
            java.util.List<TestResult> results = new java.util.ArrayList<>();

            for (com.codeduel.entity.TestCase tc : testCases) {
                TestResult result = runTestCase(language, workDir, tc.getInput(), tc.getExpectedOutput());
                results.add(result);
                if (result.passed()) passed++;
                totalMs += result.executionMs();

                // Short-circuit on TLE to avoid wasting time
                if ("TIME_LIMIT".equals(result.errorMessage())) {
                    return new ExecutionResult("TIME_LIMIT", passed, testCases.size(),
                            totalMs, null, results);
                }
            }

            String status = (passed == testCases.size()) ? "ACCEPTED" : "WRONG_ANSWER";
            return new ExecutionResult(status, passed, testCases.size(), totalMs, null, results);

        } catch (Exception e) {
            log.error("Execution engine error", e);
            return new ExecutionResult("RUNTIME_ERROR", 0, testCases.size(), 0,
                    e.getMessage(), List.of());
        } finally {
            if (workDir != null) cleanup(workDir);
        }
    }

    // ------------------------------------------------------------------ //
    //  Compilation
    // ------------------------------------------------------------------ //

    private record CompileResult(boolean success, String output) {}

    private CompileResult compile(String code, String language, Path workDir) throws IOException {
        return switch (language.toLowerCase()) {
            case "java"   -> compileJava(code, workDir);
            case "cpp"    -> compileCpp(code, workDir);
            case "python" -> {
                // Python is interpreted — just write the file
                Files.writeString(workDir.resolve("Solution.py"), code);
                yield new CompileResult(true, "");
            }
            default -> new CompileResult(false, "Unsupported language: " + language);
        };
    }

    private CompileResult compileJava(String code, Path workDir) throws IOException {
        Files.writeString(workDir.resolve("Solution.java"), code);
        ProcessResult result = runProcess(
                List.of("javac", "Solution.java"),
                workDir, null, 10_000
        );
        return new CompileResult(result.exitCode() == 0, result.stderr());
    }

    private CompileResult compileCpp(String code, Path workDir) throws IOException {
        Files.writeString(workDir.resolve("solution.cpp"), code);
        ProcessResult result = runProcess(
                List.of("g++", "-O2", "-o", "solution", "solution.cpp"),
                workDir, null, 15_000
        );
        return new CompileResult(result.exitCode() == 0, result.stderr());
    }

    // ------------------------------------------------------------------ //
    //  Test case execution
    // ------------------------------------------------------------------ //

    private TestResult runTestCase(String language, Path workDir, String input, String expected) {
        List<String> command = switch (language.toLowerCase()) {
            case "java"   -> List.of("java", "-Xmx128m", "Solution");
            case "python" -> List.of("python3", "Solution.py");
            case "cpp"    -> List.of(workDir.resolve("solution").toString());
            default       -> throw new IllegalArgumentException("Unknown language: " + language);
        };

        long start = System.currentTimeMillis();
        try {
            ProcessResult result = runProcess(command, workDir, input, timeoutMs);
            long elapsed = System.currentTimeMillis() - start;

            if (result.timedOut()) {
                return new TestResult(false, "", expected, elapsed, "TIME_LIMIT");
            }
            if (result.exitCode() != 0) {
                return new TestResult(false, result.stderr(), expected, elapsed, "RUNTIME_ERROR");
            }

            String actual   = result.stdout().trim();
            String exp      = expected.trim();
            boolean passed  = actual.equals(exp);
            return new TestResult(passed, actual, exp, elapsed, passed ? null : "WRONG_ANSWER");

        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            return new TestResult(false, "", expected, elapsed, "RUNTIME_ERROR: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------ //
    //  Low-level process runner
    // ------------------------------------------------------------------ //

    private record ProcessResult(int exitCode, String stdout, String stderr, boolean timedOut) {}

    private ProcessResult runProcess(List<String> command, Path workDir,
                                      String stdin, long timeoutMs) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(command)
                .directory(workDir.toFile())
                .redirectErrorStream(false);

        // Prevent subprocess from inheriting parent environment vars
        pb.environment().clear();
        pb.environment().put("PATH", "/usr/bin:/bin");

        Process process = pb.start();

        // Feed stdin asynchronously to avoid blocking on large inputs
        if (stdin != null && !stdin.isEmpty()) {
            try (OutputStream os = process.getOutputStream()) {
                os.write(stdin.getBytes());
            }
        } else {
            process.getOutputStream().close();
        }

        // Capture stdout and stderr concurrently
        ExecutorService pool = Executors.newFixedThreadPool(2);
        Future<String> stdoutFuture = pool.submit(() -> readStream(process.getInputStream()));
        Future<String> stderrFuture = pool.submit(() -> readStream(process.getErrorStream()));

        boolean finished;
        try {
            finished = process.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            process.destroyForcibly();
            return new ProcessResult(-1, "", "Interrupted", true);
        }

        if (!finished) {
            process.destroyForcibly();
            pool.shutdownNow();
            return new ProcessResult(-1, "", "Time limit exceeded", true);
        }

        String stdout = "";
        String stderr = "";
        try {
            stdout = stdoutFuture.get(1, TimeUnit.SECONDS);
            stderr = stderrFuture.get(1, TimeUnit.SECONDS);
        } catch (Exception ignored) {}
        pool.shutdown();

        return new ProcessResult(process.exitValue(), stdout, stderr, false);
    }

    private String readStream(InputStream is) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            int linesRead = 0;
            while ((line = reader.readLine()) != null && linesRead < 1000) {
                sb.append(line).append("\n");
                linesRead++;
            }
        } catch (IOException ignored) {}
        return sb.toString();
    }

    private void cleanup(Path dir) {
        try {
            Files.walk(dir)
                    .sorted(java.util.Comparator.reverseOrder())
                    .forEach(p -> { try { Files.delete(p); } catch (IOException ignored) {} });
        } catch (IOException e) {
            log.warn("Failed to clean up temp dir: {}", dir);
        }
    }
}
