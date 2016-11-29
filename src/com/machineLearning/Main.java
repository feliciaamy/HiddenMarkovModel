package com.machineLearning;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Main {
    private static TrainingResult trainingResult;
    public final static String LANGUAGE = "EN";
    public static void main(String[] args) {
        File trainingFile = (new File(LANGUAGE + "/train.txt"));

        Training trainingData = new Training(trainingFile);
        trainingResult = trainingData.getTrainingResult();

//        printEmission(trainingResult.emission);
//        printTransition(trainingResult.transition);
//        printLabel(trainingResult.label);

        File testFiles = new File(LANGUAGE + "/" + LANGUAGE+ ".in");
        Test test = new Test(trainingResult);
        test.writePrediction(testFiles, PredictionAlgorithm.VITERBI);
    }

    public static void printEmission(Map<EmissionNode, Integer> map) {
        for (Map.Entry<EmissionNode, Integer> item : map.entrySet()) {
            System.out.println(item.getKey() + " : " + item.getValue());
        }
    }
    public static void printTransition(Map<TransitionNode, Integer> map) {
        for (Map.Entry<TransitionNode, Integer> item : map.entrySet()) {
            System.out.println(item.getKey() + " : " + item.getValue());
        }
    }

    public static void printLabel(Map<String, Integer> map) {
        for (Map.Entry<String, Integer> item : map.entrySet()) {
            System.out.println(item.getKey() + " : " + item.getValue());
        }
    }
}
