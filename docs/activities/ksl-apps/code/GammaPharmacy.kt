package student.activities

import ksl.controls.ControlType
import ksl.controls.KSLControl
import ksl.modeling.elements.EventGenerator
import ksl.modeling.elements.EventGeneratorIfc
import ksl.modeling.elements.GeneratorActionIfc
import ksl.modeling.queue.Queue
import ksl.modeling.variable.Counter
import ksl.modeling.variable.IndicatorResponse
import ksl.modeling.variable.RandomVariable
import ksl.modeling.variable.RandomVariableCIfc
import ksl.modeling.variable.Response
import ksl.modeling.variable.ResponseCIfc
import ksl.modeling.variable.TWResponse
import ksl.modeling.variable.TWResponseCIfc
import ksl.simulation.ExperimentRunParametersIfc
import ksl.simulation.KSLEvent
import ksl.simulation.Model
import ksl.simulation.ModelBuilderIfc
import ksl.simulation.ModelElement
import ksl.utilities.random.rvariable.ExponentialRV

/**
 * The drive-through pharmacy of Chapter 4, copied so that its service time
 * distribution can be changed. As given, it is the book's model: exponential
 * service with a mean of 0.5 minute. The activity asks for two changes,
 * marked TODO 1 and TODO 2.
 */
class GammaPharmacy(
    parent: ModelElement,
    numServers: Int = 1,
    name: String? = null
) : ModelElement(parent, name = name) {

    @set:KSLControl(
        controlType = ControlType.INTEGER,
        lowerBound = 1.0
    )
    var numPharmacists = numServers
        set(value) {
            require(value > 0)
            require(!model.isRunning) { "Cannot change the number of pharmacists while the model is running!" }
            field = value
        }

    // TODO 1: Replace the exponential service time with a gamma distribution whose
    //  shape and scale match the mean and variance of the service time data, keeping
    //  stream 2: GammaRV(shape, scale, 2). Import ksl.utilities.random.rvariable.GammaRV.
    private val myServiceRV: RandomVariable = RandomVariable(
        this, rSource = ExponentialRV(0.5, 2), name = "ServiceTime"
    )
    val serviceRV: RandomVariableCIfc
        get() = myServiceRV

    private val myNumBusy: TWResponse = TWResponse(parent = this, name = "NumBusy")
    val numBusyPharmacists: TWResponseCIfc
        get() = myNumBusy

    private val myNS: TWResponse = TWResponse(parent = this, name = "Num in System")
    val numInSystem: TWResponseCIfc
        get() = myNS

    private val mySysTime: Response = Response(parent = this, name = "System Time")
    val systemTime: ResponseCIfc
        get() = mySysTime

    private val myNumCustomers: Counter = Counter(parent = this, name = "Num Served")

    private val myWaitingQ: Queue<QObject> = Queue(parent = this, name = "PharmacyQ")

    private val mySTGT4: IndicatorResponse = IndicatorResponse(
        predicate = { x -> x >= 4.0 },
        observedResponse = mySysTime,
        name = "SysTime >= 4 minutes"
    )

    // TODO 2: Give the arrival generator a name, so that the apps show its random
    //  variables under a readable name: add the argument name = "Arrivals".
    private val ad = ExponentialRV(1.0, 1)
    private val myArrivalGenerator: EventGenerator = EventGenerator(
        parent = this,
        generateAction = Arrivals(),
        timeUntilFirstRV = ad,
        timeBtwEventsRV = ad
    )

    private val endServiceEvent = this::endOfService

    private inner class Arrivals : GeneratorActionIfc {
        override fun generate(generator: EventGeneratorIfc) {
            myNS.increment() // a new customer arrived
            val arrivingCustomer = QObject()
            myWaitingQ.enqueue(arrivingCustomer)
            if (myNumBusy.value < numPharmacists) { // a pharmacist is free
                myNumBusy.increment()
                val customer: QObject? = myWaitingQ.removeNext()
                schedule(endServiceEvent, myServiceRV, customer)
            }
        }
    }

    private fun endOfService(event: KSLEvent<QObject>) {
        myNumBusy.decrement() // the departing customer frees a pharmacist
        if (!myWaitingQ.isEmpty) {
            val customer: QObject? = myWaitingQ.removeNext()
            myNumBusy.increment()
            schedule(endServiceEvent, myServiceRV, customer)
        }
        departSystem(event.message!!)
    }

    private fun departSystem(departingCustomer: QObject) {
        mySysTime.value = (time - departingCustomer.createTime)
        myNS.decrement()
        myNumCustomers.increment()
    }
}

/**
 * The builder is what makes the model loadable by the KSL applications: the
 * Bundle Workbench finds every public, no-argument ModelBuilderIfc in a JAR and
 * offers it as a model. Its model id is the class name without "ModelBuilder".
 */
class GammaPharmacyModelBuilder : ModelBuilderIfc {
    override fun build(
        modelConfiguration: Map<String, String>?,
        experimentRunParameters: ExperimentRunParametersIfc?
    ): Model {
        val model = Model("GammaPharmacy", autoCSVReports = false)
        GammaPharmacy(model, numServers = 1, name = "Pharmacy")
        model.numberOfReplications = 30
        model.lengthOfReplication = 20000.0
        model.lengthOfReplicationWarmUp = 5000.0
        return model
    }
}

fun main() {
    val model = GammaPharmacyModelBuilder().build()
    model.simulate()
    model.print()
}
