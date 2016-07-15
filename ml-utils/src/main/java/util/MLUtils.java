package util;

import algorithms.*;
import datasets.*;
import datasets.Instance;
import org.apache.commons.math3.stat.descriptive.*;

import java.util.*;

public class MLUtils {
    public static float[] toPrimitiveFloatArray(Collection<Float> floatCollection) {
        float[] primitiveArray = new float[floatCollection.size()];
        Iterator iterator = floatCollection.iterator();

        for (int i = 0; i < floatCollection.size(); i++) {
            primitiveArray[i] = (float) iterator.next();
        }

        return primitiveArray;
    }

    public static Result crossValidate(DataSet dataSet, int numFolds, Algorithm algorithm) {
        List<Instance>[] groups = dataSet.splitDataSetRandomly(numFolds);

        SummaryStatistics validationErrors = new SummaryStatistics();
        SummaryStatistics trainingErrors = new SummaryStatistics();

        for (int i = 0; i < numFolds; i++) {
            Instance[] testingData = groups[i].toArray(new Instance[groups[i].size()]);

            DataSet<Instance> trainingDataSet = new DataSet<Instance>(combineAllListsExceptOne(groups, i), dataSet.hasDiscreteOutput());

            algorithm.train(trainingDataSet);

            double sumOfValidationError = 0;
            double sumOfTrainingError = 0;

            for (Instance testInstance : testingData) {
                double output = (double) algorithm.evaluate(testInstance.getInput());
                sumOfValidationError += testInstance.getDifference(output);
            }

            for (Instance trainingInstance : trainingDataSet.getInstances()) {
                double output = (double) algorithm.evaluate(trainingInstance.getInput());
                sumOfTrainingError += trainingInstance.getDifference(output);
            }

            validationErrors.addValue(sumOfValidationError / testingData.length);
            trainingErrors.addValue(sumOfTrainingError / trainingDataSet.getInstances().length);
        }
        return new Result(validationErrors.getMean(), trainingErrors.getMean(), validationErrors.getStandardDeviation(), numFolds, groups[0].size());
    }

    private static Instance[] combineAllListsExceptOne(List<Instance>[] lists, int listToLeaveOut) {
        if (lists.length == 1) {
            return lists[0].toArray(new Instance[lists[0].size()]);
        }

        Instance[] combinedArray = new Instance[totalCountOfAllLists(lists) - lists[listToLeaveOut].size()];

        int combinedArrayIndex = 0;

        for (int listIndex = 0; listIndex < lists.length; listIndex++) {
            if (listIndex == listToLeaveOut) {
                continue;
            }

            for (Instance instance : lists[listIndex]) {
                combinedArray[combinedArrayIndex] = instance;
                combinedArrayIndex += 1;
            }
        }

        return combinedArray;
    }

    private static int totalCountOfAllLists(List[] lists) {
        int total = 0;
        for (List list : lists) {
            total += list.size();
        }

        return total;
    }

    public static Result leaveOneOutCrossValidate(DataSet dataSet, Algorithm algorithm) {
        return crossValidate(dataSet, dataSet.getInstances().length, algorithm);
    }

    public static void printLearningCurve(DataSet dataSet, Algorithm algorithm) {

        System.out.println("percentOfDataForTesting,trainingError,crossValidationError");
        for (int divisor = 2; divisor < 11; divisor++) {
            Result results = crossValidate(dataSet, divisor, algorithm);

            System.out.println(String.format("%.3f", 1.0f / divisor) + "," + results.getMeanTrainingError() + "," + String.format("%.3f", results.getMeanValidationError()));
        }

        Result results = leaveOneOutCrossValidate(dataSet, algorithm);

        System.out.println(String.format("%.3f", 1.0f / dataSet.getInstances().length) + "," + results.getMeanTrainingError() + "," + String.format("%.3f", results.getMeanValidationError()));
    }

    public static int getNumInputs(DataSet<Instance> dataSet) {
        Instance firstInstance = dataSet.getInstances()[0];

        return firstInstance.getInput().length;
    }

    public static Csv createCsvForClusterResults(Map<Instance, Integer> clusterings) {
        Csv csv = new Csv("InstanceIndex", "Output", "Classification");

        int index= 0;
        for (Instance instance: clusterings.keySet()) {
            csv.addRow((Integer) index, instance.getOutput(), clusterings.get(instance));
            index += 1;
        }

        return csv;
    }
}
