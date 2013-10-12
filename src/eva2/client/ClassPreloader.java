package eva2.client;

import eva2.gui.editor.GenericObjectEditor;

/**
 * This Runnable just requests a number of classes as does the
 * GenericObjectEditor so that they are loaded into the system cache. It can be
 * done at startup time and accelerates later reloading.
 *
 * @author mkron
 */
public class ClassPreloader implements Runnable {

    /**
     * List of classes to load when ClassPreloader is started.
     */
    private String[] classNames = null;

    /* Creates a new ClassPreloader and sets the
     * list of classes to load.
     *
     * @param classes List of classes to load
     */
    public ClassPreloader(final String... classes) {
        this.classNames = classes;
    }

    /**
     * Load classes via GenericObjectEditor in a thread.
     */
    @Override
    public void run() {
        if (classNames != null) {
            for (int i = 0; i < classNames.length; i++) {
                GenericObjectEditor.getClassesFromClassPath(classNames[i], null);
            }
        }
    }
}
