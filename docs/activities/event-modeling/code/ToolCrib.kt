package student.activities

import ksl.modeling.queue.Queue
import ksl.modeling.queue.QueueCIfc
import ksl.modeling.variable.Counter
import ksl.modeling.variable.HistoricalVariable
import ksl.modeling.variable.Response
import ksl.modeling.variable.ResponseCIfc
import ksl.modeling.variable.TWResponse
import ksl.modeling.variable.TWResponseCIfc
import ksl.observers.ResponseTrace
import ksl.simulation.KSLEvent
import ksl.simulation.Model
import ksl.simulation.ModelElement
import ksl.utilities.GetValueIfc
import ksl.utilities.io.plotting.StateVariablePlot

/**
 * The tool crib of the hand simulation, as a KSL model driven by the same data.
 * The model is given; the activity writes main, marked TODO. As given, the
 * file compiles; main does nothing yet.
 */
fun main() {
    // TODO 1: the arrays of the times between arrivals and the service times.
    // TODO 2: the Model.
    // TODO 3: HistoricalVariables for the arrivals and the service times, and
    //  the ToolCrib.
    // TODO 4: ResponseTraces of the number in the system and in the queue.
    // TODO 5: one replication of length 31; simulate and print.
    // TODO 6: StateVariablePlots of the two traces, saved to files.
}

class ToolCrib(
    parent: ModelElement,
    numServers: Int = 1,
    private val timeBtwArrivals: GetValueIfc,
    private val serviceTime: GetValueIfc,
    name: String? = null
) : ModelElement(parent, name) {
    init {
        require(numServers >= 1) { "The number of servers must be >= 1" }
    }

    var numServers = numServers
        set(value) {
            require(value >= 1) { "The number of servers must be >= 1" }
            field = value
        }

    private val myNumBusy: TWResponse = TWResponse(this, "NumBusy")
    val numBusy: TWResponseCIfc
        get() = myNumBusy
    private val myNS: TWResponse = TWResponse(this, "Num in System")
    val numInSystem: TWResponseCIfc
        get() = myNS
    private val myNumCustomers: Counter = Counter(this, name = "Num Served")

    private val mySysTime: Response = Response(this, "System Time")
    val systemTime: ResponseCIfc
        get() = mySysTime
    private val myWaitingQ: Queue<QObject> = Queue(this, "ServiceQ")
    val waitingQ: QueueCIfc<QObject>
        get() = myWaitingQ

    private val myArrivalEventAction: ArrivalEventAction = ArrivalEventAction()
    private val myEndServiceEventAction: EndServiceEventAction = EndServiceEventAction()
    private val repEndTime = Response(this, "Rep End Time")

    override fun initialize() {
        super.initialize()
        schedule(myArrivalEventAction, timeBtwArrivals)
    }

    override fun replicationEnded() {
        repEndTime.value = time
    }

    private inner class ArrivalEventAction : EventAction<Nothing>() {
        override fun action(event: KSLEvent<Nothing>) {
            myNS.increment()
            myWaitingQ.enqueue(QObject())
            if (myNumBusy.value < numServers) {
                myNumBusy.increment()
                schedule(myEndServiceEventAction, serviceTime, myWaitingQ.removeNext())
            }
            schedule(myArrivalEventAction, timeBtwArrivals)
        }
    }

    private inner class EndServiceEventAction : EventAction<QObject>() {
        override fun action(event: KSLEvent<QObject>) {
            myNumBusy.decrement()
            if (!myWaitingQ.isEmpty) {
                myNumBusy.increment()
                schedule(myEndServiceEventAction, serviceTime, myWaitingQ.removeNext())
            }
            val departingCustomer = event.message!!
            mySysTime.value = (time - departingCustomer.createTime)
            myNS.decrement()
            myNumCustomers.increment()
        }
    }
}
