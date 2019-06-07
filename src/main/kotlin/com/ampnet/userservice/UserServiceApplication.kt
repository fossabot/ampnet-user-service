package com.ampnet.userservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class UserServiceApplication

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<UserServiceApplication>(*args)
}
