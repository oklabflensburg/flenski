<script setup lang="ts">
import type { Document } from '@/types/document'
import Card from 'primevue/card'
import { computed } from 'vue'

const props = defineProps<{ document: Document; keyword?: string }>()

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
            severity="info"
            v-for="category in document.categories"
            :key="category"
            size="small"
            :value="category"
          />
          <Tag v-if="document.type" size="small" :value="document.type" />
          <Tag severity="secondary" v-if="document.group" size="small" :value="document.group" />
        </div>
      </div>
    </template>
  </Card>
</template>
