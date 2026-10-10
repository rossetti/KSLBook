package student.activities

import ksl.utilities.io.KSLFileUtil
import ksl.utilities.io.plotting.ACFPlot
import ksl.utilities.io.plotting.ObservationsPlot
import ksl.utilities.statistic.Histogram
import ksl.utilities.statistic.Statistic
import java.nio.file.Path

/**
 * Fitting a distribution to 250 service times (seconds) at a movie theater counter.
 * The activity fills in the two functions marked TODO; as given, the file compiles
 * and runs, and shows the data but fits nothing.
 */
fun main() {
    val split = 80.0 // the split point for part 3
    // the data file: change the path to where you saved movieTheaterData.txt
    val file = Path.of("docs/activities/randomness/data/movieTheaterData.txt")
    val data = KSLFileUtil.scanToArray(file)

    // Part 1: look at the data
    println(Statistic("Service times", data))
    val histogram = Histogram.create(data)
    println(histogram)
    histogram.histogramPlot().showInBrowser("Movie theater service times")
    ObservationsPlot(data).showInBrowser("Observations")
    ACFPlot(data).showInBrowser("ACF")

    // Part 2: fit all the data as one distribution
    fitAndReport(data, "All the data")

    // Part 3: split the data and fit each part
    splitAnalysis(data, split)
}

/**
 * TODO 1: Fit the default continuous distributions to [data] with a PDFModeler.
 * Print the distributions in the order the scoring model ranks them, with their
 * scores, then the top one with its shift and its goodness-of-fit p-values.
 */
fun fitAndReport(data: DoubleArray, title: String) {
    println("== $title: ${data.size} observations")
}

/**
 * TODO 2: Split [data] into the observations at or below [split] and those above
 * it, fit each part with fitAndReport, and print the fraction of observations in
 * each part.
 */
fun splitAnalysis(data: DoubleArray, split: Double) {
}
