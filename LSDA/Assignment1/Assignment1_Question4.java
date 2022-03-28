import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Question4{
    int[] array;

    public static void main(String[] args) {

        //Test1
        // Initializing an array of int with thirty two unordered elements
        int[] ArrayIntegers = {40, 9, 0, 60, 1, 150, 21, 121, 1022, 61, 14, 12, 99, 255, 8, 15,
                                11, 19, 40, 600, 51, 20, 302, 10, 44, 100, 23, 56, 1000, 5, 3, 98 };

        //Printing array before being sorted
        System.out.println("Original Array: " + Arrays.toString(ArrayIntegers));

        Question4 sorting = new Question4();
        //Sorting array by using 'sort' method
        sorting.sort(ArrayIntegers);

        //Printing array after being sorted
        System.out.println("Sorted Array: " + Arrays.toString(ArrayIntegers));

        //Test2
        // Initializing an array of int with sixteen unordered elements
        int[] ArrayIntegers2 = {28,10,6,0,44,99,23,45,19,30,33,45,67,21,57,32};

        //Printing array before being sorted
        System.out.println("Original Array: " + Arrays.toString(ArrayIntegers2));

        //Sorting array by using 'sort' method
        sorting.sort(ArrayIntegers2);

        //Printing array after being sorted
        System.out.println("Sorted Array: " + Arrays.toString(ArrayIntegers2));
    }

    public void sort(int[] array) { // array length must be a power of 2
        this.array = array;
        sort(0, array.length);
    }

    private void sort(int low, int n){

        if (n > 1) {
            int mid = n >> 1;

            //Initializing first Thread that will execute sort(low,mid)
            Thread worker1 = new Thread(){
                public void run(){
                    sort(low, mid);
                }
            };


            //Initializing second Thread that will execute sort(low + mid, mid)
            Thread worker2 = new Thread(){
                public void run(){
                    sort(low + mid, mid);
                }
            };

            //Starting both threads
            worker1.start();
            worker2.start();

            //Waiting for threads to finish execution before calling combine
            try {
                worker1.join();
                worker2.join();
            }
            catch (Exception e) {
                System.out.println(e);
            }
            combine(low, n, 1);

        }
    }

    private synchronized void combine(int low, int n, int st) {

        int m = st << 1;

        if (m < n) {
            combine(low, n, m);
            combine(low + st, n, m);

            for (int i = low + st; i + st < low + n; i += m)
                compareAndSwap(i, i + st);

        } else
            compareAndSwap(low, low + st);
    }

    private void compareAndSwap(int i, int j) {
        if (array[i] > array[j])
            swap(i, j);
    }

    private void swap(int i, int j) {
        int h = array[i];
        array[i] = array[j];
        array[j] = h;
    }

}
