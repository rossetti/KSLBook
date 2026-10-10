package student.activities

import ksl.modeling.queue.Queue
import ksl.modeling.queue.QueueCIfc
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
import ksl.utilities.io.dbutil.KSLDatabaseObserver
import ksl.utilities.random.rvariable.ExponentialRV
import ksl.utilities.random.rvariable.RVariableIfc

/**
 * One ATM against two slower ATMs. The model is given; the activity writes the
 * experiment in main, marked TODO. As given, the file compiles and runs one
 * experiment with the current ATM.
 */
fun main() {
    val model = Model("ATMComparison")
    model.numberOfReplications = 30 // 30 days
    model.lengthOfReplication = 600.0 // 10 hours, in minutes
    val atm = ATM(model, name = "ATM")

    // TODO 1: capture the results of every experiment in a KSL database.
    // TODO 2: name this experiment "1 ATM", simulate it, and print it.
    // TODO 3: make the second experiment, "2 ATM": two ATMs, each with an
    //  exponential service time with mean 1.8 minutes (stream 2), using the
    //  same random numbers as the first; simulate it and print it.
    // TODO 4: compare the two experiments' probability of a wait of 3 minutes
    //  or more, with a multiple comparison analyzer from the database.

    model.simulate()
    model.print()
}

class ATM(
    parent: ModelElement,
    numUnits: Int = 1,
    ad: RVariableIfc = ExponentialRV(1.0, 1),
    sd: RVariableIfc = ExponentialRV(0.9, 2),
    name: String? = null
) : ModelElement(parent, name = name) {

    var numATMs = numUnits
        set(value) {
            require(value > 0)
            require(!model.isRunning) { "Cannot change the number of ATMs while the model is running!" }
            field = value
        }

    private val myServiceTime: RandomVariable = RandomVariable(this, sd)
    val serviceRV: RandomVariableCIfc
        get() = myServiceTime
    private val myArrivalRV: RandomVariable = RandomVariable(this, ad)
    val arrivalRV: RandomVariableCIfc
        get() = myArrivalRV

    private val myNumBusy: TWResponse = TWResponse(this, "NumBusy")
    val numBusy: TWResponseCIfc
        get() = myNumBusy

    private val myNS: TWResponse = TWResponse(this, "NumInSystem")
    val numInSystem: TWResponseCIfc
        get() = myNS
    private val mySysTime: Response = Response(this, "System Time")
    val systemTime: ResponseCIfc
        get() = mySysTime

    private val myWaitingQ: Queue<QObject> = Queue(this, "WaitQ")
    val waitQ: QueueCIfc<QObject>
        get() = myWaitingQ

    // The indicator observes a Response; the queue exposes its time in queue read-only.
    private val myWTGE3: IndicatorResponse = IndicatorResponse(
        { x -> x >= 3.0 }, myWaitingQ.timeInQ as Response, "WaitTime >= 3 minutes"
    )
    val probWaitTimeGE3Minutes: ResponseCIfc
        get() = myWTGE3

    private val myArrivalEvent = this::arrivalEvent
    private val myEndServiceEvent = this::endOfService

    override fun initialize() {
        schedule(myArrivalEvent, myArrivalRV)
    }

    private fun arrivalEvent(event: KSLEvent<Nothing>) {
        myNS.increment()
        myWaitingQ.enqueue(QObject())
        if (myNumBusy.value < numATMs) {
            myNumBusy.increment()
            val nextCustomer: QObject? = myWaitingQ.removeNext()
            schedule(myEndServiceEvent, myServiceTime, nextCustomer)
        }
        schedule(myArrivalEvent, myArrivalRV)
    }

    private fun endOfService(event: KSLEvent<QObject>) {
        myNumBusy.decrement()
        if (!myWaitingQ.isEmpty) {
            val nextCustomer: QObject? = myWaitingQ.removeNext()
            myNumBusy.increment()
            schedule(myEndServiceEvent, myServiceTime, nextCustomer)
        }
        departSystem(event.message!!)
    }

    private fun departSystem(completed: QObject) {
        mySysTime.value = (time - completed.createTime)
        myNS.decrement()
    }
}
