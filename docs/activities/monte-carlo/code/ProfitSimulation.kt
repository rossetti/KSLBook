package student.activities

import ksl.utilities.random.rvariable.DEmpiricalRV
import ksl.utilities.random.rvariable.GeometricRV
import ksl.utilities.random.rvariable.TriangularRV
import ksl.utilities.statistic.Statistic

/**
 * A static simulation of a year's profit for an item:
 * profit = (unit price - unit cost) x (quantity sold) - fixed cost.
 * The activity fills in the function marked TODO; as given, the file compiles and
 * runs, and reports empty statistics.
 */
fun main() {
    val sampleSize = 1000 // the number of years simulated at each price
    val firstPrice = 200
    val lastPrice = 200
    val step = 5
    println("Sample size = $sampleSize")
    println(" Price   Avg profit   Half-width   P(> 150K)   Half-width   n for +/- 6000")
    for (price in firstPrice..lastPrice step step) {
        val (profit, prob) = simulateProfit(price.toDouble(), sampleSize)
        println(
            "%6d %12.0f %12.0f %11.4f %12.4f %16d".format(
                price, profit.average, profit.halfWidth, prob.average, prob.halfWidth,
                if (profit.count > 1) profit.estimateSampleSize(6000.0) else 0
            )
        )
    }
}

/**
 * TODO: Simulate [sampleSize] years at the given [unitPrice]. The quantity sold
 * (thousands of units) is geometric on 0, 1, 2, ... with mean 4 (stream 1); the
 * unit cost is 80, 90, 100, or 110 with probabilities 0.2, 0.4, 0.3, 0.1
 * (stream 2); the fixed cost is 65000 + 10000 X, with X triangular(-1, 0, 1)
 * (stream 3). Every price must see the same years. Return the statistics on the
 * profit and on the indicator that the profit exceeds 150,000.
 */
fun simulateProfit(unitPrice: Double, sampleSize: Int): Pair<Statistic, Statistic> {
    val profitStat = Statistic("Profit")
    val probStat = Statistic("P(Profit > 150K)")
    return Pair(profitStat, probStat)
}
