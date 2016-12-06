package com.machineLearning;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Main {
    private static TrainingResult trainingResult;
    public final static String LANGUAGE = "EN";
    private static boolean optimize = true;
    private static PredictionAlgorithm algorithm = PredictionAlgorithm.VITERBIK;
    public static void main(String[] args) {
        File trainingFile = (new File(LANGUAGE + "/train.txt"));

        Training trainingData = new Training(trainingFile, optimize, LANGUAGE);
        trainingResult = trainingData.getTrainingResult();

        System.out.println(trainingResult.ignored);
        System.out.println(trainingResult.trainedWords);
        System.out.println(trainingResult.emissionProbability);

//        printEmission(trainingResult.emission);
//        printTransition(trainingResult.transition);
//        printLabel(trainingResult.label);

//        File testFiles = new File(LANGUAGE + "/" + LANGUAGE+ ".in");
//        if (algorithm == PredictionAlgorithm.VITERBIK){
//            Test_k test = new Test_k(trainingResult, 1, optimize);
//            test.writePrediction(testFiles);
//        } else{
//            Test test = new Test(trainingResult, optimize);
//            test.writePrediction(testFiles, algorithm);
//        }
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
