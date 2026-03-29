import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useSettingsStore = defineStore('settings', () => {
  const searchType = ref<string>('hybrid')
  const timeBoost = ref<boolean>(false)
  const fromSourceDateTime = ref<string | null>(null)
  const untilSourceDateTime = ref<string | null>(null)
  const timeBoostMidPoint = ref<number>(0)
  const timeBoostScale = ref<number>(0)
  const limit = ref<number>(0)
  const timeBoostDateField = ref<string>('')

  return {
    searchType,
    timeBoost,
    fromSourceDateTime,
    untilSourceDateTime,
    timeBoostMidPoint,
    timeBoostScale,
    limit,
    timeBoostDateField,
  }
}, {
  persist: true,
})

