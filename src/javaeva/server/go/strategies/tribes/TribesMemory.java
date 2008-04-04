package javaeva.server.go.strategies.tribes;

public class TribesMemory implements java.io.Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = -322241030553753023L;
	private TribesPosition position; // Memorized position
    private TribesPosition positionPrev; // Previous position
    int status; /*
        0  =>pas d'amÃ©lioration
    1 => amÃ©lioration
    */
   int label;
    public TribesMemory(int problemDim) {
    	position = new TribesPosition(problemDim);
    	positionPrev = new TribesPosition(problemDim);
    }

//    private void print(String string, out output) {
//        output.out.append(string);
//    }

    public TribesPosition getPos() {
    	return position;
    }

    public void setPos(TribesPosition pos) {
    	position = pos;
    }
    
    public void setPrevPos(TribesPosition ppos) {
    	positionPrev = ppos;
    }
    
    public TribesExplorer asDummyExplorer(double[][] range, double objFirstDim) {
    	TribesExplorer exp = new TribesExplorer(range, objFirstDim);
    	exp.position = getPos().clone();
        exp.contact = -1;
        exp.positionT_2 = null;
        exp.positionT_1 = null;
        exp.velocity = new TribesPosition(range.length);
        exp.strategy = -1;
        exp.status = -1;
        exp.iGroupNb = -1;

    	return exp;
    }
    
    public TribesPosition getPrevPos() {
    	return positionPrev;
    }
    
    public TribesMemory clone() {
    	TribesMemory clone = new TribesMemory(position.x.length);
        clone.status = status;
        clone.position = position.clone();
        clone.positionPrev = positionPrev.clone();
        return clone;
    }


    public int statusMemory(TribesMemory memory,int functionNb,int fitnessSize) {
        /* On cherche un minimum, donc amÃ©lioration
         si la valeur de la position a diminuÃ©
         */

        if (position.firstIsBetter(position.getFitness(),positionPrev.getFitness())) {
           return 1;
        }
        return 0;
    }

//    public void displayMemory(/*out out*/) {
//        int d;
//        
//        System.out.println("\n Status " + status);
//        System.out.println("\n totalError " + position.getTotalError());
//        System.out.println("\nposition (dimension " + position.x.length + ")\n ");
//        for (d = 0; d < position.x.length; d++) {
//        	System.out.println(position.x[d] + " ");
//        }                
//      print("\n Status " + status, out);
//      print("\n totalError " + position.totalError, out);
//      print("\nposition (dimension " + position.Dimension + ")\n ", out);
//      for (d = 0; d < position.Dimension; d++) {
//          print(position.x[d] + " ", out);
//      }
//    }
}
