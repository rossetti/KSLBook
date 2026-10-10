package student.activities

/**
 * A linear congruential generator (LCG): R(i+1) = (a R(i) + c) mod m, with the
 * pseudo-random number U(i) = R(i)/m. The activity fills in the two functions
 * marked TODO; as given, the file compiles and runs, and prints zeros.
 */
fun main() {
    val c = 3
    val m = 16
    val seed = 1
    val a = 7 // the multiplier
    println("LCG with a = $a, c = $c, m = $m, seed = $seed")
    println("  i  R(i)    U(i)")
    var r = seed
    for (i in 1..m) {
        r = nextR(a, c, m, r)
        println("%3d  %4d  %6.4f".format(i, r, r.toDouble() / m))
    }
    println("Period = ${lcgPeriod(a, c, m, seed)}")
}

/** TODO 1: Return the next value of the LCG, (a r + c) mod m. */
fun nextR(a: Int, c: Int, m: Int, r: Int): Int {
    return 0
}

/**
 * TODO 2: Return the period of the LCG started from [seed]: the number of steps
 * until a value repeats. Hint: remember the step at which each value first appeared.
 */
fun lcgPeriod(a: Int, c: Int, m: Int, seed: Int): Int {
    return 0
}
