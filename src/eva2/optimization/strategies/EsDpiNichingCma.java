package eva2.optimization.strategies;

import eva2.optimization.operator.selection.SelectBestSingle;

public class EsDpiNichingCma extends EsDpiNiching {

    /**
     * Preset all values according to the (1,lambda)-ES. This activates the automatic niche radius estimation
     * (which frequently overestimates) and expects 10 peaks.
     */
    public EsDpiNichingCma() {
        this(10, 10, 0, 0);
    }

    /**
     * Preset all values according to the (1,lambda)-ES
     */
    public EsDpiNichingCma(double nicheRadius, int lambda, int expectedPeaks) {
        this(nicheRadius, lambda, expectedPeaks, 0, 0);
    }

    /**
     * Preset all values according to the (1,lambda)-ES
     */
    public EsDpiNichingCma(int lambda, int expectedPeaks, int explorerPeaks, int resetExplInterval) {
        this(-1, lambda, expectedPeaks, explorerPeaks, resetExplInterval);
    }

    /**
     * Preset all values according to the (1,lambda)-ES
     */
    public EsDpiNichingCma(double nicheRadius, int lambda, int expectedPeaks, int explorerPeaks, int resetExplInterval) {
        super(nicheRadius, 1, lambda, expectedPeaks, 0, explorerPeaks, resetExplInterval, 0);
        setParentSelection(new SelectBestSingle());
        setAllowSingularPeakPops(true);
    }

    @Override
    public String getName() {
        return "CMA-" + super.getName();
    }

    public static String globalInfo() {
        return "A variant of the DPI Niching ES to be usable with CMA-Mutation (Shir&Bäck, CEC'05). " +
                "Remember to turn off crossover for lambda=1, and to set CMA as mutation in the individual template.";
    }
}
