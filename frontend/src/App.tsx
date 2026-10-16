import { useEffect, useState } from 'react'
import './App.css'

function App() {
  const [apiStatus, setApiStatus] = useState('Checking...')

  useEffect(() => {
    fetch('http://localhost:8080/api/health')
      .then((response) => {
        if (!response.ok) {
          throw new Error('API request failed')
        }
        return response.json()
      })
      .then((data) => setApiStatus(data.status))
      .catch(() => setApiStatus('Unavailable'))
  }, [])
  return (
    <main className="dashboard">
      <header className="header">
        <h1>FitStud</h1>
        <p>Train smarter. Stay consistent.</p>
      </header>

      <section className="welcome">
        <p className="eyebrow">YOUR FITNESS SPACE</p>
        <h2>Ready to get stronger?</h2>
        <p>Every workout is a step toward your goal.</p>
        <button onClick={() => alert('Workout logging coming soon!')}>
          Start Workout
        </button>
      </section>
      <section className="stat-card">
        <p>Backend Status</p>
        <h3>{apiStatus}</h3>
      </section>

      <section className="stats">
        <article className="stat-card">
          <p>This Week</p>
          <h3>0 workouts</h3>
        </article>

        <article className="stat-card">
          <p>Total Volume</p>
          <h3>0 kg</h3>
        </article>
      </section>
    </main>
  )
}

export default App
