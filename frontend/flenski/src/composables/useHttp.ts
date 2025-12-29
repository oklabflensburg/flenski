import axios from 'axios'

export const useHttp = axios.create({
  baseURL: import.meta.env.VITE_BACKEND_ENDPOINT,
  timeout: 10000,
  withCredentials: true,
})

useHttp.interceptors.response.use(
  (r) => r,
  (e) => {
    return Promise.reject(e)
  },
)

export default useHttp
