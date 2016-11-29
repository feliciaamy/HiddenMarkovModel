package com.machineLearning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Amy on 26/11/16.
 */
public class TrainingResult {
    Map<String, Integer> label;
    List<String> trainedWords;
    List<String> labelSorted;
    Map<EmissionNode, Integer> emission;
    Map<EmissionNode, Double> emissionProbabilityDefault;
    Map<TransitionNode, Integer> transition;
    Map<TransitionNode, Double> transitionProbability;

    public TrainingResult(){
        emissionProbabilityDefault = new HashMap<EmissionNode, Double>();
        label = new HashMap<String, Integer>();
        emission = new HashMap<EmissionNode, Integer>();
        transition = new HashMap<TransitionNode, Integer>();
        transitionProbability = new HashMap<TransitionNode, Double>();
        trainedWords = new ArrayList<String>();
        labelSorted = new ArrayList<String >();
    }
}
