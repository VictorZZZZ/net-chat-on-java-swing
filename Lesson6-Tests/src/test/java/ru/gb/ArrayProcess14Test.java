package ru.gb;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ArrayProcess14Test {
    private static HashMap<int[], Boolean> satisfyArr;

    @BeforeClass
    public static void init(){
        satisfyArr= new HashMap<>();
        satisfyArr.put(new int[]{1,1,1,1},true);
        satisfyArr.put(new int[]{1,2,1,1},false);
        satisfyArr.put(new int[]{1,4,1,1,4},true);
        satisfyArr.put(new int[]{4,4,5,6},false);
        satisfyArr.put(new int[]{1,4,1,4},false);
        satisfyArr.put(new int[]{4,4,4,4},true);
    }

    @Test
    public void check14Test(){
        for(Map.Entry<int[], Boolean> entry : satisfyArr.entrySet()) {
            int[] arr = entry.getKey();
            Boolean isSatisfy = entry.getValue();
            if(ArrayProcess.checkIf14Only(arr)!=isSatisfy) {
                System.out.println("Не отработал массив "+Arrays.toString(arr) + "=="+isSatisfy);
                Assert.fail();
            }
        }
        assert(true);
    }
}
