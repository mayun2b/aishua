const TOKEN_KEY = 'token'
const USER_KEY = 'user'

export function loadAuthStorage() {
  const token = localStorage.getItem(TOKEN_KEY) || ''
  const rawUser = localStorage.getItem(USER_KEY)

  let user = null
  if (rawUser) {
    try {
      user = JSON.parse(rawUser)
    } catch (error) {
      user = null
    }
  }

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
}

export function clearAuthStorage() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}
