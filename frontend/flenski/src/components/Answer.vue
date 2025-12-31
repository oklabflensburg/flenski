<script setup lang="ts">
const props = defineProps<{ answer: string }>()
</script>
<template>


    <Card class="w-full max-w-250 mt-2">
      <template #content>
        <p>

          {{ props.answer}}
        </p>
      </template>
    </Card>
</template>
