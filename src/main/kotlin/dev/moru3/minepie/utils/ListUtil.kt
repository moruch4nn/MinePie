package dev.moru3.minepie.utils

class ListUtil {
    companion object {
        fun <T> List<T>.getOrNull(index: Int): T? { return if(this.size>=index) null else this[index] }
    }
}