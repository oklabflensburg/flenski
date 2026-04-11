<script setup lang="ts">
import { computed } from 'vue'
import { storeToRefs } from 'pinia'
import { useSettingsStore } from '@/stores/settings'

defineProps({
  visibleSettings: {
    type: Boolean,
    required: true
  }
})
const emit = defineEmits(['update:visibleSettings'])

const searchTypes = [
  { label: 'Lexikalisch', value: 'lexical' },
  { label: 'Semantisch', value: 'semantic' },
  { label: 'Hybrid', value: 'hybrid' }
]

const settingsStore = useSettingsStore()
const { searchType, timeBoost, timeBoostScale, limit, collection, titleBoost, titleBoostFactor, categories } = storeToRefs(settingsStore)
const categoriesString = computed({
  get: () => (categories.value && categories.value.length > 0)
    ? categories.value.join(', ')
    : '',
  set: (val: string) => {
    categories.value = val
      .split(',')
      .map(s => s.trim())
      .filter(s => s.length > 0)
  }
})

const collections = [
  { label: 'Produktion', value: 'production' },
  { label: 'Test', value: 'test' }
]

const fromDate = computed<Date | null>({
  get: () => settingsStore.fromSourceDateTime ? new Date(settingsStore.fromSourceDateTime) : null,
  set: (val: Date | null) => { settingsStore.fromSourceDateTime = val ? val.toISOString() : null }
})

const untilDate = computed<Date | null>({
  get: () => settingsStore.untilSourceDateTime ? new Date(settingsStore.untilSourceDateTime) : null,
  set: (val: Date | null) => { settingsStore.untilSourceDateTime = val ? val.toISOString() : null }
})

</script>

<template>
  <Dialog :visible="visibleSettings" @update:visible="emit('update:visibleSettings', $event)" modal :dismissableMask="true" header="Einstellungen" :style="{ width: '60rem' }">
    <form class="grid grid-cols-4 gap-6 py-2" @submit.prevent>

      <div class="flex flex-col gap-2">
        <label for="searchType" class="font-medium text-gray-700">Suchmodus</label>
        <Select
          id="searchType"
          v-model="searchType"
          :options="searchTypes"
          optionLabel="label"
          optionValue="value"
          placeholder="Suchmodus wählen"
          class="w-full"
          size="small"
        />
      </div>

      <div class="flex flex-col gap-2">
        <label for="collection" class="font-medium text-gray-700">Collection</label>
        <Select
          id="collection"
          v-model="collection"
          :options="collections"
          optionLabel="label"
          optionValue="value"
          placeholder="Collection wählen"
          class="w-full"
          size="small"
        />
      </div>
      <div class="flex flex-col gap-2">
        <label for="categories" class="font-medium text-gray-700">Categories</label>
        <input
          id="categories"
          v-model="categoriesString"
          type="text"
          class="w-full border border-gray-300 rounded px-2 py-1 text-sm"
          placeholder="e.g. news, sports, tech"
        />
        <small class="text-gray-500">Comma-separated list of categories.</small>
      </div>
      <div class="flex flex-col gap-2">
      </div>

      <div class="flex flex-col gap-2">
        <label class="font-medium text-gray-700">Time Boost</label>
        <div class="flex items-center gap-2">
          <Checkbox inputId="timeBoost" v-model="timeBoost" :binary="true" />
          <label for="timeBoost" class="cursor-pointer">Time Boost aktivieren</label>
        </div>
        <small class="text-gray-500">Neuere Dokumente werden bei der Suche höher gewichtet.</small>
      </div>

      <!-- Time Boost Scale -->
      <div class="flex flex-col gap-2">
        <label for="timeBoostScale" class="font-medium text-gray-700">Time Boost Scale</label>
        <InputNumber
          id="timeBoostScale"
          v-model="timeBoostScale"
          class="w-full"
          placeholder="0 = Standardwert"
          size="small"
        />
        <small class="text-gray-500">Skalierungsfaktor für den Time Boost in Tagen.</small>
      </div>
      <div class="flex flex-col gap-2">
        <a
          href="https://qdrant.tech/documentation/search/search-relevance/#decay-functions"
          target="_blank"
          rel="noopener noreferrer"
          aria-label="More information about time boost (opens in new tab)"
          class="inline-flex items-center text-blue-600 hover:text-blue-800 focus:outline-none"
        >
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor" viewBox="0 0 20 20" aria-hidden="true">
            <circle cx="10" cy="10" r="9" stroke="currentColor" stroke-width="2" fill="none" />
            <text x="10" y="15" text-anchor="middle" font-size="12" fill="currentColor" font-family="Arial, sans-serif">?</text>
          </svg>
        </a>
      </div>
      <div class="flex flex-col gap-2"></div>


      <div class="flex flex-col gap-2">
        <label class="font-medium text-gray-700">Title Boost</label>
        <div class="flex items-center gap-2">
          <Checkbox inputId="titleBoost" v-model="titleBoost" :binary="true" />
          <label for="titleBoost" class="cursor-pointer">Title Boost aktivieren</label>
        </div>
      </div>

      <div class="flex flex-col gap-2">
        <label for="timeBoostScale" class="font-medium text-gray-700">Title Boost Faktor</label>
        <InputNumber
          id="titleBoostScale"
          v-model="titleBoostFactor"
          class="w-full"
          placeholder="0 = Standardwert"
          size="small"
          :minFractionDigits="1" :maxFractionDigits="1"
        />

      </div>
      <div class="flex flex-col gap-2"></div>


      <div class="flex flex-col gap-2">

      </div>

      <!-- Von Datum -->
      <div class="flex flex-col gap-2">
        <label for="fromDate" class="font-medium text-gray-700">Von Datum</label>
        <DatePicker
          id="fromDate"
          v-model="fromDate"
          showTime
          hourFormat="24"
          showButtonBar
          dateFormat="dd.mm.yy"
          class="w-full"
          placeholder="Kein Filter"
          size="small"
        />
      </div>

      <!-- Bis Datum -->
      <div class="flex flex-col gap-2">
        <label for="untilDate" class="font-medium text-gray-700">Bis Datum</label>
        <DatePicker
          id="untilDate"
          v-model="untilDate"
          showTime
          hourFormat="24"
          showButtonBar
          dateFormat="dd.mm.yy"
          class="w-full"
          placeholder="Kein Filter"
          size="small"
        />
        <small class="text-gray-500">Nur Dokumente bis zu diesem Datum berücksichtigen.</small>
      </div>

      <!-- Limit -->
      <div class="flex flex-col gap-2">
        <label for="limit" class="font-medium text-gray-700">Ergebnislimit</label>
        <InputNumber
          id="limit"
          v-model="limit"
          :min="0"
          class="w-full"
          placeholder="0 = Standardwert"
          size="small"
        />
        <small class="text-gray-500">Maximale Anzahl an Suchergebnissen. 0 = Konfigurationsstandard.</small>
      </div>
    </form>
  </Dialog>
</template>

