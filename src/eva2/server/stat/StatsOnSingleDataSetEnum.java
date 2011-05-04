package eva2.server.stat;

public enum StatsOnSingleDataSetEnum {
	mean, median, variance, stdDev;
	
	public static String[] getInfoStrings(){
		return new String[] {"The mean value", "The median value", "The variance", "The standard deviation"}; 
	}
}
