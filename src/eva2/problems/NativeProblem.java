package eva2.problems;

/**
 *
 */
public class NativeProblem extends AbstractProblemDouble {

    private static boolean isLibraryLoaded = false;

    public NativeProblem() {
        if (!isLibraryLoaded) {
            System.loadLibrary("eva2problem");
        }
        isLibraryLoaded = true;
    }

    @Override
    public native double[] evaluate(double[] x);

    @Override
    public native String getName();

    @Override
    public Object clone() {
        return this.clone();
    }
}
