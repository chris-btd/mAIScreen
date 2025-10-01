package com.btd.maiscreen

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform