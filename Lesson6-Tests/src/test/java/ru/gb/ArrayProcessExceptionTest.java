package ru.gb;

import org.junit.Test;

public class ArrayProcessExceptionTest {

    //Выдаёт ли Runtime
    @Test(expected = RuntimeException.class)
    public void checkIfRuntime(){
        int[] arr=new int[]{2,3,5,6,8};
        ArrayProcess.dropBeforeFour(arr);
    }
}
