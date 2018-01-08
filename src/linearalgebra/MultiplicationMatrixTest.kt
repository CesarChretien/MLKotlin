package linearalgebra

/**
 * Created by cesarchretien on 21-12-17.
 */
fun main(args: Array<String>) {
    val matrix1 = Matrix(100, 100)
    val matrix12 = Matrix(100, 200)
    val matrix2 = Matrix(500, 500)
    val matrix22 = Matrix(500, 750)
    val matrix3 = Matrix(1000, 1000)
    val matrix32 = Matrix(1000, 1500)
    val matrix4 = Matrix(5000, 5000)
    val matrix42 = Matrix(5000, 8000)

    println("Setting up matrices...")

    matrix1.forEachIndexed { row, col, value ->
        this[row, col] = Math.random()
    }

    matrix12.forEachIndexed { row, col, value ->
        this[row, col] = Math.random()
    }

    matrix2.forEachIndexed { row, col, value ->
        this[row, col] = Math.random()
    }

    matrix22.forEachIndexed { row, col, value ->
        this[row, col] = Math.random()
    }

    matrix3.forEachIndexed { row, col, value ->
        this[row, col] = Math.random()
    }

    matrix32.forEachIndexed { row, col, value ->
        this[row, col] = Math.random()
    }

    println("Done setting matrices...")

    time("Multiplying 100 x 100 and 100 x 200 matrices") {
        matrix1 * matrix12
    }

    time("Multiplying 500 x 500 and 500 x 750 matrices") {
        matrix2 * matrix22
    }

    time("Multiplying 1000 x 1000 and 1000 x 1500 matrices") {
        matrix3 * matrix32
    }


}