package eva2.gui;
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

import java.io.File;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JToolBar;

/**
 *
 */
public abstract class JDocFrame extends JInternalFrame {
    private File m_file;
    private String titleStr;
    protected boolean changed = false;

    /**
     *
     */
    public JDocFrame(String title) {
        super(title, true, true /* not closable*/, true, true);
        titleStr = title;
    }

    /**
     *
     */
    public JDocFrame(File file) {
        this(file.getName());
        m_file = file;
    }

    /**
     *
     */
    public File getFile() {
        return m_file;
    }

    /**
     *
     */
    public String getFileTitle() {
        return titleStr;
    }

    /**
     *
     */
    public void save() {
        if (m_file != null) {
            save(m_file);
        }
    }

    /**
     *
     */
    public void save(File f) {
        if (!f.equals(m_file)) {
            m_file = f;
            titleStr = f.getName();
        }
        setChangedImpl(false);
    }

    /**
     *
     */
    private void setChangedImpl(boolean value) {
        changed = value;
        if (changed) {
            setTitle(titleStr + " *");
        } else {
            setTitle(titleStr);
        }
    }

    /**
     *
     */
    protected void setChanged(boolean value) {
        if (changed != value) {
            setChangedImpl(value);
        }
    }

    /**
     *
     */
    public boolean isChanged() {
        return changed;
    }

    public abstract String[] getActionGroups();

    public abstract JMenu getMenu(String group);

    public abstract JToolBar getToolBar(String group);
}

