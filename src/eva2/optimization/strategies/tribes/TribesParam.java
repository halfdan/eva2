package eva2.optimization.strategies.tribes;

/**
 * This class for now is used only for keeping the door open to the original
 * constraint handling, still it is deactivated as of now. 
 */
public class TribesParam implements java.io.Serializable {
/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//    double accuracy;
//    int[] function = new int[Tribes.maxFunctionNb];
//    int functionNb;
//    int maxEval;
//    double objective;
//    int maxRun;
//    int initType;
    // 0 => dans tous l'espace de recherche,
    // 1 => dans un sous-espace (cf H.xInitMin et H.xInitMax)
    //TribesSearchSpace H;
    int gNb; // Number of g constraints  (<=0)
    int hNb; // Number of h constraints (<ups)
    double ups; // Tolerance for h constraints
    boolean constraint;
//    int fitnessSize;
    
    public TribesParam() {
    //	H = new TribesSearchSpace();
    	constraint = false;
    }
}