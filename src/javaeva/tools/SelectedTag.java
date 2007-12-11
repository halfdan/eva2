package javaeva.tools;

/**
 * This serves as activation state of one item in an array of Tags
 * identified by integer IDs. String names should be unique as well as integer IDs.
 */

public class SelectedTag implements java.io.Serializable {
	protected int m_Selected;
	protected Tag[] m_Tags;

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
	public SelectedTag(int selID, String[] tagStrings) {
		init(selID, tagStrings);
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
		if (m_Selected == -1) throw new IllegalArgumentException("Selected tag is not valid");
	}

	private void init(int selID, String[] tagStrings) {
		m_Tags = new Tag[tagStrings.length];
		m_Selected = -1;
		for (int i = 0; i < m_Tags.length; i++) {
			m_Tags[i] = new Tag(i, tagStrings[i]);
			if (selID == i) m_Selected = i;
		}
		if (m_Selected == -1) throw new IllegalArgumentException("Selected tag is not valid");		
	}
	
	//~ Methods ////////////////////////////////////////////////////////////////


	/** This gives me the chance to set the selected tag index from a java program
	 * @param i     The new selected tag index
	 */
	public void setSelectedTag(int i) {
		if ((i >= 0) && (i < this.m_Tags.length)) this.m_Selected = i;
	}
	
	/** This gives me the chance to set the selected tag index from a java program
	 * @param i     The new selected tag index
	 */
	public void setSelectedTag(String str) {
		for (int i=0; i<m_Tags.length; i++) {
			if (m_Tags[i].m_String.compareTo(str) == 0) {
				m_Selected = i;
				return;
			}
		}
		System.err.println("Warning, trying to select unknown string (SelectedTag::setSelectedTag(String)");
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
}