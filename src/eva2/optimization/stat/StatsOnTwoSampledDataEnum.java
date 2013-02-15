package eva2.optimization.stat;

public enum StatsOnTwoSampledDataEnum {
	tTestEqLenEqVar, tTestUneqLenEqVar, tTestUneqLenUneqVar, mannWhitney;
	
	public static String[] getInfoStrings(){
		return new String[] {"Two-sampled t-Test with equal sized data sets", 
				"Two-sampled t-Test with unequal sized data sets", 
				"Two-sampled t-Test with unequal data sets and unequal variances", 
				"Two-sampled Mann-Whitney test"}; 
	}
}
