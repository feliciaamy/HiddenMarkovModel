package com.machineLearning;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by Amy on 26/11/16.
 */
public class Training {
    private TrainingResult tr;

    public Training(File file) {
        tr = new TrainingResult();
        train(file);
    }

    public TrainingResult getTrainingResult() {
        return tr;
    }

    private void train(File file) {
        System.out.println("Train data " + file.getPath());
        try {
            Scanner in = new Scanner(file);
            boolean start = true;
            String previousNode = "START";
            while (in.hasNext()) {
                String[] temp = in.nextLine().split(" ");

                if (temp.length > 1) {
                    // LABEL
                    updateLabel(temp[1]);

                    //WORD
                    if (!tr.trainedWords.contains(temp[0])) {
                        tr.trainedWords.add(temp[0]);
                    }

                    // EMISSION
                    EmissionNode newEmission = new EmissionNode(temp[0], temp[1]);
                    if (tr.emission.containsKey(newEmission)) {
                        tr.emission.put(newEmission, tr.emission.get(newEmission) + 1);
                    } else {
                        tr.emission.put(newEmission, 1);
                    }
                }

                // TRANSITION
                TransitionNode newTransition;
                if (temp.length > 1) {
                    if (start) {
                        start = false;
                        updateLabel("START");
                    }
                    newTransition = new TransitionNode(previousNode, temp[1]);
                    previousNode = temp[1];
                } else {
                    start = true;
                    newTransition = new TransitionNode(previousNode, "STOP");
                    previousNode = "START";
//                    updateLabel("STOP");
                }
                if (tr.transition.containsKey(newTransition)) {
                    tr.transition.put(newTransition, tr.transition.get(newTransition) + 1);
                } else {
                    tr.transition.put(newTransition, 1);
                }
            }
            in.close();
            computeEmissionProbability();
            computeTransitionProbability();
            sortLabel();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void updateLabel(String label) {
        if (tr.label.containsKey(label)) {
            tr.label.put(label, tr.label.get(label) + 1);
        } else {
            tr.label.put(label, 1);
            if(!label.equals("START") && !label.equals("STOP")){
                tr.labelSorted.add(label);
            }

        }
    }

    private void sortLabel() {
        for (int i = 0; i < tr.labelSorted.size(); i++) {
            for (int j = i; j < tr.labelSorted.size(); j++) {
                if (tr.label.get(tr.labelSorted.get(i)) < tr.label.get(tr.labelSorted.get(j))) {
                    String labelTemp = tr.labelSorted.get(j);
                    tr.labelSorted.set(j, tr.labelSorted.get(i));
                    tr.labelSorted.set(i, labelTemp);
                }
            }
        }
        System.out.println(tr.label.toString());
        System.out.println(tr.labelSorted.toString());
    }

    private void computeEmissionProbability() {
        System.out.println("Compute Emission Probability");
        for (Map.Entry<EmissionNode, Integer> entry : tr.emission.entrySet()) {
            double totalY = tr.label.get(entry.getKey().y) + 1;
            double probability = entry.getValue() / totalY;
            tr.emissionProbabilityDefault.put(entry.getKey(), probability);
        }
//        System.out.println(tr.emissionProbability.toString());
    }

    private void computeTransitionProbability() {
        System.out.println("Compute Transition Probability");
        for (Map.Entry<TransitionNode, Integer> entry : tr.transition.entrySet()) {
            double totalY = tr.label.get(entry.getKey().y1);
            double probability = entry.getValue() / totalY;
            tr.transitionProbability.put(entry.getKey(), probability);
        }
        System.out.println(tr.transitionProbability.toString());
    }
}
