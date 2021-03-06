@file:JvmName("RbfExperiments")

import algorithms.RbfAlgorithm
import datasets.DataSet
import datasets.Instance
import datasets.PcaPropaneDataReader
import util.CrossValidation
import util.LearningCurve
import util.Result
import java.util.*

fun main(args: Array<String>) {
    rbfLearningCurve()
}

fun rbfLearningCurve() {
    val rbf = RbfAlgorithm()
    rbf.setParams(createParams(15, .1, false))

    val absoluteError = {error:Double -> Math.abs(error)}

    val learningCurve = LearningCurve(PcaPropaneDataReader().propaneDataSet, rbf, absoluteError)
    val csv = learningCurve.run()

    println(csv)
}

fun runRbf(dataSet: DataSet<Instance>, cgd: Boolean, tolerance: Double, rbfCount: Int): Result {
    val params = createParams(rbfCount, tolerance, cgd)

    val rbf = RbfAlgorithm()

    rbf.setParams(params)

    val numFolds = 10

    val crossValidation = CrossValidation(CrossValidation.AbsoluteError(), numFolds, dataSet, rbf)

    return crossValidation.run()
}

fun testConjugateGradientDescent(dataSet: DataSet<Instance>) {

    val paramsWithCgd = newDefaultParams()
    paramsWithCgd.put(RbfAlgorithm.KEY_CONJUGATE_GRADIENT_DESCENT, true)

    val paramsWithoutCgd = newDefaultParams()
    paramsWithoutCgd.put(RbfAlgorithm.KEY_CONJUGATE_GRADIENT_DESCENT, false)

    val rbf = RbfAlgorithm()
    rbf.setParams(paramsWithCgd)

    val numFolds = 10

    val crossValidation = CrossValidation(CrossValidation.AbsoluteError(), numFolds, dataSet, rbf)

    val resultsWithCgd = crossValidation.run()

    rbf.setParams(paramsWithoutCgd)

    val resultsWithoutCgd = crossValidation.run()

    println("\nvalidation error with Conjugate Gradient Descent: " + resultsWithCgd.meanValidationError)
    println("\nvalidation error without Conjugate Gradient Descent: " + resultsWithoutCgd.meanValidationError)
}

fun findBestTolerance(dataSet: DataSet<Instance>) {
    val rbf = RbfAlgorithm()

    val numFolds = 10

    val crossValidation = CrossValidation(CrossValidation.AbsoluteError(), numFolds, dataSet, rbf)

    val params = newDefaultParams()

    println("Tolerance,TrainingError,ValidationError")
    var divisor = 10
    while (divisor < 100000) {
        val tolerance = 1.0 / divisor
        params.put(RbfAlgorithm.KEY_TOLERANCE, tolerance)

        rbf.setParams(params)

        val result = crossValidation.run()

        println("$tolerance,${result.meanTrainingError},${result.meanValidationError}")
        divisor = divisor shl 1
    }
}

fun findBestRbfCount(dataSet: DataSet<Instance>, minValueToTest: Int, interval: Int, maxValueToTest: Int) {
    val rbf = RbfAlgorithm()

    val numFolds = 10

    val crossValidation = CrossValidation(CrossValidation.AbsoluteError(), numFolds, dataSet, rbf)

    val params = newDefaultParams()

    println("RbfCount,TrainingError,ValidationError")
    var rbfCount = minValueToTest
    while (rbfCount <= maxValueToTest) {
        params.put(RbfAlgorithm.KEY_NUM_RBFS, rbfCount)

        rbf.setParams(params)

        val result = crossValidation.run()

        println("$rbfCount,${result.meanTrainingError},${result.meanValidationError}")
        rbfCount += interval
    }
}

private fun newDefaultParams(): MutableMap<String, Any> {
    val params = HashMap<String, Any>()

    params.put(RbfAlgorithm.KEY_TOLERANCE, 0.00001)

    params.put(RbfAlgorithm.KEY_NUM_RBFS, 5)

    params.put(RbfAlgorithm.KEY_CONJUGATE_GRADIENT_DESCENT, false)

    return params
}

private fun createParams(rbfCount: Int, tolerance: Double, cgd: Boolean): Map<String, Any> {
    val params = HashMap<String, Any>()

    params.put(RbfAlgorithm.KEY_TOLERANCE, tolerance)

    params.put(RbfAlgorithm.KEY_NUM_RBFS, rbfCount)

    params.put(RbfAlgorithm.KEY_CONJUGATE_GRADIENT_DESCENT, cgd)

    return params
}
