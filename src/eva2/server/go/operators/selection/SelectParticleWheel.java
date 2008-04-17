package eva2.server.go.operators.selection;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.operators.selection.probability.InterfaceSelectionProbability;
import eva2.server.go.operators.selection.probability.SelProbBoltzman;
import eva2.server.go.operators.selection.probability.SelProbStandard;
import eva2.server.go.populations.Population;
import wsi.ra.math.RNG;


/** This method implements the roulette wheel selection for
 * a partical filter. In case of multiple fitness values the selection
 * critria should be selected randomly for each selection event.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 18.03.2003
 * Time: 16:36:11
 * To change this template use Options | File Templates.
 */
public class SelectParticleWheel implements InterfaceSelection, java.io.Serializable {
    
    private boolean                 m_ObeyDebsConstViolationPrinciple = true;
    /**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;
    private InterfaceSelectionProbability   m_SelProbCalculator = new SelProbStandard();

    public SelectParticleWheel() {
    }

    public SelectParticleWheel(SelectParticleWheel a) {
        this.m_SelProbCalculator    = (InterfaceSelectionProbability)a.m_SelProbCalculator.clone();
        this.m_ObeyDebsConstViolationPrinciple = a.m_ObeyDebsConstViolationPrinciple;
    }

    public Object clone() {
        return (Object) new SelectParticleWheel(this);
    }
    
    public void prepareSelection(Population population) {
    	m_SelProbCalculator.computeSelectionProbability(population, "Fitness", m_ObeyDebsConstViolationPrinciple);
    }

    /**
     * This method will select individuals from the given Population with respect to their 
     * selection propability. This implements a fixed segment roulette wheel selection which ensures
     * that every individual which has a selection probability p >= (k/size) is selected k or k+1 times.
     * 
     * @param population    The source population where to select from
     * @param size          The number of Individuals to select
     * @return The selected population.
     */
    public Population selectFrom(Population population, int size) {
        Population result = new Population();
        result.setPopulationSize(size);
        
        // use a fixed segment roulette wheel selection
        double segment = 1./(size+1);
        double selPoint = RNG.randomDouble(0., segment);
        
        int selIndex = 0;
        double selFitSum = ((AbstractEAIndividual)population.getIndividual(selIndex)).getSelectionProbability(0);
        
        for (int i=0; i < size; i++) {
        	while (selFitSum < selPoint) {
        		selIndex++;
        		selFitSum += ((AbstractEAIndividual)population.getIndividual(selIndex)).getSelectionProbability(0);
        	}
        	result.add(((AbstractEAIndividual)population.get(selIndex)).clone());
        	((AbstractEAIndividual)result.getIndividual(i)).SetAge(0);
        	selPoint += segment;
        }
        
////	Unfortunately, this was really problem specific (mk)
//
//        double overallFit=0.0;
//    
//        for (int i = 0; i < size; i++) {
//        	overallFit += 33.0-((AbstractEAIndividual)population.get(i)).getFitness(0);
//        }
//        overallFit/=(double)size;
//
//        double momMark=0.0;
//        double momFitSum=0.0;
//        
//        for (int i = 0; i < size; i++) {
//        	momFitSum += 33.0-((AbstractEAIndividual)population.get(i)).getFitness(0);
//        	
//        	while (momFitSum > momMark) {
//        		result.add(((AbstractEAIndividual)population.get(i)));
//        		momMark+=overallFit;        		
//        	}
//        	
//        }
       	return result;
    }

    /** This method allows you to select partners for a given Individual
     * @param dad               The already seleceted parent
     * @param avaiablePartners  The mating pool.
     * @param size              The number of partners needed.
     * @return The selected partners.
     */
    public Population findPartnerFor(AbstractEAIndividual dad, Population avaiablePartners, int size) {
        return this.selectFrom(avaiablePartners, size);
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "Particle Wheel Selection";
    }

    /** This method returns a global info string
     * @return description
     */
    public String globalInfo() {
        return "This method chooses individuals similar to the static roulette wheel. The chance for each individual to be selected depends on the selection probability. The selection probability is 1 for all Individuals with a fitness that is bigger than the midean fitness." +
                "This is a single objective selecting method, it will select in respect to a random criteria.";
    }

    /** Toggel the use of obeying the constraint violation principle
     * of Deb
     * @param b     The new state
     */
    public void setObeyDebsConstViolationPrinciple(boolean b) {
        this.m_ObeyDebsConstViolationPrinciple = b;
    }
    public boolean getObeyDebsConstViolationPrinciple() {
        return this.m_ObeyDebsConstViolationPrinciple;
    }
    public String obeyDebsConstViolationPrincipleToolTip() {
        return "Toggle the use of Deb's coonstraint violation principle(todo).";
    }
}