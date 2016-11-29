package com.machineLearning;

import com.sun.xml.internal.ws.message.EmptyMessageImpl;

/**
 * Created by Amy on 26/11/16.
 */
public class EmissionNode {
    String x;
    String y;

    public EmissionNode(String x, String y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public int hashCode()
    {
        return 31*x.hashCode() + y.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        // basic type validation
        if (!(obj instanceof EmissionNode))
            return false;

        EmissionNode t = (EmissionNode) obj;
        return this.x.equals(t.x) && this.y.equals(t.y);
    }

    public String toString(){
        return y + "->" + x;
    }

}
