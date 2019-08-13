package jp.yama.addressimportapp

import java.io.Serializable

data class Address(
    val id: Long,
    val name: String,
    val kana: String,
    val org: String,
    val phone: String,
    val phone2: String,
    val mail: String,
    val mail2: String,
    val number: String,
    val section: String
): Serializable {

    companion object {
        fun builder(section: SectionKeys, row: List<String>): Address {
            return Address(
                row[0].toLong(),
                row[1],
                row[2],
                row[3],
                row[5],
                row[7],
                row[4],
                row[6],
                row[8],
                section.label
            )
        }
    }

}