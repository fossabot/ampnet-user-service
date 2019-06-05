package com.ampnet.userservice.persistence.repository

import com.ampnet.userservice.persistence.model.Role
import org.springframework.data.jpa.repository.JpaRepository

interface RoleRepository : JpaRepository<Role, Int>
