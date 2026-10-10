package student.activities

import ksl.modeling.queue.Queue
import ksl.modeling.variable.Counter
import ksl.modeling.variable.IndicatorResponse
import ksl.modeling.variable.RandomVariable
import ksl.modeling.variable.Response
import ksl.modeling.variable.TWResponse
import ksl.simulation.KSLEvent
import ksl.simulation.Model
import ksl.simulation.ModelElement
import ksl.utilities.random.rvariable.BernoulliRV
import ksl.utilities.random.rvariable.ExponentialRV
import ksl.utilities.random.rvariable.TriangularRV

/**
 * Identification inspection at an airport security checkpoint, in the event view.
 * The activity writes the model's parts, marked TODO. As given, the file
 * compiles and runs, with no passengers.
 */
fun main() {
    val model = Model("AirportEventViewModel")
    AirportEventView(model, name = "Airport")
    model.numberOfReplications = 30 // 30 days
    model.lengthOfReplication = 600.0 // 10 hours, in minutes
    model.simulate()
    model.print()
}

class AirportEventView(
    parent: ModelElement,
    private val numInspectors: Int = 1,
    name: String? = null
) : ModelElement(parent, name) {

    // TODO 1: the random variables: time between arrivals, inspection time, and
    //  passing inspection, each a RandomVariable with its stream.

    // TODO 2: the responses: the number of busy inspectors, the inspection queue,
    //  an indicator of a wait of 5 minutes or more, counters of passengers cleared
    //  and denied, the number in the system, and the time in the system.

    // TODO 3: the events: start the arrivals in initialize(); write the arrival
    //  event and the end-of-inspection event.
}
