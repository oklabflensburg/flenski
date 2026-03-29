import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import PrimeVue from 'primevue/config'
import Material from '@primeuix/themes/material'
import App from './App.vue'
import router from './router'

const app = createApp(App)
const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)
app.use(pinia)
app.use(PrimeVue, {
  ripple: true,
  theme: {
    preset: Material,
    options: {
      prefix: 'p',
      darkModeSelector: 'none',
      cssLayer: false,
      ripple: true,
      inputVariant: 'filled',
    },
  },
})
app.use(router)
app.mount('#app')
