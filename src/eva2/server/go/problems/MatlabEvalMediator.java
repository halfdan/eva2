package eva2.server.go.problems;

import eva2.gui.BeanInspector;

/**
 * This implements a thread acting as a mediator between EvA and Matlab. Thanks to the idea 
 * of Henning Schmidt!
 * As Java calling Matlab directly causes problems (due to Matlabs single-threadedness), Matlab
 * now starts a mediator thread which receives the data necessary to perform the evaluation in matlab
 * from the optimization thread. On receiving this "question" from the optimizer, the mediator thread
 * finishes, returning to Matlab and signaling that there is work to do. 
 * However, the mediator object remains persistent, and the optimization thread keeps running 
 * and waits for the mediator to signal that there is a result, which happens if Matlab calls setAnswer().
 * Then the optimizer thread resumes, while Matlab has to restart the mediator thread, so that it may
 * be informed about the next question, and so on. I havent checked how much performance is lost compared
 * to the earlier, asynchronous version, but it feels similar, a difference being that both cpu's
 * are now at 100% load, which is because two threads are running (and always at least waiting actively).
 * Adding sleep time reduces CPU load a lot but reduces efficiency badly at the same time, probably because
 * theres so much going on. For cases where the evaluation function is very time-consuming, adding sleep time
 * might be an option.
 *   
 * @author mkron
 *
 */
public class MatlabEvalMediator implements Runnable {
	volatile boolean requesting = false;
//	final static boolean TRACE = false;
	volatile boolean fin = false;
	volatile Object question = null;
	volatile double[] answer = null;
	boolean quit = false;
	volatile Object optSolution = null;
	volatile Object[] optSolSet = null;
	MatlabProblem mp = null;
	// no good: even when waiting for only 1 ms the Matlab execution time increases by a factor of 5-10
	final static int sleepTime = 5;

	/**
	 * Request evaluation from Matlab for the given params.
	 * 
	 * @param x
	 * @return
	 */
	double[] requestEval(MatlabProblem mp, Object x) {
		this.mp = mp;
		question = x;
//		System.err.println("IN REQUESTEVAL, x is " + BeanInspector.toString(x));
		if (question.getClass().isArray()) {
//			System.err.println("array of type ** " + Array.get(question, 0).getClass().toString());
//		} else if (question instanceof BitSet){
//			BitSet b = (BitSet)x;
//			Integer.decode()
//			
			if (question == null) System.err.println("Error: requesting evaluation for null array!");
		} else System.err.println("Error, requesting evaluation for non array!"); 
		
		requesting = true;
//		int k=0;
		mp.log("-- Requesting eval for " + BeanInspector.toString(x) + ", req state is " + requesting + "\n"); 
		while (requesting && !quit) {
			// 	wait for matlab to answer the question
			if (sleepTime > 0) try { Thread.sleep(sleepTime); } catch(Exception e) {};
//			if ((k%100)==0) {
//				System.out.println("waiting for matlab to answer...");
//			}
//			k++;
		}
		mp.log("-- Requesting done\n");
		// matlab is finished, answer is here
		//return null;
		return getAnswer(); // return to JE with answer
	}

	/**
	 * Wait loop, wait until the MatlabProblem requests an evaluation (or finishes), then return.
	 */
	public void run() {
//		int k=0;
		while (!requesting && !isFinished() && !quit) {
			// wait for JE to pose a question or finish all
			if (sleepTime > 0) try { Thread.sleep(sleepTime); } catch(Exception e) {};
//			if ((k%100)==0) {
//				System.out.println("waiting for JE to ask...");
//			}
//			k++;
		}
//		System.out.println("-- Request arrived in MP thread\n");
		// requesting is true, now finish and let Matlab work
	}

	/**
	 * Cancel waiting in any case.
	 */
	public void quit() {
//		System.out.println("IN QUIT!");
		quit = true;
	}
	
	/**
	 * To be called from Matlab.
	 * @return
	 */
	public Object getQuestion() {
		mp.log("-- Question: " + BeanInspector.toString(question) + "\n");
		return question;
	}

	double[] getAnswer() {
		mp.log("-- mediator delivering " + BeanInspector.toString(answer) + "\n");
		return answer;
	}

	/**
	 * To be called from Matlab giving the result of the question.
	 * 
	 * @param y
	 */
	public void setAnswer(double[] y) {
//		System.err.println("answer is " + BeanInspector.toString(y)); 
		if (y==null) {
			System.err.println("Error: Matlab function returned null array - this is bad.");
			System.err.println("X-value was " + BeanInspector.toString(getQuestion()));
		}
		answer = y;
		requesting = false; // answer is finished, break request loop
		mp.log("-- setAnswer: " + BeanInspector.toString(y) + ", req state is " + requesting + "\n"); 
	}

	void setFinished(boolean val) {
		fin = val;
	}

	/**
	 * To be called from Matlab signalling when optimizaton is completely finished.
	 * @return
	 */
	public boolean isFinished() {
		return fin;
	}
	
	void setSolution(Object sol) {
		//System.out.println("setting Sol");
		optSolution = sol;
	}
	
	void setSolutionSet(double[][] solSet) {
//		System.err.println("setting SolSet " + ((solSet != null) ? solSet.length : 0));
		optSolSet = solSet;
	}
	
	void setSolutionSet(int[][] solSet) {
//		System.err.println("setting SolSet " + ((solSet != null) ? solSet.length : 0));
		optSolSet = solSet;
	}
	
	/**
	 * Matlab may retrieve result.
	 * @return
	 */
	public Object getSolution() {
//		System.err.println("getting Sol");
		return optSolution;
	}
	
	/**
	 * Matlab may retrieve result as Object[] containing either double[] or int[].
	 * @return
	 */
	public Object[] getSolutionSet() {
//		System.err.println("getting SolSet " + ((optSolSet != null) ? optSolSet.length : 0));
		return optSolSet;
	}
}