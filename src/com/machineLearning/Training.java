package com.machineLearning;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Amy on 26/11/16.
 */
public class Training {
    private TrainingResult tr;
    private boolean optimize = false;
    private String wordPattern = "(?i)@*[a-z]*";
    private String usernamePattern = "^@(\\w){1,15}$";
    private Pattern rWord = Pattern.compile(wordPattern);
    private Pattern rUsername = Pattern.compile(usernamePattern);

    public Training(File file, boolean optimize, String language) {
        this.optimize = optimize;
        tr = new TrainingResult(language);
        // Create a Pattern object
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
                boolean isFeature = false;
                if (temp.length > 1) {
                    String word = temp[0];
                    if (optimize) {
                        word = temp[0].toLowerCase().replace("'", "").replace("#", "").replace(".", "");
                        word = word.replace("@", "");
                    }
                    // LABEL
                    updateLabel(temp[1]);
                    if (!optimize || (isWord(word) && !tr.stopwords.contains(word))) {
                        isFeature = true;
                        if (optimize) {
                            if (word.equals("")) {
                                continue;
                            }
                        }

                        // WORD
                        if (!tr.trainedWords.containsKey(word)) {
                            tr.trainedWords.put(word, 1);
                        } else {
                            tr.trainedWords.put(word, 1 + tr.trainedWords.get(word));
                        }

                        // EMISSION
                        EmissionNode newEmission = new EmissionNode(word, temp[1]);
                        if (tr.emission.containsKey(newEmission)) {
                            tr.emission.put(newEmission, tr.emission.get(newEmission) + 1);
                        } else {
                            tr.emission.put(newEmission, 1);
                        }
                    } else {
                        if (optimize) {
                            updateIgnore(temp[1]);
                        }
//                        System.out.println(word);
                    }
                }

                // TRANSITION
                TransitionNode newTransition;
                if (temp.length > 1) {
                    if (start) {
                        start = false;
                    }
                    newTransition = new TransitionNode(previousNode, temp[1]);
                    previousNode = temp[1];
                } else {
                    start = true;
                    newTransition = new TransitionNode(previousNode, "STOP");
                    previousNode = "START";
                    // Only add start if there is a stop for it.
                    updateLabel("START");
                    updateLabel("STOP");
                }
                if (!newTransition.equals(new TransitionNode("START", "STOP"))) {
                    if (tr.transition.containsKey(newTransition)) {
                        tr.transition.put(newTransition, tr.transition.get(newTransition) + 1);
                    } else {
                        tr.transition.put(newTransition, 1);
                    }
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
            if (!label.equals("START") && !label.equals("STOP")) {
                tr.labelSorted.add(label);
            }
        }
    }

    private void updateIgnore(String label) {
        if (tr.ignored.containsKey(label)) {
            tr.ignored.put(label, tr.ignored.get(label) + 1);
        } else {
            tr.ignored.put(label, 1);
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
        boolean repeat = false;
        System.out.println("Compute Emission Probability");
        Map<String, Integer> reduceLabel = new HashMap<>();
        List<EmissionNode> remove = new ArrayList<>();
        for (Map.Entry<EmissionNode, Integer> entry : tr.emission.entrySet()) {
            String word = entry.getKey().x;
            String label = entry.getKey().y;
            double totalY = tr.label.get(label) + 1;
            if (optimize) {
                totalY -= tr.ignored.get(label);
            }
            double probability = entry.getValue() / totalY;
//            tr.emissionProbability.put(entry.getKey(), probability);
            if ((optimize && probability > 1E-5) || !optimize) {
                tr.emissionProbability.put(entry.getKey(), probability);
            } else {
                repeat = true;
                System.out.println(entry.getKey().toString() + " " + probability);
                remove.add(entry.getKey());
                tr.trainedWords.put(word, tr.trainedWords.get(word) - entry.getValue());
                if (tr.trainedWords.get(word) <= 0) {
//                    System.out.println(word);
                    tr.trainedWords.remove(word);
                }
                if (!reduceLabel.containsKey(label)) {
                    reduceLabel.put(label, 1);
                } else {
                    reduceLabel.put(label, reduceLabel.get(label) + entry.getValue());
                }
            }
        }

        if (optimize) {
            for (Map.Entry<String, Integer> entry : reduceLabel.entrySet()) {
                String label = entry.getKey();
                int reduce = entry.getValue();
                tr.ignored.put(label, reduce + tr.ignored.get(label));
            }

            for (EmissionNode e : remove) {
                tr.emission.remove(e);
            }
            if (repeat) {
                System.out.println("REPEAT");
                computeEmissionProbability();
            }
        }

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

    private boolean isWord(String word) {
        if (word.equals("")) {
            return false;
        }
        Matcher m = rWord.matcher(word);
        if (m.matches()) {
            return true;
        }
        return false;
    }

    private boolean isUsername(String word) {
        Matcher m = rUsername.matcher(word);
        if (m.matches()) {
            return true;
        }
        return false;
    }
}
