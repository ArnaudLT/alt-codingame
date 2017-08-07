import java.util.Random;

public class Utils {

    static final Random rnd = new Random(777);

    static double min(double a, double b) {
        return (a <= b) ? a : b;
    }

    static double max(double a, double b) {
        return (a >= b) ? a : b;
    }

    static double min(int a, int b) {
        return (a <= b) ? a : b;
    }

    static int max(int a, int b) {
        return (a >= b) ? a: b;
    }

    // ================================================
    //                    ARRAYS
    // ================================================

    static void shuffleArray(int[] array, int size) {
        int index;
        for (int i = size - 1; i > 0; i--) {
            index = rnd.nextInt(i + 1);
            if (index != i) {
                array[index] ^= array[i];
                array[i] ^= array[index];
                array[index] ^= array[i];
            }
        }
    }

    static double mean(int[] array, int size) {
        double sum = 0d;
        for (int i=0; i<size; i++) {
            sum += array[i];
        }
        return sum / size;
    }

    static double mean(double[] array, int size) {
        double sum = 0d;
        for (int i=0; i<size; i++) {
            sum += array[i];
        }
        return sum / size;
    }

    static int max(int[] array, int size) {
        int max = array[0];
        for (int i=1; i<size; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }
        return max;
    }

    static double max(double[] array, int size) {
        double max = array[0];
        for (int i=1; i<size; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }
        return max;
    }

    static int maxIndex(int[] array, int size) {
        int max = array[0];
        int maxIndex = 0;
        for (int i=1; i<size; i++) {
            if (array[i] > max) {
                max = array[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    static int maxIndex(double[] array, int size) {
        double max = array[0];
        int maxIndex = 0;
        for (int i=1; i<size; i++) {
            if (array[i] > max) {
                max = array[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    static int min(int[] array, int size) {
        int min = array[0];
        for (int i=1; i<size; i++) {
            if (array[i] < min) {
                min = array[i];
            }
        }
        return min;
    }

    static double min(double[] array, int size) {
        double min = array[0];
        for (int i=1; i<size; i++) {
            if (array[i] < min) {
                min = array[i];
            }
        }
        return min;
    }

    static int minIndex(int[] array, int size) {
        int min = array[0];
        int minIndex = 0;
        for (int i=1; i<size; i++) {
            if (array[i] < min) {
                min = array[i];
                minIndex = i;
            }
        }
        return minIndex;
    }

    static int minIndex(double[] array, int size) {
        double min = array[0];
        int minIndex = 0;
        for (int i=1; i<size; i++) {
            if (array[i] < min) {
                min = array[i];
                minIndex = i;
            }
        }
        return minIndex;
    }

}
