package com.ampnet.userservice.persistence.repository

import com.ampnet.userservice.persistence.model.UserInfo
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface UserInfoRepository : JpaRepository<UserInfo, Int> {
    fun findByIdentyumNumber(identyumNumber: String): Optional<UserInfo>
}
