import axios from 'axios'

const instance = axios.create({
  baseURL: '/api',
  timeout: 5000
})

// 请求拦截器
instance.interceptors.request.use(config => {
  const token = localStorage.getItem('agri_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器
instance.interceptors.response.use(
  response => response.data,
  error => {
    console.warn('API 请求异常，平滑降级处理:', error.message)
    return Promise.reject(error)
  }
)

export default instance
