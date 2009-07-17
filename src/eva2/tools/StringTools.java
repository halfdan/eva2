package eva2.tools;

import java.util.LinkedList;
import java.util.StringTokenizer;

import eva2.gui.BeanInspector;

public class StringTools {

    /**
     * Returns a HTML formated String, in which each line is at most lineBreak
     * symbols long.
     *
     * @param string
     * @param lineBreak
     * @return
     */
    public static String toHTML(String string, int lineBreak) {
        StringTokenizer st = new StringTokenizer(string, " ");
        if (!st.hasMoreTokens()) return "<html><body></body></html>";
        StringBuffer sbuf = new StringBuffer(st.nextToken());
        
        int length = sbuf.length();
        while (st.hasMoreElements()) {
            if (length >= lineBreak) {
                sbuf.append("<br>");
                length = 0;
            } else sbuf.append(" ");
            String tmp = st.nextToken();
            length += tmp.length() + 1;
            sbuf.append(tmp);
        }
        sbuf.insert(0, "<html><body>");
        sbuf.append("</body></html>");
        return sbuf.toString();
    }
    
    public static boolean arrayContains(String[] arr, String key, boolean ignoreCase) {
    	return (searchStringArray(arr, key, 0, ignoreCase))>=0; 
    }
    
    /**
     * Search a String array for a given String and return its index if it is found.
     * If it is not found, -1 is returned.
     * 
     * @param arr
     * @param key
     * @param startIndex
     * @param ignoreCase
     * @return
     */
    public static int searchStringArray(String[] arr, String key, int startIndex, boolean ignoreCase) {
    	for (int i=startIndex; i<arr.length; i++) {
    		if (ignoreCase) {
    			if (arr[i].equalsIgnoreCase(key)) return i;
    		} else {
    			if (arr[i].equals(key)) return i;
    		}
    	}
    	return -1;
    }
    
//    public static void main(String[] args) {
//    	System.out.println(toHTML("Hallo-asdfsadfsafdsadfo, dies ist ein doller test text!", 15));
//    	System.out.println(toHTML("Set the interval of data output for intermediate verbosity (in generations).", 15));
//    	System.out.println(toHTML("Set the interval of data output for intermediate verbosity (in generations).", 25));
//    	System.out.println(toHTML("Set the interval of data output for intermediate verbosity (in generations).", 30));
//    }
    
	/**
	 * Parse an array of Strings as an argument list. Take the argument list, a set of keys together
	 * with their arities. Returns for each key a value depending on arity and whether it was found.
	 * For any key, if it was not found null is returned as value. Its index in the keys array is contained
	 * in the returned integer array.
	 * For any key, if it was found, the corresponding value is either Boolean(true) for zero-arity-keys
	 * or a String array of length of the arity containing the arguments to the key. 
	 * 
	 * @param args
	 * @param keys
	 * @param arities
	 * @param values
	 * @param ignoreCase
	 * @return
	 */
    public static Integer[] parseArguments(String[] args, String[] keys, int[] arities, Object[] values, boolean ignoreCase) {
    	LinkedList<Integer> unrecogs=new LinkedList<Integer>();
    	//for (String string : argsArr) args.add(string); // create a linked list copy
    	for (int i=0; i<args.length; i++) { // loop all arguments
    		boolean found=false;
    		for (int k=0; k<keys.length; k++) { // loop all keys
	    		if ((ignoreCase && (args[i].equalsIgnoreCase(keys[k]))) 
	    				|| (!ignoreCase && (args[i].equals(keys[k])))) { // if the key was found
	    			found=true;
	    			if (arities[k]==0) values[k]=new Boolean(true); // and its zero-arity, just return true as its value
	    			else { // else return an array of size arity with following strings
	    				values[k]=new String[arities[k]];
	    				for (int j=0; j<arities[k]; j++) {
	    					i++;
	    					((String[])values[k])[j]=args[i];
	    				}
	    			}
	    		}
    		}
    		if (!found) unrecogs.add(i);
    	}
    	return unrecogs.toArray(new Integer[unrecogs.size()]);
    }
    
	
	/**
	 * Check whether an object is a valid String array and if so return the i-th String.
	 * Returns null otherwise. 
	 * 
	 * @param key
	 * @param object
	 * @param i
	 * @return
	 */
	public static String checkSingleStringArg(String key, Object object, int i) {
		if (object==null) return null;
		if (object instanceof String[]) {
			String[] arr = (String[])object;
			if (i<arr.length) return arr[i];
			else {
				System.err.println("Not enough arguments for " + key);
				return null;
			}
		} else {
			System.err.println("Invalid argument: " + key + " " + BeanInspector.toString(object));
			return null;
		}
	}
}
