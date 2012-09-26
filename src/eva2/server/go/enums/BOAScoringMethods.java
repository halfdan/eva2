package eva2.server.go.enums;

public enum BOAScoringMethods {
	BDM, K2, BIC;
	
	public static String[] getInfoStrings(){
		return new String[] {"The Bayesian Dirichlet Metric", "The K2 Metric", "The Bayesian Information Criterion"};
	}
}