import java.util.Arrays;
import java.util.Comparator;

//Modifying class to include generic type
public class Question3<T> {
    //Initializing array of generic type
    T[] array;
    //Initializing comparator of generic type
    Comparator<T> comp;

    public static void main(String[] args) {

        //Creating comparator instance for an object of type Double using a Lambda expression
        Comparator<Double> compDouble = (a, b) -> {
            if (a.compareTo(b)>0) {
                return 1;
            } else {
                return 0;
            }
        };

        Question3<Double> sortingDouble = new Question3();
        // Initializing an array of numbers type Double with sixteen unordered elements
        Double[] arrayDoubleType = {11.5, 11.1, 5.5, 100.6, 199.1, 199.2, 11.4, 4.5,
                82.7, 76.7, 12.3, 1455.5, 76.2, 11.4, 82.2, 15.5};

        //Printing array before being sorted
        System.out.println("Original Array Double: " + Arrays.toString(arrayDoubleType));

        //Sorting array by using 'sort' method and comparator CompDouble
        sortingDouble.sort(arrayDoubleType,compDouble);

        //Printing array after being sorted
        System.out.println("Sorted Array Double: " + Arrays.toString(arrayDoubleType));

        //Creating comparator instance for a object of type String using a Lambda expression
        Comparator<String> compString= (a, b) -> {
            if (a.compareTo(b)>0) {
                return 1;
            } else {
                return 0;
            }
        };

        Question3<String> sortingArray = new Question3();
        // Initializing an array of Strings with sixteen unordered elements
        String[] arrayStringType = {"Pomegranate","Grape","Banana","Apple","Watermelon","Orange","Strawberry","Cherry",
                "Raspberry","Apricot","Papaya","Kiwi","Pineapple","Lemon","Blueberry","Lychee"};

        //Printing array before being sorted
        System.out.println("Original Array String: " + Arrays.toString(arrayStringType));

        //Sorting array by using 'sort' method and comparator CompString
        sortingArray.sort(arrayStringType,compString);

        //Printing array after being sorted
        System.out.println("Sorted Array String: " + Arrays.toString(arrayStringType));

    }
    //Comparator instance added as a parameter of sort
    public void sort(T[] array,Comparator<T> comp) { // array length must be a power of 2
        this.array = array;
        this.comp = comp;
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

    private void compareAndSwap(int i, int j){
        //compareAndSwap uses now a comparator instance of a specific type to compare two elements
        if (comp.compare(array[i], array[j]) > 0){
            swap(i, j);
        }
    }

    private void swap(int i, int j) {
        T h = array[i];
        array[i] = array[j];
        array[j] = h;
    }
}
