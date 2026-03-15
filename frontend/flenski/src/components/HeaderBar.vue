<script setup lang="ts">
// import { defineProps, defineEmits } from 'vue';

const props = defineProps({
  visible: Boolean
});
const emit = defineEmits(['update:visible']);
</script>

<template>
  <header class="w-full   text-sm py-4 px-6 shadow flex items-center justify-between">
    <div>
      <span
        @click="emit('update:visible', true)"
        class="cursor-pointer hover:underline"
      >Über Flenski</span>
    </div>
  </header>
</template>
