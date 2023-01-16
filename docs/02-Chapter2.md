# Modeling Randomness {#ch2rng}

**[Learning Objectives]{.smallcaps}**

- To be able to generate random numbers using the Kotlin Simulation Library (KSL)
-	To understand how to control random number streams within the KSL
- To be able to generate random variates using the KSL
-	To understand how to use the KSL for basic probability computations

This chapter overviews the functionality of the KSL for modeling randomness within simulation models.  The focus of this chapter is on getting started using the basic classes and functionality of the KSL.  The theory and methods related to random number generation and random variate generation are provided in Appendix \@ref(appRNRV).  In that appendix, the underlying theory of the inverse transform method, convolution, acceptance rejection, and particular distribution modeling concepts are reviewed. In addition, the concepts of pseudo-random number generation are discussed. This chapter assumes that the reader has some familiarity with the general concepts presented in Appendix \@ref(appRNRV).

## Random Number Generator {#ch2generator}

This section discusses how to random number generation is implemented
within the KSL. The purpose is to present how these concepts can be put into
practice. 

The random number generator used within the KSL is described in
@ecuyer2002an and has excellent statistical properties. It is based on the
combination of two multiple recursive generators resulting in a period
of approximately $3.1 \times 10^{57}$. This is the same generator that
is now used in many commercial simulation packages. The generator used in the KSL is 
defined by the following equations.

$$
\begin{aligned}
R_{1,i} & = (1,403,580 R_{1,i-2} - 810,728 R_{1,i-3})\bmod (2^{32}-209)\\
R_{2,i} & = (527,612R_{2,i-1} - 1,370,589 R_{2,i-3})\bmod (2^{32}-22,853)\\
Y_i & = (R_{1,i}-R_{2,i})\bmod(2^{32}-209)\\
U_i & = \frac{Y_i}{2^{32}-209}
\end{aligned}
$$

To illustrate how this generator works, consider generating an initial sequence of
pseudo-random numbers from the generator. The generator takes as its
initial seed a vector of six initial values
$(R_{1,0}, R_{1,1}, R_{1,2}, R_{2,0}, R_{2,1}, R_{2,2})$. The first
initially generated value, $U_{i}$, will start at index $3$. To produce five pseudo random numbers using this generator we need an initial seed vector, such as:
$$\lbrace R_{1,0}, R_{1,1}, R_{1,2}, R_{2,0}, R_{2,1}, R_{2,2} \rbrace = \lbrace 12345, 12345, 12345, 12345, 12345, 12345\rbrace$$

Using the recursive equations, the resulting random numbers are as follows:

                       i=3              i=4              i=5              i=6             i=7           
  -------------- ---------------- ---------------- ---------------- --------------- ---------------- -- --
    $Z_{1,i-3}=$      12345            12345            12345         3023790853       3023790853       
    $Z_{1,i-2}=$      12345            12345          3023790853      3023790853       3385359573       
    $Z_{1,i-1}=$      12345          3023790853       3023790853      3385359573       1322208174       
    $Z_{2,i-3}=$      12345            12345            12345         2478282264       1655725443       
    $Z_{2,i-2}=$      12345            12345          2478282264      1655725443       2057415812       
    $Z_{2,i-1}=$      12345          2478282264       1655725443      2057415812       2070190165       
      $Z_{1,i}=$    3023790853       3023790853       3385359573      1322208174       2930192941       
      $Z_{2,i}=$    2478282264       1655725443       2057415812      2070190165       1978299747       
          $Y_i=$    545508589        1368065410       1327943761      3546985096       951893194        
          $U_i=$  0.127011122076   0.318527565471   0.309186015655   0.82584686312   0.221629915834     

While it is beyond the scope of this document to explore the theoretical
underpinnings of this generator, it is important to note that the generator allows multiple independent streams to be defined along with sub-streams.

The fantastic thing about this generator is the sheer size of the
period. Based on their analysis, @ecuyer2002an state that it will be
"approximately 219 years into the future before average desktop
computers will have the capability to exhaust the cycle of the
(generator) in a year of continuous computing." In addition to the
period length, the generator has an enormous number of streams,
approximately $1.8 \times 10^{19}$ with stream lengths of
$1.7 \times 10^{38}$ and sub-streams of length $7.6 \times 10^{22}$
numbering at $2.3 \times 10^{15}$ per stream. Clearly, with these
properties, you do not have to worry about overlapping random numbers
when performing simulation experiments. The generator was subjected to a
rigorous battery of statistical tests and is known to have excellent
statistical properties.

### Random Package {#ch2randompkg}

The concepts within L'Ecuyer et al. (2002) have been implemented within the `ksl.utilities.random.rng` package in the KSL. A key organizing principle for the `random` package is the use of interfaces. An interface allows classes to act like other classes. It is a mechanism by which a class can promise to have
certain behaviors (i.e. methods). The KSL utilizes interfaces to
separate random number generation concepts from stream control concepts.

<div class="figure">
<img src="./figures/RNStreamInterfaces.png" alt="Random Number Stream Interfaces"  />
<p class="caption">(\#fig:RNStreamInterfaces)Random Number Stream Interfaces</p>
</div>

Figure \@ref(fig:RNStreamInterfaces) shows the important interfaces within the
`ksl.utilities.random.rng` package. The `RandU01Ifc` defines the methods for
getting the next pseudo-random number and the previous pseudo-random
number via `randU01()` and `previousU.` The `randInt(i: Int, j: Int)` method can be used to generate
a random integer uniformly over the range from $i$ to $j$. 
The `GetAntitheticStreamIfc` and `RNStreamNewInstanceIfc` interfaces allow a new object
instance to be created from the stream.  In the case of the `GetAntitheticStreamIfc` interface
the created stream will produce antithetic variates from the stream. If $U$ is a pseudo-random number,
then $1-U$ is the antithetic variate of $U$.

The `RNStreamControlIfc` defines methods for controlling the underlying stream of pseudo-random numbers.

*   `resetStartStream()` - positions the random number stream at the
    beginning of its sequence This is the same location in the stream as
    assigned when the random number stream was created and
    initialized.
*   `resetStartSubstream()` - resets the position of the random number
    stream to the start of the current substream. If the random
    number stream has advanced into the substream, then this method
    resets to the beginning of the substream.
*   `advanceToNextSubStream()` - positions the random number stream at
    the beginning of its next substream. This method move through the
    current substream and positions the stream at the beginning of
    the next substream.
*   `antithetic` indicates to the stream to start producing antithetic variates. If the option is true, the
    stream should start producing antithetic variates with the next
    call to `randU01().` If the option is false, the stream should stop
    producing antithetic variates. 
    
The `StreamOptionIfc` defines methods for automating the control of the stream during simulation runs.
    
*   `advanceToNextSubStreamOption` - Indicates that the stream will be advance to the next substream for the beginning of the next simulation replication.
*   `resetStartStreamOption` - Indicates that the underlying stream will be reset to its starting point for the beginning of the next simulation replication.

The `RNStreamIfc` interface assumes that the underlying pseudo-random number generator can produce
multiple streams that can be further divided into substreams. The reset
methods allow the user to move within the streams. Classes that
implement the `RNStreamControlIfc` can manipulate the streams in a
well-defined manner. 

<div class="figure">
<img src="./figures/RNStreamProvider.png" alt="RNStreamProviderIfc Interface"  />
<p class="caption">(\#fig:RNStreamProvider)RNStreamProviderIfc Interface</p>
</div>

To create an concrete instance of a stream, we must have a random number stream provider. This
functionality is defined by the `RNStreamProviderIfc` interface and its concrete implementation, 
`RNStreamProvider.` Figure \@ref(fig:RNStreamProvider) illustrates the functionality available for
creating random number streams. This interface conceptualizes the creation of random number streams
as a process of making a sequence of streams numbered 1, 2, 3, ... 

A random number stream provider must define a default stream, which can be retrieved via the 
`defaultRNStream()` method. For the KSL, the default stream is the first
stream created and is labeled with the sequence number 1.  The sequence number of a stream
can be used to retrieve a particular stream from the provider.  The following methods
allow for creation and access to streams.

* `nextRNStream()` - returns the next random number stream associated with the provider. Each call
to `nextRNStream()` makes a new stream in the sequence of streams.
* `lastRNStreamNumber()` - returns the number of the stream that was last made. This indicates how
many streams have been made. If $0$ is returned, then no streams have been made by the provider.
* `rnStream(k : Int)` - returns the $k^{th}$ stream.  If $k$ is greater than `lastRNStreamNumber()` then `lastRNStreamNumber()` is advanced according to the additional number of streams by creating any intermediate streams. For example, if `lastRNStreamNumber()` = 10 and k = 15, then streams 11, 12, 13, 14, 15 are assumed provided and stream 15 is returned and `lastRNStreamNumber()` now equals 15.  If $k$ is less than or equal to `lastRNStreamNumber()`, then no new streams are created and  `lastRNStreamNumber()`stays at its current value and the $k^{th}$ stream is returned.
* `streamNumber(RNStreamIfc stream)` - returns the stream number of the instance of a stream.
* `advanceStreamMechanism(int n)` - advances the underlying stream mechanism by the specified number of streams, without actually creating the streams.  The value of `lastRNStreamNumber()` remains the same after advancing through 
the streams. In other words, this method should act as if `nextRNStream()` was not called but advance the underlying stream mechanism as if $n$ streams had been provided.
* `resetRNStreamSequence()` - Causes the random number stream provider to act as if has never created any streams. Thus, the next call to `nextRNStream()` will return the $1^{st}$ stream.

The random number stream provider also facilitates the control of all streams that have been created. This functionality is similar to how the position within an individual stream can be manipulated, except the provider performs the functionality on *all streams* that it has provided The following methods perform this function.

* `resetAllStreamsToStart()` - resets all created streams to the start of their stream.
* `resetAllStreamsToStartOfCurrentSubStream()` - resets all created streams to the start of their current sub-stream.
* `advanceAllStreamsToNextSubstream()` - advances all created streams to the start of their next sub-stream.
* `setAllStreamsAntitheticOption(option: Boolean)` - changes all created streams to have their antithetic option either off = false or on = true.

Many random number generators require the specification of a seed to start the generated sequence.  Even though the generator within the KSL use seeds, there really is no need to utilize the seeds because of the well defined methods for moving within the streams.  Now, let's illustrate how to create and manipulate streams.

### Creating and Using Streams {#ch2creatingStreams}
To create a random number stream, the user must utilize an instance of `RNStreamProvider.`  This process is illustrated in in the following code.  This code creates two instances of `RNStreamProvider` and gets the first stream from each instance.  The instances of `RNStreamProvider` use the exact same underlying default seeds. Thus, they produce *exactly the same* sequence of streams.

(ref:example1) Exhibit 1 Creating a Stream Provider
```kt 
fun main() {
    // make a provider for creating streams
    val p1 = RNStreamProvider()
    // get the first stream from the provider
    val p1s1 = p1.nextRNStream()
    // make another provider, the providers are identical
    val p2 = RNStreamProvider()
    // thus the first streams returned are identical
    val p2s1 = p2.nextRNStream()
    System.out.printf("%3s %15s %15s %n", "n", "p1s1", "p2s2")
    for (i in 1..10) {
        System.out.printf("%3d %15f %15f %n", i, p1s1.randU01(), p2s1.randU01())
    }
}
```
Thus, in the following code output, the randomly generated values are exactly the same for the two streams. 

```
  n            p1s1            p2s2 
  1        0.728510        0.728510 
  2        0.965587        0.965587 
  3        0.996184        0.996184 
  4        0.114988        0.114988 
  5        0.973145        0.973145 
```

There is is really very little need for the general programmer to create
a `RNStreamProvider` because the KSL supplies a default provider that can be used to provide a
virtually infinite number of streams. The need for directly accessing the functionality of `RNStreamProvider` is for very fine control of stream creation in such situations like running
code on different computers in parallel. While the providers produce the
same streams, you can force one provider to be different from another
provider by manipulating the seeds. In addition, the
provider can control all streams that it produces. So, unless you are
trying to do some advanced work that involves coordinating multiple streams, you should not need to create multiple instances of `RNStreamProvider.`

Because the most common use case is to just have a single provider of streams, the KSL facilitates this through the `KSLRandom` *object* The `KSLRandom` object has a wide range of methods to facilitate random variate generation.The most important methods include:

* `nextRNStream()` - calls the underlying default `RNStreamProvider` to create a new random number stream
* `rnStream(int k)` - returns the $k^{th}$ stream from the default `RNStreamProvider`
* `defaultRNStream()` - calls the underlying default `RNStreamProvider` for its default stream

In the following code example, these methods are used to create streams that are used to generate random numbers. 
The first line of the code uses the static method `defaultRNStream()` of `KSLRandom` to get the default stream and 
then generates three random numbers.  The stream is then advanced and three new random numbers are generated.  Then,
the stream is reset to its starting (initial seed) and it then repeats the original values.  Finally, the a new stream 
is created via `KSLRandom.nextRNStream()` and then used to generate new random numbers.  From a conceptual standpoint,
each stream contains an independent sequence of random numbers from any other stream (unless of course they are made from different providers). They are conceptually infinite and independent due to their enormous periods.

```kt
fun main() {
    val s1 = KSLRandom.defaultRNStream()
    println("Default stream is stream 1")
    println("Generate 3 random numbers")
    for (i in 1..3) {
        println("u = " + s1.randU01())
    }
    s1.advanceToNextSubStream()
    println("Advance to next sub-stream and get some more random numbers")
    for (i in 1..3) {
        println("u = " + s1.randU01())
    }
    println("Notice that they are different from the first 3.")
    s1.resetStartStream()
    println("Reset the stream to the beginning of its sequence")
    for (i in 1..3) {
        println("u = " + s1.randU01())
    }
    println("Notice that they are the same as the first 3.")
    println("Get another random number stream")
    val s2 = KSLRandom.nextRNStream()
    println("2nd stream")
    for (i in 1..3) {
        println("u = " + s2.randU01())
    }
    println("Notice that they are different from the first 3.")
}
```
The resulting output from this code is as follows. Again, the methods of the `RNStreamControlIfc` interface that permit movement within a stream are extremely useful for controlling the randomness associated with a simulation.

```
Default stream is stream 1
Generate 3 random numbers
u = 0.12701112204657714
u = 0.3185275653967945
u = 0.3091860155832701
Advance to next sub-stream and get some more random numbers
u = 0.07939898979733463
u = 0.4803395047575741
u = 0.8583222470551328
Notice that they are different from the first 3.
Reset the stream to the beginning of its sequence
u = 0.12701112204657714
u = 0.3185275653967945
u = 0.3091860155832701
Notice that they are the same as the first 3.
Get another random number stream
2nd stream
u = 0.7285097861965271
u = 0.9655872822837334
u = 0.9961841304801171
Notice that they are different from the first 3.
```
### Common Random Numbers {#ch2crn}

Common random numbers (CRN) is a Monte Carlo method that has different experiments utilize the same random numbers. CRN is a variance reduction technique that allows the experimenter to block out the effect of the random numbers used in the experiment.  To facilitate the use of common random numbers the KSL has the aforementioned stream control mechanism. One way to implement common random numbers is to use two instances of `RNStreamProvider` as was previously illustrated.  In that case, the two providers produce the same sequence of streams and thus those streams can be used on the different experiments.  An alternative method that does not require the use of two providers is to create a copy of the stream directly from the stream instance. The following code clones the stream instance. 

```kt
fun main() {
    // get the default stream
    val s = KSLRandom.defaultRNStream()
    // make a clone of the stream
    val clone = s.instance()
    System.out.printf("%3s %15s %15s %n", "n", "U", "U again")
    for (i in 1..3) {
        System.out.printf("%3d %15f %15f %n", i, s.randU01(), clone.randU01())
    }
}
```
Since the instances have the same underlying state, they produce the same random numbers. Please note that the cloned stream instance is not produced by the underlying `RNStreamProvider` and thus it is not part of the set of streams managed or controlled by the provider.

```
  n               U         U again 
  1        0.127011        0.127011 
  2        0.318528        0.318528 
  3        0.309186        0.309186 
```

An alternative method is to just use the `resetStartStream()` method of the stream to reset the stream to the desired location in its sequence and then reproduce the random numbers. This is illustrated in the following code.

```kt
fun main() {
    val s = KSLRandom.defaultRNStream()
    // generate regular
    System.out.printf("%3s %15s %n", "n", "U")
    for (i in 1..3) {
        val u = s.randU01()
        System.out.printf("%3d %15f %n", i, u)
    }
    // reset the stream and generate again
    s.resetStartStream()
    println()
    System.out.printf("%3s %15s %n", "n", "U again")
    for (i in 1..3) {
        val u = s.randU01()
        System.out.printf("%3d %15f %n", i, u)
    }
}
```
Notice that the generated numbers are the same. 

```  
  n               U 
  1        0.127011 
  2        0.318528 
  3        0.309186 

  n         U again 
  1        0.127011 
  2        0.318528 
  3        0.309186 
```
Thus, an experiment can be executed, then the random numbers reset to the desired location. Then, by changing the experimental conditions and re-running the simulation, the same random numbers are used. If many streams are used, then by accessing the `RNStreamProvider` you can reset all of the controlled streams with one call and then perform the next experiment.

### Creating and Using Antithetic Streams {#ch2antitheticStreams}

Recall that if a pseudo-random number is called $U$ then its antithetic value is $1-U$.  There are a number of methods to access antithetic values. The simplest is to create an antithetic instance from a given stream.  This is illustrated is in the following code. Please note that the antithetic stream instance is not produced by the underlying `RNStreamProvider` and thus it is not part of the set of streams managed or controlled by the provider. The new instance process directly creates the new stream based on the current stream so that it has the same underling state and it is set to produce antithetic values.

```kt
fun main() {
    // get the default stream
    val s = KSLRandom.defaultRNStream()
    // make its antithetic version
    val ans = s.antitheticInstance()
    System.out.printf("%3s %15s %15s %15s %n", "n", "U", "1-U", "sum")
    for (i in 1..5) {
        val u = s.randU01()
        val au = ans.randU01()
        System.out.printf("%3d %15f %15f %15f %n", i, u, au, u + au)
    }
}
```
Notice that the generated values sum to 1.0.
```
  n               U             1-U             sum 
  1        0.127011        0.872989        1.000000 
  2        0.318528        0.681472        1.000000 
  3        0.309186        0.690814        1.000000 
  4        0.825847        0.174153        1.000000 
  5        0.221630        0.778370        1.000000 
```
An alternate method that does not require the creation of another stream involves using the `resetStartStream()` method and the `antithetic` property of the current stream. If you have a stream, you can use the `antithetic` property to cause the stream to start producing antithetic values. If you use the `resetStartStream()` method and then set the antithetic option to true, the stream will be set to its initial starting point and then produce antithetic values.

```kt
fun main() {
    val s = KSLRandom.defaultRNStream()
    s.resetStartStream()
    // generate regular
    System.out.printf("%3s %15s %n", "n", "U")
    for (i in 1..5) {
        val u = s.randU01()
        System.out.printf("%3d %15f %n", i, u)
    }
    // generate antithetic
    s.resetStartStream()
    s.antithetic = true
    println()
    System.out.printf("%3s %15s %n", "n", "1-U")
    for (i in 1..5) {
        val u = s.randU01()
        System.out.printf("%3d %15f %n", i, u)
    }
}
```
Notice that the second set of random numbers is the antithetic complement of the first set in this output. Of course, you can also create multiple instances of `RNStreamProvider,` and then create streams and set one of the streams to produce antithetic values.
```
  n               U 
  1        0.127011 
  2        0.318528 
  3        0.309186 
  4        0.825847 
  5        0.221630
  
  n             1-U 
  1        0.872989 
  2        0.681472 
  3        0.690814 
  4        0.174153 
  5        0.778370
```

### Frequently Asked Questions about Random Numbers {#ch2rnFAQ}

1. **What are pseudo-random numbers?**
Numbers generated through an algorithm that appear to be random, when in fact, they are created by a deterministic process.

2. **Why do we want to control randomness within simulation models?**
By controlling randomness, we can better understand if changes in simulation responses are due to factors of interest or due to underlying statistical variation caused by sampling.  Do you think that it is better to compare two systems using the same inputs or different inputs?  Suppose we have a work process that we have redesigned.  We have the old process and the new process.  Would it be better to test the difference in the process by using two different workers or the same worker? Most people agree that using the same worker is better. This same logic applies to randomness. Since we can control which pseudo-random numbers we use, it is better to test the difference between two model alternatives by using the same pseudo-random numbers.  We use seeds and streams to do this.

3. **What are seeds and streams?**
A random number stream is a sub-sequence of pseudo-random numbers that start at particular place within a larger sequence of pseudo-random numbers. The starting point of a sequence of pseudo-random numbers is called the seed.  A seed allows us to pick a particular stream.  Having multiple streams is useful to assign different streams to different sources of randomness within a model.  This facilitates the control of the use of pseudo-random numbers when performing experiments.

4. **How come my simulation results are always the same?**
Random number generators in computer simulation languages come with a default set of streams that divide the “circle” up into independent sets of random numbers. The streams are only independent if you do not use up all the random numbers within the subsequence. These streams allow the randomness associated with a simulation to be controlled. During the simulation, you can associate a specific stream with specific random processes in the model. This has the advantage of allowing you to check if the random numbers are causing significant differences in the outputs. In addition, this allows the random numbers used across alternative simulations to be better synchronized.
Now a common question in simulation can be answered. That is, “If the simulation is using random numbers, why to I get the same results each time I run my program?” The corollary to this question is, “If I want to get different random results each time I run my program, how do I do it?” The answer to the first question is that the underlying random number generator is starting with the same seed each time you run your program. Thus, your program will use the same pseudo random numbers today as it did yesterday and the day before, etc. The answer to the corollary question is that you must tell the random number generator to use a different seed (or alternatively a different stream) if you want different invocations of the program to produce different results. The latter is not necessarily a desirable goal. For example, when developing your simulation programs, it is desirable to have repeatable results so that you can know that your program is working correctly.

5. **How come my simulation results are unexpectedly different?**
Sometimes by changing the order of method calls you change the sequence of random numbers that are assigned to various things that happen in the model (e.g. attribute, generated service times, paths taken, etc.). Please see the FAQ "How come my results are always the same?". Now, the result can sometimes be radically different if different random numbers are used for different purposes. By using streams, you reduce this possibility and increase the likelihood that two models that have different configurations will have differences due to the change and not due to the random numbers used.

## Random Variate Generation {#rvg}

The KSL has the capability to generate random variates from both
discrete and continuous distributions. The `ksl.utilities.random.rvariable` package supports this functionality. The package has a set of interfaces
that define the behavior associated with random variables. Concrete
sub-classes of specific random variables are created by sub-classing
`RVariable.` As shown in Figure \@ref(fig:RVariableIfc), every random variable has
access to an object that implements the `RNStreamIfc` interface. This
gives it the ability to generate pseudo-random numbers and to control
the streams. The `GetValueIfc` interface is the key interface because in
this context it returns a random value from the random variable. For
example, if `d` is a reference to an instance of a sub-class of type
`RVariable`, then `d.value` generates a random value.

<div class="figure">
<img src="./figures/RVariableIfc.png" alt="Random Variable Interfaces"  />
<p class="caption">(\#fig:RVariableIfc)Random Variable Interfaces</p>
</div>

### Continuous and Discrete Random Variables {#rvg_dists}

The names and parameters (based on common naming conventions) associated with the continuous random variables are as follows:

-   `BetaRV(alpha1, alpha2)`
-   `ChiSquaredRV(degreesOfFreedom)`
-   `ExponentialRV(mean)`
-   `GammaRV(shape, scale)`
-   `GeneralizedBetaRV(alpha1, alpha2, min, max)`
-   `JohnsonBRV(alpha1, alpha2, min, max)`
-   `LaplaceRV(mean scale)`
-   `LogLogisticRV(shape, scale)`
-   `LognormalRV(mean, variance)`
-   `NormalRV(mean, variance)`
-   `PearsonType5RV(shape, scale)`
-   `PearsonType6RV(alpha1, alpha2, beta)`
-   `StudentTRV(degreesOfFreedom)`
-   `TriangularRV(min, mode, max)`
-   `UniformRV(min, max)`
-   `WeibullRV(shape, scale)`

The names of the discrete random variables are as follows:

-   `BernoulliRV(probOfSuccess)`
-   `BinomialRV(pSucces, numTrials)`
-   `ConstantRV(constVal)` a degenerate probability mass on a single value that cannot be changed
-   `DEmpiricalRV(values, cdf)` values is an array of numbers and cdf is an array representing the cumulative distribution function over the values
-   `DUniformRV(min, max)`
-   `GeometricRV(probOfSucces)` with range is 0 to infinity
-   `NegativeBinomialRV(probOfSuccess, numSuccess)` with range is 0 to infinity. The number of failures before the $r^{th}$ success.
-   `PoissonRV(mean)`
-   `ShiftedGeometricRV(probOfSucces)` range is 1 to infinity
-   `VConstantRV(constVal)` a degenerate probability mass on a single value that can be changed

All classes that represent random variables also have optional parameters to provide a stream and a name. If the stream is not provided, then the next stream from the default provider is allocated to the new instance of the random variable.  Thus, all random variables are automatically constructed such that they use different underlying streams, unless the programming specifically assigns streams.  The following sections will overview the generation algorithms and provide examples for using some of these distributions.

### Overview of Generation Algorithms {#rvg_algo}

As you can see, the name of the distribution followed by the letters RV designate the class names.  Implementations of these classes extend the `RVarable` class, which implements the `RVariableIfc` interface.  Users simply create and instance of the class and then use it to get a sequence of values that have the named probability distribution. In order to implement a new random variable (i.e. some random variable
that is not already implemented) you can extend the class
`RVariable.` This provides a basic template for what is expected
in the implementation. However, it implies that you need to implement
all of the required interfaces. The key method to implement is the
protected `generate()` method, which should return the generated random
value according to some algorithm.

In almost all cases, the KSL utilizes the inverse transform algorithm for generating random variates. Thus, there is a one to one mapping of the underlying pseudo-random number and the resulting random variate. Even in the case of distributions that do not have closed form inverse cumulative distribution functions, the KSL utilizes numerical methods to approximate the function whenever feasible. For example, the KSL uses a rational function approximation, see Cody (1969), to
implement the inverse cumulative distribution function for the standard
normal distribution. The inversion for the gamma distribution is based
on Algorithm AS 91 for inverting the chi-squared distribution and
exploiting its relationship with the gamma. The beta distribution also
uses numerical methods to compute the cumulative distribution function
as well as bi-section search to determine the inverse for cumulative
distribution function.

The KSL implements the `BernoulliRV,` `DUniformRV,` `GeometricRV,`
`NegativeBinomialRV,` and `ShiftedGeometricRV` classes using the methods
described in Chapter 2 of @Rossetti2015. While more efficient methods may be available, the
`PoissonRV` and `BinomialRV` distributions are implemented by searching the
probability mass functions. Both search methods use an approximation to
get close to the value of the inverse and then search up or down through
the cumulative distribution function. Because of this both distributions
use numerically stable methods to compute the cumulative distribution
function values. The `DEmpiricalRV` class also searches through the
cumulative distribution function.

### Creating and Using Random Variables {#rvg_use}

The following example code illustrates how to create a normal random variable and how to generate values.

```kt
fun main() {
    // create a normal mean = 20.0, variance = 4.0 random variable
    val n = NormalRV(20.0, 4.0)
    System.out.printf("%3s %15s %n", "n", "Values")
    // generate some values
    for (i in 1..5) {
        // value property returns generated values
        val x = n.value
        System.out.printf("%3d %15f %n", i, x)
    }
}
```
The resulting output is what you would expect.

```
  n          Values 
  1       21.216624 
  2       23.639128 
  3       25.335884 
  4       17.599163 
  5       23.858350 
```
Alternatively, the user can use the `sample()` method to generate an array of values that can be later processed. The following code illustrates how to do that with a triangular distribution.

```kt
fun main() {
    // create a triangular random variable with min = 2.0, mode = 5.0, max = 10.0
    val t = TriangularRV(2.0, 5.0, 10.0)
    // sample 5 values
    val sample = t.sample(5)
    System.out.printf("%3s %15s %n", "n", "Values")
    for (i in sample.indices) {
        System.out.printf("%3d %15f %n", i + 1, sample[i])
    }
}
```
Again, the output is what we would expect.

```
  n          Values 
  1        3.515540 
  2        6.327783 
  3        4.382075 
  4        7.392228 
  5        8.409238 
```
It is important to note that the full range of functionality related to stream control is also available for random variables.  That is, the underlying stream can be reset to its start, can be advanced to the next substream, can generate antithetic variates, etc.  Each new instance of a random variable is supplied with its own *unique* stream that is not shared with another other random variable instances.  Since the underlying random number generator has an enormous number of streams, approximately $1.8 \times 10^{19}$, it is very unlikely that the user will not create so many streams as to start reusing them.  However, the streams that are used by random variable instances can be supplied directly so that they may be shared.  The following code example illustrates how to assign a specific stream by passing a specific stream instance into the constructor of the random variable.

```kt
fun main() {
    // get stream 3
    val stream = KSLRandom.rnStream(3)
    // create a normal mean = 20.0, variance = 4.0, with the stream
    val n = NormalRV(20.0, 4.0, stream)
    System.out.printf("%3s %15s %n", "n", "Values")
    for (i in 1..5) {
        // value property returns generated values
        val x = n.value
        System.out.printf("%3d %15f %n", i, x)
    }
}
```
As a final example, the discrete empirical distribution requires a little more setup. The user must supply the set of values that can be generated as well as an array holding the cumulative distribution probability across the values. The following code illustrates how to do this.

```kt
fun main() {
    // values is the set of possible values
    val values = doubleArrayOf(1.0, 2.0, 3.0, 4.0)
    // cdf is the cumulative distribution function over the values
    val cdf = doubleArrayOf(1.0 / 6.0, 3.0 / 6.0, 5.0 / 6.0, 1.0)
    //create a discrete empirical random variable
    val n1 = DEmpiricalRV(values, cdf)
    println(n1)
    System.out.printf("%3s %15s %n", "n", "Values")
    for (i in 1..5) {
        System.out.printf("%3d %15f %n", i, n1.value)
    }
}
```

While the preferred method for generating random values from random
variables is to create instance of the appropriate random variable
class, the KSL also provide a set of functions for generating random
values within the `KSLRandom` object. For all the previously listed random variables, there is a 
corresponding function that will generate a random value.  For
example, the method `rNormal()` will generate a normally distributed
value. Each method is named with an \"r\" in front of the distribution
name. By using an import of `KSLRandom` functions the user can more conveniently call these methods. The following code example illustrates how to do this.

```kt
fun main() {
    val v = rUniform(10.0, 15.0) // generate a U(10, 15) value
    val x = rNormal(5.0, 2.0) // generate a Normal(mu=5.0, var= 2.0) value
    val n = rPoisson(4.0).toDouble() //generate from a Poisson(mu=4.0) value
    System.out.printf("v = %f, x = %f, n = %f %n", v, x, n)
}
```

In addition to random values through these functions, the
`KSLRandom` object provides a set of methods for randomly selecting from
arrays and lists and for creating permutations of arrays and lists. In
addition, there is a set of methods for sampling from arrays and lists
without replacement. The following code provide examples of using these methods.

```kt
fun main() {
    // create a list
    val strings = listOf("A", "B", "C", "D")
    // randomly pick from the list, with equal probability
    for (i in 1..5) {
        println(KSLRandom.randomlySelect(strings))
    }
    println()
    for (i in 1..5) {
        println(strings.randomlySelect())
    }
}
```
There are also extension functions declared on arrays for directly performing this form of random selection.  This next example illustrates how to define a population of values (`DPopulation`) and use it to perform sampling operations such as random samples and permutations.  Similar functionality is also demonstrated by directly using the functions of the `KSLRandom` object

```kt
fun main() {
    // create an array to hold a population of values
    val y = DoubleArray(10)
    for (i in 0..9) {
        y[i] = (i + 1).toDouble()
    }

    // create the population
    val p = DPopulation(y)
    println(p.contentToString())

    println("Print the permuted population")
    // permute the population
    p.permute()
    println(p.contentToString())

    // directly permute the array using KSLRandom
    println("Permuting y")
    KSLRandom.permute(y)
    println(y.contentToString())

    // sample from the population
    val x = p.sample(5)
    println("Sampling 5 from the population")
    println(x.contentToString())

    // create a string list and permute it
    val strList: MutableList<String> = ArrayList()
    strList.add("a")
    strList.add("b")
    strList.add("c")
    strList.add("d")
    println("The mutable list")
    println(strList)
    KSLRandom.permute(strList)
    println("The permuted list")
    println(strList)
    println("Permute using extension function")
    strList.permute()
    println(strList)
}
```
### Functions of Random Variables

The KSL also contains an algebra for working with random variables.  A well-known property of random variables is that a function of a random variable is also a random variable. That is, let $f(\cdot)$ be an arbitrary function and let $X$ be a random variable. Then, the quantity $Y = f(X)$ is also a random variable. Various properties of $Y$ such as expectation, $E[Y]$ and $Var[Y]$ may be of interest.  A classic example of this is the relationship between the normal random variable and the lognormal random variable.  If $X \sim N(\mu, \sigma^2)$ then the random variable $Y=e^X$ will be lognormally distributed $LN(\mu_l,\sigma_{l}^{2})$, where

\begin{equation}
E[Y] = \mu_l = e^{\mu + \sigma^{2}/2}
\end{equation}

\begin{equation}
Var[Y] = \sigma_{l}^{2}  = e^{2\mu + \sigma^{2}}\left(e^{\sigma^{2}} - 1\right)
\end{equation}

Thus, one can define new random variables simply as functions of other random variables.

The interface `RVariableIfc` and base class `RVariable` provides the ability to construct new random variables that are functions of other random variables by overriding the $(+, -, \times, \div)$ operators and providing extension functions for various math functions.  For example, we can defined two random variables and then a third that is the sum of the first two random variables. The random variable that is defined as the sum will generate random variates that represent the sum. Functions, such as `sin(),` `cos()` as well as many other standard math functions can be applied to random variables to create new random variables.  That is, the KSL provides the ability to create arbitrarily complex random variables that are defined as *functions* of other random variables. This capability will be illustrated in this section with a couple of examples.

***
::: {.example #Erlang}
Suppose we have $k$ independent random variables, $X_i$ each exponentially distributed with mean $\theta$. Then, the random variable:
\begin{equation} 
Y = \sum_{i=i}^{k}X_i 
\end{equation}
will be an Erlang$(k, \theta)$ where $k$ is the shape parameter and $\theta$ is the scale parameter.  Set up a KSL model to generate 1000 Erlang random variables with $k = 5$ and $\theta = 10$.
:::

***

A simple solution to this problem is to use the KSL to define a new random variable that is the sum of 5 exponential random variables.

```kt
fun main(){
    var erlang: RVariableIfc = ExponentialRV(10.0)
    for(i in 1..4) {
        erlang = erlang + ExponentialRV(10.0)
    }
    val sample = erlang.sample(1000)
    val stats = Statistic(sample)
    print(stats)
    sample.writeToFile("erlang.txt")
}
```
The first line of this code creates and stores an instance of an exponential random variable with mean 10.  The for loop is **not** generating any random variates.  It is defining a new random variable that is the sum of 4 additional exponential random variables. The defined random variable is used to generate a sample of size 1000 and using the `Statistic` class (discussed in the next chapter) a basic statistical summary is computed. Also, using the `writeToFile` KSL extension function for double arrays, the sample is written to a file.  The results as a histogram are also presented. The statistical results are as follows.

```
ID 30
Name Statistic_1
Number 1000.0
Average 51.11021981152218
Standard Deviation 23.62319194179008
Standard Error 0.7470309213939245
Half-width 1.4659295758838757
Confidence Level 0.95
Confidence Interval [49.6442902356383, 52.576149387406055]
Minimum 5.03168557560841
Maximum 177.85302754920727
Sum 51110.219811522176
Variance 558.0551975186557
Deviation Sum of Squares 557497.1423211371
Last value collected 58.568877001859285
Kurtosis 1.5267730984137933
Skewness 0.9637663719643743
Lag 1 Covariance -13.659309232961249
Lag 1 Correlation -0.024501128698329766
Von Neumann Lag 1 Test Statistic -0.7301901389345133
Number of missing observations 0.0
Lead-Digit Rule(1) -1
```

<div class="figure">
<img src="02-Chapter2_files/figure-html/ErlangHist-1.svg" alt="Histogram for Erlang Generated Data" width="672" />
<p class="caption">(\#fig:ErlangHist)Histogram for Erlang Generated Data</p>
</div>

Notice that the histogram looks like an Erlang distribution and the estimated results are what we would expect for an Erlang distribution with $k=5$ and $\theta = 10$. 

To illustrate a couple of other examples consider the following code. In this code, the previously noted relationship between normal random variables and lognormal random variables is demonstrated in the first 6 lines of the code.

```kt
fun main(){
    // define a lognormal random variable, y
    val x = NormalRV(2.0, 5.0)
    val y = exp(x)
    // generate from y
    val ySample = y.sample(1000)
    println(ySample.statistics())
    // define a beta random variable in terms of gamma
    val alpha1 = 2.0
    val alpha2 = 5.0
    val y1 = GammaRV(alpha1, 1.0)
    val y2 = GammaRV(alpha2, 1.0)
    val betaRV = y1/(y1+y2)
    val betaSample = betaRV.sample(500)
    println(betaSample.statistics())
}
```

One method for generating Beta random variables exploits its relationship with the Gamma distribution. If $Y_1 \sim Gamma(\alpha_1, 1)$ and $Y_2 \sim Gamma(\alpha_2, 1)$, then $X = Y_1/(Y_1 + Y_2)$ has a Beta($\alpha_1, \alpha_2$) distribution. In the previous KSL code, we defined two gamma random variables and define the beta random variable using the algebraic expression. This code *defines* and *constructs* a new random variable that is function of the previously define random variables. Through this pattern you can define complex random variables and use those random variables anywhere a random variable is needed.

## Probability Distribution Models

The `ksl.utilities.random.rvariable` package is the key package for generating random variables; however, it does not facilitate performing calculations involving the underlying probability distributions. To perform calculations involving probability distributions, you should use the `ksl.utilities.distributions` package.  This package has almost all the same distributions represented within the `ksl.utilities.random.rvariable` package.  

<div class="figure">
<img src="./figures/Distributions.png" alt="Distribution Interfaces"  />
<p class="caption">(\#fig:DistPackage)Distribution Interfaces</p>
</div>

Figure\@ref(fig:DistPackage) illustrates the interfaces used to define probability distributions. First, the interface, `CDFIfc` serves as the basis for discrete distributions via the `DiscreteDistributionIfc` interface, for continuous distributions via the `ContinuousDistributionIfc` interface and the general `DistributionIfc` interface. The discrete distributions such as the geometric, binomial, etc. implement the `DiscreteDistributionIfc` and `PMFIfc` interfaces. Similarly, continuous distributions like the normal, uniform, etc. implement the `ContinuousDistributionIfc` and `PDFIfc` interfaces.  All concrete implementations of distributions extend from the abstract base class `Distribution`, which implements the `DistributionIfc` interface.  Thus, all distributions have the following capabilities:

* `cdf(b: Double)` - computes the cumulative probability, $F(b) = P(X \leq b)$
* `cdf(a: Double, b: Double)` - computes the cumulative probability, $P( a \leq X \leq b)$
* `complementaryCDF(b: Double)` - computes the cumulative probability, $1-F(b) = P(X > b)$
* `mean()` - returns the expected value (mean) of the distribution
* `variance()` - returns the variance of the distribution
* `standardDeviation()` - returns the standard deviation of the distribution
* `invCDF(p: Double)` - returns the inverse of the cumulative distribution function $F^{-1}(p)$. This is performed by numerical search if necessary

Discrete distributions have a method called `pmf(k: Double)` that returns the probability associated with the value $k$.  Continuous distributions have a probability density function, $f(x)$, implemented in the method, `pdf(x : Double)`.  Finally, all distributions know how to create random variables through the `GetRVariableIfc` interface that provides the following methods.

* `RVariableIfc randomVariable(stream: RNStreamIfc)` - returns a new instance of a random variable based on the current values of the distribution's parameters that uses the supplied stream
* `RVariableIfc randomVariable(streamNum: Int)` - returns a new instance of a random variable based on the current values of the distribution's parameters that uses the supplied stream number
* `RVariableIfc randomVariable()` - returns a new instance of a random variable based on the current values of the distribution's parameters that uses a newly created stream

As an example, the following code illustrates some calculations for the binomial distribution.

```kt
fun main() {
    // make and use a Binomial(p, n) distribution
    val n = 10
    val p = 0.8
    println("n = $n")
    println("p = $p")
    val bnDF = Binomial(p, n)
    println("mean = " + bnDF.mean())
    println("variance = " + bnDF.variance())
    // compute some values
    System.out.printf("%3s %15s %15s %n", "k", "p(k)", "cdf(k)")
    for (i in 0..10) {
        System.out.printf("%3d %15.10f %15.10f %n", i, bnDF.pmf(i), bnDF.cdf(i))
    }
    println()
    // change the probability and number of trials
    bnDF.probOfSuccess = 0.5
    bnDF.numTrials = 20
    println("mean = " + bnDF.mean())
    println("variance = " + bnDF.variance())
    // make random variables based on the distributions
    val brv = bnDF.randomVariable
    System.out.printf("%3s %15s %n", "n", "Values")
    // generate some values
    for (i in 1..5) {
        // value property returns generated values
        val x = brv.value.toInt()
        System.out.printf("%3d %15d %n", i, x)
    }
}
```
The output shows the mean, variance, and basic probability calculations.

```
n = 10
p = 0.8
mean = 8.0
variance = 1.5999999999999996
  k            p(k)          cdf(k) 
  0    0.0000001024    0.0000001024 
  1    0.0000040960    0.0000041984 
  2    0.0000737280    0.0000779264 
  3    0.0007864320    0.0008643584 
  4    0.0055050240    0.0063693824 
  5    0.0264241152    0.0327934976 
  6    0.0880803840    0.1208738816 
  7    0.2013265920    0.3222004736 
  8    0.3019898880    0.6241903616 
  9    0.2684354560    0.8926258176 
 10    0.1073741824    1.0000000000 
```
The `ksl.utilities.random.rvariable` package creates instances of random variables that are immutable. That is, once you create a random variable, its parameters cannot be changed.  However, distributions permit their parameters to be changed and they also facilitate the creation of random variables.  The previous example code uses the properties `probOfSuccess` and `numTrials` to change the parameters of the previously created binomial distribution and then creates a random variable based on the mutated distribution.

```
mean = 10.0
variance = 5.0
  n          Values 
  1              11 
  2              14 
  3              16 
  4               7 
  5              14 
```

The results are as we would expect. Similar calculations can be made for continuous distributions. In most cases, the concrete implementations of the various distributions have specialize methods beyond those generic methods described here. Please refer to the documentation for further details.

There are a number of useful companion object methods defined for the binomial, normal, gamma, and Student-T distributions. Specifically, for the binomial distribution, has the following methods

* `binomialPMF(j: Int, n: Int, p: Double)` - directly computes the probability for the value $j$
* `binomialCDF(j: Int, n: Int, p: Double)` - directly computes the cumulative distribution function for the value $j$
* `binomialCCDF(j: Int, n: Int, p: Double)`- directly computes the complementary cumulative distribution function for the value of $j$
* `binomialInvCDF(x: Double, n: Int, p: Double)` - directly computes the inverse cumulative distribution function

These methods are designed to perform their calculations in a numerically stable manner to ensure numerical accuracy. The normal distribution has the following companion object methods for computations involving the standard normal distribution.

* `stdNormalCDF(z: Double)` - the cumulative probability for a $Z ~ N(0,1)$ random variable, i.e. $F(z) = P(Z \leq z)$
* `stdNormalComplementaryCDF(z: Double)` - returns $1-P(Z \leq z)$
* `stdNormalInvCDF(p: Double)` - returns $z = F^{-1}(p)$ the inverse of the cumulative distribution function

The Student-T distribution also has two convenience methods to facilitate computations.

* `cdf(dof: Double, x: Double)` - computes the cumulative distribution function for $x$ given the degrees of freedom
* `invCDF(dof: Double, p: Double)` - computes the inverse cumulative distribution function or t-value for the supplied probability given the degrees of freedom.

Within the `Gamma` class's companion object there are some convenience methods for computing the gamma function, the natural logarithm of the gamma function, the incomplete gamma function, and the digamma function (derivative of the natural logarithm of the gamma function).

## Summary

The KSL contains packages that support the generation of random numbers, random variates, and the modeling of probability distributions that are commonly found in practice.  These constructs facilitate the incorporation of randomness within simulation modeling.  The following classes may be of interest:

* `ShiftedRV` - models random variables that have their domain shifted to the right
* `MixtureRV` - models random variables that are expressed as a mixture distribution.  That is, a distribution that is a weighted mixture of other distributions
* `AcceptanceRejectionRV` - permits the implementation of the acceptance and rejection algorithm for generating random variates in a general manner
* `InverseCDFRV` - facilitates the implementation of the inverse transform method via bisection search of the CDF
* `RatioOfUniformsRV` - facilitates the implementation of the ratio of uniforms method for generating random variates

In addition, the KSL has additional utilities that assist the modeler with common aspects of working with arrays and generating arrays of data.  That is, the generation of multi-variate random vectors of data. The following classes may be of interest for situations involving multi-variate distributions:

* `RArrays` - defines extension functions for randomly sampling from arrays and some lists
* `MVSampleIfc` - defines the interface for generating random arrays of data
* `MVRVariableIfc` - the multi-variate analog for the `RVariableIfc`
* `MVRVariable` - an abstract base class for defining multi-variate random variables
* `MVIndependentMarginals` - a concrete implementation for generating independent vectors that have specified random variates for each coordinate of the vector.

Some of these more advanced capabilities will be illustrated in future chapters.

## Exercises

***

::: {.exercise #Ch2Ex1}
Consider the following discrete distribution of the random variable $X$ whose
probability mass function is $p(x)$.
:::

    $x$      0     1     2     3     4
  -------- ----- ----- ----- ----- -----
   $p(x)$   0.3   0.2   0.2   0.1   0.2

Write a KSL program to generate 4 random variates from this distribution using stream 1.

***

::: {.exercise #Ch2Ex2}
Suppose that customers arrive at an ATM via a Poisson process with mean 7 per hour.
Write a KSL program that outputs the arrival time of the first 6 customers using stream 1.
:::

***

::: {.exercise #Ch2Ex3}
The service times for a automated storage and retrieval system has a shifted
exponential distribution. It is known that it takes a minimum of 15
seconds for any retrieval. The parameter of the exponential distribution
is $\lambda = 45$. Write a KSL program to generate 10 observations from this distribution using stream 1.
:::

***

::: {.exercise #Ch2Ex4}
The time to failure for a computer
printer fan has a Weibull distribution with shape parameter $\alpha = 2$
and scale parameter $\beta = 3$. Testing has indicated that the
distribution is limited to the range from 1.5 to 4.5. Write a KSL program to generate 10 observations from this distribution using stream 1.
:::

***

::: {.exercise #Ch2Ex5}
The interest rate for a capital project is unknown. An accountant has
estimated that the minimum interest rate will between 2% and 5% within
the next year. The accountant believes that any interest rate in this
range is equally likely. You are tasked with generating interest rates
for a cash flow analysis of the project. Write a KSL program to generate 10 observations from this distribution using stream 1.
:::

***

::: {.exercise #Ch2Ex6}
Consider the following probability density function:

$$f(x) = 
  \begin{cases}
     \dfrac{3x^2}{2} & -1 \leq x \leq 1\\
     0 & \text{otherwise} \\
  \end{cases}$$
  
Write a KSL program to generate 10 observations from this distribution using stream 1 using the inverse transform technique.  
:::

***

::: {.exercise #Ch2Ex7}
Consider the following probability density function:

$$f(x) = 
  \begin{cases}
     0.5x - 1 & 2 \leq x \leq 4\\
     0 & \text{otherwise} \\
  \end{cases}$$
       
Write a KSL program to generate 10 observations from this distribution using stream 1 using the inverse transform technique.
:::

***

::: {.exercise #Ch2Ex8}
Consider the following probability density function:

$$f(x) = 
  \begin{cases}
     \dfrac{2x}{25} & 0 \leq x \leq 5\\
     0 & \text{otherwise} \\
  \end{cases}$$

Write a KSL program to generate 10 observations from this distribution using stream 1 using the inverse transform technique.
:::

***

::: {.exercise #Ch2Ex9}
Consider the following probability density function:

$$f(x) = 
  \begin{cases}
     \dfrac{2}{x^3} & x > 1\\
     0 & x \leq 1\\
  \end{cases}$$

Write a KSL program to generate 10 observations from this distribution using stream 1 using the inverse transform technique.
:::

***

::: {.exercise #Ch2Ex10}
The times to failure for an automated production process have been found to be
randomly distributed according to a Rayleigh distribution:

$$\ f(x) = 
   \begin{cases}
     2 \beta^{-2} x e^{(-(x/\beta)^2)} & x > 0\\
     0 & \text{otherwise}
  \end{cases}$$

Write a KSL program to generate 10 observations from this distribution using stream 1 using the inverse transform technique.
:::

***

::: {.exercise #Ch2Ex11}
Suppose that the processing time for a job consists of two distributions. There
is a 30% chance that the processing time is lognormally distributed with
a mean of 20 minutes and a standard deviation of 2 minutes, and a 70%
chance that the time is uniformly distributed between 10 and 20 minutes.

Write a KSL program to generate 10 observations from this distribution using stream 1.
:::

***

::: {.exercise #Ch2Ex12}
Suppose that the service time for a patient consists of two distributions. There
is a 25% chance that the service time is uniformly distributed with
minimum of 20 minutes and a maximum of 25 minutes, and a 75% chance that
the time is distributed according to a Weibull distribution with shape
of 2 and a scale of 4.5. 

Write a KSL program to generate 10 observations from this distribution using stream 1.
:::

***

::: {.exercise #Ch2Ex13}
Consider the following probability density function:
:::
$$f(x) = 
  \begin{cases}
     \dfrac{3x^2}{2} & -1 \leq x \leq 1\\
     0 & \text{otherwise} \\
  \end{cases}$$

Write a KSL program to generate 10 observations from this distribution using the acceptance-rejection technique.

***

::: {.exercise #Ch2Ex14}
Consider the following function:

$$f(x) = 
  \begin{cases}
     cx^{2} & a \leq x \leq b\\
     0 & \text{otherwise} \\
  \end{cases}
$$
:::

a. Determine the value of $c$ that will turn $g(x)$ into a probability density function. The resulting probability density function is called a parabolic distribution.
b. Denote the probability density function found in part (a), $f(x)$.  Let $X$ be a random variable from $f(x)$.  Derive the inverse cumulative distribution function for $f(x)$.
c. Write a KSL program to generate 10 observations from this distribution over the range of $a=4$ and $b=12$ using your work from part (a) and (b).

***

::: {.exercise #Ch2Ex15}
Consider the following probability density function:

$$f(x) = 
  \begin{cases}
     \frac{3(c - x)^{2}}{c^{3}} & 0 \leq x \leq c\\
     0 & \text{otherwise} \\
  \end{cases}
$$
  
Derive an inverse cumulative distribution algorithm for generating from $f(x)$. Write a KSL program to generate 10 observations from this distribution for $c=5$.
:::

***
