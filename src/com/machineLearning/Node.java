package com.machineLearning;

/**
 * Created by lakshitachhikara on 12/6/16.
 */

/* This class stores previous tag, previous index and current pi probability
   It overrides compareTo method of Comparable interface to compare Nodes based on probability.
 */
public class Node implements Comparable<Node> {
    String prevtag;
    int previndex;
    double probability;

    public Node(String tag, int index, double probability) {
        this.prevtag = tag;
        this.previndex = index;
        this.probability = probability;
    }

    public String toString() {
        return ("" + prevtag + ", " + previndex + ", " + probability);
    }

    @Override
    public int compareTo(Node t2) {
        return this.probability < t2.probability ? 1 : this.probability == t2.probability ? 0 : -1;
    }

}