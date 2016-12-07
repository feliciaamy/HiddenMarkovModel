package com.machineLearning;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lakshitachhikara on 12/4/16.
 */

public class Test_k {
    //Contains probabilities from training - emission, transition, laebeled words etc.
    TrainingResult trainingResult;
    /*For each level, it stores top k probabilites for each label.
      i.e #labels*k = 7*k probabilties and back pointers
     */
    Map<Integer, List<Pi_k>> piMap;
    //Stores the k-th sequence during backward viterbi.
    String[] yMax;
    boolean allZeros;
    boolean optimize;
    int k;

    private String wordPattern = "(?i)@*[a-z]*";
    private String usernamePattern = "^@?(\\w){1,15}$";
    private Pattern rWord = Pattern.compile(wordPattern);
    private Pattern rUsername = Pattern.compile(usernamePattern);

    public Test_k(TrainingResult trainingResult, int k, boolean optimize) {
        this.optimize = optimize;
        this.trainingResult = trainingResult;
        this.k = k;
    }

    public void writePrediction(File testFile, String language) {
        try {
            String prediction;
            prediction = getPredictionViterbi(testFile);

            System.out.println("Writing prediction to file");
            PrintWriter writer = new PrintWriter(language + "/" + language + "_VITERBIK.prediction", "UTF-8");
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
            //Scan all the data and segregate as sentences. sequence contains sentences as list of words
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

            //Predict labels for each sentence
            for (List<String> sqn : sequence) {
                piMap = new HashMap<Integer, List<Pi_k>>();
                yMax = new String[sqn.size()];
                //Calculate forward viterbi proabilities for each word in a sentence
                for (int i = 0; i < sqn.size()+2; i++) {
                    //calculates top k probabilites of label for each word
                    computePi(i, sqn, false);
                }

                //Terminating Viterbi by setting nth label = k-th probability label for STOP
                List<Pi_k> prevPi = piMap.get(sqn.size()+1);
                Node topk = prevPi.get(0).nodeList.get(k-1);
                yMax[sqn.size()-1] = topk.prevtag;
                //Backward Viterbi to identify rest of the label until START
                for (int n = sqn.size(); n > 1; n--) {
                    topk = getYmax(topk, n);
                    yMax[n - 2] = topk.prevtag;

                }

                System.out.println("PREDICT: " + sqn.toString() + " " + sqn.size());
                //Store result of predictions in file
                for (int i = 0; i < sqn.size(); i++) {
                    result = result + sqn.get(i) + " " + yMax[i] + "\n";
                }
                result = result + "\n";

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    //Uses backpointers to find k-th best sequence
    private Node getYmax(Node node, int n) {
        List<Pi_k> prevPi = piMap.get(n);
        for (Pi_k pi : prevPi) {
            if (pi.tag.equals(node.prevtag)){
                return pi.nodeList.get(node.previndex);
            }
        }
        return null;
    }

    //Computes k best probabilities for each label and stores in piMap.
    private void computePi(int index, List<String> sqn, boolean emissionDefault) throws InterruptedException {
        List<Pi_k> piList = new ArrayList<Pi_k>();
        List<Node> nodeList = new ArrayList<Node>();

        if (index==0){
            nodeList.add(new Node(null,-1,1.0));
            piList.add(new Pi_k("START", nodeList));
            piMap.put(0,piList);
        }
        else if (index == sqn.size() + 1) {
            List<Pi_k> prevPi = piMap.get(index - 1);
            String y = "STOP";
            for (Pi_k prev: prevPi){
                double tProbability = getTransmissionProbability(prev.tag, y);
                for(int i=0;i<prev.nodeList.size();i++){
                    Node node = prev.nodeList.get(i);
                    nodeList.add(new Node(prev.tag, i,  tProbability * node.probability));
                }
            }
            Collections.sort(nodeList);
            piList.add(new Pi_k(y,nodeList.subList(0,k)));
            piMap.put(index, piList);
        }
        else {
            String word = sqn.get(index - 1);
            List<Pi_k> prevPi = piMap.get(index - 1);
            allZeros = true;

            if (optimize) {
                word = word.toLowerCase().replace("'", "").replace("#", "").replace(".", "").replace("@", "").replace(" ", "");
            }


            //For each label, compute k probabilities
            for (String y : trainingResult.labelSorted) {
                if (y.equals("STOP") || y.equals("START")) {
                    continue;
                }
                Pi_k kmax = computePiHelper(prevPi, word, y, emissionDefault);
                piList.add(kmax);
            }
            if (allZeros) {
                computePi(index, sqn, true);
            } else {
                piMap.put(index, piList);
            }
        }
    }

    //Computes k best proabilites from previous level probabilites.
    private Pi_k computePiHelper(List<Pi_k> prevPi, String word, String y, boolean emissionDefault) {
        List<Node> nodeList = new ArrayList<Node>();
        for (Pi_k prev: prevPi){
            double eProbability = 0;
            double tProbability = getTransmissionProbability(prev.tag, y);
            EmissionNode e = new EmissionNode(word, y);
            if (y.equals("O") && optimize && ignore(word)) {
                eProbability = 1.0;
            }
            else if (emissionDefault) {
                eProbability = getEmissionProbabilityDefault(word, y);
            } else {
                eProbability = getEmissionProbability(word, y);
            }
            for(int i=0;i<prev.nodeList.size();i++){
                Node node = prev.nodeList.get(i);
                nodeList.add(new Node(prev.tag, i, eProbability * tProbability * node.probability));
            }
        }

        Collections.sort(nodeList);
        if (nodeList.get(0).probability!=0.0)
            allZeros = false;
        if (k>nodeList.size())
            return new Pi_k(y,nodeList);
        return new Pi_k(y,nodeList.subList(0,k));

    }



    private double getEmissionProbability(String x, String y) {
        EmissionNode test = new EmissionNode(x, y);
        double probability = 0;
        if (trainingResult.emissionProbability.containsKey(test)) {
            probability = trainingResult.emissionProbability.get(test);
        } else if (!trainingResult.trainedWords.containsKey(x)) {
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