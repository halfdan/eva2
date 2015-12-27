package eva2.tools;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class StringToolsTest {

	/**
	 * Test of humaniseCamelCase method, of class StringTools.
	 */
	@Test
	public void testHumaniseCamelCase() {
		HashMap<String, String> map = new HashMap<>();
		map.put("camelCase", "Camel Case");
		map.put("Camel Case", "Camel Case");
		map.put("thisIsAwesome", "This Is Awesome");
		map.put("THQIsNice", "THQ Is Nice");
		map.put("iLikeABC", "I Like ABC");
        map.put("foo2Bar", "Foo2 Bar");
        map.put("phi1", "Phi1");

		String key, value;
        for (Object o : map.entrySet()) {
            Map.Entry pairs = (Map.Entry) o;
            key = (String) pairs.getKey();
            value = (String) pairs.getValue();
            String result = StringTools.humaniseCamelCase(key);
            assertEquals(value, result);
        }
	}

    /**
     * Test of humaniseCamelCase method, of class StringTools.
     */
    @Test
    public void testConvertToUnderscore() {
        HashMap<String, String> map = new HashMap<>();
        map.put("camelCase", "camel-case");
        map.put("CamelCase", "camel-case");
        map.put("thisIsAwesome", "this-is-awesome");
        map.put("THQIsNice", "thq-is-nice");
        map.put("iLikeABC", "i-like-abc");
        map.put("THIS", "this");

        String key, value;
        for (Object o : map.entrySet()) {
            Map.Entry pairs = (Map.Entry) o;
            key = (String) pairs.getKey();
            value = (String) pairs.getValue();
            String result = StringTools.convertToUnderscore(key);
            assertEquals(value, result);
        }
    }

	/**
	 * Test of upcaseFirst method, of class StringTools.
	 */
	@Test
	public void testUpcaseFirst() {
		assertEquals("Camel", StringTools.upcaseFirst("camel"));
		assertEquals("UpWeGo", StringTools.upcaseFirst("upWeGo"));
	}

    @Test
    public void testCutClassName() {
        assertEquals("StringTools", StringTools.cutClassName("eva2.tools.StringTools"));
        assertEquals("RandomClass", StringTools.cutClassName("eva2.optimization.operator.RandomClass"));
    }

    @Test
    public void testTranslateGreek() {
        // Doesn't touch string without greek characters
        assertEquals("Not a greek string", StringTools.translateGreek("Not a greek string"));

        // Replaces greek characters irregardless of case
        assertEquals("τ", StringTools.translateGreek("tau"));
        assertEquals("τ", StringTools.translateGreek("Tau"));
        assertEquals("τ", StringTools.translateGreek("tAU"));

        // Handles multiple values for one letter
        assertEquals("μ", StringTools.translateGreek("mu"));
        assertEquals("μ", StringTools.translateGreek("my"));
        assertEquals("μ", StringTools.translateGreek("myu"));

        // Handles indices like sigma1, sigma2
        assertEquals("ϕ1", StringTools.translateGreek("phi1"));
        assertEquals("ϕ10", StringTools.translateGreek("phi10"));

        // Doesn't mess up other strings
        assertEquals("Stau", StringTools.translateGreek("Stau"));
        assertEquals("taur", StringTools.translateGreek("taur"));
        assertEquals("taur12", StringTools.translateGreek("taur12"));
    }

    @Test
    public void testSubscriptIndices() throws Exception {
        // Doesn't alter numbers at the start or mid string
        assertEquals("12derp", StringTools.subscriptIndices("12derp"));
        assertEquals("der12p", StringTools.subscriptIndices("der12p"));

        // Replaces trailing numbers with a sub html tag
        assertEquals("derp<sub>12</sub>", StringTools.subscriptIndices("derp12"));
        assertEquals("This is a string <sub>12</sub>", StringTools.subscriptIndices("This is a string 12"));
        assertEquals("ϕ<sub>10</sub>", StringTools.subscriptIndices("ϕ10"));
    }

    @Test
    public void testDeleteChar() throws Exception {
        assertEquals("Taylor", StringTools.deleteChar('s', "Taylors"));
        assertEquals("Swift", StringTools.deleteChar('q', "Swift"));
    }

    @Test
    public void testGetSubstringAfterLast() throws Exception {
        assertEquals("Swift", StringTools.getSubstringAfterLast("TaylorSwift", 'r'));
        assertEquals("Bad Blood", StringTools.getSubstringAfterLast("Bad Blood", 'q'));
    }

    @Test
    public void testConcatFields() throws Exception {
        List<String> list = new ArrayList<>();
        list.add("Taylor");
        list.add("Swift");
        list.add("Bad");

        assertEquals("Taylor,Swift,Bad", StringTools.concatFields(list, ","));

        assertEquals("Taylor,Swift,Bad,Blood", StringTools.concatFields(
                new String[]{"Taylor", "Swift", "Bad", "Blood"},
                ","
        ));
    }

    @Test
    public void testExpandPrefixZeros() throws Exception {
        assertEquals("01", StringTools.expandPrefixZeros(1, 19));
        assertEquals("0001", StringTools.expandPrefixZeros(1, 1900));
        assertEquals("000001", StringTools.expandPrefixZeros(1, 219837));
        assertEquals("00001", StringTools.expandPrefixZeros(1, 12398));
    }

    @Test
    public void testConcatValues() throws Exception {
        List<Object> list = new ArrayList<>();
        list.add("Foo");
        list.add(12);
        list.add("Sieben");
        assertEquals("Foo;12;Sieben", StringTools.concatValues(list, ";"));
    }

    @Test
    public void testSimplifySymbols() throws Exception {
        assertEquals("foo-bar", StringTools.simplifySymbols("foo--bar"));
        assertEquals("foo_bar", StringTools.simplifySymbols("foo__bar"));
        assertEquals("foo_bar", StringTools.simplifySymbols("foo_-bar"));
        assertEquals("foo_bar", StringTools.simplifySymbols("foo-_bar"));

        assertEquals("foobar", StringTools.simplifySymbols("f(o){o}b*[a]r"));

        assertEquals("foo-bar", StringTools.simplifySymbols("foo;bar"));
        assertEquals("foo-bar", StringTools.simplifySymbols("foo,bar"));
        assertEquals("foo-bar", StringTools.simplifySymbols("foo\\bar"));
        assertEquals("foo-bar", StringTools.simplifySymbols("foo/bar"));

        assertEquals("foo_bar", StringTools.simplifySymbols("foo bar"));
        assertEquals("foo_bar", StringTools.simplifySymbols("foo\n\tbar"));
        assertEquals("foo_bar_", StringTools.simplifySymbols("foo\tbar\n"));
    }
}
