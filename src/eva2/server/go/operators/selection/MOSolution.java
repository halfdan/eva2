package eva2.server.go.operators.selection;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 24.02.2005
 * Time: 16:29:35
 * To change this template use File | Settings | File Templates.
 */
public class MOSolution {

    public double[]     fitness;
    public double[]     weights;
    public boolean      isDominant = true;

    public MOSolution(double[] fit, double[] w) {
        this.fitness = fit;
        this.weights = w;
    }

    public void testDominace(MOSolution p) {
        if ((p.fitness[0] < this.fitness[0]) && (p.fitness[1] < this.fitness[1])) {
            this.isDominant &= false;
        }
        if ((this.fitness[0] < p.fitness[0]) && (this.fitness[1] < p.fitness[1])) {
            p.isDominant &= false;
        }
    }


}