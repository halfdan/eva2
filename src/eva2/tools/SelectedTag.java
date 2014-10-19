package eva2.tools;

/**
 * This serves as activation state of one item in an array of Tags
 * identified by integer IDs. String names should be unique as well as integer IDs.
 */

public class SelectedTag implements java.io.Serializable {
    protected int selectedId;
    protected Tag[] tags;

    @Override
    public Object clone() {
        SelectedTag result = new SelectedTag(this.selectedId, this.tags);
        return result;
    }

    /**
     * Constructor from a String array, creates a Tag array where the IDs correspond to array index.
     * Standard selection is 0.
     *
     * @param strings
     */

    public SelectedTag(String... strings) {
        init(0, strings);
    }

    /**
     * Constructor from a String array, creates a Tag array where the IDs correspond to array index.
     *
     * @param selID
     * @param tagStrings
     */
    public SelectedTag(int selID, String... strings) {
        init(selID, strings);
    }

    /**
     * Constructor with a given Tag array. The IDs should correspond to the array index.
     *
     * @param selID
     * @param tags
     */
    public SelectedTag(int selID, Tag[] tags) {
        this.tags = tags;
        selectedId = -1;
        for (int i = 0; i < tags.length; i++) {
            if (i != tags[i].getID()) {
                System.err.println("warning, SelectedTag with inconsistent ID, this may cause problems");
            }
            if (tags[i].getID() == selID) {
                selectedId = i;
            }
        }
        if (selectedId == -1) {
            throw new IllegalArgumentException("Selected tag is not valid");
        }
    }

    private void init(int selID, String[] tagStrings) {
        tags = new Tag[tagStrings.length];
        selectedId = -1;
        for (int i = 0; i < tags.length; i++) {
            tags[i] = new Tag(i, tagStrings[i]);
            if (selID == i) {
                selectedId = i;
            }
        }
        if (selectedId == -1) {
            throw new IllegalArgumentException("Selected tag is not valid");
        }
    }

    //~ Methods ////////////////////////////////////////////////////////////////


    /**
     * Set the selected tag by index.
     *
     * @param i The new selected tag index
     */
    public SelectedTag setSelectedTag(int i) {
        if ((i >= 0) && (i < this.tags.length)) {
            this.selectedId = i;
        }
        return this;
    }

    /**
     * Set the selected tag by String tag name. If the given name doesnt exist, nothing
     * will change and an error message will be printed to System.err. This should of course
     * be avoided.
     *
     * @param str The new selected tag name
     */
    public SelectedTag setSelectedTag(String str) {
        for (int i = 0; i < tags.length; i++) {
            if (tags[i].text.compareTo(str) == 0) {
                selectedId = i;
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
     * @param o another SelectedTag instance, preferably with the same tags
     */
    public void setSelectedAs(SelectedTag o) {
        setSelectedTag(o.getSelectedTag().getID());
    }

    /**
     *
     */
    public Tag getSelectedTag() {
        return tags[selectedId];
    }

    /**
     *
     */
    public int getSelectedTagID() {
        return tags[selectedId].getID();
    }

    public String getSelectedString() {
        return tags[selectedId].getString();
    }

    public int getTagIDByString(String str) {
        for (int i = 0; i < tags.length; i++) {
            if (tags[i].equals(str)) {
                return tags[i].getID();
            }
        }
        return -1;
    }

    /**
     * Returns true if the given String is equivalent to the currently selected Tags string component,
     * else false.
     *
     * @param str String to compare to
     * @return true if the given string is equivalent to the currently selected Tags string component
     */
    public boolean isSelectedString(String str) {
        return str.equals(getSelectedTag().getString());
    }

    /**
     *
     */
    public Tag[] getTags() {
        return tags;
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

        if ((s.getTags() == tags) &&
                (s.getSelectedTag() == tags[selectedId])) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Check if the given object has the same string tags as this one and return true if so, else false.
     *
     * @param selT
     * @return
     */
    public boolean hasSameTags(SelectedTag selT) {
        Tag[] oTags = selT.getTags();
        if (oTags.length != tags.length) {
            return false;
        } else {
            for (int i = 0; i < oTags.length; i++) {
                if (oTags[i].getString().compareTo(tags[i].getString()) != 0) {
                    return false;
                }
            }
            return true;
        }
    }

    @Override
    public String toString() {
        return tags[selectedId].getString();
//		Character selSign = '*';
//		Character separator = '|';
//		StringBuffer sbuf;
//		if (selectedId != 0) sbuf = new StringBuffer(tags[0].getString());
//		else {
//			sbuf = new StringBuffer(selSign.toString());
//			sbuf.append(tags[0].getString());
//			sbuf.append(selSign);
//		}
//		for (int i=1; i<tags.length; i++) {
//			sbuf.append(separator);
//			if (selectedId == i) {
//				sbuf.append(selSign);
//				sbuf.append(tags[i].getString());
//				sbuf.append(selSign);
//			} else sbuf.append(tags[i].getString());
//		}
//		return sbuf.toString();
    }
}