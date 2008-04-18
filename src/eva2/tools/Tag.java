package eva2.tools;
/**
 * Title:        EvA2
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 10 $
 *            $Date: 2006-01-18 11:02:22 +0100 (Wed, 18 Jan 2006) $
 *            $Author: streiche $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
public class Tag implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	protected int m_ID;
	protected String m_String;
	public Tag(){}
	/**
	 *
	 */
	public Tag(int ident, String str) {
		m_ID = ident;
		m_String = str;
	}
	/**
	 *
	 */
	public int getID() {
		return m_ID;
	}
	/**
	 *
	 */
	public String getString() {
		return m_String;
	}
}
