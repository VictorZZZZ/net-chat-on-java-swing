package ru.gb;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class ArrayProcessMassTest {

    @Parameterized.Parameters
    public static Collection<Object[]> data(){
        return Arrays.asList(new Object[][]{
                {new int[]{1,2,4,6,2,1},new int[]{6,2,1}},
                {new int[]{1,4,5,3,2},new int[]{5,3,2}},
                {new int[]{144,444,4,22,145,15},new int[]{22,145,15}},
                {new int[]{4,2,4,4,4,1},new int[]{1}}
        });
    }

    int[]array;
    int[]arrayExp;

    public ArrayProcessMassTest(int[] array, int[] arrayExp) {
        this.array = array;
        this.arrayExp = arrayExp;
    }

    @Test
    public void dropBefore4Test(){
        Assert.assertArrayEquals(arrayExp,ArrayProcess.dropBeforeFour(array));
    }
}
