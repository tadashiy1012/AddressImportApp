package jp.yama.addressimportapp

class CsvUtil {

    companion object {
        fun parseCsv(csvStr: String): Csv {
            var ls = mutableListOf<List<String>>()
            for (line in csvStr.split("\n")) {
                ls.add(line.split(","))
            }
            return Csv(ls.toList())
        }
    }

}