package eva2.optimization.enums;

public enum StatisticsOnSingleDataSet {
    mean, median, variance, stdDev;

    public static String[] getInfoStrings() {
        return new String[]{"The mean value", "The median value", "The variance", "The standard deviation"};
    }
}
