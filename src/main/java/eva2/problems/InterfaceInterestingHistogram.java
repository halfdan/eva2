package eva2.problems;

import eva2.optimization.operator.postprocess.SolutionHistogram;

/**
 * Target functions may provide an idea which fitness values are
 * interesting.
 */
public interface InterfaceInterestingHistogram {
    /**
     * For this specific instance, provide an empty histogram defining in which
     * area interesting solutions would lie.
     *
     * @return
     */
    SolutionHistogram getHistogram();
}
