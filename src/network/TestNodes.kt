package network

/**
 * Created by cesarchretien on 29-12-17.
 */

fun main(args: Array<String>) {

    val simple =
            multiply {
                add {
                    root(-2.0)
                    root(5.0)
                }
                root(-4.0)
            }

    simple.value()
    simple.gradient()
    println("$simple\n")

    val complex: (Double, Double, Double, Double) -> OperationNode = { w, x, y, z ->
        val rootw = RootNode(w)

        multiply {
            add {
                root(x)
                multiply {
                    root(y)
                    add {
                        put(rootw)
                        root(z)
                    }
                }
            }
            put(rootw)
        }
    }


    val y = complex(1.0, 2.0, 3.0, 4.0)

    y.value()
    val z = y.gradient()
    println(z.map { "\nv: ${it.value}, g: ${it.gradientValue}" })

    val test = (0..5).map { RootNode(1.0 * it) }

    val sdf = add {
        put(test)
    }

    println("${sdf.value()}")
}

