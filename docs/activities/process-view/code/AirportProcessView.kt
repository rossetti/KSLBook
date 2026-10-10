package student.activities

import ksl.modeling.entity.KSLProcess
import ksl.modeling.entity.ProcessModel
import ksl.modeling.entity.ResourceWithQ
import ksl.modeling.entity.ResourceWithQCIfc
import ksl.modeling.variable.Counter
import ksl.modeling.variable.CounterCIfc
import ksl.modeling.variable.IndicatorResponse
import ksl.modeling.variable.RandomVariable
import ksl.modeling.variable.RandomVariableCIfc
import ksl.modeling.variable.Response
import ksl.modeling.variable.ResponseCIfc
import ksl.modeling.variable.TWResponse
import ksl.modeling.variable.TWResponseCIfc
import ksl.simulation.KSLEvent
import ksl.simulation.Model
import ksl.simulation.ModelElement
import ksl.utilities.random.rvariable.BernoulliRV
import ksl.utilities.random.rvariable.ExponentialRV
import ksl.utilities.random.rvariable.TriangularRV

/**
 * Identification inspection at an airport security checkpoint, in the process
 * view. The random variables and the responses are given; the activity writes
 * the parts marked TODO. As given, the file compiles and runs, with no
 * passengers.
 */
fun main() {
    val m = Model("AirportProcessView")
    AirportProcessView(m, name = "Airport")
    m.numberOfReplications = 30  // 30 days
    m.lengthOfReplication = 600.0 // 10 hours, in minutes
    m.simulate()
    m.print()
}

class AirportProcessView(
    parent: ModelElement,
    numInspectors: Int = 1,
    name: String? = null
) : ProcessModel(parent, name = name) {

    private val myArrivalRV: RandomVariable = RandomVariable(this, ExponentialRV(2.0, 1))
    val arrivalRV: RandomVariableCIfc
        get() = myArrivalRV

    private val myInspectionTimeRV: RandomVariable = RandomVariable(this, TriangularRV(0.75, 1.5, 3.0, 2))
    val inspectionRV: RandomVariableCIfc
        get() = myInspectionTimeRV

    private val myPassRV: RandomVariable = RandomVariable(this, BernoulliRV(0.93, 3))

    private val myNS: TWResponse = TWResponse(this, "Num in System")
    val numInSystem: TWResponseCIfc
        get() = myNS

    private val mySysTime: Response = Response(this, "System Time")
    val systemTime: ResponseCIfc
        get() = mySysTime

    private val myNumCleared: Counter = Counter(this, "Num Cleared")
    val numPassengersCleared: CounterCIfc
        get() = myNumCleared

    private val myNumDenied: Counter = Counter(this, "Num Denied")
    val numPassengersDenied: CounterCIfc
        get() = myNumDenied

    private val myWaitTime: Response = Response(this, "Wait Time")
    val waitTime: ResponseCIfc
        get() = myWaitTime

    private val myWTGE5: IndicatorResponse = IndicatorResponse(
        { x -> x >= 5.0 }, myWaitTime, "WaitTime >= 5 minutes"
    )

    // TODO 1: construct the security officer: a ResourceWithQ with
    //  numInspectors units, named "Officer".

    // TODO 2: start the arrivals. Override initialize() to schedule the first
    //  arrival, and write an arrival event that creates a Passenger, activates
    //  its process, and schedules the next arrival.

    // TODO 3: write the Passenger entity and its process: enter the system,
    //  seize the officer, record the wait, delay for the inspection, release
    //  the officer, then decide pass or deny and record the results.
}
