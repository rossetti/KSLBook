package student.activities

import ksl.modeling.elements.REmpiricalList
import ksl.modeling.entity.KSLProcess
import ksl.modeling.entity.ProcessModel
import ksl.modeling.entity.ResourceWithQ
import ksl.modeling.variable.IndicatorResponse
import ksl.modeling.variable.RandomVariable
import ksl.modeling.variable.Response
import ksl.simulation.Model
import ksl.simulation.ModelElement
import ksl.utilities.random.rvariable.BernoulliRV
import ksl.utilities.random.rvariable.DEmpiricalRV
import ksl.utilities.random.rvariable.ExponentialRV
import ksl.utilities.random.rvariable.LognormalRV

/**
 * A small manufacturing system with three machines and rework, in the process
 * view. The random variables and the responses are given; the activity writes
 * the parts marked TODO. As given, the file compiles and runs, with no parts.
 */
fun main() {
    val m = Model("MfgSystemWithRework")
    MfgSystemWithRework(m, name = "MfgSystem")
    m.numberOfReplications = 20
    m.lengthOfReplication = 60.0 * 60.0 // 60 hours, in minutes
    m.simulate()
    m.print()
}

class MfgSystemWithRework(
    parent: ModelElement,
    name: String? = null
) : ProcessModel(parent, name) {

    private val timeBetweenArrivalsRV = ExponentialRV(10.0, 1)
    private val machineSelectionRV: RandomVariable = RandomVariable(
        this, DEmpiricalRV(doubleArrayOf(1.0, 2.0, 3.0), doubleArrayOf(0.2, 0.5, 1.0), 2)
    )
    private val processingTimeRV: RandomVariable = RandomVariable(this, LognormalRV(27.0, 11.0 * 11.0, 3))
    private val reworkRV: RandomVariable = RandomVariable(this, BernoulliRV(0.03, 4))

    private val mySysTime = Response(this, "System Time")
    private val mySysTimeGT225 = IndicatorResponse({ x -> x > 225.0 }, mySysTime, "System Time > 225")

    // TODO 1: construct the three machines, each a ResourceWithQ with one unit,
    //  named "Machine1", "Machine2", and "Machine3".

    // TODO 2: create the parts with an EntityGenerator that uses
    //  timeBetweenArrivalsRV for the first arrival and between arrivals.

    // TODO 3: write the Part entity and its process, marked as the process the
    //  generator starts with process(isDefaultProcess = true): choose a machine,
    //  process the part on it, test for rework, and if needed choose a machine
    //  again and process the part again; then record the time in the system.
}
