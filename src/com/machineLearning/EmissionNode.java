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

    public static long count(int[] numbers, int  k){
        int total = 0;
        int counter = 0;
        for (int i : numbers){
            if (i < k){
                total += 1 + solver(numbers, k, i, counter +1);
            }
            counter++;
        }
        return total;
    }

    public static long solver (int[] numbers, int k, int sofar, int index){
        if (numbers.length == index){
            return 0;
        } else{
            sofar = sofar * numbers[index];
            if (sofar < k){
                return 1 + solver(numbers, k, sofar, index+1);
            }else{
                return 0;
                // return solver(numbers, k, sofar, index+1, path + numbers[index] +" x ");
            }
        }
    }
}
