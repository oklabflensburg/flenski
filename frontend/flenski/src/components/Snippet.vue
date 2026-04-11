<script setup lang="ts">
import type { Document } from '@/types/document'
import Card from 'primevue/card'
import { computed } from 'vue'
import { useSettingsStore } from '@/stores/settings'

const props = defineProps<{ document: Document; keyword?: string }>()

const settingsStore = useSettingsStore()

const highlightedContent = computed(() => {
  if (!props.document.content || !props.keyword) return props.document.content
  // Escape special regex characters in keyword
  const escaped = props.keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  const regex = new RegExp(`(${escaped})`, 'gi')
  return props.document.content.replace(regex, '<mark class="bg-yellow-200">$1</mark>')
})

function formatDate(dateString?: string): string {
  if (!dateString) return ''
  const date = new Date(dateString)
  if (isNaN(date.getTime())) return dateString
  return date.toLocaleDateString('de-DE', {
    year: 'numeric',
    month: 'numeric',
    day: 'numeric',
  })
}

function toggleCategoryInSettings(category: string) {
  if (settingsStore.categories.includes(category)) {
    settingsStore.categories = settingsStore.categories.filter(c => c !== category)
  } else {
    settingsStore.categories = [...settingsStore.categories, category]
  }
}

const isCategoryActive = (category: string) => settingsStore.categories.includes(category)
</script>

<template>
  <Card class="mb-8">
    <template #title>
      <a
        v-if="document.url"
        :href="document.url"
        target="_blank"
        class="text-lg text-blue-600 hover:underline"
        >{{ document.title }}
      </a>
      <span v-else>{{ document.title }}</span>
      <span v-if="document.sourceDateTime" class="text-sm"
        >, {{ formatDate(document.sourceDateTime) }}</span
      >
    </template>
    <template #content>
      <div class="flex flex-col gap-y-3">
        <div class="text-gray-600" v-if="document.description">{{ document.description }}</div>
        <blockquote
          v-if="document.content"
          class="border-l-4 border-blue-400 bg-blue-50 text-blue-900 italic px-4 py-2 mb-4 text-sm"
          v-html="highlightedContent"
        ></blockquote>

        <div class="flex gap-1">
          <Tag
            v-for="category in document.categories"
            :key="category"
            size="small"
            :value="category"
            :severity="isCategoryActive(category) ? 'success' : 'info'"
            class="cursor-pointer"
            @click="toggleCategoryInSettings(category)"
          />
          <Tag v-if="document.type" size="small" :value="document.type" />
          <Tag severity="secondary" v-if="document.group" size="small" :value="document.group" />
        </div>
        <div>Score: {{document.score}}</div>
      </div>
    </template>s
  </Card>
</template>
