package student.activities

import ksl.utilities.distributions.ContinuousDistributionIfc
import ksl.utilities.distributions.InverseCDFIfc
import ksl.utilities.distributions.Normal
import ksl.utilities.io.plotting.FitDistPlot
import ksl.utilities.random.rvariable.NormalRV
import ksl.utilities.random.rvariable.RVariableIfc
import ksl.utilities.random.rvariable.UniformRV
import ksl.utilities.statistic.Statistic
import ksl.utilities.statistic.averages
import ksl.utilities.statisticsByRow
import kotlin.math.sqrt

/**
 * Sampling distributions: the averages of many samples from a population, against
 * the normal distribution the central limit theorem predicts, and the coverage of
 * the confidence interval. The activity fills in the two functions marked TODO; as
 * given, the file compiles and runs, and reports empty statistics.
 */
fun main() {
    val sampleSize = 10 // the size of each sample
    val numSamples = 50
    println("Sample size = $sampleSize, number of samples = $numSamples")

    // Part 2: a normal population with mean 5 and variance 4 (the KSL's NormalRV
    // takes the variance, not the standard deviation)
    val normal = NormalRV(mean = 5.0, variance = 4.0, streamNum = 1)
    val normalAverages = sampleAverages(normal, sampleSize, numSamples)
    describeAverages("Averages from N(5, 4)", normalAverages, 5.0, 4.0, sampleSize)
    plotAverages(normalAverages, Normal(5.0, 4.0 / sampleSize), "NormalAverages_n$sampleSize")

    // Part 3: a uniform population on [10, 60]
    val uniform = UniformRV(10.0, 60.0, streamNum = 2)
    val uniformAverages = sampleAverages(uniform, sampleSize, numSamples)
    val uVar = (60.0 - 10.0) * (60.0 - 10.0) / 12.0
    describeAverages("Averages from U(10, 60)", uniformAverages, 35.0, uVar, sampleSize)
    plotAverages(uniformAverages, Normal(35.0, uVar / sampleSize), "UniformAverages_n$sampleSize")

    // Part 4: how often does a 95% confidence interval contain the true mean?
    val normal3 = NormalRV(mean = 5.0, variance = 4.0, streamNum = 3)
    describeCoverage("Coverage, N(5, 4)", coverage(normal3, 5.0, sampleSize, 1000))
}

/**
 * TODO 1: Generate [numSamples] samples, each of size [sampleSize], from [rv].
 * Return the average of each sample.
 */
fun sampleAverages(rv: RVariableIfc, sampleSize: Int, numSamples: Int): DoubleArray {
    return DoubleArray(0)
}

/**
 * TODO 2: Generate [numSamples] samples of size [sampleSize] from [rv]. For each
 * sample, compute the 95% confidence interval for the mean, and collect true if it
 * contains [trueMean]. Return the statistic.
 */
fun coverage(rv: RVariableIfc, trueMean: Double, sampleSize: Int, numSamples: Int): Statistic {
    val ciStat = Statistic("Coverage")
    return ciStat
}

/** Prints the statistics of the averages beside what the theory predicts. */
fun describeAverages(title: String, averages: DoubleArray, mu: Double, sigma2: Double, n: Int) {
    val stat = Statistic(title, averages)
    val sd = sqrt(sigma2 / n)
    val outside = averages.count { it < mu - 2.0 * sd || it > mu + 2.0 * sd }
    println(title)
    println("                      Theory    Averages")
    println("  Mean            %10.4f  %10.4f".format(mu, stat.average))
    println("  Variance        %10.4f  %10.4f".format(sigma2 / n, stat.variance))
    println("  Std deviation   %10.4f  %10.4f".format(sd, stat.standardDeviation))
    println("  Outside mean +/- 2 std deviations: $outside of ${averages.size}")
}

/** Prints the estimated coverage probability with its 95% confidence interval. */
fun describeCoverage(title: String, stat: Statistic) {
    println(title)
    println("  Intervals     = %d".format(stat.count.toInt()))
    println("  Estimate      = %.4f".format(stat.average))
    println("  Half-width    = %.4f".format(stat.halfWidth))
    println("  95%% interval  = [%.4f, %.4f]".format(stat.confidenceInterval.lowerLimit, stat.confidenceInterval.upperLimit))
}

/** Plots the averages against the normal distribution the theory predicts. */
fun <T> plotAverages(averages: DoubleArray, theory: T, fileName: String)
        where T : ContinuousDistributionIfc, T : InverseCDFIfc {
    if (averages.size < 2) return // nothing to plot yet
    val plot = FitDistPlot(averages, theory, theory)
    plot.saveToFile(fileName) // in kslOutput/plotDir
    plot.showInBrowser()
}
