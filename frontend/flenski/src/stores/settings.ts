import {defineStore} from 'pinia'
import {ref} from 'vue'

export const useSettingsStore = defineStore('settings', () => {
  const searchType = ref<string>('hybrid')
  const timeBoost = ref<boolean>(false)
  const fromSourceDateTime = ref<string | null>(null)
  const untilSourceDateTime = ref<string | null>(null)
  const timeBoostScale = ref<number>(0)
  const titleBoost = ref<boolean>(false)
  const titleBoostFactor = ref<number>(0)
  const limit = ref<number>(0)
  const collection = ref<string>('production')
  const categories = ref<string[]>([])
  const sparseSearchScoreThreshold = ref<number | null>(null)
  const denseSearchScoreThreshold = ref<number | null>(null)

  async function fetchDefaultParameters() {
    try {
      const backendEndpoint = import.meta.env.VITE_BACKEND_ENDPOINT
      const url = `${backendEndpoint.replace(/\/$/, '')}/api/chat/defaultParameters`
      const response = await fetch(url, { method: 'get' })
      if (!response.ok) throw new Error('Failed to fetch default parameters')
      const data = await response.json()
      if (data.queryMode) searchType.value = data.queryMode.toLowerCase()
      if (data.enableTimeBoost !== undefined) timeBoost.value = data.enableTimeBoost
      if (data.fromSourceDateTime) fromSourceDateTime.value = data.fromSourceDateTime
      if (data.untilSourceDateTime) untilSourceDateTime.value = data.untilSourceDateTime
      if (data.timeBoostScale !== undefined) timeBoostScale.value = data.timeBoostScale
      if (data.limit !== undefined) limit.value = data.limit
      if (data.collection) collection.value = data.collection
      if (data.enableTitleBoost !== undefined) titleBoost.value = data.enableTitleBoost
      if (data.titleBoostFactor !== undefined) titleBoostFactor.value = data.titleBoostFactor
      if (Array.isArray(data.categories)) categories.value = data.categories
      if (data.sparseSearchScoreThreshold !== undefined) sparseSearchScoreThreshold.value = data.sparseSearchScoreThreshold
      if (data.denseSearchScoreThreshold !== undefined) denseSearchScoreThreshold.value = data.denseSearchScoreThreshold
    } catch (e) {
      // Optionally handle error (e.g., show notification)
      // console.error(e)
    }
  }

  return {
    searchType,
    timeBoost,
    fromSourceDateTime,
    untilSourceDateTime,
    timeBoostScale,
    limit,
    collection,
    titleBoostFactor,
    titleBoost,
    categories,
    fetchDefaultParameters,
    sparseSearchScoreThreshold,
    denseSearchScoreThreshold,
  }
}, {
  persist: true,
})

