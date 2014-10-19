package eva2.tools;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

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
	 * Test of upcaseFirst method, of class StringTools.
	 */
	@Test
	public void testUpcaseFirst() {
		assertEquals("Camel", StringTools.upcaseFirst("camel"));
		assertEquals("UpWeGo", StringTools.upcaseFirst("upWeGo"));
	}
}
