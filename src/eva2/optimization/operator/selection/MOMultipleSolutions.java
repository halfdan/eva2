package eva2.optimization.operator.selection;

import java.util.ArrayList;

/**
 *
 */
public class MOMultipleSolutions {

    public int paretoOptimalSolutions;
    private ArrayList solutions = new ArrayList();
    public int iterations;
    public int sizeDominantSolutions = 0;

    public void add(double[] fit, double[] w) {
        this.solutions.add(new MOSolution(fit, w));
    }

    public void add(MOSolution p) {
        this.solutions.add(p);
    }

    public MOSolution get(int i) {
        return (MOSolution) this.solutions.get(i);
    }

    public Object remove(int i) {
        return this.solutions.remove(i);
    }

    public int size() {
        return this.solutions.size();
    }

    public void reset() {
        this.sizeDominantSolutions = 0;
    }

    public void testDominance(MOMultipleSolutions malta) {
        MOSolution p1;
        for (int i = 0; i < this.size(); i++) {
            p1 = this.get(i);
            for (int j = 0; j < malta.size(); j++) {
                p1.testDominace(malta.get(j));
            }
        }
    }
}
