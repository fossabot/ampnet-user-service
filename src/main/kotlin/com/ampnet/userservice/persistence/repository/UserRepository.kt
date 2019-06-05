package com.ampnet.userservice.persistence.repository

import com.ampnet.userservice.persistence.model.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface UserRepository : JpaRepository<User, Int> {
    fun findByEmail(email: String): Optional<User>

    fun findByEmailContainingIgnoreCase(email: String): List<User>
}
