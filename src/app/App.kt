package app

import java.io.File
import java.io.FileInputStream
import java.util.zip.GZIPInputStream


/**
 * Created by cesarchretien on 18-12-17.
 */

fun data(): List<Pair<DoubleArray, DoubleArray>> {

    val images = File("./src/train-images-idx3-ubyte.gz")
    val imagesInputStream = FileInputStream(images)
    val gzipImagesInputStream = GZIPInputStream(imagesInputStream)

    val imgl = gzipImagesInputStream.readBytes().iterator().let { inputStream ->
        val magicNumber = inputStream.readNextInt()
        val numOfImages = inputStream.readNextInt()
        val numOfRows = inputStream.readNextInt()
        val numOfColumns = inputStream.readNextInt()

        val arraySize = numOfColumns * numOfRows
        val imageList: Array<DoubleArray> = Array(numOfImages, { DoubleArray(arraySize) })

        imageList.apply {
            var imgIndex = 0
            inputStream.forEachRemainingIndexed { index, byte ->
                this[imgIndex][index % arraySize] = (byte.toInt() and 0xFF).toDouble()
                if (index != 0 && index % arraySize == 0) ++imgIndex
            }
        }
    }

    val labels = File("./src/train-labels-idx1-ubyte.gz")
    val labelsInputStream = FileInputStream(labels)
    val gzipLabelsInputStream = GZIPInputStream(labelsInputStream)

    val lbll = gzipLabelsInputStream.readBytes().iterator().let { inputStream ->
        val magicNumber = inputStream.readNextInt()
        val numOfItems = inputStream.readNextInt()

        val labelList: Array<DoubleArray> = Array(numOfItems, { DoubleArray(10) })

        labelList.apply {
            inputStream.forEachRemainingIndexed { index, byte ->
                labelList[index][byte.toInt() and 0xFF] = 1.0
            }
        }
    }

    return imgl.zip(lbll)
}

fun <T> Iterator<T>.forEachRemainingIndexed(f: (Int, T) -> Unit) {
    var index = 0
    while (this.hasNext())
        f(index++, this.next())
}

fun ByteIterator.readNextInt(): Int = (nextByte().toInt() and 0xFF).shl(24) or
        (nextByte().toInt() and 0xFF).shl(16) or
        (nextByte().toInt() and 0xFF).shl(8) or
        (nextByte().toInt() and 0xFF)
