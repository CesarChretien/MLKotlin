package network

import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import linearalgebra.time

/**
 * Created by cesarchretien on 23-01-18.
 */

fun main(args: Array<String>) {

    val size = 50000
    val vecx = DoubleArray(size) { Math.random() }
    val vecy = DoubleArray(size) { Math.random() }
    var res = 0.0

    time("2 vectors of size $size in parallel") {
        val p = parallelInProd(size, vecx, vecy)
        println(p)
    }

    time("2 vectors of size $size sequential") {
        val s = sequentialInProd(size, vecx, vecy)
        println(s)
    }
}

private fun parallelInProd(size: Int, vecx: DoubleArray, vecy: DoubleArray): Double = runBlocking {
    val s = List(size) {
        async {
            vecx inProd vecy
        }
    }
    s.map { it.await() }.sum()
}

private fun sequentialInProd(size: Int, vecx: DoubleArray, vecy: DoubleArray): Double {
    return List(size) {
        vecx inProd vecy
    }.sum()
}

private infix fun DoubleArray.inProd(other: DoubleArray) = if (size == other.size) {
    var res = 0.0

    forEachIndexed { index, value ->
        res += value + other[index]
    }

    res
} else {
    throw Exception("Dimensions don't match")
}