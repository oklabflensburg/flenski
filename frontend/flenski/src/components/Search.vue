<script setup lang="ts">
import { ref } from 'vue'
import InputText from 'primevue/inputtext'
import Button from 'primevue/button'
import Snippet from './Snippet.vue'
import type { Document } from '@/types/document'
import Answer from './Answer.vue'
import DateRange from '@/components/DateRange.vue'
import { useSettingsStore } from '@/stores/settings'

const settingsStore = useSettingsStore()

const searchTerm = ref('')
const searchResults = ref<Document[]>([])
const searched = ref(false)
const isWaitingForAnswer = ref(false)
const answer = ref('')
const startDate = ref<string | null>(null)
const endDate = ref<string | null>(null)

function resetValues() {
  searchResults.value = []
  searched.value = false
  isWaitingForAnswer.value = false
  answer.value = ''
  startDate.value = null
  endDate.value = null
}

async function onSearch() {
  resetValues()
  isWaitingForAnswer.value = true
  const backendEndpoint = import.meta.env.VITE_BACKEND_ENDPOINT
  const url = `${backendEndpoint.replace(/\/$/, '')}/api/chat/query?q=${encodeURIComponent(searchTerm.value)}`

  try {
    const response = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        queryMode: settingsStore.searchType.toUpperCase(),
        enableTimeBoost: settingsStore.timeBoost,
        fromSourceDateTime: settingsStore.fromSourceDateTime ?? null,
        untilSourceDateTime: settingsStore.untilSourceDateTime ?? null,
        timeBoostScale: settingsStore.timeBoostScale ?? 0,
        limit: settingsStore.limit,
        collection: settingsStore.collection || null,
        enableTitleBoost: settingsStore.titleBoost || false,
        titleBoostFactor: settingsStore.titleBoostFactor || 0
      }),
    })

    const reader = response.body!.getReader()
    const decoder = new TextDecoder()
    let buffer = ''
    let eventName = ''
    const dataLines: string[] = []

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() ?? ''


      for (const line of lines) {
        if (line.startsWith('event:')) {
          eventName = line.slice(6).trim()
        } else if (line.startsWith('data:')) {
          dataLines.push(line.slice(5).trim())
        } else if (line === '') {
          if (eventName && dataLines.length > 0) {
            handleSseEvent(eventName, dataLines.join('\n'))
          }
          eventName = ''
          dataLines.length = 0
        }
      }
    }
  } catch {
    searchResults.value = []
    searched.value = true
  } finally {
    isWaitingForAnswer.value = false
  }
}

function handleSseEvent(eventName: string, data: string) {
  if (eventName === 'documents') {
    try {
      searchResults.value = JSON.parse(data)
      searched.value = true
    } catch {
      searchResults.value = []
      searched.value = true
    }
  } else if (eventName === 'dateRange') {
    try {
      const parsed = JSON.parse(data)
      startDate.value = parsed.startDate ?? null
      endDate.value = parsed.endDate ?? null
    } catch {
      startDate.value = null
      endDate.value = null
    }
  } else if (eventName === 'answer') {
    answer.value = data
    isWaitingForAnswer.value = false
  }
}
</script>
<template>
  <div class="flex flex-col items-center justify-center gap-8 p-6 bg-white">
    <h2 class="text-2xl font-semibold text-gray-800 mb-2">FlensKI</h2>
    <div class="flex w-full max-w-250 gap-1">
      <InputText
        v-model="searchTerm"
        placeholder="was möchtest Du über Flensburg wissen?"
        class="flex-1"
        size="large"
        @keyup.enter="onSearch"
      />
      <Button label="Fragen" icon="pi pi-search" @click="onSearch" size="large" />
    </div>
    <DateRange :startDate="startDate" :endDate="endDate" />
    <Answer v-if="answer" :answer="answer" />
    <ProgressSpinner v-if="isWaitingForAnswer" style="width: 40px" strokeWidth="6" />
    <div v-if="searchResults.length > 0" class="w-full max-w-250 mt-2">
      <h2 class="text-lg mb-5">Quellen</h2>
      <div v-for="(document, idx) in searchResults" :key="idx">
        <Snippet :document="document" :keyword="searchTerm" />
      </div>
    </div>
    <div v-else-if="searched" class="text-gray-400 mt-6">Keine Ergebnisse gefunden.</div>
  </div>
</template>
