package eva2.gui;

import javax.swing.*;
import java.io.File;

/**
 *
 */
public abstract class JDocFrame extends JInternalFrame {
    private File file;
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
        this.file = file;
    }

    /**
     *
     */
    public File getFile() {
        return file;
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
        if (file != null) {
            save(file);
        }
    }

    /**
     *
     */
    public void save(File f) {
        if (!f.equals(file)) {
            file = f;
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

