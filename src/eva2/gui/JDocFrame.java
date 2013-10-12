package eva2.gui;

import javax.swing.*;
import java.io.File;

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

