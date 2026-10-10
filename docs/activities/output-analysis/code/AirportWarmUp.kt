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
 * The airport security checkpoint, built with the station package:
 * identification inspection, then X-ray for those who pass; those who do not
 * pass do paperwork and rejoin the inspection line, and always pass the second
 * time. The checkpoint runs around the clock. The activity's six steps are
 * marked TODO. As given, the file compiles and runs the model.
 */
fun main() {
    val model = Model("AirportWarmUpModel")
    val airport = AirportWarmUp(model, name = "Airport")
    model.lengthOfReplication = 30000.0 // minutes, about 3 weeks
    model.numberOfReplications = 10

    // TODO 1: change the time between arrivals to exponential with mean 2.5 minutes.
    // TODO 5: capture the results in a KSL database.
    // TODO 6: observe the system time for a Welch plot.

    model.simulate()
    model.print()

    // TODO 6: make the Welch plot and save it to a file.
}

class AirportWarmUp(
    parent: ModelElement,
    numInspectors: Int = 1,
    name: String? = null
) : ModelElement(parent, name) {

    private val myArrivalRV = ExponentialRV(2.0, 1)
    private val myArrivalGenerator = EventGenerator(
        this, generateAction = this::arrivalEvent,
        timeUntilFirstRV = myArrivalRV, timeBtwEventsRV = myArrivalRV,
        name = "Arrivals"
    )
    // TODO 1: expose the arrival generator, so that its time between
    //  arrivals can be changed.

    private val myPassRV: RandomVariable = RandomVariable(this, BernoulliRV(0.93, 3))
    // TODO 2: add a way to change the probability of passing inspection.

    private val myInspectionStation = SingleQStation(
        this, activityTime = TriangularRV(0.75, 1.5, 3.0, 2),
        initialCapacity = numInspectors, name = "Inspection"
    )

    private val myXRayStation = SingleQStation(
        this, activityTime = ExponentialRV(2.0, 4), name = "XRay"
    )
    // TODO 3: add a way to change the number of X-ray machines.

    private val myPaperWorkStation = ActivityStation(
        this, UniformRV(10.0, 15.0, 5), name = "PaperWork"
    )

    private val myNS = TWResponse(this, "Num in System")
    private val mySystemTime = Response(this, "System Time")
    // TODO 4: give direct (read-only) access to the system time.

    private val mySTGE5 = IndicatorResponse({ x -> x >= 5.0 }, mySystemTime, "System Time >= 5")

    private inner class Passenger : QObject() {
        var passed: Boolean = myPassRV.value.toBoolean()
    }

    init {
        myInspectionStation.nextReceiver(this::decidePassing)
        myXRayStation.nextReceiver(this::departSystem)
        myPaperWorkStation.nextReceiver(myInspectionStation)
    }

    private fun arrivalEvent(generator: EventGeneratorIfc) {
        myNS.increment()
        myInspectionStation.receive(Passenger())
    }

    private fun decidePassing(qObject: QObject) {
        val passenger = qObject as Passenger
        if (passenger.passed) {
            myXRayStation.receive(passenger)
        } else {
            passenger.passed = true // passengers sent back always pass
            myPaperWorkStation.receive(passenger)
        }
    }

    private fun departSystem(qObject: QObject) {
        myNS.decrement()
        mySystemTime.value = time - qObject.createTime
    }
}
