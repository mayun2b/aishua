import request from '../core/http/request'

function normalizeUrl(url) {
  if (typeof url !== 'string') {
    return url
  }

  // 旧模块大量写法是 /api/xxx，统一适配为新请求层的 /xxx。
  return url.startsWith('/api/') ? url.slice(4) : url
}

function normalizeConfig(config = {}) {
  return {
    ...config,
    url: normalizeUrl(config.url)
  }
}

function legacyRequest(config) {
  return request(normalizeConfig(config))
}

legacyRequest.get = (url, config) => request.get(normalizeUrl(url), config)
legacyRequest.post = (url, data, config) => request.post(normalizeUrl(url), data, config)
legacyRequest.put = (url, data, config) => request.put(normalizeUrl(url), data, config)
legacyRequest.delete = (url, config) => request.delete(normalizeUrl(url), config)
legacyRequest.patch = (url, data, config) => request.patch(normalizeUrl(url), data, config)
legacyRequest.interceptors = request.interceptors
legacyRequest.defaults = request.defaults

export default legacyRequest
