package eva2.tools;

/**
 * This serves as activation state of one item in an array of Tags
 * identified by integer IDs. String names should be unique as well as integer IDs.
 */

public class SelectedTag implements java.io.Serializable {
	protected int m_Selected;
	protected Tag[] m_Tags;

    @Override
	public Object clone() {
		SelectedTag result = new SelectedTag(this.m_Selected, this.m_Tags);
		return (Object) result;
	}

	/**
	 * Constructor from a String array, creates a Tag array where the IDs correspond to array index.
	 * Standard selection is 0.
	 * 
	 * @param strings
	 */

	public SelectedTag(String ... strings) {
		init(0, strings);
	}
	
	/**
	 * Constructor from a String array, creates a Tag array where the IDs correspond to array index.
	 * 
	 * @param selID
	 * @param tagStrings
	 */
	public SelectedTag(int selID, String ... strings) {
		init(selID, strings);
	}

	/**
	 * Constructor with a given Tag array. The IDs should correspond to the array index.
	 * 
	 * @param selID
	 * @param tags
	 */
	public SelectedTag(int selID, Tag[] tags) {
		m_Tags = tags;
		m_Selected = -1;
		for (int i = 0; i < tags.length; i++) {
			if (i!=tags[i].getID()) {
				System.err.println("warning, SelectedTag with inconsistent ID, this may cause problems");
			}
			if (tags[i].getID() == selID) {
				m_Selected = i;
			}
		}
		if (m_Selected == -1) {
                throw new IllegalArgumentException("Selected tag is not valid");
            }
	}

	private void init(int selID, String[] tagStrings) {
		m_Tags = new Tag[tagStrings.length];
		m_Selected = -1;
		for (int i = 0; i < m_Tags.length; i++) {
			m_Tags[i] = new Tag(i, tagStrings[i]);
			if (selID == i) {
                        m_Selected = i;
                    }
		}
		if (m_Selected == -1) {
                throw new IllegalArgumentException("Selected tag is not valid");
            }		
	}
	
	//~ Methods ////////////////////////////////////////////////////////////////


	/** 
	 * Set the selected tag by index.
	 * 
	 * @param i     The new selected tag index
	 */
	public SelectedTag setSelectedTag(int i) {
		if ((i >= 0) && (i < this.m_Tags.length)) {
                this.m_Selected = i;
            }
		return this;
	}
	
	/**
	 * Set the selected tag by String tag name. If the given name doesnt exist, nothing
	 * will change and an error message will be printed to System.err. This should of course
	 * be avoided. 
	 *  
	 * @param str    The new selected tag name
	 */
	public SelectedTag setSelectedTag(String str) {
		for (int i=0; i<m_Tags.length; i++) {
			if (m_Tags[i].m_String.compareTo(str) == 0) {
				m_Selected = i;
				return this;
			}
		}
		System.err.println("Warning, trying to select unknown string (SelectedTag::setSelectedTag(String)");
		return this;
	}
	
	/**
	 * Sets the local selection to the same ID currently selected by the given instance.
	 * If the ID is not valid, nothing is changed.
	 *
	 * @param o	another SelectedTag instance, preferably with the same tags
	 */
	public void setSelectedAs(SelectedTag o) {
		setSelectedTag(o.getSelectedTag().getID());    	
	}

	/**
	 *
	 */
	public Tag getSelectedTag() {
		return m_Tags[m_Selected];
	}
	
	/**
	 *
	 */
	public int getSelectedTagID() {
		return m_Tags[m_Selected].getID();
	}
	
	public String getSelectedString() {
		return m_Tags[m_Selected].getString();
	}
	
	public int getTagIDByString(String str) {
		for (int i=0; i<m_Tags.length; i++) {
			if (m_Tags[i].equals(str)) {
                        return m_Tags[i].getID();
                    }
		} 
		return -1;
	}
	/**
	 * Returns true if the given String is equivalent to the currently selected Tags string component,
	 * else false. 
	 *
	 * @param str String to compare to
	 * @return	true if the given string is equivalent to the currently selected Tags string component
	 */
	public boolean isSelectedString(String str) {
		return str.equals(getSelectedTag().getString());
	}

	/**
	 *
	 */
	public Tag[] getTags() {
		return m_Tags;
	}

	/**
	 *
	 */
    @Override
	public boolean equals(Object o) {
		if ((o == null) || !(o.getClass().equals(this.getClass()))) {
			return false;
		}

		SelectedTag s = (SelectedTag) o;

		if ((s.getTags() == m_Tags) &&
				(s.getSelectedTag() == m_Tags[m_Selected])) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Check if the given object has the same string tags as this one and return true if so, else false.
	 * @param selT
	 * @return
	 */
	public boolean hasSameTags(SelectedTag selT) {
		Tag[] oTags = selT.getTags();
		if (oTags.length != m_Tags.length) {
			return false;
		} else {
			for (int i=0;i<oTags.length; i++) {
				if (oTags[i].getString().compareTo(m_Tags[i].getString()) != 0) {
                                return false;
                            }
			}
			return true;
		}
	}
	
    @Override
	public String toString() {
		return m_Tags[m_Selected].getString();
//		Character selSign = '*';
//		Character separator = '|';
//		StringBuffer sbuf;
//		if (m_Selected != 0) sbuf = new StringBuffer(m_Tags[0].getString());
//		else {
//			sbuf = new StringBuffer(selSign.toString());
//			sbuf.append(m_Tags[0].getString());
//			sbuf.append(selSign);
//		}
//		for (int i=1; i<m_Tags.length; i++) {
//			sbuf.append(separator);
//			if (m_Selected == i) {
//				sbuf.append(selSign);
//				sbuf.append(m_Tags[i].getString());
//				sbuf.append(selSign);
//			} else sbuf.append(m_Tags[i].getString());
//		}
//		return sbuf.toString();
	}
}