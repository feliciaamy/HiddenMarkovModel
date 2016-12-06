package com.machineLearning;

import java.util.List;

/**
 * Created by lakshitachhikara on 12/4/16.
 */

/*This class stores the current tag and list of top k probabilities and their back pointers.
  The back pointer and probability from each previous node is stored as a List of Nodes.
 */
public class Pi_k {
    String tag;
    List<Node> nodeList;

    public Pi_k(String tag, List<Node> nodeList) {
        this.tag = tag;
        this.nodeList = nodeList;
    }

    public String toString() {
        return "(" + tag + " ," + nodeList.toString() + ")";
    }
}
