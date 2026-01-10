<script setup lang="ts">
const props = defineProps<{
  startDate: string | null
  endDate: string | null
}>()

function formatDate(dateStr: string | null): string | null {
  if (!dateStr) return null
  const date = new Date(dateStr)
  if (isNaN(date.getTime())) return dateStr // Fallback falls ungültig
  return date.toLocaleDateString('de-DE', {
    day: 'numeric',
    month: 'numeric',
    year: 'numeric',
  })
}
</script>

<template>
  <div v-if="props.startDate || props.endDate" class="text-sm">
    <span>Schränke Zeitraum ein </span>
    <span v-if="props.startDate">von {{ formatDate(props.startDate) }} </span>
    <span v-if="props.endDate"> bis {{ formatDate(props.endDate) }} </span>
  </div>
</template>
