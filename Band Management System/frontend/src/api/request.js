import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const request = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000,
  withCredentials: true // 允许携带cookie（用于Session认证）
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 后端使用Session认证，不需要手动设置token
    // Session cookie会自动携带
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    
    // 如果返回的状态码不是200，则认为是错误
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      
      // 2000: 未授权（后端ErrorCode.UNAUTHORIZED），跳转到登录页
      if (res.code === 2000) {
        console.log('检测到未授权错误，清除登录信息并跳转到登录页')
        // 清除本地存储的用户信息
        localStorage.removeItem('userRole')
        localStorage.removeItem('userId')
        localStorage.removeItem('username')
        localStorage.removeItem('relatedId')
        window.location.href = '/login'
      }
      
      return Promise.reject(new Error(res.message || '请求失败'))
    } else {
      return res
    }
  },
  error => {
    console.error('响应错误:', error)
    
    // 处理HTTP 401未授权错误（如果后端设置了HTTP状态码）
    if (error.response && error.response.status === 401) {
      ElMessage.error('未登录或登录已过期，请重新登录')
      localStorage.removeItem('userRole')
      localStorage.removeItem('userId')
      localStorage.removeItem('username')
      localStorage.removeItem('relatedId')
      window.location.href = '/login'
      return Promise.reject(error)
    }
    
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request
