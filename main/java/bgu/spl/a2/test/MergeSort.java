package bgu.spl.a2.test;

import bgu.spl.a2.Task;
import bgu.spl.a2.WorkStealingThreadPool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class MergeSort extends Task<int[]> {

    private final int[] array;

    public MergeSort(int[] array) {

        this.array = array;
    }

    @Override

    protected void start() {
        List<Task<int[]>> tasks = new ArrayList<>();
        if(array.length != 1){
            int[] leftArray = new int[array.length/2];
            for(int i = 0; i <  leftArray.length; i++)
                 leftArray[i] = array[i];
            MergeSort leftArrayTask = new MergeSort( leftArray);
            tasks.add(leftArrayTask);

            int[] rightArray = new int[array.length - array.length/2];
            for(int i = 0; i < rightArray.length; i++)
                rightArray[i] = array[i+array.length/2];
            MergeSort rightArrayTask = new MergeSort(rightArray);
            tasks.add(rightArrayTask);

            whenResolved(tasks, ()->{
                int[] result = new int[array.length];
                int i = 0, i1 = 0, i2 = 0;
                int len1 = tasks.get(0).getResult().get().length,
                        len2 = tasks.get(1).getResult().get().length;
                while(i1 < len1 && i2 < len2){
                    if(tasks.get(0).getResult().get()[i1] < tasks.get(1).getResult().get()[i2]){
                        result[i] = tasks.get(0).getResult().get()[i1];
                        i1++;
                    } else {
                        result[i] = tasks.get(1).getResult().get()[i2];
                        i2++;
                    }
                    i++;
                }
                for(int j = i1; j < len1; j++){
                    result[i] = tasks.get(0).getResult().get()[j];
                    i++;
                }
                for(int j = i2; j < len2; j++){
                    result[i] = tasks.get(1).getResult().get()[j];
                    i++;
                }
                complete(result);
            });
            spawn(leftArrayTask);
            spawn(rightArrayTask);
        }
        else complete(array);
    }

    public static void main(String[] args) throws InterruptedException {
            WorkStealingThreadPool pool = new WorkStealingThreadPool(20);
            int n = 20; //you may check on different number of elements if you like
            int[] array = new Random().ints(n).toArray();

            MergeSort task = new MergeSort(array);

            CountDownLatch l = new CountDownLatch(1);
            pool.start();
            pool.submit(task);
            task.getResult().whenResolved(() -> {
                //warning - a large print!! - you can remove this line if you wish
                System.out.println(Arrays.toString(task.getResult().get()));
                l.countDown();
            });

            l.await();
            pool.shutdown();
    }
}