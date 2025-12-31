import './assets/main.css'

import { createApp } from 'vue'
import PrimeVue from 'primevue/config'
import Aura from '@primeuix/themes/aura'
import Material from '@primeuix/themes/material'
import App from './App.vue'

const app = createApp(App)
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
app.mount('#app')
