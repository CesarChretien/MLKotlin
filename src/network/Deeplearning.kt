package network

import app.data

/**
 * Created by cesarchretien on 24-12-17.
 */
fun main(args: Array<String>) {
    val inputoutputs = data()

    for ((input, output) in inputoutputs) {
        createSystem(input, output)
    }

}

fun createSystem(input: DoubleArray, output: DoubleArray, vararg hiddenLayerSizes: Int) {
    val numOfWeightBiasSets = hiddenLayerSizes.size + 1
    val listOfWeights = ArrayList<List<RootNode>>()
    val listOfBiases = ArrayList<List<RootNode>>()

    (0 until numOfWeightBiasSets).forEach { i ->
        val x = when (i) {
            0 -> input.size * hiddenLayerSizes[i]
            numOfWeightBiasSets - 1 -> output.size * hiddenLayerSizes[i - 1]
            else -> hiddenLayerSizes[i - 1] * hiddenLayerSizes[i]
        }

        val y = if (i < numOfWeightBiasSets - 1) hiddenLayerSizes[i] else output.size

        listOfWeights.add(Array(x, { RootNode(Math.random()) }).toList())
        listOfBiases.add(Array(y, { RootNode(Math.random()) }).toList())
    }

    val inputroots = input.map { RootNode(it) }

    (0 until numOfWeightBiasSets).forEach { i ->
        val outputNodeSize = when (i) {
            numOfWeightBiasSets - 1 -> output.size
            else -> hiddenLayerSizes[i]
        }

        for (index in (0 until outputNodeSize)) {
            val start = index * inputroots.size
            val end = start + inputroots.size

            val inter = inputroots.zip(listOfWeights[i].subList(start, end)).map { (inpn, wn) ->
                multiply {
                    put(inpn)
                    put(wn)
                }
            }

            val endNode = add {
                put(inter)
                put(listOfBiases[i][index])
            }
        }
    }
}

val ReLU: (Double) -> Double = { Math.max(0.0, it) }

val softPlus: (Double) -> Double = { Math.log(1 + Math.exp(it)) }

val sigmoid: (Double) -> Double = { val ex = Math.exp(it); ex / (ex + 1) }

val networkpart: (List<Double>, List<Double>, Double) -> Node = { inputs, weights, bias ->
    if (inputs.size != weights.size) throw Exception("different sizes")
    val multiplicationNodes = inputs.zip(weights).map { (input, weight) ->
        multiply {
            root(input)
            root(weight)
        }
    }

    add {
        put(multiplicationNodes)
        root(bias)
    }
}

class NonLinearNode : OperationNode() {
    override val valuef: (List<Double>) -> Double = { if (it.size != 1) throw Exception("For now only accepts 1 argument") else ReLU(it[0]) }

    override val gradientf: (Double, Double) -> Double = { childGradientValue, _ -> if (childGradientValue <= 0.0) 0.0 else 1.0}
}