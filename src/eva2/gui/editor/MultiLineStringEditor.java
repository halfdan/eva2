package eva2.gui.editor;

import eva2.gui.MultiLineString;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;


/**
 * <p>Title: EvA2</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class MultiLineStringEditor implements PropertyEditor {
  protected MultiLineString value;  // The value we will be editing.

    @Override
  public void setValue(Object o) {  value=(MultiLineString) o;}
    @Override
  public Object getValue() { return value; }
    @Override
  public void setAsText(String s) { value.setString(s); }
    @Override
  public String getAsText() { return value.getString(); }
    @Override
  public String[] getTags() { return null; }  // not enumerated; no tags

  // Say that we allow custom editing.
    @Override
  public boolean supportsCustomEditor() { return true; }

  // Return the custom editor.  This just creates and returns a TextArea
  // to edit the multi-line text.  But it also registers a listener on the
  // text area to update the value as the user types and to fire the
  // property change events that property editors are required to fire.
    @Override
  public Component getCustomEditor() {
    final TextArea t = new TextArea(value.string);
    t.setSize(300, 150); // TextArea doesn't have a preferred size, so set one
    t.addTextListener(new TextListener() {
            @Override
      public void textValueChanged(TextEvent e) {
        value.setString(t.getText());
        listeners.firePropertyChange(null, null, null);
      }
    });
    return t;
  }

  // Visual display of the value, for use with the custom editor.
  // Just print some instructions and hope they fit in the in the box.
  // This could be more sophisticated.
    @Override
  public boolean isPaintable() { return true; }

    @Override
  public void paintValue(Graphics g, Rectangle r) {
    g.setClip(r);
    g.drawString("Click to edit...", r.x+5, r.y+15);
  }

  // Important method for code generators.  Note that it
  // ought to add any necessary escape sequences.
    @Override
  public String getJavaInitializationString() { return "\"" + value + "\""; }

  // This code uses the PropertyChangeSupport class to maintain a list of
  // listeners interested in the edits we make to the value.
  protected PropertyChangeSupport listeners = new PropertyChangeSupport(this);
  
    @Override
  public void addPropertyChangeListener(PropertyChangeListener l) {
	  if (listeners == null) {
          listeners = new PropertyChangeSupport(this);
      }
	  listeners.addPropertyChangeListener(l);
  }

    @Override
  public void removePropertyChangeListener(PropertyChangeListener l) {
	  if (listeners == null) {
          listeners = new PropertyChangeSupport(this);
      }
	  listeners.removePropertyChangeListener(l);
  }
}
