package linearalgebra

/**
 * Created by cesarchretien on 21-12-17.
 */
fun main(args: Array<String>) {
    val matrix = Matrix(100, 100)

    time("100 x 100 matrix setting") {
        matrix.forEachIndexed { row, col, value ->
            this[row, col] = Math.random()
        }
    }

    val matrix2 = Matrix(500, 500)

    time("500 x 500 matrix setting") {
        matrix2.forEachIndexed { row, col, value ->
            this[row, col] = Math.random()
        }
    }

    val matrix3 = Matrix(1000, 1000)

    time("1000 x 1000 matrix setting") {
        matrix3.forEachIndexed { row, col, value ->
            this[row, col] = Math.random()
        }
    }

    val matrix4 = Matrix(5000, 5000)

    time("5000 x 5000 matrix setting") {
        matrix4.forEachIndexed { row, col, value ->
            this[row, col] = Math.random()
        }
    }


}

fun time(tag: String = "", block: () -> Unit) {
    println("Start timing of block${if (tag.isEmpty()) "" else " ${tag.trim()}"}")
    val start = System.currentTimeMillis()
    block()
    val end = System.currentTimeMillis()
    println("Computing block ${if (tag.isEmpty()) "" else "${tag.trim()} "}took ${end - start} milliseconds")
}