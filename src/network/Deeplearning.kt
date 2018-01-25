package network

import app.data
import java.util.*

/**
 * Created by cesarchretien on 24-12-17.
 */
fun main(args: Array<String>) {
    val inputoutputs = data()


    val listy = ArrayList<ArrayList<List<Node>>>()

    var f = 0
    val s = inputoutputs.size
    for ((input, output) in inputoutputs.subList(0, 500)) {
        println("Creating system ${++f} out of $s")
        listy.add(createSystem(input, output, 15, 15))
    }

    listy.forEach {
        it[it.size - 1].forEach {
            it.value()
            it as OperationNode
            it.gradient()
        }
    }


}

fun createSystem(input: DoubleArray, output: DoubleArray, vararg hiddenLayerSizes: Int): ArrayList<List<Node>> {
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
    val results = ArrayList<List<Node>>()

    (0 until numOfWeightBiasSets).forEach { i ->
        val outputNodeSize = when (i) {
            numOfWeightBiasSets - 1 -> output.size
            else -> hiddenLayerSizes[i]
        }

        val endNodes = ArrayList<Node>(outputNodeSize)

        for (index in (0 until outputNodeSize)) {

            val intermediateInput = when (i) {
                0 -> inputroots
                else -> results[i - 1]
            }

            val start = index * intermediateInput.size
            val end = start + intermediateInput.size

            val inter = intermediateInput.zip(listOfWeights[i].subList(start, end)).map { (inpn, wn) ->
                multiply {
                    put(inpn)
                    put(wn)
                }
            }

            val endNode = add {
                put(inter)
                put(listOfBiases[i][index])
            }

            endNodes.add(endNode)
        }

        results.add(endNodes)
    }

    return results
}

val ReLU: (Double) -> Double = { Math.max(0.0, it) }

val dReLU: (Double) -> Double = { if (it > 0.0) 1.0 else if (it < 0.0) 0.0 else 0.5 }

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

    override val gradientf: (Double, Double) -> Double = { childGradientValue, _ -> if (childGradientValue <= 0.0) 0.0 else 1.0 }
}