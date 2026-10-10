package student.activities

import ksl.modeling.elements.EventGenerator
import ksl.modeling.elements.EventGeneratorIfc
import ksl.modeling.station.ActivityStation
import ksl.modeling.station.SingleQStation
import ksl.modeling.variable.IndicatorResponse
import ksl.modeling.variable.RandomVariable
import ksl.modeling.variable.Response
import ksl.modeling.variable.TWResponse
import ksl.simulation.Model
import ksl.simulation.ModelElement
import ksl.utilities.random.rvariable.BernoulliRV
import ksl.utilities.random.rvariable.ExponentialRV
import ksl.utilities.random.rvariable.TriangularRV
import ksl.utilities.random.rvariable.UniformRV
import ksl.utilities.random.rvariable.toBoolean

/**
 * The extended airport checkpoint, rebuilt with the station package. The
 * random variables that are not part of a station, and the responses, are
 * given; the activity writes the parts marked TODO. As given, the file compiles
 * and runs, with no passengers.
 */
fun main() {
    val model = Model("AirportStationsModel")
    AirportStations(model, name = "Airport")
    model.numberOfReplications = 30 // 30 days
    model.lengthOfReplication = 600.0 // 10 hours, in minutes
    model.simulate()
    model.print()
}

class AirportStations(
    parent: ModelElement,
    numInspectors: Int = 1,
    name: String? = null
) : ModelElement(parent, name) {

    private val myArrivalRV = ExponentialRV(2.0, 1)
    private val myPassRV = RandomVariable(this, BernoulliRV(0.93, 3))

    private val myNS = TWResponse(this, "Num in System")
    private val mySysTime = Response(this, "System Time")

    // TODO 1: the arrivals, as an EventGenerator that calls arrival() and uses
    //  myArrivalRV; the inspection and X-ray, as SingleQStations with their
    //  activity times (streams 2 and 4); the paperwork, as an ActivityStation
    //  (stream 5); and an indicator of a wait of 5 minutes or more at inspection.

    private inner class Passenger : QObject() {
        var passed: Boolean = myPassRV.value.toBoolean()
    }

    // TODO 2: route the passengers: say where each station sends a passenger
    //  next, write arrival(), the decision after inspection, and the departure.
}
