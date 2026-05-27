const TOKEN_KEY = 'token'
const USER_KEY = 'user'
const LEGACY_USER_ID_KEY = 'userId'
const LEGACY_USERNAME_KEY = 'username'
const LEGACY_IS_ADMIN_KEY = 'isAdmin'

function parseUser(rawUser) {
  if (!rawUser) {
    return null
  }

  try {
    return JSON.parse(rawUser)
  } catch (error) {
    return null
  }
}

function syncLegacyFields(user) {
  if (!user || typeof user !== 'object') {
    localStorage.removeItem(LEGACY_USER_ID_KEY)
    localStorage.removeItem(LEGACY_USERNAME_KEY)
    localStorage.removeItem(LEGACY_IS_ADMIN_KEY)
    return
  }

  const userId = user.id ?? user.userId
  if (userId !== undefined && userId !== null) {
    localStorage.setItem(LEGACY_USER_ID_KEY, String(userId))
  } else {
    localStorage.removeItem(LEGACY_USER_ID_KEY)
  }

  const username = user.nickname ?? user.username
  if (username) {
    localStorage.setItem(LEGACY_USERNAME_KEY, String(username))
  } else {
    localStorage.removeItem(LEGACY_USERNAME_KEY)
  }

  const isAdmin = user.isAdmin
  if (isAdmin !== undefined && isAdmin !== null) {
    const adminFlag = isAdmin === true || Number(isAdmin) === 1
    localStorage.setItem(LEGACY_IS_ADMIN_KEY, adminFlag ? 'true' : 'false')
  } else {
    localStorage.removeItem(LEGACY_IS_ADMIN_KEY)
  }
}

export function loadAuthStorage() {
  const token = localStorage.getItem(TOKEN_KEY) || ''
  const rawUser = localStorage.getItem(USER_KEY)
  const user = parseUser(rawUser)

  return { token, user }
}

export function saveAuthStorage({ token, user }) {
  if (token) {
    localStorage.setItem(TOKEN_KEY, token)
  } else {
    localStorage.removeItem(TOKEN_KEY)
  }

  if (user) {
    localStorage.setItem(USER_KEY, JSON.stringify(user))
  } else {
    localStorage.removeItem(USER_KEY)
  }

  // 兼容仍读取 userId/username/isAdmin 的旧页面。
  syncLegacyFields(user)
}

export function clearAuthStorage() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
  localStorage.removeItem(LEGACY_USER_ID_KEY)
  localStorage.removeItem(LEGACY_USERNAME_KEY)
  localStorage.removeItem(LEGACY_IS_ADMIN_KEY)
}
