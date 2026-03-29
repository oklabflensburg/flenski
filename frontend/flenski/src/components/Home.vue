<template>
  <div>
    <Search />
    <div class="card flex justify-center">
      <Info :visible="visible" @update:visible="$emit('update:visible', $event)" />
      <Settings :visibleSettings="visibleSettings" @update:visibleSettings="$emit('update:visible-settings', $event)" />
    </div>
  </div>
</template>

<script setup lang="ts">
import Search from './Search.vue'
import Info from './Info.vue'
import Settings from '@/components/Settings.vue'

defineProps({
  visible: Boolean,
  visibleSettings: Boolean
})
defineEmits(['update:visible', 'update:visible-settings'])
</script>
