package simpleprobs;

import java.io.Serializable;

public abstract class SimpleProblemDouble implements InterfaceSimpleProblem<double[]>, Serializable {
	public static String globalInfo() {
		return "A simple double valued problem. Override globalInfo() to insert more information.";
	}
}
