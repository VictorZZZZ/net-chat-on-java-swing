package ru.gb;

import java.util.Arrays;

public class ArrayProcess {

    public static int[] dropBeforeFour(int[] arr){
        int pos = positionOfLastFour(arr);
        if(pos==-1){
            throw new RuntimeException("4 not Found");
        }
        return Arrays.copyOfRange(arr,pos+1,arr.length);

    }

    public static boolean checkIf14Only(int[] arr){
        for (int i = 0; i < arr.length; i++) {
            if(arr[i]==1 || arr[i]==4) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }

    private static int positionOfLastFour(int[] arr) {
        int position=-1;//если нет четверки в массиве
        for(int i=0;i<arr.length;i++) {
            if(arr[i]==4) position=i;
        }
        return position;
    }


}
