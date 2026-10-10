package student.activities

import ksl.modeling.entity.KSLProcess
import ksl.modeling.entity.ProcessModel
import ksl.modeling.entity.ResourceWithQ
import ksl.modeling.variable.Counter
import ksl.modeling.variable.IndicatorResponse
import ksl.modeling.variable.RandomVariable
import ksl.modeling.variable.Response
import ksl.simulation.Model
import ksl.simulation.ModelElement
import ksl.utilities.random.rvariable.BernoulliRV
import ksl.utilities.random.rvariable.ExponentialRV
import ksl.utilities.random.rvariable.TriangularRV
import ksl.utilities.random.rvariable.UniformRV

/**
 * A manufacturing system with preparation, two build lines, and packaging, in
 * the process view, with palletizing as an extension. The random variables are
 * given; the activity writes the parts marked TODO. As given, the file compiles
 * and runs, with no parts.
 */
fun main() {
    val m = Model("ProductionSystemModel")
    ProductionSystem(m, name = "ProductionSystem")
    m.numberOfReplications = 100
    m.lengthOfReplication = 30.0 * 24.0 * 60.0 // 30 days, in minutes
    m.simulate()
    m.print()
}

class ProductionSystem(
    parent: ModelElement,
    name: String? = null
) : ProcessModel(parent, name) {

    private val timeBetweenArrivalsRV = ExponentialRV(5.0, 1)
    private val prepTimeRV: RandomVariable = RandomVariable(this, ExponentialRV(8.0, 2))
    private val line1ChoiceRV: RandomVariable = RandomVariable(this, BernoulliRV(0.3, 3))
    private val build1TimeRV: RandomVariable = RandomVariable(this, TriangularRV(2.0, 6.0, 8.0, 4))
    private val build2TimeRV: RandomVariable = RandomVariable(this, TriangularRV(3.0, 6.0, 7.0, 5))
    private val wrapTimeRV: RandomVariable = RandomVariable(this, ExponentialRV(2.0, 6))
    private val fillTimeRV: RandomVariable = RandomVariable(this, UniformRV(1.0, 2.0, 7))

    // TODO 1: construct the four stations' workers as ResourceWithQ, with
    //  capacities 2 (preparation), 2 (line 1), 3 (line 2), and 2 (packaging).

    // TODO 2: construct the responses: the time in the system, an indicator of
    //  a time in the system under 60 minutes, and a counter of parts completed.

    // TODO 3: create the parts with an EntityGenerator, and write the Part
    //  entity and its process: preparation, line 1 or line 2, then packaging
    //  (wrap, then fill and seal, with the same worker); record the results.

    // TODO 4 (extension): count each part arriving at the palletizer; every 10th
    //  part starts a Pallet entity that uses the palletizer for 2 minutes and
    //  counts a completed pallet.
}
