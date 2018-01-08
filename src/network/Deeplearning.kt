package network

import linearalgebra.Matrix

/**
 * Created by cesarchretien on 24-12-17.
 */
fun main(args: Array<String>) {

}

fun createSystem(input: Collection<Double>, vararg layerDimensions: Double) {

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