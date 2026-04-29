import { useState } from 'react'

function CharacterLookup() {
  const [characterName, setCharacterName] = useState('')
  const [realm, setRealm] = useState('')
  //const [result, setResult] = useState('')
  const [error, setError] = useState('')
  const [score, setScore] = useState('')
  const [professions, setProfessions] = useState('')
  const [searches, setSearches] = useState([])

  async function handleSubmit(event) {
    event.preventDefault()

    setError('')

    try {
      const scoreResponse = await fetch(`/api/lookup/mplusscore/${characterName}/${realm}`)
      const professionsResponse = await fetch(`/api/lookup/professions/${characterName}/${realm}`)

      if (!professionsResponse.ok && !scoreResponse.ok) {
        //alert('Something went wrong')
        setError('Failed to fetch character data')
        return
      }

      // Check if the char wasnt found in blizzard api
      if (scoreResponse.status === 404) {
        const msg = await scoreResponse.text()
        //alert(msg || 'Character not found')
        setError(msg)
        return
      }

      // Check if the char was found but has no score in current season
      if (scoreResponse.status === 204) {
        setError(`${characterName} - ${realm} has no M+ score for the current season`)
        return
      }

      const score = await scoreResponse.json()
      const professions = await professionsResponse.json()
      const professionsText = Array.isArray(professions) ? professions.join(', ') : String(professions)

      const normalizedName = characterName.trim().toLowerCase()
      const normalizedRealm = realm.trim().toLowerCase()

      //Check if the search already exists in the array
      const exists = searches.some(
        search => search.normalizedName === normalizedName && search.normalizedRealm === normalizedRealm
      )

      if (exists) {
        //alert('This character is already in the list')
        setError('Character already exists in the list')
        return
      }

      setSearches(prevSearches => [
        ...prevSearches,
        {
          normalizedName,
          normalizedRealm,
          score,
          professions: professionsText || 'None'
        }]
      )
      console.log(searches)
    } catch (error) {
      setError(error.message)
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

      {error && <p className="error">{error}</p>}

      <div className="lookup-results">
        <ul>
          {searches.map((search, index) => (
            <li key={index}>
              <strong>{search.normalizedName} - {search.normalizedRealm}</strong><br />
              M+ Score: {search.score}<br />
              Professions: {search.professions}
            </li>
          ))}
        </ul>
      </div>
    </section>
  )
}

export default CharacterLookup
