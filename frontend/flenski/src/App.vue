<script setup lang="ts">
import { ref } from "vue";
import HeaderBar from './components/HeaderBar.vue'
import FooterBar from './components/FooterBar.vue'

const visible = ref(false);
const visibleSettings = ref(false);
</script>

<template>
  <main>
    <HeaderBar :visible="visible" @update:visible="visible = $event" :visibleSettings="visibleSettings" @update:visible-settings="visibleSettings = $event" />
    <router-view :visible="visible" @update:visible="visible = $event" :visibleSettings="visibleSettings" @update:visible-settings="visibleSettings = $event" />
    <FooterBar />
  </main>
</template>

<style scoped></style>
