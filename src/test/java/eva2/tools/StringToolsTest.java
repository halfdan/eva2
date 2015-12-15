package eva2.tools;

import org.junit.Test;

import java.util.HashMap;
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
}
