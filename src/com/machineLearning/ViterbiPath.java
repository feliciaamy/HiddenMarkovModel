package com.machineLearning;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Amy on 28/11/16.
 */
public class ViterbiPath {
    String path = "";
    double probability = 0;
    int tries;

    public double getProbability() {
        return probability;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }

    public String getPath(){
        return path;
    }

    public ViterbiPath(String path, double probability) {
        this.path = path;
        this.probability = probability;
    }
}
