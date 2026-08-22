import { useEffect, useState } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import { ArrowLeft, LogOut, PlusCircle, ShieldCheck, Trash2 } from 'lucide-react'
import { problemsAPI } from '../services/api'
import { useAuthStore } from '../store'
import type { Problem } from '../types'

type FormState = {
  title: string
  description: string
  difficulty: 'EASY' | 'MEDIUM' | 'HARD'
  language: 'JAVA' | 'PYTHON' | 'CPP'
  timeLimit: string
  inputFormat: string
  outputFormat: string
  constraints: string
}

const initialFormState: FormState = {
  title: '',
  description: '',
  difficulty: 'EASY',
  language: 'JAVA',
  timeLimit: '',
  inputFormat: '',
  outputFormat: '',
  constraints: '',
}

const difficultyOptions: Array<FormState['difficulty']> = ['EASY', 'MEDIUM', 'HARD']
const languageOptions: Array<FormState['language']> = ['JAVA', 'PYTHON', 'CPP']

export function AdminPage() {
  const navigate = useNavigate()
  const { user, logout } = useAuthStore()
  const [form, setForm] = useState<FormState>(initialFormState)
  const [problems, setProblems] = useState<Problem[]>([])
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)

  if (user?.role !== 'ADMIN') {
    return <Navigate to="/" replace />
  }

  const loadProblems = async () => {
    try {
      const res = await problemsAPI.getAll()
      setProblems(Array.isArray(res.data) ? res.data : [])
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to load problems')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadProblems()
  }, [])

  const updateField = <K extends keyof FormState>(field: K, value: FormState[K]) => {
    setForm((prev) => ({ ...prev, [field]: value }))
  }

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    const title = form.title.trim()
    const description = form.description.trim()

    if (!title || !description || !form.difficulty || !form.language) {
      toast.error('Please fill in title, description, difficulty, and language.')
      return
    }

    try {
      setSubmitting(true)
      await problemsAPI.create(title, description, form.difficulty, form.language)
      toast.success('Problem created successfully')
      setForm(initialFormState)
      await loadProblems()
    } catch (error: any) {
      toast.error(error.response?.data?.message || error.response?.data?.error || 'Failed to create problem')
    } finally {
      setSubmitting(false)
    }
  }

  const handleDelete = async (problemId: string | number) => {
    const confirmed = window.confirm('Delete this problem?')
    if (!confirmed) return

    try {
      await problemsAPI.delete(problemId)
      toast.success('Problem deleted')
      await loadProblems()
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to delete problem')
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-900 via-purple-900 to-black text-white">
      <header className="bg-slate-900 border-b border-slate-700 px-6 py-4 flex flex-col gap-4 md:flex-row md:justify-between md:items-center">
        <div className="flex items-center gap-3">
          <ShieldCheck className="text-indigo-400" size={28} />
          <div>
            <h1 className="text-2xl font-bold">Admin Dashboard</h1>
            <p className="text-sm text-gray-400">{user?.username}</p>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={() => navigate('/')}
            className="flex items-center gap-2 px-4 py-2 rounded-lg bg-slate-700 hover:bg-slate-600 text-white transition"
          >
            <ArrowLeft size={18} />
            Home
          </button>
          <button
            type="button"
            onClick={() => {
              logout()
              navigate('/login')
            }}
            className="flex items-center gap-2 px-4 py-2 rounded-lg bg-red-600 hover:bg-red-700 text-white transition"
          >
            <LogOut size={18} />
            Logout
          </button>
        </div>
      </header>

      <main className="max-w-6xl mx-auto px-4 py-8 md:px-8">
        <section className="bg-slate-800 border border-slate-700 rounded-xl p-6 mb-8 shadow-lg">
          <div className="flex items-center gap-2 mb-6">
            <PlusCircle className="text-blue-400" size={22} />
            <h2 className="text-xl font-bold text-white">Add Problem</h2>
          </div>

          <form onSubmit={handleSubmit} className="space-y-5">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
              <div>
                <label className="block text-sm font-medium text-gray-300 mb-2">Title</label>
                <input
                  value={form.title}
                  onChange={(event) => updateField('title', event.target.value)}
                  className="w-full px-4 py-2.5 bg-slate-700 border border-slate-600 rounded-lg text-white focus:outline-none focus:border-blue-500"
                  placeholder="Problem title"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-300 mb-2">Difficulty</label>
                <select
                  value={form.difficulty}
                  onChange={(event) => updateField('difficulty', event.target.value as FormState['difficulty'])}
                  className="w-full px-4 py-2.5 bg-slate-700 border border-slate-600 rounded-lg text-white focus:outline-none focus:border-blue-500"
                  required
                >
                  {difficultyOptions.map((option) => (
                    <option key={option} value={option}>
                      {option}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-300 mb-2">Language</label>
                <select
                  value={form.language}
                  onChange={(event) => updateField('language', event.target.value as FormState['language'])}
                  className="w-full px-4 py-2.5 bg-slate-700 border border-slate-600 rounded-lg text-white focus:outline-none focus:border-blue-500"
                  required
                >
                  {languageOptions.map((option) => (
                    <option key={option} value={option}>
                      {option}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-300 mb-2">Time Limit</label>
                <input
                  type="number"
                  min="1"
                  value={form.timeLimit}
                  onChange={(event) => updateField('timeLimit', event.target.value)}
                  className="w-full px-4 py-2.5 bg-slate-700 border border-slate-600 rounded-lg text-white focus:outline-none focus:border-blue-500"
                  placeholder="Seconds"
                />
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-300 mb-2">Description</label>
              <textarea
                value={form.description}
                onChange={(event) => updateField('description', event.target.value)}
                className="w-full px-4 py-3 bg-slate-700 border border-slate-600 rounded-lg text-white focus:outline-none focus:border-blue-500 min-h-[120px]"
                placeholder="Describe the problem and expected behavior"
                required
              />
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
              <div>
                <label className="block text-sm font-medium text-gray-300 mb-2">Input Format</label>
                <textarea
                  value={form.inputFormat}
                  onChange={(event) => updateField('inputFormat', event.target.value)}
                  className="w-full px-4 py-3 bg-slate-700 border border-slate-600 rounded-lg text-white focus:outline-none focus:border-blue-500 min-h-[100px]"
                  placeholder="Optional input format"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-300 mb-2">Output Format</label>
                <textarea
                  value={form.outputFormat}
                  onChange={(event) => updateField('outputFormat', event.target.value)}
                  className="w-full px-4 py-3 bg-slate-700 border border-slate-600 rounded-lg text-white focus:outline-none focus:border-blue-500 min-h-[100px]"
                  placeholder="Optional output format"
                />
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-300 mb-2">Constraints</label>
              <textarea
                value={form.constraints}
                onChange={(event) => updateField('constraints', event.target.value)}
                className="w-full px-4 py-3 bg-slate-700 border border-slate-600 rounded-lg text-white focus:outline-none focus:border-blue-500 min-h-[100px]"
                placeholder="Optional constraints"
              />
            </div>

            <div className="flex justify-end">
              <button
                type="submit"
                disabled={submitting}
                className="bg-green-600 hover:bg-green-700 disabled:opacity-60 disabled:cursor-not-allowed text-white font-bold px-6 py-2.5 rounded-lg transition"
              >
                {submitting ? 'Creating...' : 'Create Problem'}
              </button>
            </div>
          </form>
        </section>

        <section className="bg-slate-800 border border-slate-700 rounded-xl p-6 shadow-lg">
          <h2 className="text-xl font-bold text-white mb-4">Existing Problems</h2>

          {loading ? (
            <div className="py-8 text-center text-gray-400">Loading problems...</div>
          ) : problems.length === 0 ? (
            <div className="py-8 text-center text-gray-400">No problems found.</div>
          ) : (
            <div className="space-y-4">
              {problems.map((problem) => (
                <div
                  key={String(problem.id)}
                  className="bg-slate-700 border border-slate-600 rounded-lg p-4 flex flex-col gap-4 md:flex-row md:items-center md:justify-between"
                >
                  <div className="space-y-2">
                    <div className="flex items-center gap-3 flex-wrap">
                      <h3 className="text-lg font-semibold text-white">{problem.title}</h3>
                      <span
                        className={`px-2.5 py-1 rounded-full text-xs font-semibold ${
                          problem.difficulty === 'EASY'
                            ? 'bg-green-900 text-green-200'
                            : problem.difficulty === 'MEDIUM'
                              ? 'bg-yellow-900 text-yellow-200'
                              : 'bg-red-900 text-red-200'
                        }`}
                      >
                        {problem.difficulty}
                      </span>
                    </div>
                    <p className="text-sm text-gray-300">
                      Time Limit: <span className="text-blue-400">{problem.timeLimit ?? 'N/A'}</span>
                    </p>
                  </div>

                  <button
                    type="button"
                    onClick={() => handleDelete(problem.id)}
                    className="inline-flex items-center justify-center gap-2 px-4 py-2 rounded-lg bg-red-600 hover:bg-red-700 text-white font-medium transition"
                  >
                    <Trash2 size={16} />
                    Delete
                  </button>
                </div>
              ))}
            </div>
          )}
        </section>
      </main>
    </div>
  )
}
