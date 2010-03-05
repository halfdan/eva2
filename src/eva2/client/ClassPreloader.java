package eva2.client;

import eva2.gui.GenericObjectEditor;

/**
 * This Runnable just requests a number of classes as does the GenericObjectEditor
 * so that they are loaded into the system cache. It can be done at startup time and
 * accelerates later reloading.
 * 
 * @author mkron
 *
 */
public class ClassPreloader implements Runnable {
	String[] clsNames = null;
	private static boolean TRACE=false;
	
	public ClassPreloader(String ... strs) {
		setClassNames(strs);
	}
	
	private void setClassNames(String[] strs) {
		clsNames = strs;
	}

	public void run() {
		if (clsNames !=null) {
			for (int i = 0; i < clsNames.length; i++) {
				if (TRACE) System.out.println("Preloading " + clsNames[i]);
				GenericObjectEditor.getClassesFromClassPath(clsNames[i], null);
			}
		}
	}

}
