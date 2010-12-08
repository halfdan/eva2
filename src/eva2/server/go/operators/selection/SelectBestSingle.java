package eva2.server.go.operators.selection;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.populations.Population;
import eva2.tools.math.RNG;


/** Select best individual multiple times if necessary.
 * In case of multiple fitness values the selection
 * critria is selected randomly for each selection event.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 18.03.2003
 * Time: 16:17:10
 * To change this template use Options | File Templates.
 */
public class SelectBestSingle implements InterfaceSelection, java.io.Serializable {

    private boolean     m_ObeyDebsConstViolationPrinciple = true;
    private boolean		excludeSelfAsPartner = true; // try to avoid selecting as a mating partner the individual itself

	public SelectBestSingle() {
    }
	
	public SelectBestSingle(boolean excludeSelfInPartners) {
		this.excludeSelfAsPartner=excludeSelfInPartners;
    }
	
    public SelectBestSingle(SelectBestSingle a) {
        this.m_ObeyDebsConstViolationPrinciple = a.m_ObeyDebsConstViolationPrinciple;
    }

    public Object clone() {
        return (Object) new SelectBestSingle(this);
    }

    /** This method allows an selection method to do some preliminary
     * calculations on the population before selection is performed.
     * For example: Homologeuos mate could compute all the distances
     * before hand...
     * @param population    The population that is to be processed.
     */
    public void prepareSelection(Population population) {
        // nothing to prepare here
    }

    /** This method will select >size< individuals from the given
     * Population.
     * @param population    The source population where to select from
     * @param size          The number of Individuals to select
     * @return The selected population.
     */
    public Population selectFrom(Population population, int size) {
        Population              result = new Population();
        AbstractEAIndividual    tmpIndy = null;
        int                     currentCriteria = 0, critSize;
        double                  currentBestValue;

        if (population.size()==0) return population; // trivial
        critSize = ((AbstractEAIndividual)population.get(0)).getFitness().length;
        result.setTargetSize(size);
        if (this.m_ObeyDebsConstViolationPrinciple) {
            for (int i = 0; i < size; i++) {
                currentCriteria     = RNG.randomInt(0, critSize-1);
                currentBestValue    = Double.POSITIVE_INFINITY;
                tmpIndy = null;
                for (int j = 0; j < population.size(); j++) {
                    if ((!((AbstractEAIndividual)population.get(j)).violatesConstraint()) && (((AbstractEAIndividual)population.get(j)).getFitness(currentCriteria) < currentBestValue)) {
                        currentBestValue = ((AbstractEAIndividual)population.get(j)).getFitness(currentCriteria);
                        tmpIndy = (AbstractEAIndividual)population.get(j);
                    }
                }
                if (tmpIndy == null) {
                    // darn all individuals violate the constraints
                    // so select the guy with the least worst constraint violation
                    for (int j = 0; j < population.size(); j++) {
                        if (((AbstractEAIndividual)population.get(j)).getConstraintViolation() < currentBestValue) {
                            currentBestValue = ((AbstractEAIndividual)population.get(j)).getConstraintViolation();
                            tmpIndy = (AbstractEAIndividual)population.get(j);
                        }
                    }
                }
                result.add(tmpIndy);
            }
        } else {
            for (int i = 0; i < size; i++) {
                currentCriteria     = RNG.randomInt(0, critSize-1);
                currentBestValue    = Double.POSITIVE_INFINITY;
                for (int j = 0; j < population.size(); j++) {
                    if (((AbstractEAIndividual)population.get(j)).getFitness(currentCriteria) < currentBestValue) {
                        currentBestValue = ((AbstractEAIndividual)population.get(j)).getFitness(currentCriteria);
                        tmpIndy = (AbstractEAIndividual)population.get(j);
                    }
                }
                result.add(tmpIndy);
            }
        }
        return result;
    }

    /** This method allows you to select >size< partners for a given Individual
     * @param dad               The already selected parent
     * @param availablePartners  The mating pool.
     * @param size              The number of partners needed.
     * @return The selected partners.
     */
    public Population findPartnerFor(AbstractEAIndividual dad, Population availablePartners, int size) {
    	if (excludeSelfAsPartner) {
    		Population newPartners = availablePartners.filter(new Population().addToPop(dad));
    		if (newPartners.size()==0) { 
    			// no problem if crossover is deactivated
//    			EVAERROR.errorMsgOnce("Warning, no partners available when excluding self (SelectBestSingle.findPartnerFor)");
//    			EVAERROR.errorMsgOnce("Partners where: " + availablePartners.getStringRepresentation());
    			return newPartners;
    		}
    		return this.selectFrom(newPartners, size);
    	} else return this.selectFrom(availablePartners, size);
    }
/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "This selection method will select the single Best individual (n-times if necessary)." +
                "This is a single objective selecting method, it will select in respect to a random criterion.";
    }
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    public String getName() {
        return "Totalitarian Selection";
    }

    /** Toggle the use of obeying the constraint violation principle
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
        return "Toggle the use of Deb's coonstraint violation principle.";
    }
    
    public boolean isExcludeSelfAsPartner() {
		return excludeSelfAsPartner;
	}
	public void setExcludeSelfAsPartner(boolean excludeSelfAsPartner) {
		this.excludeSelfAsPartner = excludeSelfAsPartner;
	}
	public String excludeSelfAsPartnerTipText() {
		return "When employed for partner selection, the partners may be allowed to contain the parents itself or not.";
	}
}

