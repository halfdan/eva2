package javaeva.server.go.operators.selection;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 24.02.2005
 * Time: 11:31:58
 * To change this template use File | Settings | File Templates.
 */
public class MOMultipleSolutions {
    
    public int          m_ParetoOptimalSolutions;
    private ArrayList   m_Solutions = new ArrayList();
    public int          m_Iterations;
    public int          m_SizeDominantSolutions = 0;

    public void add(double[] fit, double[] w) {
        this.m_Solutions.add(new MOSolution(fit, w));
    }

    public void add(MOSolution p) {
        this.m_Solutions.add(p);
    }

    public MOSolution get(int i) {
        return (MOSolution)this.m_Solutions.get(i);
    }

    public Object remove(int i) {
        return this.m_Solutions.remove(i);
    }

    public int size() {
        return this.m_Solutions.size();
    }

    public void reset() {
        this.m_SizeDominantSolutions = 0;
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
