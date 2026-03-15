import './assets/main.css'

import { createApp } from 'vue'
import PrimeVue from 'primevue/config'
import Material from '@primeuix/themes/material'
import App from './App.vue'
import router from './router'

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
app.use(router)
app.mount('#app')
