<script setup lang="ts">
import { ref } from 'vue'
import InputText from 'primevue/inputtext'
import Button from 'primevue/button'
import Snippet from './Snippet.vue'
import type { Document } from '@/types/document'
import Answer from './Answer.vue'
import DateRange from '@/components/DateRange.vue'

const searchTerm = ref('')
const searchResults = ref<Document[]>([])
const searched = ref(false)
const isWaitingForAnswer = ref(false)
const answer = ref(`Die **Ratsversammlung** ist das zentrale politische Gremium der Stadt Flensburg. Sie trifft Entscheidungen zu wichtigen kommunalen Themen wie Haushalt, Stadtentwicklung, Kultur und Soziales. ### Wichtige Punkte zur Ratsversammlung in Flensburg: - **Zusammensetzung**: Die Ratsversammlung besteht aus gewählten Vertreter:innen der politischen Parteien und Wählergemeinschaften. Die Anzahl der Sitze richtet sich nach dem Wahlergebnis. - **Aufgaben**: - Beschlussfassung über den städtischen Haushalt. - Entscheidung über Investitionen, Satzungen und städtische Projekte. - Kontrolle der Verwaltung, z. B. durch Anfragen oder Debatten. - **Sitzungen**: Die Sitzungen sind in der Regel öffentlich und finden im Ratssaal des Flensburger Rathauses statt. Die Tagesordnung wird vorab bekanntgegeben. - **Geschäftsordnung**: Die Arbeit der Ratsversammlung folgt einer festgelegten Geschäftsordnung, die z. B. Redezeiten, Abstimmungsverfahren und die Behandlung von Anträgen regelt. - **Themenbeispiele aus den Dokumenten**: - Haushaltsfragen (z. B. über- und außerplanmäßige Ausgaben). - Krankenhausplanung oder Gesundheitsausschuss-Vorlagen. - Einwohnerfragestunden, in denen Bürger:innen direkt Fragen stellen können. Falls Sie spezifischere Informationen benötigen – etwa zu Sitzungsterminen, aktuellen Beschlüssen oder der Zusammensetzung – kann ich diese mit dem vorliegenden Material nicht sicher beantworten. Die Dokumente enthalten hierzu keine detaillierten Angaben. Für aktuelle Daten wäre ein Blick auf die offizielle Website der Stadt Flensburg ratsam.`)
const startDate = ref<string | null>(null)
const endDate = ref<string | null>(null)

let eventSource: EventSource | null = null

function resetValues() {
  searchResults.value = []
  searched.value = false
  isWaitingForAnswer.value = false
  answer.value = ''
  startDate.value = null
  endDate.value = null
}
function onSearch() {
  resetValues()
  isWaitingForAnswer.value = true
  if (eventSource) {
    eventSource.close()
  }
  const backendEndpoint = import.meta.env.VITE_BACKEND_ENDPOINT
  const url = `${backendEndpoint.replace(/\/$/, '')}/api/chat/hybridquery-stream?q=${encodeURIComponent(searchTerm.value)}`
  eventSource = new EventSource(url)
  eventSource.addEventListener('documents', (event: MessageEvent) => {
    try {
      searchResults.value = JSON.parse(event.data)
      searched.value = true
    } catch (e) {
      searchResults.value = []
      searched.value = true
    }
  })

  eventSource.addEventListener('dateRange', (event: MessageEvent) => {
    try {
      startDate.value = JSON.parse(event.data).startDate
      endDate.value = JSON.parse(event.data).endDate
    } catch (e) {
      startDate.value = null
      endDate.value = null
    }
  })

  eventSource.addEventListener('answer', (event: MessageEvent) => {
    answer.value = event.data
    isWaitingForAnswer.value = false
    if (eventSource) {
      eventSource.close()
      eventSource = null
    }
  })
  eventSource.onerror = () => {
    if (eventSource) {
      eventSource.close()
      eventSource = null
    }
    searched.value = true
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
