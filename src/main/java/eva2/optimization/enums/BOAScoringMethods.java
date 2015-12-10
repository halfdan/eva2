package eva2.optimization.enums;

public enum BOAScoringMethods {
    BDM, K2, BIC;

    public static String[] getInfoStrings() {
        return new String[]{"The Bayesian Dirichlet Metric", "The K2 Metric", "The Bayesian Information Criterion"};
    }
}