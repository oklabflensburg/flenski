<script setup lang="ts">
import { computed } from 'vue'
import { marked } from 'marked'

const props = defineProps<{ answer: string }>()

const parsedAnswer = computed(() => marked.parse(props.answer))
</script>
<template>
  <Card class="w-full max-w-250 mt-2">
    <template #content>
      <div class="prose max-w-none" v-html="parsedAnswer" />
    </template>
  </Card>
</template>
