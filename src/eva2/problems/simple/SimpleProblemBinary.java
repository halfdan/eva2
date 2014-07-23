package eva2.problems.simple;

import java.io.Serializable;
import java.util.BitSet;

public abstract class SimpleProblemBinary implements InterfaceSimpleProblem<BitSet>, Serializable {
    public static String globalInfo() {
        return "A simple binary problem. Override globalInfo() to insert more information.";
    }
}