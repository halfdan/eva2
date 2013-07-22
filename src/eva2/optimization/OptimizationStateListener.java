/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eva2.optimization;

/**
 * @author becker
 */
public interface OptimizationStateListener {
    void performedStop();

    void performedStart(String infoString);

    void performedRestart(String infoString);

    void updateProgress(final int percent, String msg);
}
