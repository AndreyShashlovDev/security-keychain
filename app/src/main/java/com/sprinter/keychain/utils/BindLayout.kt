package com.sprinter.keychain.utils

import android.support.annotation.LayoutRes
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class BindLayout(@LayoutRes val value: Int)
