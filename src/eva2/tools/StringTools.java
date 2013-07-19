package eva2.tools;

import eva2.gui.BeanInspector;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to provide simplification functions
 * for working with Strings.
 *
 * @author Fabian Becker, Marcel Kronfeld
 */
public final class StringTools {
    
    /**
     * Private constructor to prevent instantiation.
     */
    private StringTools() { }

    /**
     * Returns a HTML formatted String, in which each line is at most lineBreak
     * symbols long.
     *
     * @param string
     * @param lineBreak
     * @return
     */
    public static String toHTML(String string, int lineBreak) {
        StringTokenizer sTok = new StringTokenizer(string, " ");
        if (!sTok.hasMoreTokens()) {
            return "<html><body></body></html>";
        }
        StringBuilder sBuf = new StringBuilder(sTok.nextToken());
        
        int length = sBuf.length();
        while (sTok.hasMoreElements()) {
            if (length >= lineBreak) {
                sBuf.append("<br>");
                length = 0;
            } else {
                sBuf.append(" ");
            }
            String tmp = sTok.nextToken();
            length += tmp.length() + 1;
            sBuf.append(tmp);
        }
        sBuf.insert(0, "<html><body>");
        sBuf.append("</body></html>");
        return sBuf.toString();
    }
    
    public static boolean arrayContains(String[] arr, String key, boolean ignoreCase) {
    	return (searchStringArray(arr, key, 0, ignoreCase))>=0; 
    }
    
	/**
	 * Convert an integer to a String filling it with zeros from the left, so
	 * that is it of the same length as the Integer maxSize would achieve.
	 * Note that this only works for positive values.
	 * 
	 * @param index
	 * @param maxSize
	 * @return
	 */
	public static String expandPrefixZeros(int index, int maxSize) {
		if (maxSize<10) {
                return ""+index;
            }
		else if (maxSize<100) {
                return ((index<10) ? "0" : "")+index;
            }
		else {
			int lenZeros = (int)Math.log10(maxSize)-(int)Math.log10(index);
			char[] zerArr = new char[lenZeros];
			Arrays.fill(zerArr, '0');
			return new String(zerArr)+index;
		} 
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
    			if (arr[i].equalsIgnoreCase(key)) {
                        return i;
                    }
    		} else {
    			if (arr[i].equals(key)) {
                        return i;
                    }
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
	 * For any key, if it was found, the corresponding value is a String containing "true" for zero-arity-keys
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
    			if (found || (i>=args.length)) {
                        break;
                    } // if a key was found look at next argument
	    		if ((ignoreCase && (args[i].equalsIgnoreCase(keys[k]))) 
	    				|| (!ignoreCase && (args[i].equals(keys[k])))) { // if the key was found
	    			found=true;
	    			if (arities[k]==0) {
                                                values[k]=new String("true");
                                            } // and its zero-arity, just return true as its value
	    			else { // else return an array of size arity with following strings
	    				try {
	    				if (arities[k]==1) {
	    					values[k]=args[i+1];
	    				} else { // create String array and fill with following args depending on arity
	    					values[k]=new String[arities[k]];
	    					if (arities[k]>0) {
	    						for (int j=0; j<arities[k]; j++) {
	    							((String[])values[k])[j]=args[i+j+1];
	    						}
	    						i+=(arities[k]-1); // jump one more for every arity beyond 1
	    					}
	    				}
	    				} catch (ArrayIndexOutOfBoundsException e) {
	    					String errMsg = "Not enough parameters for option "+ keys[k] + ", expected number of arguments: " + arities[k];
	    					System.err.println(errMsg);
	    					throw new RuntimeException(errMsg);
	    				}
	    				i++; // jump one cause we had at least arity 1
	    			}
	    		}
    		}
    		if (!found) {
                unrecogs.add(i);
            }
    	}
    	return unrecogs.toArray(new Integer[unrecogs.size()]);
    }
    
    /**
     * Store the arguments in a hash map.
     * 
     * @see #parseArguments(String[], String[], int[], Object[], boolean)
     * @param args
     * @param keys
     * @param arities
     * @param ignoreCase
     * @return
     */
    public static HashMap<String, Object> parseArguments(String[] args, String[] keys, int[] arities, boolean ignoreCase, boolean printErrorsOnUnrecog) {
    	Object[] values = new Object[keys.length];
    	Integer[] unrecogs = parseArguments(args, keys, arities, values, ignoreCase);
    	if (printErrorsOnUnrecog) {
    		if (unrecogs.length>0) {
    			System.err.println("Unrecognized command line options: ");
    			for (int i=0; i<unrecogs.length; i++) {
                    System.err.println("   " + args[unrecogs[i]]);
                }
    		}
    	}
    	HashMap<String, Object> map = new HashMap<String, Object>();
    	for (int i=0; i<keys.length; i++) {
    		map.put(keys[i], values[i]);
    	}
    	return map;
    }
	
	/**
	 * Check whether an object is a valid String of String array and if so return the i-th String.
	 * Returns null otherwise. 
	 * 
	 * @param key
	 * @param object
	 * @param i
	 * @return
	 */
	public static String checkSingleStringArg(String key, Object object, int i) {
		if (object==null) {
                return null;
            }
		if (object instanceof String) {
			if (i==0) {
                        return (String)object;
                    }
			else {
				System.err.println("Invalid argument; cannot access element " + i + " for " + key + " as only one was given.");
				return null;
			}
		}
		if (object instanceof String[]) {
			String[] arr = (String[])object;
			if (i<arr.length) {
                        return arr[i];
                    }
			else {
				System.err.println("Not enough arguments for " + key);
				return null;
			}
		} else {
			System.err.println("Invalid argument; " + key + " " + BeanInspector.toString(object));
			return null;
		}
	}

	/**
	 * Rewrap a given string to lines of approx. length len.
	 * 
	 * @param str
	 * @param len
	 * @return
	 */
	public static String wrapLine(String str,  int len, double tolerancePerCent) {
		return wrapLine(str, new char[]{' ', '-', ',', '.'}, len, tolerancePerCent);
	}
	
	/**
	 * Rewrap a given string to lines of approx. length len.
	 * 
	 * @param str
	 * @param len
	 * @return
	 */
	public static String wrapLine(String str, char[] breakChars, int len, double tolerancePerCent) {
		StringBuffer res=new StringBuffer(); 
		String rest=str;
		int minLen = (int)((1.-tolerancePerCent)*(double)len);
		int maxLen = (int)((1.+tolerancePerCent)*(double)len);
		int nextBreak=-1;
		while (rest.length()>0) {
			if (rest.length()<=maxLen) {
				nextBreak = rest.length()-1;
			} else {
				nextBreak = getNextBreak(minLen, maxLen, breakChars, rest); // search for a break character in a certain interval
				if (nextBreak<0) {
                                nextBreak = len;
                            } // if none found force the break at the intended length
			}
			if (res.length()>0) {
                        res.append("\n");
                    } // insert newline
			res.append(rest.substring(0, nextBreak+1));
			rest = rest.substring(nextBreak+1);
		}
		return res.toString();
	}
	
	public static int getNextBreak(int startIndex, int endIndex, char[] brkChars, String str) {
		int index;
		for (int i=0; i<brkChars.length; i++) {
			//indices[i] = str.indexOf(""+brkChars[i], startIndex);
			index =str.indexOf(""+brkChars[i], startIndex);
			if (index>=0 && (index <= endIndex)) {
                        return index;
                    }
		}
		return -1;
	}

	/**
	 * Concatenate a list of Strings using a given delimiter string.
	 * 
	 * @param strings List of Strings to concatenate
	 * @param delim Delimiter for concatenation
	 * @return String representation
	 */
	public static String concatFields(final List<String> strings,
			final String delim) {
		StringBuilder sBuilder = new StringBuilder();
		int cnt = 0;
		for (String field : strings) {
			if (cnt > 0) {
                            sBuilder.append(delim);
                        }
			sBuilder.append(field);
			cnt++;
		}
		return sBuilder.toString();
	}
	
	/**
	 * Concatenate a list of Objects using a given delimiter string.
	 * The objects are converted to strings using the BeanInspector class.
	 * 
	 * @param headlineFields
	 * @param delim
	 * @return
	 */
	public static String concatValues(List<Object> objects,
			final String delim) {
		StringBuilder sb = new StringBuilder();
		int cnt = 0;
		for (Object v : objects) {
			if (cnt > 0) {
                            sb.append(delim);
                        }
			sb.append(BeanInspector.toString(v));
			cnt++;
		}
		return sb.toString();
	}

	public static String concatFields(String[] strs,
			String delim) {
		StringBuilder sb = new StringBuilder();
		int cnt = 0;
		for (String str : strs) {
			if (cnt > 0) { 
                            sb.append(delim);
                        }
			sb.append(str);
			cnt++;
		}
		return sb.toString();
	}

	/**
	 * Remove or replace blanks, braces, etc. from a string for use as a file name.
	 * 
	 * @param predefName
	 * @return
	 */
	public static String simplifySymbols(String str) {
		char[] toUnderscore = new char[] {' ', '\t', '\n'};
		for (char c : toUnderscore) {
			str = str.replace(c, '_');
		}
		char[] toDash = new char[] {',', ';', '/', '\\'};
		for (char c : toDash) {
			str = str.replace(c, '-');
		}
		char[] toRemove = new char[] {')', '(', '[', ']', '{', '}', '*'};
		for (char c : toRemove)	{
			str = deleteChar(c, str);
		}
		str = str.replaceAll("--", "-");
		str = str.replaceAll("__", "_");
		str = str.replaceAll("-_", "_");
		str = str.replaceAll("_-", "_");

		return str;
	}

	/**
	 * Delete a certain character from a string.
	 * 
	 * @param c Character to delete
	 * @param str String to remove c from.
	 * @return String with character c removed.
	 */
	public static String deleteChar(final char c, final String str) {
		StringBuilder sBuilder = new StringBuilder();
		for (int i = 0; i < str.length(); i++) {
			if (c != str.charAt(i)) {
                            sBuilder.append(str.charAt(i));
                        }
		}
		return sBuilder.toString();
	}

	/**
	 * Return the substring after the last occurrence of a character. If the
	 * character does not occur, the full string is returned.
	 * 
	 * @param str
	 * @param c
	 * @return
	 */
	public static String getSubstringAfterLast(String str, char c) {
		int p = str.lastIndexOf(c);
		return str.substring(p+1); // for -1 this just works as well
	}
        
        /**
        * Converts a camelCase to a more human form, with spaces.
        * E.g. 'Camel Case'.
        *
        * @param word Word to convert to a readable String
        * @return Readable String representation of input word
        */
        public static String humaniseCamelCase(final String word) {
            Pattern pattern = Pattern.compile("([A-Z]|[a-z])[a-z]*");

            List<String> tokens = new ArrayList<String>();
            Matcher matcher = pattern.matcher(word);
            String acronym = "";
            while (matcher.find()) {
                String found = matcher.group();
                if (found.matches("^[A-Z]$")) {
                    acronym += found;
                } else {
                    if (acronym.length() > 0) {
                        //we have an acronym to add before we continue
                        tokens.add(acronym);
                        acronym = "";
                    }
                    tokens.add(upcaseFirst(found));
                }
            }

            if (acronym.length() > 0) {
                tokens.add(acronym);
            }

            if (!tokens.isEmpty()) {
                return concatFields(tokens, " ");                
            }

            return upcaseFirst(word);
        }

        /**
         * Takes a string and returns it with the first character
         * converted to uppercase.
         *
         * @param word Word to modify
         * @return Parameter with its first character converted to uppercase
         */
        public static String upcaseFirst(final String word) {
            return word.substring(0, 1).toUpperCase() + word.substring(1);
        }
}

