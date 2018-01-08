package network

/**
 * Created by cesarchretien on 04-01-18.
 */
sealed class Node(var value: Double = 0.0) {

    val children = ArrayList<Node>()

    open var gradientValue = 0.0

    fun value(): Double = when (this) {
        is RootNode -> value
        is OperationNode -> valuef(parents.map { it.value() }.also { if (inputValues.isEmpty()) inputValues += it }).also { value = it }
    }

    override fun toString(): String = "{ type: ${this.javaClass.simpleName}, value: $value, gradientValue: $gradientValue"
}

class RootNode(value: Double) : Node(value)

abstract class OperationNode : Node() {

    val inputValues = ArrayList<Double>()

    val parents = ArrayList<Node>()

    abstract val valuef: (List<Double>) -> Double

    abstract val gradientf: (Double, Double, List<Double>) -> Double

    fun put(node: Node) = node.also { linkParent(it) }

    fun put(nodes: List<Node>) = nodes.map { put(it) }

    fun root(value: Double) = RootNode(value)
            .also { linkParent(it) }

    fun add(f: AdditionNode.() -> Unit) = AdditionNode()
            .also { linkParent(it) }
            .apply(f)

    fun multiply(f: MultiplicationNode.() -> Unit) = MultiplicationNode()
            .also { linkParent(it) }
            .apply(f)

    private fun linkParent(parent: Node) {
        this.parents.add(parent)
        parent.children.add(this)
    }

    fun gradient(): List<RootNode> {
        gradientValue = 1.0
        return gradient(gradientValue)
    }

    private fun gradient(childGradientValue: Double, result: MutableList<RootNode> = ArrayList()): List<RootNode> {
        parents.forEach { parent ->
            parent.gradientValue += gradientf(childGradientValue, parent.value, inputValues)

            when (parent) {
                is RootNode -> result.indexOf(parent).let { if (it == -1) result.add(parent) }
                is OperationNode -> parent.gradient(parent.gradientValue, result)
            }
        }

        return result
    }

    override fun toString(): String = "${super.toString()}" + if (parents.isNotEmpty()) {
        ", inputValues: $inputValues, child: ${parents.joinToString(separator = ", ") { "$it" }}}"
    } else {
        "}"
    }
}

class AdditionNode : OperationNode() {

    override val valuef: (List<Double>) -> Double = addf

    override val gradientf: (Double, Double, List<Double>) -> Double = { childGradientValue, _, _ -> childGradientValue }
}

class MultiplicationNode : OperationNode() {

    override val valuef: (List<Double>) -> Double = multiplyf

    override val gradientf: (Double, Double, List<Double>) -> Double = { childGradientValue, parentValue, inputValues ->
        inputValues.filterFirst { it != parentValue }.reduce { l, r -> l * r }.let { childGradientValue * it }
    }
}

private val addf: (List<Double>) -> Double = { it.sum() }

private val multiplyf: (List<Double>) -> Double = { it.reduce { l, r -> l * r } }

fun add(f: AdditionNode.() -> Unit) = AdditionNode().apply(f)

fun multiply(f: MultiplicationNode.() -> Unit) = MultiplicationNode().apply(f)

inline fun <T> Iterable<T>.filterFirst(predicate: (T) -> Boolean): List<T> {
    return filterFirstTo(ArrayList(), predicate)
}

inline fun <T, C : MutableCollection<in T>> Iterable<T>.filterFirstTo(destination: C, predicate: (T) -> Boolean): C {
    val x = this.iterator()
    for (element in x) if (predicate(element)) destination.add(element) else break
    x.forEachRemaining { destination.add(it) }
    return destination
}