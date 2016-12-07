package com.machineLearning;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Main {
    private static TrainingResult trainingResult;
    private final static String[] LANGUAGES = {"EN", "ES", "SG", "CN"};
    private static boolean optimize = false;
    private static PredictionAlgorithm algorithm = PredictionAlgorithm.OPTIMIZED;

    public static void main(String[] args) {
        if (algorithm == PredictionAlgorithm.OPTIMIZED) {
            optimize = true;
        }
        for (String language : LANGUAGES) {
            if (optimize && (language.equals("CN") || language.equals("SG"))) {
                continue;
            }
            File trainingFile = (new File(language + "/train.txt"));

            Training trainingData = new Training(trainingFile, optimize, language);
            trainingResult = trainingData.getTrainingResult();

            System.out.println(trainingResult.ignored);
            System.out.println(trainingResult.trainedWords);
            System.out.println(trainingResult.emissionProbability);

            File testFiles = new File(language + "/" + language + ".in");
            if (algorithm == PredictionAlgorithm.VITERBIK) {
                Test_k test = new Test_k(trainingResult, 5, optimize);
                test.writePrediction(testFiles, language);
            } else {
                Test test = new Test(trainingResult, optimize);
                test.writePrediction(testFiles, algorithm, language);
            }
        }
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
