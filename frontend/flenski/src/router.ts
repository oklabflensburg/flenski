import { createRouter, createWebHistory } from 'vue-router'
import App from './App.vue'
import Datenschutzerklaerung from './components/Datenschutzerklaerung.vue'
import Home from './components/Home.vue'

const routes = [
  {
    path: '/',
    component: Home
  },
  {
    path: '/datenschutzerklaerung',
    component: Datenschutzerklaerung
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
