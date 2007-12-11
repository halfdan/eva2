package javaeva.server.go.problems;

/**
 * An interface for optimization problems having an extra initial range
 * opposed to the all-over problem range. 
 * 
 * TODO generalize this!
 * 
 * @author mkron
 *
 */
public interface InterfaceHasInitRange {
	public double[][] getInitRange();
}
