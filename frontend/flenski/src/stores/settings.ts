
import { defineStore } from 'pinia'
import { ref } from 'vue'

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
  }
}, {
  persist: true,
})

