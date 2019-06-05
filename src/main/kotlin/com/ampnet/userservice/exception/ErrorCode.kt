package com.ampnet.userservice.exception

enum class ErrorCode(val categoryCode: String, var specificCode: String, var message: String) {
    // Registration: 01
    REG_INCOMPLETE("01", "01", "Incomplete signup information"),
    REG_INVALID("01", "02", "Signup information complete but invalid"),
    REG_USER_EXISTS("01", "03", "Signup failed because user exists"),
    REG_EMAIL_INVALID_TOKEN("01", "04", "Failed Email confirmation, invalid token format"),
    REG_EMAIL_NON_EXISTING_TOKEN("01", "05", "Failed Email confirmation, non existing token"),
    REG_EMAIL_EXPIRED_TOKEN("01", "06", "Failed Email confirmation, token expired"),
    REG_SOCIAL("01", "07", "Social exception"),
    REG_IDENTYUM("01", "08", "Identyum exception"),

    // Authentication: 02
    AUTH_INVALID_LOGIN_METHOD("02", "01", "Invalid login method"),

    // Users: 03
    USER_MISSING("03", "01", "Non existing user"),
    USER_ROLE_MISSING("03", "02", "Missing user role"),
}
