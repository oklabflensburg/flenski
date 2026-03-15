<template>
  <footer class="footer-bar w-full py-4 bg-gray-100 text-center text-gray-600 border-t border-gray-200">
    <span>© 2026 Flenski. Alle Rechte vorbehalten. | <router-link to="/datenschutzerklaerung" class="underline hover:text-blue-600">Datenschutzerklärung</router-link></span>
  </footer>
</template>

<script setup lang="ts">
// Keine Logik notwendig
</script>

<style scoped>
.footer-bar {
  position: fixed;
  left: 0;
  bottom: 0;
  width: 100%;
  z-index: 10;
}
</style>
