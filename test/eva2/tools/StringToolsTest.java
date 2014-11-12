package eva2.tools;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author becker
 */
public class StringToolsTest {

	public StringToolsTest() {
	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

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
}
