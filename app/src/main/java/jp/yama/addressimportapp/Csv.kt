package jp.yama.addressimportapp

import java.io.Serializable

data class Csv(
    val ary: List<List<String>>
): Serializable {
    fun get(row: Int, col: Int): String {
        return ary.get(row).let {
            it.get(col)
        }
    }
}