package com.machineLearning;

/**
 * Created by Amy on 28/11/16.
 */
public class Pi {
    String tag;
    double probability;

    public Pi(String tag, double probability) {
        this.tag = tag;
        this.probability = probability;
    }

    @Override
    public int hashCode()
    {
        return 31*tag.hashCode() + Double.valueOf(probability).hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        // basic type validation
        if (!(obj instanceof Pi))
            return false;

        Pi t = (Pi) obj;
        return this.tag.equals(t.tag) && this.probability == t.probability;
    }

    public String toString(){
        return "(" + tag + " ," + probability + ")";
    }
}
