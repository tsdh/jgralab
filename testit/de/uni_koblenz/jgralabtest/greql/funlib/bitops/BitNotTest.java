package de.uni_koblenz.jgralabtest.greql.funlib.bitops;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.uni_koblenz.jgralab.greql.funlib.FunLib;

public class BitNotTest extends BitOpTest {

	@Test
	public void testInteger() {
		for (int i = 0; i < intValues.length; i++) {
			int expected = ~intValues[i];
			Object result = FunLib.apply("bitNot", intValues[i]);
			assertTrue(result instanceof Integer);
			assertEquals(expected, result);
		}
	}

	@Test
	public void testLong() {
		for (int i = 0; i < longValues.length; i++) {
			long expected = ~longValues[i];
			Object result = FunLib.apply("bitNot", longValues[i]);
			assertTrue(result instanceof Long);
			assertEquals(expected, result);
		}
	}

}
