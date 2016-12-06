package com.machineLearning;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
    TrainingResult trainingResult;
    Map<Integer, List<Pi>> piMap;
    String[] yMax;
    int count = 0;
    boolean optimize;

    private String wordPattern = "(?i)@*[a-z]*";
    private String usernamePattern = "^@?(\\w){1,15}$";
    private Pattern rWord = Pattern.compile(wordPattern);
    private Pattern rUsername = Pattern.compile(usernamePattern);

    public Test(TrainingResult trainingResult, boolean optimize) {
        this.optimize = optimize;
        this.trainingResult = trainingResult;
    }

    public void writePrediction(File testFile, PredictionAlgorithm algo) {
        try {
            String prediction;
            if (algo == PredictionAlgorithm.SIMPLE) {
                prediction = getPredictionSimple(testFile);
            } else {
                prediction = getPredictionViterbi(testFile);
            }
            System.out.println("Writing prediction to file");
            PrintWriter writer = new PrintWriter(Main.LANGUAGE + ".prediction", "UTF-8");
            writer.print(prediction);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getPredictionViterbi(File test) {
        String result = "";
        try {
            Scanner in = new Scanner(test);
            List<List<String>> sequence = new ArrayList<List<String>>();
            List<String> newSequence = new ArrayList<String>();
            while (in.hasNextLine()) {
                String word = in.nextLine();
                if (word.length() > 0) {
                    newSequence.add(word);
                } else {
                    sequence.add(newSequence);
                    newSequence = new ArrayList<String>();
                }
            }
            in.close();

            for (List<String> sqn : sequence) {
                piMap = new HashMap<Integer, List<Pi>>();
                yMax = new String[sqn.size()];
                for (int i = 0; i < sqn.size() + 2; i++) {
                    computePi(i, sqn, false);
                }

                String next = "STOP";
                for (int n = sqn.size() + 1; n > 1; n--) {
                    next = getYmax(n, next);
                    yMax[n - 2] = next;
                }

                System.out.println(piMap.toString());
                System.out.println(printYmax());
                System.out.println("PREDICT: " + sqn.toString() + " " + sqn.size());
                for (int i = 0; i < sqn.size(); i++) {
                    result = result + sqn.get(i) + " " + yMax[i] + "\n";
                }
                result = result + "\n";
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    private String getYmax(int n, String finalNode) {
        List<Pi> prevPi = piMap.get(n - 1);
        Pi max = new Pi("", Double.MIN_VALUE);
        for (Pi pi : prevPi) {
            double newProb = pi.probability * getTransmissionProbability(pi.tag, finalNode);
            System.out.println(n + " " + pi.tag + " " + newProb + " " + pi.probability + " " + getTransmissionProbability(pi.tag, finalNode));
            if (newProb > max.probability) {
                System.out.println(pi.tag);
                max.probability = newProb;
                max.tag = pi.tag;
            }
        }
        return max.tag;
    }

    private void computePi(int index, List<String> sqn, boolean emissionDefault) {
        List<Pi> piList = new ArrayList<Pi>();
        if (index == 0) {
            piList.add(new Pi("START", 1));
            piMap.put(0, piList);
        } else if (index == sqn.size() + 1) {
            List<Pi> prevPi = piMap.get(index - 1);
            Pi max = new Pi("", Double.MIN_VALUE);
            double maxForY = Double.MIN_VALUE;
            for (Pi prev : prevPi) {
                double tProbability = getTransmissionProbability(prev.tag, "STOP");
                double prob = tProbability * prev.probability;
                if (maxForY < prob) {
                    maxForY = prob;
                }
                if (max.probability < prob) {
                    max.probability = prob;
                    max.tag = "STOP";
                }
            }
            piList.add(new Pi("STOP", maxForY));
            piMap.put(index, piList);
        } else {
            String word = sqn.get(index - 1);
            if (optimize) {
                System.out.println(word);
                word = word.toLowerCase().replace("'", "").replace("#", "").replace(".", "").replace("@", "").replace(" ", "");
                System.out.println(word);
            }
            List<Pi> prevPi = piMap.get(index - 1);
            boolean allZeros = true;

            for (String y : trainingResult.labelSorted) {
                if (y.equals("O") && optimize && ignore(word)) {
                    System.out.println(word);
                    piList.add(new Pi(y, 1));
                    continue;
                }
                if (y.equals("STOP") || y.equals("START")) {
                    continue;
                }

                Pi max = computePiHelper(index, prevPi, word, y, emissionDefault);

                if (max.probability != 0) {
                    allZeros = false;
                }
                piList.add(new Pi(y, max.probability));
            }
            if (allZeros) {
                computePi(index, sqn, true);
            } else {
                piMap.put(index, piList);
            }
        }
    }

    private Pi computePiHelper(int index, List<Pi> prevPi, String word, String y, boolean emissionDefault) {
        double max = -1;
        String tag = "";
        for (Pi prev : prevPi) {
            double eProbability = 0;
            double tProbability = getTransmissionProbability(prev.tag, y);
            EmissionNode e = new EmissionNode(word, y);
            if (emissionDefault) {
                eProbability = getEmissionProbabilityDefault(word, y);
            } else {
                eProbability = getEmissionProbability(word, y);
            }

            double prob = eProbability * tProbability * prev.probability;

            if (max <= prob) {
                max = prob;
                tag = y;
            }
        }
        return new Pi(tag, max);
    }

    private String getPredictionSimple(File test) {
        String result = "";
        try {
            Scanner in = new Scanner(test);
            while (in.hasNextLine()) {
                String temp = in.nextLine();
                if (temp.length() > 0) {
                    String prediction = predictSimple(temp);
                    result = result + temp + " " + prediction + "\n";
                } else {
                    result = result + "\n";
                }
            }
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    private String predictSimple(String word) {
        String prediction = "";
        double max = Double.MIN_VALUE;
        for (String y : trainingResult.label.keySet()) {
            if (y.equals("START") || y.equals("STOP")) {
                continue;
            }

            double probability = getEmissionProbability(word, y);
            if (probability > max) {
                max = probability;
                prediction = y;
            }
        }
        return prediction;
    }

    private double getEmissionProbability(String x, String y) {
        EmissionNode test = new EmissionNode(x, y);
        double probability = 0;
        if (trainingResult.emissionProbability.containsKey(test)) {
            probability = trainingResult.emissionProbability.get(test);
        } else if (!trainingResult.trainedWords.contains(x)) {
            probability = getEmissionProbabilityDefault(x, y);
        }
//        System.out.println(y + "->" + x + " : " + probability);
        return probability;
    }

    private double getEmissionProbabilityDefault(String x, String y) {
        EmissionNode test = new EmissionNode(x, y);
        double probability = 0;
        if (trainingResult.emissionProbability.containsKey(test)) {
            probability = trainingResult.emissionProbability.get(test);
        } else {
            double totalY = trainingResult.label.get(y) + 1;
            if (optimize) {
                totalY -= trainingResult.ignored.get(y);
            }

            probability = 1 / totalY;
        }
        return probability;
    }

    private double getTransmissionProbability(String y1, String y2) {
        TransitionNode test = new TransitionNode(y1, y2);
        double probability;

        if (trainingResult.transitionProbability.containsKey(test)) {
            probability = trainingResult.transitionProbability.get(test);
        } else {
            probability = 0;
        }
//        System.out.println(y1 + "->" + y2 + " : " + probability);
        return probability;
    }

    private String printYmax() {
        String result = "";
        for (int i = 0; i < yMax.length; i++) {
            result = result + yMax[i] + " ";
        }
        return result;
    }

    private boolean ignore(String word) {
        if (trainingResult.stopwords.contains(word)) {
            return true;
        } else if (!isWord(word)) {
            return true;
        }
        return false;
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