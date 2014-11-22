package eva2.optimization;

import eva2.problems.InterfaceAdditionalPopulationInformer;

import java.util.List;

public interface InterfaceNotifyOnInformers {
    /**
     * Notify the object about informer instances.
     */
    public void setInformers(List<InterfaceAdditionalPopulationInformer> informers);
}
