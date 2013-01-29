package eva2.server.go.operators.crossover;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.InterfaceDataTypeBinary;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;
import java.util.BitSet;

/**
 * calculates a weight based on the fitnessValues and the configuration of each bit from the two individuals and use it as a probability to set the bit
 * It only mates 2 Individuals, not more
 * 
 * @author Alex
 *
 */
public class CM3 implements InterfaceCrossover, java.io.Serializable {
	private InterfaceOptimizationProblem    m_OptimizationProblem;

	public CM3(){

	}

	public CM3(CM3 c){
		this.m_OptimizationProblem = c.m_OptimizationProblem;
	}

    @Override
	public Object clone(){
		return new CM3(this);
	}

	private int convertBoolean(boolean b){
		if(b){
			return 1;
		}else{
			return 0;
		}
	}

	private double weight(double valX, boolean xi, double valY, boolean yi){
		double result = valX*convertBoolean(xi)+valY*convertBoolean(yi);
		result /= (valX + valY);
		return result;
	}

    @Override
	public AbstractEAIndividual[] mate(AbstractEAIndividual indy1,
			Population partners) {
		AbstractEAIndividual[]  result = null;
		result      = new AbstractEAIndividual[1];
		if(indy1 instanceof InterfaceDataTypeBinary && partners.getEAIndividual(0) instanceof InterfaceDataTypeBinary){
			BitSet data = ((InterfaceDataTypeBinary) indy1).getBinaryData();
			BitSet data2 = ((InterfaceDataTypeBinary) partners.getEAIndividual(0)).getBinaryData();
			for(int j=0; j<data.size(); j++){
				double w = weight(indy1.getFitness(0), data.get(j), partners.getEAIndividual(0).getFitness(0), data2.get(j));
				if(RNG.flipCoin(w)){
					data.set(j, true);
				}else{
					data.set(j, false);
				}
			}
			((InterfaceDataTypeBinary) indy1).SetBinaryGenotype(data);
		}
		result[0] = indy1;
		return result;
	}

    @Override
	public void init(AbstractEAIndividual individual,
			InterfaceOptimizationProblem opt) {
		this.m_OptimizationProblem = opt;
	}

    @Override
	public String getStringRepresentation() {
		return this.getName();
	}

	/*****************************************************
	 * GUI
	 */

	public String getName(){
		return "Combination Method 3";
	}

	public static String globalInfo() {
		return "Weight driven crossover method";
	}

}
