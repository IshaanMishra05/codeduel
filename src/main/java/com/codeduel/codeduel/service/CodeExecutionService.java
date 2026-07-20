package com.codeduel.codeduel.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

@Service
public class CodeExecutionService {

    public String execute(String code, String input) {
        Path tempDir = null;
        try {
            // 1. Create a unique temporary folder for this submission
            // e.g. /tmp/codeduel8472619273/
            // Each submission gets its own folder so concurrent submissions don't collide
            tempDir = Files.createTempDirectory("codeduel");

            // 2. Write the submitted code to Main.java inside that folder
            // Must be named Main.java because the class inside is "public class Main"
            // Java compiler rule: filename must match the public class name
            Path javaFile = tempDir.resolve("Main.java");
            Files.writeString(javaFile, code);

            // 3. COMPILE — run "javac Main.java" as a subprocess
            ProcessBuilder compile = new ProcessBuilder("javac", javaFile.toString());
            compile.directory(tempDir.toFile()); // run javac from inside the temp folder
            compile.redirectErrorStream(true);   // merge stderr into stdout so we capture compile errors
            Process compileProcess = compile.start();
            compileProcess.waitFor();            // block until compilation finishes

            // 4. Check if compilation succeeded
            // Exit code 0 = success, anything else = compile error
            if (compileProcess.exitValue() != 0) {
                String error = new String(compileProcess.getInputStream().readAllBytes());
                return "COMPILE_ERROR: " + error;
            }

            // 5. RUN — run "java Main" as a subprocess
            ProcessBuilder run = new ProcessBuilder("java", "-Xmx128m", "Main");
            run.directory(tempDir.toFile()); // run java from inside the temp folder
            Process runProcess = run.start();

            // 6. Feed the test case input into the program's stdin
            // The submitted program reads from System.in — we write to it here
            // getOutputStream() = the pipe going INTO the subprocess (its stdin)
            if (input != null && !input.isEmpty()) {
                runProcess.getOutputStream().write(input.getBytes());
            }
            runProcess.getOutputStream().close(); // signal "no more input"

            // 7. Enforce a time limit — 5 seconds max
            // waitFor returns false if the process didn't finish in time
            boolean finished = runProcess.waitFor(5, TimeUnit.SECONDS);
            if (!finished) {
                runProcess.destroyForcibly(); // kill the process
                return "TIME_LIMIT_EXCEEDED";
            }

            // 8. Capture whatever the program printed to stdout
            // getInputStream() = the pipe coming OUT of the subprocess (its stdout)
            String output = new String(runProcess.getInputStream().readAllBytes());
            return output.trim(); // trim trailing newline — System.out.println adds \n

        } catch (IOException | InterruptedException e) {
            return "EXECUTION_ERROR: " + e.getMessage();

        } finally {
            // 9. Always clean up temp files, even if something threw an exception
            // "finally" runs regardless of success or failure
            if (tempDir != null) {
                try {
                    Files.walk(tempDir)
                            .sorted(Comparator.reverseOrder()) // delete files before folders
                            .forEach(path -> path.toFile().delete());
                } catch (IOException ignored) {}
            }
        }
    }
}