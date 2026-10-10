package student.activities

import ksl.utilities.distributions.Gamma
import ksl.utilities.distributions.fitting.PDFModeler
import ksl.utilities.io.plotting.ACFPlot
import ksl.utilities.io.plotting.ObservationsPlot
import ksl.utilities.io.plotting.PPPlot
import ksl.utilities.io.plotting.QQPlot
import ksl.utilities.io.writeToFile
import ksl.utilities.random.rvariable.NormalRV
import ksl.utilities.statistic.BoxPlotSummary
import ksl.utilities.statistic.Histogram
import ksl.utilities.statistic.Statistic

/**
 * Chi-squared random variates as the sum of k squared standard normal variates,
 * checked against the theory: mean k, variance 2k, and the Gamma(k/2, 2)
 * distribution. The activity fills in the two functions marked TODO; as given, the
 * file compiles and runs, and reports empty statistics.
 */
fun main() {
    val dof = 5 // the degrees of freedom
    val sampleSize = 1000
    val normalRV = NormalRV(mean = 0.0, variance = 1.0, streamNum = 1)
    val data = sampleChiSquared(sampleSize, dof, normalRV)

    // Part 2: the statistics against the theory
    val stat = Statistic("Chi-squared($dof)", data)
    println("                      Theory      Sample")
    println("  Mean            %10.4f  %10.4f".format(dof.toDouble(), stat.average))
    println("  Variance        %10.4f  %10.4f".format(2.0 * dof, stat.variance))
    println("  Minimum         %10.4f  %10.4f".format(0.0, stat.min))
    println("  95%% CI for the mean: [%.4f, %.4f]".format(
        stat.confidenceInterval.lowerLimit, stat.confidenceInterval.upperLimit))
    if (data.size < 2) return // nothing more to show yet

    // Part 3: the plots
    val histogram = Histogram.create(data)
    println(histogram)
    println(BoxPlotSummary(data))
    val theory = Gamma(shape = dof / 2.0, scale = 2.0)
    val plots = mapOf(
        "Histogram" to histogram.histogramPlot(),
        "BoxPlot" to BoxPlotSummary(data).boxPlot(),
        "Observations" to ObservationsPlot(data),
        "ACF" to ACFPlot(data),
        "PP" to PPPlot(data, theory),
        "QQ" to QQPlot(data, theory)
    )
    for ((name, plot) in plots) {
        plot.saveToFile("ChiSquared${dof}_$name") // in kslOutput/plotDir
        plot.showInBrowser()
    }

    // Part 4: save the data, and fit a distribution to it
    data.writeToFile("ChiSquared$dof.txt") // in kslOutput
    val results = PDFModeler(data).estimateAndEvaluateScores(automaticShifting = false)
    println("Distributions ranked by the scoring model:")
    for (r in results.resultsSortedByScoring) {
        println("  %.4f  %s".format(r.weightedValue, r.distribution))
    }
}

/**
 * TODO 1: Return one chi-squared variate with [dof] degrees of freedom: the sum of
 * [dof] squared variates from [normalRV].
 */
fun generateChiSquared(normalRV: NormalRV, dof: Int): Double {
    return 0.0
}

/** TODO 2: Return an array of [sampleSize] chi-squared variates with [dof] degrees of freedom. */
fun sampleChiSquared(sampleSize: Int, dof: Int, normalRV: NormalRV): DoubleArray {
    return DoubleArray(0)
}
