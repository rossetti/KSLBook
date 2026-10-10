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
 * The airport checkpoint in the event view: the model of the previous activity.
 * The activity extends it with paperwork for those who fail inspection and an
 * X-ray for those who pass; the changes are marked TODO. As given, the file
 * compiles and runs the previous activity's model.
 */
fun main() {
    val model = Model("AirportExtendedModel")
    AirportExtended(model, name = "Airport")
    model.numberOfReplications = 30 // 30 days
    model.lengthOfReplication = 600.0 // 10 hours, in minutes
    model.simulate()
    model.print()
}

class AirportExtended(
    parent: ModelElement,
    private val numInspectors: Int = 1,
    name: String? = null
) : ModelElement(parent, name) {

    // the random variables
    private val myArrivalRV = RandomVariable(this, ExponentialRV(2.0, 1))
    private val myInspectionTimeRV = RandomVariable(this, TriangularRV(0.75, 1.5, 3.0, 2))
    private val myPassRV = RandomVariable(this, BernoulliRV(0.93, 3))
    // TODO 1: add the X-ray time (stream 4) and the paperwork time (stream 5).

    // the responses
    private val myNumBusy = TWResponse(this, "NumBusy")
    private val myInspectionQ: Queue<QObject> = Queue(this, "InspectionQ")
    // the indicator observes a Response; the queue exposes its time in queue read-only
    private val myWaitGE5 = IndicatorResponse(
        { x -> x >= 5.0 }, myInspectionQ.timeInQ as Response, "Wait >= 5 minutes"
    )
    private val myNumCleared = Counter(this, "Num Cleared")
    private val myNumDenied = Counter(this, "Num Denied")
    private val myNS = TWResponse(this, "Num in System")
    private val mySysTime = Response(this, "System Time")
    // TODO 2: add the X-ray's busy count and queue, and a counter of passengers
    //  processed; the cleared and denied counters are no longer needed.

    // TODO 3: add a Passenger class, a QObject that remembers whether it passes.

    // the events
    override fun initialize() {
        schedule(this::arrival, myArrivalRV)
    }

    private fun arrival(event: KSLEvent<Nothing>) {
        myNS.increment()
        myInspectionQ.enqueue(QObject())
        if (myNumBusy.value < numInspectors) {
            myNumBusy.increment()
            schedule(this::endOfInspection, myInspectionTimeRV, myInspectionQ.removeNext())
        }
        schedule(this::arrival, myArrivalRV)
    }

    private fun endOfInspection(event: KSLEvent<QObject>) {
        myNumBusy.decrement()
        if (myInspectionQ.isNotEmpty) {
            myNumBusy.increment()
            schedule(this::endOfInspection, myInspectionTimeRV, myInspectionQ.removeNext())
        }
        // TODO 4: send a passenger who passes to the X-ray, and one who fails to
        //  paperwork, after which it rejoins the inspection line and passes.
        //  Write the end-of-paperwork and end-of-X-ray events.
        val passenger = event.message!!
        if (myPassRV.value == 1.0) {
            myNumCleared.increment()
        } else {
            myNumDenied.increment()
        }
        mySysTime.value = time - passenger.createTime
        myNS.decrement() // cleared or denied, the passenger leaves the model
    }
}
