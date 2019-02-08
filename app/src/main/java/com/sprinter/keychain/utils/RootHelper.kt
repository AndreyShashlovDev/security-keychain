package com.sprinter.keychain.utils

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

object RootHelper {

    private val WARN_PATH = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su"
    )

    val isDeviceRooted: Boolean
        get() = checkTagsAndPath() || executeRuntime()

    private fun checkTagsAndPath(): Boolean {
        val buildTags = android.os.Build.TAGS
        if (!(buildTags != null && buildTags.contains("test-keys"))) {
            for (path in WARN_PATH) {
                if (File(path).exists()) {
                    return true
                }
            }
        }

        return false
    }

    private fun executeRuntime(): Boolean {
        var process: Process? = null
        return try {
            process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            val reader = BufferedReader(InputStreamReader(process!!.inputStream))

            reader.readLine() != null

        } catch (t: Throwable) {
            false

        } finally {
            process?.destroy()
        }
    }

}
