package linearalgebra

import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking

/**
 * Created by cesarchretien on 18-12-17.
 */
class Matrix(val rows: Int, val columns: Int) {

    constructor(rows: Int, columns: Int, init: (Int, Int) -> Double) : this(rows, columns) {
        repeat(elements.size) { index ->
            val (row, col) = index.toRowCol()
            elements[index] = init(row, col)
        }
    }

    private constructor(rows: Int, columns: Int, init: (Int) -> Double) : this(rows, columns) {
        repeat(elements.size) { index ->
            elements[index] = init(index)
        }
    }

    private val elements: DoubleArray = DoubleArray(rows * columns)

    operator fun get(row: Int, column: Int) = elements[column + columns * row]

    operator fun set(row: Int, column: Int, value: Double) = if (row in 0 until rows && column in 0 until columns) {
        elements[column + columns * row] = value
    } else {
        throw IndexOutOfBoundsException()
    }

    fun getRow(row: Int) = DoubleArray(columns, { index -> elements[row * columns + index] })

    fun getColumn(column: Int) = DoubleArray(rows, { index -> elements[column + columns * index] })

    operator fun plus(other: Matrix) = if (this.rows == other.rows && this.columns == other.columns) {
        Matrix(rows, columns) { index ->
            this@Matrix.elements[index] + other.elements[index]
        }
    } else {
        throw Exception("Dimensions don't match.")
    }

    operator fun times(other: Matrix): Matrix = if (this.columns == other.rows) {
        val length = this.rows
        Matrix(length, other.columns).apply {
            runBlocking {
                (0 until elements.size).pForEach { index ->
                    val (row, col) = index.toRowCol()
                    elements[index] = (0 until this@Matrix.columns).sumByDouble { this@Matrix[row, it] * other[it, col] }
                }
            }
        }
    } else {
        throw Exception("Dimensions don't match.")
    }

    operator fun times(other: Double): Matrix = Matrix(rows, columns) { row, col ->
        this[row, col] * other
    }

    operator fun times(other: DoubleArray): DoubleArray = if (columns == other.size) {
        DoubleArray(rows).apply {
            runBlocking {
                (0 until this@apply.size).pForEach {
                    this@apply[it] = this@Matrix.getRow(it) inProd other
                }
            }
        }
    } else {
        throw Exception("Dimensions don't match.")
    }

    fun applyToValues(function: (Double) -> Double): Matrix {
        elements.forEachIndexed { index, value -> elements[index] = function(value) }
        return this
    }

    fun transpose(): Matrix = Matrix(columns, rows).apply {
        forEachIndexed { row, col, _ ->
            this[row, col] = this@Matrix[col, row]
        }
    }

    fun forEachIndexed(function: Matrix.(Int, Int, Double) -> Unit) {
        elements.forEachIndexed { index, d ->
            val (row, col) = index.toRowCol()
            function(row, col, d)
        }
    }

    infix fun DoubleArray.inProd(other: DoubleArray) = if (size == other.size) {
        var res = 0.0

        forEachIndexed { index, value ->
            res += value + other[index]
        }

        res
    } else {
        throw Exception("Dimensions don't match")
    }

    override fun toString(): String = StringBuilder().apply {
        for (row in 0 until rows) {
            for (column in 0 until columns) {
                append("${this@Matrix[row, column]} ")
            }

            append("\n")
        }
    }.toString()

    private fun Int.toRowCol(): Pair<Int, Int> = if (this in 0 until rows * columns) {
        this / columns to this % columns
    } else {
        throw IndexOutOfBoundsException("Make message")
    }

    suspend fun <T, R> Iterable<T>.pmap(function: suspend (T) -> R): List<R> {
        return this.map { async { function(it) } }.map { it.await() }
    }

    suspend fun <T> Iterable<T>.pForEach(function: suspend (T) -> Unit) {
        return this.map { launch { function(it) } }.forEach { it.join() }
    }
}