
public class AltArray<T> {

    T[] data;
    int size;

    AltArray(T[] initialElements, int initialSize) {
        this.data = initialElements;
        this.size = initialSize;
    }

    void clear() {
        this.size = 0;
    }

    void add(T element) {
        this.data[size] = element;
        size++;
    }

    T get(int index) {
        return this.data[index];
    }

}
