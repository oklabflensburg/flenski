<template>
  <div>
    <Search />
    <div class="card flex justify-center">
      <Info :visible="visible" @update:visible="$emit('update:visible', $event)" />
    </div>
  </div>
</template>

<script setup lang="ts">
import Search from './Search.vue'
import Info from './Info.vue'

defineProps({
  visible: Boolean
})
defineEmits(['update:visible'])
</script>
