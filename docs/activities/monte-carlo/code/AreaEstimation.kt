package student.activities

import ksl.utilities.random.rvariable.UniformRV
import ksl.utilities.statistic.Statistic
import kotlin.math.sqrt

/**
 * Two Monte Carlo estimators of the area under f(x) = sqrt(1 - x^2) on [0, 1],
 * a quarter of the unit circle, whose area is pi/4. The activity fills in the
 * two functions marked TODO; as given, the file compiles and runs, and reports
 * empty statistics.
 */
fun main() {
    val n = 1000 // the sample size
    println("n = $n")
    val crude = crudeMonteCarlo(n)
    report("Crude Monte Carlo", crude)
    val hits = hitOrMiss(n)
    report("Hit or miss", hits)
    println("Sample size for a half-width of 0.001:")
    println("  crude       ${crude.estimateSampleSize(0.001)}")
    println("  hit or miss ${hits.estimateSampleSize(0.001)}")
}

/** The function whose area is estimated. */
fun f(x: Double): Double = sqrt(1.0 - x * x)

/**
 * TODO 1: Estimate the area by averaging Y = (b - a) f(X) over [sampleSize]
 * observations of X ~ U(0, 1), drawn from stream 3. Return the statistic.
 */
fun crudeMonteCarlo(sampleSize: Int): Statistic {
    val stat = Statistic("Crude Monte Carlo")
    return stat
}

/**
 * TODO 2: Estimate the area as the fraction of [sampleSize] random points
 * (x, y) in the unit square, x drawn from stream 4 and y from stream 5, that
 * fall on or under f. Collect true for a hit and false for a miss. Return the
 * statistic.
 */
fun hitOrMiss(sampleSize: Int): Statistic {
    val stat = Statistic("Hit or Miss")
    return stat
}

/** Prints an estimate beside the true area, with its 95% confidence interval. */
fun report(title: String, stat: Statistic) {
    println(title)
    println("  True area     = %.6f".format(Math.PI / 4.0))
    println("  Estimate      = %.6f".format(stat.average))
    println("  Half-width    = %.6f".format(stat.halfWidth))
    println("  95% interval  = ${stat.confidenceInterval}")
    println("  Std deviation = %.6f".format(stat.standardDeviation))
}
