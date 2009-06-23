package eva2.tools;

import java.io.Serializable;

/**
 * An array of Strings that can be selected and deselected. May be created directly from an Enum.
 * 
 * @author mkron
 *
 */
public class StringSelection implements Serializable {
	private String[] strObjects;
	boolean[] selStates;
	
	public StringSelection(String[] sArr) {
		strObjects = sArr;
		selStates=new boolean[sArr.length];
	}	
	
	public StringSelection(String[] sArr, int initialSel) {
		this(sArr);
		if (initialSel<getLength()) setSelected(initialSel, true);
	}
	
	public StringSelection(Enum<?> e) {
		strObjects = new String[e.getClass().getEnumConstants().length];
		selStates = new boolean[strObjects.length];
		for (int i = 0; i < strObjects.length; i++) {
			strObjects[i] = e.getClass().getEnumConstants()[i].toString();
		}
		setSelected(e.ordinal(), true);
	}
	
	public StringSelection(StringSelection stringSelection) {
		strObjects = stringSelection.strObjects.clone();
		selStates = stringSelection.selStates.clone();
	}

	public Object clone() {
		return new StringSelection(this);
	}
	
	public int getLength() {
		return strObjects.length;
	}
	
	public String getElement(int i) {
		return strObjects[i];
	}
	
	public String[] getStrings() {
		return strObjects;
	}
	
	public boolean isSelected(int i) {
		return selStates[i];
	}
	
	/**
	 * Returns true if the given enum is selected (as its string representation)
	 * within the instance.
	 * 
	 * @param e
	 * @return
	 */
	public boolean isSelected(Enum<?> e) {
		if (isSelected(e.ordinal())) {
			return e.toString().equals(strObjects[e.ordinal()]);
		} else return false;
	}
	
	public void setSelected(int i, boolean v) {
		selStates[i]=v;
	}	
	
	public void toggleSelected(int i) {
		selStates[i]=!selStates[i];
	}
}