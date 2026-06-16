import axios from 'axios'
import { message } from 'ant-design-vue'

/**
 * 自定义 JSON 解析，解决大整数精度丢失问题。
 * 当 JSON 中的数字超过 Number.MAX_SAFE_INTEGER (2^53-1) 时，
 * 将其转换为字符串以保持精度。
 */
function safeJsonParse(text: string) {
  // 匹配大整数的正则：超过 15 位的数字（安全整数范围外）
  return JSON.parse(text, (_key: string, value: unknown) => {
    if (typeof value === 'number' && !Number.isInteger(value)) {
      return value
    }
    if (typeof value === 'number' && Number.isInteger(value)) {
      // 对于超大整数（超过 JS 安全整数范围），转为字符串
      const strVal = String(value)
      if (strVal.length >= 16 || value > Number.MAX_SAFE_INTEGER || value < Number.MIN_SAFE_INTEGER) {
        return strVal
      }
    }
    return value
  })
}

// 创建 Axios 实例
const myAxios = axios.create({
  baseURL: import.meta.env.VITE_APP_API_BASE_URL,
  timeout: 60000,
  withCredentials: true,
  // 自定义响应数据转换，解决大整数精度问题
  transformResponse: [
    (data: unknown) => {
      if (typeof data === 'string') {
        try {
          return safeJsonParse(data)
        } catch {
          return data
        }
      }
      return data
    },
  ],
})

// 全局请求拦截器
myAxios.interceptors.request.use(
  function (config) {
    // Do something before request is sent
    return config
  },
  function (error) {
    // Do something with request error
    return Promise.reject(error)
  },
)

// 全局响应拦截器
myAxios.interceptors.response.use(
  function (response) {
    const { data } = response
    // 未登录
    if (data.code === 40100) {
      // 不是获取用户信息的请求，并且用户目前不是已经在用户登录页面，则跳转到登录页面
      if (
        !response.request.responseURL.includes('user/getLoginUser') &&
        !window.location.pathname.includes('/user/login')
      ) {
        message.warning('请先登录')
        // 只保存路径部分（不含 origin），以便登录后 router.push 能正确识别
        const currentPath = window.location.pathname + window.location.search
        window.location.href = `/user/login?redirect=${encodeURIComponent(currentPath)}`
      }
    }
    return response
  },
  function (error) {
    // Any status codes that falls outside the range of 2xx cause this function to trigger
    // Do something with response error
    return Promise.reject(error)
  },
)

export default myAxios
