
public class AltDoubleArray {

    double[] data;
    int size;

    AltDoubleArray(double[] initialElements, int initialSize) {
        this.data = initialElements;
        this.size = initialSize;
    }

    void clear() {
        this.size = 0;
    }

    void add(int element) {
        this.data[size] = element;
        size++;
    }

    double get(int index) {
        return this.data[index];
    }

}
