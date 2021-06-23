/*
 * Copyright (c) 2021. moru3_48. All Right Reserved.
 */

package dev.moru3.minepie.utils

class Utils {
    companion object {
        fun <T> T?.isNull(runnable: () -> T): T { return this ?: return runnable.invoke() }
    }
}