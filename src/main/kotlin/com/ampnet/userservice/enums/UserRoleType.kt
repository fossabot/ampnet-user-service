package com.ampnet.userservice.enums

enum class UserRoleType(val id: Int) {

    ADMIN(1) {
        override fun getPrivileges(): List<PrivilegeType> {
            return listOf(
                    PrivilegeType.MONITORING,
                    PrivilegeType.PRA_PROFILE,
                    PrivilegeType.PWA_PROFILE,
                    PrivilegeType.PRO_PROFILE,
                    PrivilegeType.PRA_ORG,
                    PrivilegeType.PWA_ORG_APPROVE,
                    PrivilegeType.PRO_ORG_INVITE,
                    PrivilegeType.PWO_ORG_INVITE)
        }
    },

    USER(2) {
        override fun getPrivileges(): List<PrivilegeType> {
            return listOf(
                    PrivilegeType.PRO_PROFILE,
                    PrivilegeType.PRO_ORG_INVITE,
                    PrivilegeType.PWO_ORG_INVITE)
        }
    };

    companion object {
        private val map = values().associateBy(UserRoleType::id)
        fun fromInt(type: Int) = map[type]
    }

    abstract fun getPrivileges(): List<PrivilegeType>
}
