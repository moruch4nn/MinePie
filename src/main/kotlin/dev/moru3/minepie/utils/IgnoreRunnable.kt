package dev.moru3.minepie.utils

import java.lang.Exception

interface IgnoreRunnable {
    companion object {
        fun Runnable.ignoreException(): Boolean {
            return try {
                this.run()
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}