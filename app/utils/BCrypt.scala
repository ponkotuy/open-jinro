package utils

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

object BCrypt extends ThreadLocal[BCryptPasswordEncoder] {
  def apply() = get()
  override def initialValue(): BCryptPasswordEncoder = new BCryptPasswordEncoder(11)
}
