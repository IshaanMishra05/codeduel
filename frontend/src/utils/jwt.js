export function decodeJwt(token) {
  try {
    const payload = token.split('.')[1]
    return JSON.parse(atob(payload))
  } catch {
    return null
  }
}

export function getUsernameFromToken(token) {
  const decoded = decodeJwt(token)
  return decoded?.sub ?? null
}
