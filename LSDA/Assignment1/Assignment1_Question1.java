import java.util.Arrays;

public class Question1 {
    int[] array;

    public static void main(String[] args) {

        // Initializing an array of int with sixteen unordered elements
        int[] ArrayIntegers = {40, 9, 0, 60, 1, 150, 21, 121, 1022, 61, 14, 12, 99, 255, 8, 15};

        //Printing array before being sorted
        System.out.println("Original Array: " + Arrays.toString(ArrayIntegers));

        Question1 sorting = new Question1();
        //Sorting array by using 'sort' method
        sorting.sort(ArrayIntegers);

        //Printing array after being sorted
        System.out.println("Sorted Array: " + Arrays.toString(ArrayIntegers));
    }

    public void sort(int[] array) { // array length must be a power of 2
        this.array = array;
        sort(0, array.length);
    }

    private void sort(int low, int n) {

        if (n > 1) {
            int mid = n >> 1;

            sort(low, mid);
            sort(low + mid, mid);

            combine(low, n, 1);
        }
    }
    private void combine(int low, int n, int st) {

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
