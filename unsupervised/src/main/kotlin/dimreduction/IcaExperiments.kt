package dimreduction

import datasets.DataSet
import datasets.Instance
import datasets.IrisDataReader
import datasets.PropaneDataReader
import filters.IndependentComponentsFilter
import learningCurveREPBoosting
import util.MLUtils
import util.writeToFile

fun main(args: Array<String>) {
    System.out.println("Kurtosis: ${MLUtils.calculateAverageKurtosisForAttributes(IrisDataReader().irisDataSet)}")
}

fun testIcaBoosting(dataSet: DataSet<Instance>) {
    System.out.println("regular data REP Boosting")

    val absoluteError = {difference: Double -> Math.abs(difference)}

    learningCurveREPBoosting(dataSet, absoluteError)

    val icaDataSet = IndependentComponentsFilter().filterDataSet(dataSet)

    val numFeatures = icaDataSet.getInstances()[0].input.size;

    System.out.println("ICA data REP Boosting with $numFeatures features")
    learningCurveREPBoosting(icaDataSet, absoluteError)
}

fun printIcaForDataSet(dataSet: DataSet<Instance>) {
    val icaDataSet = IndependentComponentsFilter().filterDataSet(dataSet)

    System.out.println(icaDataSet)

//    writeToFile("propane_ica.csv", icaDataSet.toString());
}




