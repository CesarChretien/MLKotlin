package network

import app.data
import linearalgebra.Matrix
import linearalgebra.time

/**
 * Created by cesarchretien on 24-01-18.
 */
fun main(args: Array<String>) {
    time("Algebraic network") {
        createAlgebraicNetwork(data(), 10, 10)
    }
}

fun createAlgebraicNetwork(data: List<Pair<DoubleArray, DoubleArray>>, vararg layerDepths: Int) {
    //check if inputs and outputs are the same size, and if all their elements are the same size too


    //aye you loop twice over your inputs/outputs but you'd rather fail here than halfway through the interesting stuff
    val inputOutputs = data.subList(0, 200)
    val inputSize = inputOutputs.first().first.size
    val outputSize = inputOutputs.first().second.size
    val eta = 10.0 //learning rate
    val genFunc = { (2.0 * Math.random()) - 1.0 }
    val dGenFunc: (Double) -> Double = { genFunc() }
    val iGenFunc: (Int) -> Double = { genFunc() }
    val nonLinF = ReLU
    val dNonLinF = dReLU


    val weightMats: List<Matrix> = List(1 + layerDepths.size) { index ->
        when (index) {
            0 -> Matrix(layerDepths[index], inputSize)
            layerDepths.size -> Matrix(outputSize, layerDepths[index - 1])
            else -> Matrix(layerDepths[index], layerDepths[index - 1])
        }.applyToValues(dGenFunc)
    }

    val biases: List<DoubleArray> = List(1 + layerDepths.size) { index ->
        when (index) {
            layerDepths.size -> DoubleArray(outputSize, iGenFunc)
            else -> DoubleArray(layerDepths[index], iGenFunc)
        }
    }

    for (unused in 1..500) {

        var cost = 0.0
        val totaldCdW = List(1 + layerDepths.size) { index ->
            val weightMat = weightMats[index]
            Matrix(weightMat.rows, weightMat.columns)
        }

        val totaldCdb = List(1 + layerDepths.size) { index -> DoubleArray(biases[index].size) }

        //this is where the magic happens
//        runBlocking {
        List(inputOutputs.size) { i ->
            //                launch {
            val (input, output) = inputOutputs[i]
            val innerNonLinearValues = MutableList(layerDepths.size) { index ->
                DoubleArray(layerDepths[index])
            }

            val innerLinearValues = MutableList(layerDepths.size + 1) { index ->
                when (index) {
                    layerDepths.size -> DoubleArray(outputSize)
                    else -> DoubleArray(layerDepths[index])
                }
            }

            for (index in 0..layerDepths.size) {
                when (index) {
                    layerDepths.size -> {
                        innerLinearValues[index] = (weightMats[index] * innerNonLinearValues[index - 1]) elPlus biases[index]
                    }
                    else -> {
                        innerLinearValues[index] = (weightMats[index] * if (index == 0) input else innerNonLinearValues[index - 1]) elPlus biases[index]
                        innerNonLinearValues[index] = innerLinearValues[index].normalize().doubleArrayMap(nonLinF)
                    }
                }
            }

            val predictedOutput = innerLinearValues[layerDepths.size].normalize().doubleArrayMap(nonLinF)

            val dCdx = MutableList(1 + layerDepths.size) { index ->
                if (index == layerDepths.size) {
                    predictedOutput elMinus output
                } else {
                    DoubleArray(layerDepths[index])
                }
            }

            //fill dCdx recursively
            for (index in layerDepths.size downTo 1) {
                var const = 0.0
                innerLinearValues[index].forEach {
                    const += it * it
                }

                const = 1.0 / Math.sqrt(const)
                dCdx[index - 1] = weightMats[index].transpose() * (innerLinearValues[index].doubleArrayMap { const * dNonLinF(const * it) } hProd dCdx[index])
            }

            val dCdb = MutableList(1 + layerDepths.size) { index ->
                var const = 0.0
                innerLinearValues[index].forEach {
                    const += it * it
                }

                const = 1.0 / Math.sqrt(const)
                dCdx[index] hProd innerLinearValues[index].doubleArrayMap { const * dNonLinF(const * it) * eta }
            }

            val dCdW = MutableList(1 + layerDepths.size) { index ->
                val dCdbMat = Matrix(dCdb[index].size, 1) { row, _ ->
                    dCdb[index][row]
                }

                val dCdxprevMat = Matrix(1, if (index == 0) input.size else innerNonLinearValues[index - 1].size) { _, column ->
                    (if (index == 0) input else innerNonLinearValues[index - 1])[column]
                }

                dCdbMat * dCdxprevMat * eta
            }

            totaldCdW.forEachIndexed { index, matrix ->
                matrix.forEachIndexed { row, col, _ ->
                    matrix[row, col] += (1.0 / inputOutputs.size) * dCdW[index][row, col]
                }
            }

            totaldCdb.forEachIndexed { indexOuter, doubles ->
                doubles.forEachIndexed { indexInner, _ ->
                    doubles[indexInner] += (1.0 / inputOutputs.size) * dCdb[indexOuter][indexInner]
                }
            }

            predictedOutput.forEachIndexed { index, value ->
                cost += (value - output[index]) * (value - output[index])
            }
        }
//            }.forEach { it.join() }
//        }

        weightMats.forEachIndexed { index, _ ->
            weightMats[index].forEachIndexed { row, col, _ ->
                weightMats[index][row, col] -= totaldCdW[index][row, col]
            }
        }

        biases.forEachIndexed { indexOuter, _ ->
            biases[indexOuter].forEachIndexed { indexInner, _ ->
                biases[indexOuter][indexInner] -= totaldCdb[indexOuter][indexInner]
            }
        }

        println("Average cost of iteration $unused: ${cost / inputOutputs.size}")

    }

}

fun DoubleArray.doubleArrayMap(f: (Double) -> Double): DoubleArray = DoubleArray(size) { index -> f(this[index]) }

infix fun DoubleArray.hProd(other: DoubleArray): DoubleArray = if (size == other.size) {
    DoubleArray(size) { index -> this[index] * other[index] }
} else {
    throw Exception("Check dims: $size != ${other.size}")
}

infix fun DoubleArray.elPlus(other: DoubleArray): DoubleArray = if (size == other.size) {
    DoubleArray(size) { index -> this[index] + other[index] }
} else {
    throw Exception("Dimensions don't match.")
}

infix fun DoubleArray.elMinus(other: DoubleArray): DoubleArray = if (size == other.size) {
    DoubleArray(size) { index -> this[index] - other[index] }
} else {
    throw Exception("Dimensions don't match.")
}

private fun DoubleArray.normalize(): DoubleArray {
    var length = 0.0

    forEach {
        length += it * it
    }

    length = Math.sqrt(length)
    return doubleArrayMap { it / length }
}