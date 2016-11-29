package com.machineLearning;

/**
 * Created by Amy on 26/11/16.
 */
public class TransitionNode {
    String y1, y2;

    public TransitionNode(String y1, String y2){
        this.y1 = y1;
        this.y2 = y2;
    }

    @Override
    public int hashCode()
    {
        return 31*y1.hashCode() + y2.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        // basic type validation
        if (!(obj instanceof TransitionNode))
            return false;

        TransitionNode t = (TransitionNode) obj;
        return this.y1.equals(t.y1) && this.y2.equals(t.y2);
    }

    public String toString(){
        return y1 + "->" + y2;
    }
}
