package eva2.optimization.operator.crossover;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.InterfaceDataTypeBinary;
import eva2.optimization.population.Population;
import eva2.optimization.problems.InterfaceOptimizationProblem;
import eva2.tools.math.RNG;
import java.util.BitSet;

/**
 * This crossover-Method performs an \"intersection\" of the selected Individuals and then tries to improve it by randomly setting Bits to 1
 * It only mates 2 Individuals, not more
 * 
 * @author Alex
 *
 */
public class CM5 implements InterfaceCrossover, java.io.Serializable {
	private InterfaceOptimizationProblem    m_OptimizationProblem;

	public CM5(){

	}

	public CM5(CM5 c){
		this.m_OptimizationProblem = c.m_OptimizationProblem;
	}

    @Override
	public Object clone(){
		return new CM5(this);
	}

    @Override
	public AbstractEAIndividual[] mate(AbstractEAIndividual indy1,
			Population partners) {
		AbstractEAIndividual[]  result = null;
		result      = new AbstractEAIndividual[1];
		if(indy1 instanceof InterfaceDataTypeBinary && partners.getEAIndividual(0) instanceof InterfaceDataTypeBinary){
			BitSet data = ((InterfaceDataTypeBinary) indy1).getBinaryData();
			BitSet data2 = ((InterfaceDataTypeBinary) partners.getEAIndividual(0)).getBinaryData();
			for(int i=0; i<data.size(); i++){
				boolean setBit = data2.get(i);
				data.set(i, setBit);
				if(!setBit && RNG.flipCoin(0.5)){
					data.set(i, true);
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
		return "Combination Method 5";
	}

	public static String globalInfo() {
		return "Intersection with random change";
	}

}
