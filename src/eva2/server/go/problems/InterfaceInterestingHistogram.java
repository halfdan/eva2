package eva2.server.go.problems;

import eva2.server.go.operators.postprocess.SolutionHistogram;

/**
 * Target functions may provide an idea which fitness values are 
 * interesting.
 * 
 * @author mkron
 *
 */
public interface InterfaceInterestingHistogram {
	/**
	 * For this specific instance, provide an empty histogram defining in which
	 * area interesting solutions would lie.
	 * 
	 * @return
	 */
	public SolutionHistogram getHistogram();
}
