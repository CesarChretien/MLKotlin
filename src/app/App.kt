package app

import java.io.File
import java.io.FileInputStream
import java.util.zip.GZIPInputStream


/**
 * Created by cesarchretien on 18-12-17.
 */
fun main(args: Array<String>) {

    val file = File("./src/train-images-idx3-ubyte.gz")

    val fileInputStream = FileInputStream(file)
    val gzipInputStream = GZIPInputStream(fileInputStream)

    gzipInputStream.readBytes().iterator().apply {
        val magicNumber = readNextInt()
        val numOfImages = readNextInt()
        val numOfRows = readNextInt()
        val numOfColumns = readNextInt()
        println("magic number: $magicNumber")
        println("number of images: $numOfImages")
        println("number of rows: $numOfRows")
        println("number of columns: $numOfColumns")

        var curCol = 0
        var curRow = 0

        forEachRemaining { byte ->
            val unsigned = byte.toInt() and 0xFF
            print("${if (unsigned < 10) "  " else if (unsigned < 100) " " else ""}$unsigned")

            if (++curCol % numOfColumns == 0) {
                println()
                curCol = 0
                if (++curRow % numOfRows == 0) {
                    println()
                    curRow = 0
                }
            }
        }
    }

}

fun ByteIterator.readNextInt(): Int = (nextByte().toInt() and 0xFF).shl(24) or
        (nextByte().toInt() and 0xFF).shl(16) or
        (nextByte().toInt() and 0xFF).shl(8) or
        (nextByte().toInt() and 0xFF)
