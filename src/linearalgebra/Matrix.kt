package linearalgebra

import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking

/**
 * Created by cesarchretien on 18-12-17.
 */
class Matrix(val rows: Int, val columns: Int) {

    private val elements: DoubleArray = DoubleArray(rows * columns)

    operator fun get(row: Int, column: Int) = elements[column + columns * row]


    operator fun set(row: Int, column: Int, value: Double) = if (row in 0 until rows && column in 0 until columns) {
        elements[column + columns * row] = value
    } else {
        throw IndexOutOfBoundsException("Make message")
    }

    fun getRow(row: Int) = DoubleArray(columns, { index -> elements[row * columns + index] })

    fun getColumn(column: Int) = DoubleArray(rows, { index -> elements[column + columns * index] })

    operator fun plus(other: Matrix) = if (this.rows == other.rows && this.columns == other.columns) {
        Matrix(rows, columns).apply {
            for (index in 0 until elements.size) {
                this@apply.elements[index] = this@Matrix.elements[index] + other.elements[index]
            }
        }
    } else {
        throw Exception("Dimensions don't match.")
    }

    operator fun times(other: Matrix) = if (this.columns == other.rows) {
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

    operator fun times(other: Double): Matrix = Matrix(rows, columns).apply {
        elements.map { it * other }
    }

    operator fun times(other: DoubleArray): DoubleArray = if (this.columns == other.size) {
        DoubleArray(this.rows).apply {
            runBlocking {
                (0 until this@apply.size).pmap {
                    this@apply[it] = this@Matrix.getRow(it).zip(other).map { (l, r) -> l * r }.sum()
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

    fun transpose(): Matrix = Matrix(this.columns, this.rows).apply {
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

    suspend fun <T, R> Iterable<T>.pForEach(function: suspend (T) -> R) {
        return this.map { async { function(it) } }.forEach { it.await() }
    }
}