import { useState } from 'react'
import { useEffect } from 'react'

function CharacterLookup() {
  const [characterName, setCharacterName] = useState('')
  const [realm, setRealm] = useState('')
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
      console.log("Character not found: " + characterName + " on realm: " + realm+" in score fetch")
      setError("Character not found")
      return 404;
    }

    if (response.status === 204) {
      return 204;
    }

    const scoreData = await response.json()
    setScore(scoreData)
    console.log("Fetched M+ score: ", scoreData)
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

    const professionData = await response.json()
    setProfessions(professionData)
    console.log("Fetched professions: ", professionData)
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

    if (response.status === 204) {
      const msg = await response.text()
      console.log("babayaga" + msg)
      setError("Character does not have raid progression for current expansion")
      return 204;
    }
    if (response.status === 404) {
      setError("Character not found")
      console.log("Character not found: " + characterName + " on realm: " + realm+" in raid progress fetch")
      return 404;
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

      

      if (scoreData === 404 || raidProgressData === 404) {
        setLoading(false)
        console.log("Character does not exist, aborting search")
        return
      }

      if (scoreData === 204 || raidProgressData === 204) {
        setLoading(false)
        if (scoreData === 204 && raidProgressData === 204) {
          setError(characterName + " has no M+ score or raid progression for the current season")
        } else if (scoreData === 204) {
          setError(characterName + " has no M+ score for the current season")
        } else {
          setError(characterName + " has no raid progression for the current season")
        }
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
          //If score data is 204 from the backend status code then we set it to 0
          score: scoreData !== 204 ? scoreData : 0,
          professions: professionsText || 'None',
          raidProgress: Array.isArray(raidProgressData) ? raidProgressData : []
        }]
      )
      console.log(searches)
      setLoading(false)
    } catch (error) {
      setError(error.message)
      console.log("error i catchen: " + error.message)
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
                    <td key={raid.name} className={'difficulty-' + (progress?.progressDifficulty ?? 'none')}>
                      {progress?.progressBossCount ?? 0}/{raid.bossCount}</td>
                  )
                })}
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      
      <div className="legend">
        <p><strong>Difficulty Legend:</strong></p>
        <p><span className="difficulty-4">Orange</span> = Mythic</p>
        <p><span className="difficulty-3">Pink</span> = Heroic</p>
        <p><span className="difficulty-2">Blue</span> = Normal</p>
        <p><span className="difficulty-1">Green</span> = LFR</p>
      </div>

      <div className="explanation">
        <h2>The raid progression shows the color of the hardest difficulty the character has done with atleast one boss defeated</h2>
      </div>
    </section>
    
  )
}

export default CharacterLookup
