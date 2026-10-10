package student.activities

import ksl.utilities.random.rvariable.GammaRV
import ksl.utilities.random.rvariable.RVariableIfc
import ksl.utilities.statistic.Statistic

/**
 * The aircraft stocking problem: choose the stock level q of a part for a week,
 * when unused parts cost h to refurbish and each part short costs b. The weekly
 * cost is G(q, D) = h max(0, q - D) + b max(0, D - q). The activity fills in the
 * function marked TODO; as given, the file compiles and runs, and reports empty
 * statistics.
 */
fun main() {
    val sampleSize = 1000 // the number of weeks simulated at each q
    val firstQ = 30
    val lastQ = 30
    val step = 5
    val demandRV = GammaRV(shape = 6.5998, scale = 4.2653, streamNum = 1)
    println("Sample size = $sampleSize")
    println("   q   Avg cost   Half-width   Std dev")
    for (q in firstQ..lastQ step step) {
        val cost = weeklyCost(q.toDouble(), demandRV, sampleSize)
        println("%4d %10.2f %12.2f %9.2f".format(q, cost.average, cost.halfWidth, cost.standardDeviation))
    }
}

/**
 * TODO: Estimate the expected weekly cost of stocking [q] parts over [sampleSize]
 * weeks of demand from [demandRV], with h = 1000 per part left over and b = 9000
 * per part short. Every q must see the same weeks of demand. Return the statistic.
 */
fun weeklyCost(q: Double, demandRV: RVariableIfc, sampleSize: Int): Statistic {
    val stat = Statistic("Cost for q = $q")
    return stat
}
