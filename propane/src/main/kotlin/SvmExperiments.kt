@file:JvmName("SvmExperiments")

import algorithms.SvmAlgorithm
import datasets.DataSet
import datasets.Instance
import datasets.PcaPropaneDataReader
import datasets.PropaneDataReader
import util.CrossValidation
import util.Result

fun main(args: Array<String>) {
    comparePropaneDataSets(SvmAlgorithm.Kernel.RadialBasisFunction,1.0,1.0);
//    findBestC(PcaPropaneDataReader().propaneDataSet as DataSet<Instance>)
}

fun runSvm(dataSet: DataSet<Instance>, kernel: SvmAlgorithm.Kernel, c: Double, gamma: Double) : Result {
    val svm = SvmAlgorithm()

    svm.setParams(SvmAlgorithm.createParams(kernel, c, gamma));

    val crossValidation = CrossValidation(CrossValidation.AbsoluteError(), 20, dataSet, svm);

    return crossValidation.run()
}

fun comparePropaneDataSets(kernel: SvmAlgorithm.Kernel, c: Double, gamma: Double) {
    val pcaDataResults = runSvm(PcaPropaneDataReader().propaneDataSet as DataSet<Instance>, kernel, c, gamma)
    val originalDataResults = runSvm(PropaneDataReader().propaneDataSet as DataSet<Instance>, kernel, c, gamma)

    println("error running svm with kernel ${kernel}, c ${c}, and gamma ${gamma} on original data:${originalDataResults.meanValidationError}");
    println("error running svm with kernel ${kernel}, c ${c}, and gamma ${gamma} on pca data:${pcaDataResults.meanValidationError}");
}

fun findBestC(dataSet: DataSet<Instance>) {
    println("C,TrainingError,ValidationError");

    var c = 1000000.0
    while (c >= .0000001) {
        c /= 10.0

        val result = runSvm(dataSet, SvmAlgorithm.Kernel.RadialBasisFunction, c, 0.1);

        println("$c,${result.meanTrainingError},${result.meanValidationError}");
    }
}