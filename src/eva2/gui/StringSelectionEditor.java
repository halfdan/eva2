package eva2.gui;

import eva2.tools.StringSelection;

public class StringSelectionEditor extends AbstractListSelectionEditor {
	StringSelection strs;
	
	public StringSelectionEditor() {
		strs = new StringSelection(new String[]{}, null);
	}
	
	@Override
	protected boolean actionOnSelect() {
		for (int i = 0; i < this.m_BlackCheck.length; i++) {
			strs.setSelected(i, this.m_BlackCheck[i].isSelected());
		}
		return true;
	}

	@Override
	protected int getElementCount() {
		return strs.getLength();
	}

	@Override
	protected String getElementName(int i) {
		return strs.getElement(i);
	}
	
	@Override
	protected String getElementToolTip(int i) {
		return strs.getElementInfo(i);
	}

	@Override
	public Object getValue() {
		return strs;
	}

	@Override
	protected boolean isElementSelected(int i) {
		return strs.isSelected(i);
	}

	@Override
	protected boolean setObject(Object o) {
        if (o instanceof StringSelection) {
            strs = (StringSelection) o;
//            m_AreaObject.addPropertyChangeListener(this);
            return true;
        } else {
                return false;
            }
	}

	@Override
	public String getName() {
		return "StringSelection";
	}

	@Override
	public String getAsText() {
		StringBuffer sbuf = new StringBuffer("{");
		boolean first = true;
		for (int i=0; i<getElementCount(); i++) {
			if (isElementSelected(i)) {
				if (!first) {
                                sbuf.append(", ");
                            }
				sbuf.append(getElementName(i));
				first=false;
			}
		}
		sbuf.append("}");
		return sbuf.toString();
	}

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		for (int i=0; i<getElementCount(); i++) {
			strs.setSelected(i, text.contains(getElementName(i)));
		}
	}
	
}
