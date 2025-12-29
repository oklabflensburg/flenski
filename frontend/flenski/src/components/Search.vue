<script setup lang="ts">
import {ref} from 'vue'
import InputText from 'primevue/inputtext'
import Button from 'primevue/button'
import useHttp from "@/composables/useHttp.ts";
import Snippet from './Snippet.vue';
import type { Document } from '@/types/document';

const searchTerm = ref('')
const searchResults = ref<Document[]>([])
const searched = ref(false)

function onSearch() {
  useHttp.get('api/sparsequery', { params: { q: searchTerm.value } })
    .then(response => {
      searchResults.value = response.data
      searched.value = true
    })
    .catch(() => {
      searchResults.value = []
      searched.value = true
    })
}


</script>
<template>
  <div class="flex flex-col items-center justify-center gap-8 p-6 bg-white ">
    <h2 class="text-2xl font-semibold text-gray-800 mb-2">FlensKI Suche</h2>
    <div class="flex w-full max-w-md gap-2">
      <InputText v-model="searchTerm" class="flex-1" size="large"/>
      <Button label="Suchen" icon="pi pi-search" @click="onSearch" size="large"/>
    </div>
    <div v-if="searchResults.length > 0" class="w-full  max-w-250 mt-2">
      <div v-for="(document, idx) in searchResults" :key="idx">
        <Snippet :document="document" :keyword="searchTerm" />
      </div>
    </div>
    <div v-else-if="searched" class="text-gray-400 mt-6">Keine Ergebnisse gefunden.</div>
  </div>
</template>
