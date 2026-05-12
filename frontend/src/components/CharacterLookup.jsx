import { useState } from 'react'
import { useEffect } from 'react'

function CharacterLookup() {
  const [characterName, setCharacterName] = useState('')
  const [realm, setRealm] = useState('')
  //const [result, setResult] = useState('')
  const [error, setError] = useState('')
  const [score, setScore] = useState('')
  const [professions, setProfessions] = useState('')
  const [searches, setSearches] = useState([])
  const [activeColumn, setActiveColumn] = useState('')
  const [sortDirection, setSortDirection] = useState('asc')
  const [loading, setLoading] = useState(false)
  const [raidInfo, setRaidInfo] = useState([])
  const [characterRaidProgress, setCharacterRaidProgress] = useState([])

  //Running this instantly as the page loads
  useEffect(() => {
    async function fetchRaidNames() {
      const response = await fetch('/api/lookup/raidnames')
      if (!response.ok) {
        setError('Failed to fetch raid names')
        return
      }
      console.log('we here')
      const data = await response.json()
      setRaidInfo(data)
      console.log('Fetched raid info:', data)

    }
    fetchRaidNames()
  }, [])

  /**
   * 
   * @param {*} characterName 
   * @param {*} realm 
   * @returns 
   */
  async function fetchCharacterScore(characterName, realm) {
    const response = await fetch(`/api/lookup/mplusscore/${characterName}/${realm}`)

    if (response.status === 404) {
      const msg = await response.text()
      setError(msg)
      return null
    }

    if (response.status === 204) {
      setError(`${characterName} - ${realm} has no M+ score for the current season`)
      return null
    }

    if (!response.ok) {
      setError('Failed to fetch M+ score')
      return null
    }

    const scoreData = await response.json()
    setScore(scoreData)
    return scoreData
  }

  /**
   * 
   * @param {*} characterName 
   * @param {*} realm 
   * @returns 
   */
  async function fetchCharacterProfessions(characterName, realm) {
    const response = await fetch(`/api/lookup/professions/${characterName}/${realm}`)

    if (!response.ok) {
      setError('Failed to fetch professions')
      return null
    }

    const professionData = await response.json()
    setProfessions(professionData)
    return professionData
  }

  /**
   * 
   * @param {*} characterName 
   * @param {*} realm 
   * @returns 
   */
  async function fetchCharacterRaidProgress(characterName, realm) {
    const response = await fetch(`/api/lookup/raidprogress/${characterName}/${realm}`)

    if (!response.ok) {
      setError('Failed to fetch raid progression data')
      return null
    }

    const raidProgressData = await response.json()
    setCharacterRaidProgress(raidProgressData)
    console.log("Fetched raid progression data: ", raidProgressData)
    return raidProgressData
  }

  /**
   * 
   * @param {*} event 
   * @returns 
   */
  async function handleSubmit(event) {
    event.preventDefault()

    setError('')
    setLoading(true)

    try {
      const [scoreData, professions, raidProgressData] = await Promise.all([
        fetchCharacterScore(characterName, realm),
        fetchCharacterProfessions(characterName, realm),
        fetchCharacterRaidProgress(characterName, realm)
      ])

      if (!scoreData || !professions || !raidProgressData) {
        setLoading(false)
        return
      }

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
        setLoading(false)
        return
      }

      setSearches(prevSearches => [
        ...prevSearches,
        {
          normalizedName,
          normalizedRealm,
          score: scoreData,
          professions: professionsText || 'None',
          raidProgress: raidProgressData
        }]
      )
      console.log(searches)
      setLoading(false)
    } catch (error) {
      setError(error.message)
      setLoading(false)
    }
  }

  /**
   * 
   * @param {*} column 
   */
  function handleSort(column) {

    if (sortDirection === 'asc') {
      setSortDirection('desc')
    } else {
      setSortDirection('asc')
    }

    const sortedSearches = [...searches].sort((a, b) => {
      switch (column) {
        case 'score': {
          return sortDirection === 'asc' ? a.score - b.score : b.score - a.score
        }
        case 'name':
          return sortDirection === 'asc'
            ? a.normalizedName.localeCompare(b.normalizedName)
            : b.normalizedName.localeCompare(a.normalizedName)
      }
    })
    setSearches(sortedSearches)
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
      {loading && <p className="loading">Searching for character...</p>}

      <div className="lookup-results">
        <table>
          <thead>
            <tr>
              <th onClick={() => handleSort('name')}>Character</th>
              <th>Realm</th>
              <th onClick={() => handleSort('score')}>M+ Score</th>
              <th>Professions</th>
              {raidInfo.map((raid) => (
                <th key={raid.name}>{raid.name}</th>
              ))}
              
              
            </tr>
          </thead>
          <tbody>
            {searches.map((search, index) => (
              <tr key={index}>
                <td>{search.normalizedName}</td>
                <td>{search.normalizedRealm}</td>
                <td>{search.score}</td>
                <td>{search.professions}</td>
                {raidInfo.map((raid) => {
                  const progress = (search.raidProgress || []).find(progress => progress.raidName === raid.name)
                  return (
                    <td key={raid.name} className={'difficulty-' + (progress?.progressDifficulty)}>
                      {progress.progressBossCount}/{raid.bossCount}</td>
                  )
                })}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  )
}

export default CharacterLookup
