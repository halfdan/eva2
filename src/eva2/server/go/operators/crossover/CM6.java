package eva2.server.go.operators.crossover;

import java.util.BitSet;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.InterfaceDataTypeBinary;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.server.go.strategies.BinaryScatterSearch;
import eva2.tools.math.RNG;

/**
 * Score driven Crossover-Method. It uses the same score as the BinaryScatterSearch.
 * Only the first individual of the given Population in the mate method is used. the rest is only for calculating the score
 * It only mates 2 Individuals, not more
 * 
 * @author Alex
 *
 */
public class CM6 implements InterfaceCrossover, java.io.Serializable {
	private InterfaceOptimizationProblem    m_OptimizationProblem;

	public CM6(){

	}

	public CM6(CM6 c){
		this.m_OptimizationProblem = c.m_OptimizationProblem;
	}

    @Override
	public Object clone(){
		return new CM6(this);
	}

    @Override
	public AbstractEAIndividual[] mate(AbstractEAIndividual indy1,
			Population partners) {
		AbstractEAIndividual[]  result = null;
		result      = new AbstractEAIndividual[1];
		if(indy1 instanceof InterfaceDataTypeBinary && partners.getEAIndividual(0) instanceof InterfaceDataTypeBinary){
			BitSet data = ((InterfaceDataTypeBinary) indy1).getBinaryData();
			BitSet data2 = ((InterfaceDataTypeBinary) partners.getIndividual(0)).getBinaryData();
			for(int j=0; j<data2.size(); j++){
				if(data2.get(j)){
					data.set(j, true);
				}
			}
			partners.remove(0);
			for(int i=0; i<data.size(); i++){
				if(!(data.get(i) && RNG.flipCoin(Math.min(0.1+BinaryScatterSearch.score(i, partners),1)))){
					data.set(i, false);
				}
			}
			((InterfaceDataTypeBinary) indy1).SetBinaryGenotype(data);
		}
		result[0]=indy1;
		return result;
	}

    @Override
	public void init(AbstractEAIndividual individual,
			InterfaceOptimizationProblem opt) {
		this.m_OptimizationProblem = opt;
	}

    @Override
	public String getStringRepresentation() {
		return getName();
	}

	/*****************************************************
	 * GUI
	 */

	public String getName(){
		return "Combination Method 6";
	}

	public static String globalInfo() {
		return "score driven crossover method";
	}

}
