package eva2.gui;
import java.util.ArrayList;
/*
 * Title:        EvA2
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 10 $
 *            $Date: 2006-01-18 11:02:22 +0100 (Wed, 18 Jan 2006) $
 *            $Author: streiche $
 */


public class JEFrameRegister {
	private static ArrayList<JEFrame> JEFrameList;

	static {
		JEFrameList = new ArrayList<JEFrame>();
	}

	public static void register(JEFrame jf) {
		if (!JEFrameList.contains(jf)) JEFrameList.add(jf);
//		System.out.println("reg  " + jf.getTitle() + "/" + (jf.hashCode()) + ", list size: " + JEFrameList.size());
	}

	public static void unregister(JEFrame jf) {
		JEFrameList.remove(jf); // Plot windows produce double closing events, so ignore it
//		if (!JEFrameList.remove(jf)) System.err.println("Warning: tried to unregister frame " + jf.getTitle() + " which was not registered! (JEFrameRegister)");
//		System.out.println("unreg " + jf.getTitle() + "/" + jf.hashCode() + ", list size:" + JEFrameList.size());
	}

	public static Object[] getFrameList() {
		return JEFrameList.toArray();
	}

	public static void setFocusToNext(JEFrame jf) {
		int idx = JEFrameList.indexOf(jf);
		idx = (idx + 1) % JEFrameList.size();
		JEFrame toset =    ((JEFrame) JEFrameList.get(idx));
		toset.setExtendedState(JEFrame.NORMAL);
		toset.toFront();
	}

	/**
	 * Return all prefixes which occur at least twice in the registered frame list.
	 * @param prefLen
	 * @return
	 */
	public static String[] getCommonPrefixes(int prefLen) {
		ArrayList<String> prefixes = new ArrayList<String>();
		ArrayList<Integer> count = new ArrayList<Integer>();
		for (int i=0; i<JEFrameList.size(); i++) {
			String title = JEFrameList.get(i).getTitle();
			String titPref = title.substring(0, Math.min(prefLen, title.length()));
			int earlierIndex = prefixes.indexOf(titPref);
			if (earlierIndex<0) {
				prefixes.add(titPref);
				count.add(1);
			} else count.set(earlierIndex, 1+count.get(earlierIndex));
		}
		for (int i=prefixes.size()-1; i>=0; i--) {
			if (count.get(i)<=1) {
				prefixes.remove(i);
				count.remove(i);
			}
		}
		return prefixes.toArray(new String[prefixes.size()]);
	}

	public static void closeAllByPrefix(String prefix) {
		for (int i=0; i<JEFrameList.size(); i++) {
			String title = JEFrameList.get(i).getTitle();
			if (title.startsWith(prefix)) JEFrameList.get(i).dispose();
		}
	}
}