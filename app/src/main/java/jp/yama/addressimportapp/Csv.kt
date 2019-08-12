package jp.yama.addressimportapp

data class Csv(
    val ary: List<List<String>>
) {
    fun get(row: Int, col: Int): String {
        return ary.get(row).let {
            it.get(col)
        }
    }
}