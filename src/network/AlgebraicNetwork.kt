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

fun createAlgebraicNetwork(inputOutputs: List<Pair<DoubleArray, DoubleArray>>, vararg layerDepths: Int) {
    //check if inputs and outputs are the same size, and if all their elements are the same size too


    //aye you loop twice over your inputs/outputs but you'd rather fail here than halfway through the interesting stuff
    val inputSize = inputOutputs.first().first.size
    val outputSize = inputOutputs.first().second.size
    val eta = 1.0 //learning rate


    val weightMats: List<Matrix> = List(1 + layerDepths.size) { index ->
        when (index) {
            0 -> Matrix(layerDepths[index], inputSize)
            layerDepths.size -> Matrix(outputSize, layerDepths[index - 1])
            else -> Matrix(layerDepths[index], layerDepths[index - 1])
        }.applyToValues { Math.random() }
    }

    val biases: List<DoubleArray> = List(1 + layerDepths.size) { index ->
        when (index) {
            layerDepths.size -> DoubleArray(outputSize) { Math.random() }
            else -> DoubleArray(layerDepths[index]) { Math.random() }
        }
    }

    for (unused in 1..100) {

        val totaldCdW = List(1 + layerDepths.size) { index -> Matrix(weightMats[index].rows, weightMats[index].columns) }
        val totaldCdb = List(1 + layerDepths.size) { index -> DoubleArray(biases[index].size) }

        //this is where the magic happens
        inputOutputs.subList(0, 100).forEachIndexed { i, (input, output) ->
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
                        innerNonLinearValues[index] = innerLinearValues[index].map(ReLU).toDoubleArray()
                    }
                }
            }

            val predictedOutput = innerLinearValues[layerDepths.size].map(ReLU).toDoubleArray()

            val dCdx = MutableList(1 + layerDepths.size) { index ->
                if (index == layerDepths.size) {
                    predictedOutput.zip(output).map { (o, ro) -> o - ro }.toDoubleArray()
                } else {
                    DoubleArray(layerDepths[index])
                }
            }

            //fill dCdx recursively
            for (index in layerDepths.size downTo 1) {
                dCdx[index - 1] = weightMats[index].transpose() * (innerLinearValues[index].map(dReLU).toDoubleArray() hProd dCdx[index])
            }

            val dCdb = MutableList(1 + layerDepths.size) { index ->
                (dCdx[index] hProd innerLinearValues[index].map(dReLU).toDoubleArray()).map { it * eta }.toDoubleArray()
            }

            val dCdW = MutableList(1 + layerDepths.size) { index ->
                val dCdbMat: Matrix = Matrix(dCdb[index].size , 1).apply {
                    forEachIndexed { row, column, _ -> this[row, column] = dCdb[index][row] }
                }
                val dCdxprevMat: Matrix = Matrix(1, if (index == 0) input.size else innerNonLinearValues[index - 1].size).apply {
                    forEachIndexed { row, column, _ -> this[row, column] = (if (index == 0) input else innerNonLinearValues[index - 1])[column] }
                }

                println(dCdxprevMat)

                dCdbMat * dCdxprevMat * eta
            }

            totaldCdW.forEachIndexed { index, matrix ->
                matrix.forEachIndexed { row, col, _ ->
                    matrix[row, col] += (1.0/inputOutputs.size) * dCdW[index][row, col]
                }
            }

            totaldCdb.forEachIndexed { indexOuter, doubles ->
                doubles.forEachIndexed { indexInner, _ ->
                    doubles[indexInner] += (1.0/inputOutputs.size) * dCdb[indexOuter][indexInner]
                }
            }
        }

        weightMats.forEachIndexed { index, _ ->
            weightMats[index].forEachIndexed { row, col, _ ->
                weightMats[index][row, col] += totaldCdW[index][row, col]
            }
        }

        biases.forEachIndexed { indexOuter, _ ->
            biases[indexOuter].forEachIndexed { indexInner, _ ->
                biases[indexOuter][indexInner] += totaldCdb[indexOuter][indexInner]
            }
        }

    }

}

infix fun DoubleArray.elPlus(other: DoubleArray): DoubleArray = if (this.size == other.size) {
    this.zip(other).map { (l, r) -> l + r }.toDoubleArray()
} else {
    throw Exception("Dimensions don't match.")
}

private fun DoubleArray.normalize(): DoubleArray {
    val length = Math.sqrt(this.map { it * it }.sum())
    return this.map { it / length }.toDoubleArray()
}