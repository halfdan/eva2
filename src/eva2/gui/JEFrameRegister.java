package eva2.gui;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
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


public final class JEFrameRegister {
    /**
     * Singleton instance.
     */
    private static JEFrameRegister instance = null;

    /**
     * List of all frames maintained.
     */
    private List<JEFrame> frameList;

    private JDesktopPane desktopPane;

    private JEFrameRegister() {
        this.frameList = new ArrayList<JEFrame>();
    }

    public static JEFrameRegister getInstance() {
        if (instance == null) {
            instance = new JEFrameRegister();
        }
        return instance;
    }

    public void setDesktopPane(JDesktopPane desktopPane) {
        this.desktopPane = desktopPane;
        if (!frameList.isEmpty()) {
            for (JEFrame frame : frameList) {
                this.desktopPane.add(frame);
            }
        }
    }

    public void register(JEFrame jeFrame) {
        if (!frameList.contains(jeFrame)) {
            frameList.add(jeFrame);

            if (desktopPane != null) {
                desktopPane.add(jeFrame);
            }
        }
    }

    public void unregister(JEFrame jeFrame) {
        // Plot windows produce double closing events, so ignore it
        frameList.remove(jeFrame);
    }

    public List<JEFrame> getFrameList() {
        return frameList;
    }

    public void setFocusToNext(JEFrame jeFrame) {
        int idx = frameList.indexOf(jeFrame);
        idx = (idx + 1) % frameList.size();
        JEFrame toset = ((JEFrame) frameList.get(idx));
        toset.toFront();
    }

    /**
     * Return all prefixes which occur at least twice in the registered frame list.
     *
     * @param prefLen Preferred length of prefixes
     * @return List of prefixes
     */
    public String[] getCommonPrefixes(final int prefLen) {
        List<String> prefixes = new ArrayList<String>();
        List<Integer> count = new ArrayList<Integer>();
        for (int i = 0; i < frameList.size(); i++) {
            String title = frameList.get(i).getTitle();
            String titPref = title.substring(0, Math.min(prefLen, title.length()));
            int earlierIndex = prefixes.indexOf(titPref);
            if (earlierIndex < 0) {
                prefixes.add(titPref);
                count.add(1);
            } else {
                count.set(earlierIndex, 1 + count.get(earlierIndex));
            }
        }
        for (int i = prefixes.size() - 1; i >= 0; i--) {
            if (count.get(i) <= 1) {
                prefixes.remove(i);
                count.remove(i);
            }
        }
        return prefixes.toArray(new String[prefixes.size()]);
    }

    /**
     * Close (dispose) all frames whose title starts with a given prefix.
     *
     * @param prefix The prefix
     */
    public void closeAllByPrefix(final String prefix) {
        for (int i = 0; i < frameList.size(); i++) {
            String title = frameList.get(i).getTitle();
            if (title.startsWith(prefix)) {
                frameList.get(i).dispose();
            }
        }
    }

    /**
     * Close (dispose) all frames registered in this list.
     */
    public void closeAll() {
        for (int i = 0; i < frameList.size(); i++) {
            frameList.get(i).dispose();
        }
    }
}