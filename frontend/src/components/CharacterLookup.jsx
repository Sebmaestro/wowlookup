import { useState } from 'react'

function CharacterLookup() {
  const [characterName, setCharacterName] = useState('')
  const [realm, setRealm] = useState('')
  const [result, setResult] = useState('')
  const [error, setError] = useState('')

  async function handleSubmit(event) {
    event.preventDefault()

    if (!characterName || !realm) {
      setError('Enter character and realm')
      setResult('')
      return
    }

    setError('')

    try {
      const scoreResponse = await fetch(`/api/lookup/mplusscore/${characterName}/${realm}`)
      const professionsResponse = await fetch(`/api/lookup/professions/${characterName}/${realm}`)

      if (!scoreResponse.ok || !professionsResponse.ok) {
        throw new Error('Failed to fetch character data');
      }

      const score = await scoreResponse.json()
      const professions = await professionsResponse.json()
      const professionsText = Array.isArray(professions)
        ? professions.join(', ')
        : String(professions)

      setResult(`M+ Score: ${score} | Professions: ${professionsText || 'None'}`)
    } catch (error) {
      setError(error.message)
      setResult("")
    }
  }

  return (
    <section className="lookup-panel">
      <h1>WoW Character Lookup</h1>
      <form className="lookup-form" onSubmit={handleSubmit}>
        <label>
          Character
          <input
            value={characterName}
            onChange={(event) => setCharacterName(event.target.value)}
            placeholder="Name"
          />
        </label>
        <label>
          Realm
          <input
            value={realm}
            onChange={(event) => setRealm(event.target.value)}
            placeholder="Realm"
          />
        </label>
        <button type="submit">Search</button>
      </form>

      <div className="lookup-results">
        {error && <p className="error">{error}</p>}
        {result && <p>{result}</p>}
      </div>
    </section>
  )
}

export default CharacterLookup
