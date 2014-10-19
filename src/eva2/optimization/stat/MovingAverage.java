package eva2.optimization.stat;


/**
 *
 */
public class MovingAverage {
    private int size = 0;
    private int index = 0;
    private double average;
    private double[] array;
    private boolean overflow = false;

    /**
     *
     */
    public MovingAverage(int size) {
        this.size = size;
        array = new double[size];
    }

    /**
     *
     */
    private MovingAverage(MovingAverage Source) {
        size = Source.size;
        index = Source.index;
        average = Source.average;
        array = Source.array.clone();
        overflow = Source.overflow;
    }

    /**
     *
     */
    public MovingAverage getClone() {
        return new MovingAverage(this);
    }

    /**
     *
     */
    public void add(double value) {
        array[index] = value;
        index++;
        if (index == size) {
            index = 0;
            overflow = true;
        }
        //
        average = 0;
        int tail = index;
        //if (overflow=true)
        if (overflow) {
            tail = size;
        }
        for (int i = 0; i < tail; i++) {
            average += array[i];
        }
        average /= tail;
    }

    /**
     *
     */
    public double getAverage() {
        return average;
    }
}