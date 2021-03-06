package de.uni_koblenz.jgralabtest.greql.evaluator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pcollections.ArrayPSet;
import org.pcollections.POrderedSet;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.graphmarker.SubGraphMarker;
import de.uni_koblenz.jgralab.greql.GreqlEnvironment;
import de.uni_koblenz.jgralab.greql.GreqlQuery;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlEnvironmentAdapter;
import de.uni_koblenz.jgralab.greql.exception.UnknownTypeException;
import de.uni_koblenz.jgralab.greql.executable.ExecutableQuery;
import de.uni_koblenz.jgralab.greql.executable.GreqlCodeGenerator;
import de.uni_koblenz.jgralab.greql.types.Table;
import de.uni_koblenz.jgralab.greql.types.Tuple;
import de.uni_koblenz.jgralab.schema.GraphElementClass;

public class ResidualEvaluatorTest {

	private static Graph datagraph;

	@BeforeClass
	public static void setUpBeforeClass() throws GraphIOException {
		datagraph = GraphIO.loadGraphFromFile(
				"./testit/testgraphs/greqltestgraph.tg",
				ImplementationType.STANDARD, null);
	}

	@AfterClass
	public static void tearDownAfterClass() {
		datagraph = null;
	}

	private Object evaluateQuery(String query) {
		return GreqlQuery.createQuery(query).evaluate(datagraph);
	}

	@SuppressWarnings("unchecked")
	private <SC extends GraphElementClass<SC, IC>, IC extends GraphElement<SC, IC>> boolean isInstanceOf(
			GraphElement<SC, IC> v, String type) {
		return v.isInstanceOf((SC) datagraph.getSchema()
				.getAttributedElementClass(type));
	}

	private String createContainmentMessage(GraphElement<?, ?> v,
			boolean showWordNot) {
		return (v instanceof Vertex ? "v" : "e") + v.getId() + " of type "
				+ v.getAttributedElementClass().getQualifiedName() + " is "
				+ (showWordNot ? "not" : "") + " contained.";
	}

	private void compareResultSets(Object r1, Object r2)
			throws InstantiationException, IllegalAccessException {
		POrderedSet<?> result1 = (POrderedSet<?>) r1;
		assertNotNull(result1);

		POrderedSet<?> result2 = (POrderedSet<?>) r1;
		assertNotNull(result2);

		assertEquals(result1.size(), result2.size());
		for (int i = 0; i < result1.size(); i++) {
			assertEquals(result1.get(i), result2.get(i));
		}
	}

	public ExecutableQuery createQueryClass(String query, String classname)
			throws InstantiationException, IllegalAccessException {
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(), classname);
		return generatedQuery.newInstance();
	}

	/*
	 * VertexSetExpression
	 */

	@Test
	public void testVertexSetExpression_allVertices()
			throws InstantiationException, IllegalAccessException {
		String query = "V";
		@SuppressWarnings("unchecked")
		Set<Vertex> ergSet = (Set<Vertex>) evaluateQuery(query);
		assertEquals(datagraph.getVCount(), ergSet.size());
		for (Vertex v : datagraph.vertices()) {
			assertTrue(createContainmentMessage(v, true), ergSet.contains(v));
		}

		Object generatedResult = createQueryClass(query,
				"testdata.TestVertexSetExpression_allVertices").execute(
				datagraph);
		compareResultSets(ergSet, generatedResult);
	}

	@Test
	public void testVertexSetExpression_verticesOfOneType()
			throws InstantiationException, IllegalAccessException {
		String query = "import junctions.*;V{Crossroad}";
		@SuppressWarnings("unchecked")
		Set<Vertex> ergSet = (Set<Vertex>) evaluateQuery(query);
		for (Vertex v : datagraph.vertices()) {
			if (isInstanceOf(v, "junctions.Crossroad")) {
				assertTrue(createContainmentMessage(v, true),
						ergSet.contains(v));
			} else {
				assertFalse(createContainmentMessage(v, false),
						ergSet.contains(v));
			}
		}

		Object generatedResult = createQueryClass(query,
				"testdata.TestVertexSetExpression_verticesOfOneType").execute(
				datagraph);
		compareResultSets(ergSet, generatedResult);
	}

	@Test
	public void testVertexSetExpression_verticesOfExactlyOneType()
			throws InstantiationException, IllegalAccessException {
		String query = "import junctions.*;V{Crossroad!}";
		@SuppressWarnings("unchecked")
		Set<Vertex> ergSet = (Set<Vertex>) evaluateQuery(query);
		for (Vertex v : datagraph.vertices()) {
			if (isInstanceOf(v, "junctions.Crossroad")
					&& !isInstanceOf(v, "junctions.Plaza")
					&& !isInstanceOf(v, "junctions.Roundabout")) {
				assertTrue(createContainmentMessage(v, true),
						ergSet.contains(v));
			} else {
				assertFalse(createContainmentMessage(v, false),
						ergSet.contains(v));
			}
		}

		Object generatedResult = createQueryClass(query,
				"testdata.TestVertexSetExpression_verticesOfExactlyOneType")
				.execute(datagraph);
		compareResultSets(ergSet, generatedResult);
	}

	@Test
	public void testVertexSetExpression_verticesOfNotOneType()
			throws InstantiationException, IllegalAccessException {
		String query = "import junctions.*;V{^Crossroad}";
		@SuppressWarnings("unchecked")
		Set<Vertex> ergSet = (Set<Vertex>) evaluateQuery(query);
		for (Vertex v : datagraph.vertices()) {
			if (!isInstanceOf(v, "junctions.Crossroad")) {
				assertTrue(createContainmentMessage(v, true),
						ergSet.contains(v));
			} else {
				assertFalse(createContainmentMessage(v, false),
						ergSet.contains(v));
			}
		}

		Object generatedResult = createQueryClass(query,
				"testdata.TestVertexSetExpression_verticesOfNotOneType")
				.execute(datagraph);
		compareResultSets(ergSet, generatedResult);
	}

	@Test
	public void testVertexSetExpression_verticesOfNotExactlyOneType()
			throws InstantiationException, IllegalAccessException {
		String query = "import junctions.*;V{^Crossroad!}";
		@SuppressWarnings("unchecked")
		Set<Vertex> ergSet = (Set<Vertex>) evaluateQuery(query);
		for (Vertex v : datagraph.vertices()) {
			if (!(isInstanceOf(v, "junctions.Crossroad")
					&& !isInstanceOf(v, "junctions.Plaza") && !isInstanceOf(v,
						"junctions.Roundabout"))) {
				assertTrue(createContainmentMessage(v, true),
						ergSet.contains(v));
			} else {
				assertFalse(createContainmentMessage(v, false),
						ergSet.contains(v));
			}
		}

		Object generatedResult = createQueryClass(query,
				"testdata.TestVertexSetExpression_verticesOfNotExactlyOneType")
				.execute(datagraph);
		compareResultSets(ergSet, generatedResult);
	}

	@Test
	public void testVertexSetExpression_verticesOfOneTypeButNotASubtype()
			throws InstantiationException, IllegalAccessException {
		String query = "import junctions.*;V{^Plaza}";
		@SuppressWarnings("unchecked")
		Set<Vertex> ergSet = (Set<Vertex>) evaluateQuery(query);
		for (Vertex v : datagraph.vertices()) {
			if (isInstanceOf(v, "junctions.Plaza")) {
				assertFalse(createContainmentMessage(v, false),
						ergSet.contains(v));
			} else {
				assertTrue(createContainmentMessage(v, true),
						ergSet.contains(v));
			}
		}

		Object generatedResult = createQueryClass(query,
				"testdata.TestVertexSetExpression_verticesOfOneTypeButNotASubtype")
				.execute(datagraph);
		compareResultSets(ergSet, generatedResult);
	}

	// @Test
	// public void testVertexSetExpression_verticesOfNotOneTypeButASubtype() {
	// @SuppressWarnings("unchecked")
	// Set<Vertex> ergSet = (Set<Vertex>)
	// evaluateQuery("import junctions.*;V{^Crossroad,Plaza}");
	// for (Vertex v : datagraph.vertices()) {
	// if (!isInstanceOf(v, "junctions.Crossroad")
	// || isInstanceOf(v, "junctions.Plaza")) {
	// assertTrue(createContainmentMessage(v, true),
	// ergSet.contains(v));
	// } else {
	// assertFalse(createContainmentMessage(v, false),
	// ergSet.contains(v));
	// }
	// }
	// }
	//
	// @Test
	// public void testVertexSetExpression_verticesOfASubtypeNotButOneType() {
	// @SuppressWarnings("unchecked")
	// Set<Vertex> ergSet = (Set<Vertex>)
	// evaluateQuery("import junctions.*;V{Plaza,^Crossroad}");
	// for (Vertex v : datagraph.vertices()) {
	// if (!isInstanceOf(v, "junctions.Crossroad")
	// || isInstanceOf(v, "junctions.Plaza")) {
	// assertTrue(createContainmentMessage(v, true),
	// ergSet.contains(v));
	// } else {
	// assertFalse(createContainmentMessage(v, false),
	// ergSet.contains(v));
	// }
	// }
	// }

	/*
	 * EdgeSetExpression
	 */

	@Test
	public void testEdgeSetExpression_allEdges() throws InstantiationException,
			IllegalAccessException {
		String query = "E";
		@SuppressWarnings("unchecked")
		Set<Edge> ergSet = (Set<Edge>) evaluateQuery(query);
		assertEquals(datagraph.getECount(), ergSet.size());
		for (Edge e : datagraph.edges()) {
			assertTrue(createContainmentMessage(e, true), ergSet.contains(e));
		}

		Object generatedResult = createQueryClass(query,
				"testdata.TestEdgeSetExpression_allEdges").execute(datagraph);
		compareResultSets(ergSet, generatedResult);
	}

	@Test
	public void testEdgeSetExpression_edgesOfOneType()
			throws InstantiationException, IllegalAccessException {
		String query = "import connections.*;E{Street}";
		@SuppressWarnings("unchecked")
		Set<Edge> ergSet = (Set<Edge>) evaluateQuery(query);
		for (Edge e : datagraph.edges()) {
			if (isInstanceOf(e, "connections.Street")) {
				assertTrue(createContainmentMessage(e, true),
						ergSet.contains(e));
			} else {
				assertFalse(createContainmentMessage(e, false),
						ergSet.contains(e));
			}
		}

		Object generatedResult = createQueryClass(query,
				"testdata.TestEdgeSetExpression_edgesOfOneType").execute(
				datagraph);
		compareResultSets(ergSet, generatedResult);
	}

	@Test
	public void testEdgeSetExpression_edgesOfExactlyOneType()
			throws InstantiationException, IllegalAccessException {
		String query = "import connections.*;E{Street!}";
		@SuppressWarnings("unchecked")
		Set<Edge> ergSet = (Set<Edge>) evaluateQuery(query);
		for (Edge e : datagraph.edges()) {
			if (isInstanceOf(e, "connections.Street")
					&& !isInstanceOf(e, "connections.Highway")) {
				assertTrue(createContainmentMessage(e, true),
						ergSet.contains(e));
			} else {
				assertFalse(createContainmentMessage(e, false),
						ergSet.contains(e));
			}
		}

		Object generatedResult = createQueryClass(query,
				"testdata.TestEdgeSetExpression_edgesOfExactlyOneType")
				.execute(datagraph);
		compareResultSets(ergSet, generatedResult);
	}

	@Test
	public void testEdgeSetExpression_edgesOfNotOneType()
			throws InstantiationException, IllegalAccessException {
		String query = "import connections.*;E{^Street}";
		@SuppressWarnings("unchecked")
		Set<Edge> ergSet = (Set<Edge>) evaluateQuery(query);
		for (Edge e : datagraph.edges()) {
			if (!isInstanceOf(e, "connections.Street")) {
				assertTrue(createContainmentMessage(e, true),
						ergSet.contains(e));
			} else {
				assertFalse(createContainmentMessage(e, false),
						ergSet.contains(e));
			}
		}

		Object generatedResult = createQueryClass(query,
				"testdata.TestEdgeSetExpression_edgesOfNotOneType").execute(
				datagraph);
		compareResultSets(ergSet, generatedResult);
	}

	@Test
	public void testEdgeSetExpression_edgesOfNotExactlyOneType()
			throws InstantiationException, IllegalAccessException {
		String query = "import connections.*;E{^Street!}";
		@SuppressWarnings("unchecked")
		Set<Edge> ergSet = (Set<Edge>) evaluateQuery(query);
		for (Edge e : datagraph.edges()) {
			if (!(isInstanceOf(e, "connections.Street") && !isInstanceOf(e,
					"connections.Highway"))) {
				assertTrue(createContainmentMessage(e, true),
						ergSet.contains(e));
			} else {
				assertFalse(createContainmentMessage(e, false),
						ergSet.contains(e));
			}
		}

		Object generatedResult = createQueryClass(query,
				"testdata.TestEdgeSetExpression_edgesOfNotExactlyOneType")
				.execute(datagraph);
		compareResultSets(ergSet, generatedResult);
	}

	@Test
	public void testEdgeSetExpression_edgesOfOneTypeButNotASubtype()
			throws InstantiationException, IllegalAccessException {
		String query = "import connections.*;E{^Highway}";
		@SuppressWarnings("unchecked")
		Set<Edge> ergSet = (Set<Edge>) evaluateQuery(query);
		for (Edge e : datagraph.edges()) {
			if (isInstanceOf(e, "connections.Highway")) {
				assertFalse(createContainmentMessage(e, false),
						ergSet.contains(e));
			} else {
				assertTrue(createContainmentMessage(e, true),
						ergSet.contains(e));
			}
		}

		Object generatedResult = createQueryClass(query,
				"testdata.TestEdgeSetExpression_edgesOfOneTypeButNotASubtype")
				.execute(datagraph);
		compareResultSets(ergSet, generatedResult);
	}

	// @Test
	// public void testEdgeSetExpression_edgesOfNotOneTypeButASubtype() {
	// @SuppressWarnings("unchecked")
	// Set<Edge> ergSet = (Set<Edge>)
	// evaluateQuery("import connections.*;E{^Street,Highway}");
	// for (Edge e : datagraph.edges()) {
	// if (!isInstanceOf(e, "connections.Street")
	// || isInstanceOf(e, "connections.Highway")) {
	// assertTrue(createContainmentMessage(e, true),
	// ergSet.contains(e));
	// } else {
	// assertFalse(createContainmentMessage(e, false),
	// ergSet.contains(e));
	// }
	// }
	// }
	//
	// @Test
	// public void testEdgeSetExpression_edgesOfASubtypeNotButOneType() {
	// @SuppressWarnings("unchecked")
	// Set<Edge> ergSet = (Set<Edge>)
	// evaluateQuery("import connections.*;E{Highway,^Street}");
	// for (Edge e : datagraph.edges()) {
	// if (!isInstanceOf(e, "connections.Street")
	// || isInstanceOf(e, "connections.Highway")) {
	// assertTrue(createContainmentMessage(e, true),
	// ergSet.contains(e));
	// } else {
	// assertFalse(createContainmentMessage(e, false),
	// ergSet.contains(e));
	// }
	// }
	// }

	/*
	 * TypeIdEvaluator
	 */

	@Test(expected = UnknownTypeException.class)
	public void testTypeId_UnknownType() {
		evaluateQuery("import junctions.*;V{UnknownType}");
	}

	@Test(expected = UnknownTypeException.class)
	public void testTypeId_UnknownType_Generated()
			throws InstantiationException, IllegalAccessException {
		String query = "import junctions.*;V{UnknownType}";
		createQueryClass(query, "testdata.TestTypeId_UnknownType_Generated")
				.execute(datagraph);
	}

	/*
	 * QuantifiedExpressionEvaluator
	 */

	@Test
	public void testQuantifiedExpressionEvaluator_forall_true()
			throws InstantiationException, IllegalAccessException {
		String query = "forall n:list(1..9)@n>0";
		assertTrue((Boolean) evaluateQuery(query));
		assertTrue((Boolean) createQueryClass(query,
				"testdata.TestQuantifiedExpressionEvaluator_forall_true")
				.execute(datagraph));
	}

	@Test
	public void testQuantifiedExpressionEvaluator_forall_false()
			throws InstantiationException, IllegalAccessException {
		String query = "forall n:list(1..9)@n<0";
		assertFalse((Boolean) evaluateQuery(query));
		assertFalse((Boolean) createQueryClass(query,
				"testdata.TestQuantifiedExpressionEvaluator_forall_false")
				.execute(datagraph));
	}

	@Test
	public void testQuantifiedExpressionEvaluator_forall_false_onlyone()
			throws InstantiationException, IllegalAccessException {
		String query = "forall n:list(1..9)@n<9";
		assertFalse((Boolean) evaluateQuery(query));
		assertFalse((Boolean) createQueryClass(query,
				"testdata.TestQuantifiedExpressionEvaluator_forall_false_onlyone")
				.execute(datagraph));
	}

	@Test
	public void testQuantifiedExpressionEvaluator_forall_withNonBooleanPredicate()
			throws InstantiationException, IllegalAccessException {
		String query = "forall n:list(1..9)@V{}";
		assertTrue((Boolean) evaluateQuery(query));
		assertTrue((Boolean) createQueryClass(query,
				"testdata.TestQuantifiedExpressionEvaluator_forall_withNonBooleanPredicate")
				.execute(datagraph));
	}

	@Test
	public void testQuantifiedExpressionEvaluator_eixtst_true()
			throws InstantiationException, IllegalAccessException {
		String query = "exists n:list(1..9)@n>0";
		assertTrue((Boolean) evaluateQuery(query));
		assertTrue((Boolean) createQueryClass(query,
				"testdata.TestQuantifiedExpressionEvaluator_eixtst_true")
				.execute(datagraph));
	}

	@Test
	public void testQuantifiedExpressionEvaluator_eixtst_onlyone()
			throws InstantiationException, IllegalAccessException {
		String query = "exists n:list(1..9)@n>8";
		assertTrue((Boolean) evaluateQuery(query));
		assertTrue((Boolean) createQueryClass(query,
				"testdata.TestQuantifiedExpressionEvaluator_eixtst_onlyone")
				.execute(datagraph));
	}

	@Test
	public void testQuantifiedExpressionEvaluator_exists_false()
			throws InstantiationException, IllegalAccessException {
		String query = "exists n:list(1..9)@n<0";
		assertFalse((Boolean) evaluateQuery(query));
		assertFalse((Boolean) createQueryClass(query,
				"testdata.TestQuantifiedExpressionEvaluator_exists_false")
				.execute(datagraph));
	}

	@Test
	public void testQuantifiedExpressionEvaluator_exists_withNonBooleanPredicate()
			throws InstantiationException, IllegalAccessException {
		String query = "exists n:list(1..9)@V{}";
		assertTrue((Boolean) evaluateQuery(query));
		assertTrue((Boolean) createQueryClass(query,
				"testdata.TestQuantifiedExpressionEvaluator_exists_withNonBooleanPredicate")
				.execute(datagraph));
	}

	@Test
	public void testQuantifiedExpressionEvaluator_eixtstExactly_true()
			throws InstantiationException, IllegalAccessException {
		String query = "exists! n:list(1..9)@n=5";
		assertTrue((Boolean) evaluateQuery(query));
		assertTrue((Boolean) createQueryClass(query,
				"testdata.TestQuantifiedExpressionEvaluator_eixtstExactly_true")
				.execute(datagraph));
	}

	@Test
	public void testQuantifiedExpressionEvaluator_existsExactly_false_severalExists()
			throws InstantiationException, IllegalAccessException {
		String query = "exists! n:list(1..9)@n>0";
		assertFalse((Boolean) evaluateQuery(query));
		assertFalse((Boolean) createQueryClass(query,
				"testdata.TestQuantifiedExpressionEvaluator_existsExactly_false_severalExists")
				.execute(datagraph));
	}

	@Test
	public void testQuantifiedExpressionEvaluator_existsExactly_false_noneExists()
			throws InstantiationException, IllegalAccessException {
		String query = "exists! n:list(1..9)@n<0";
		assertFalse((Boolean) evaluateQuery(query));
		assertFalse((Boolean) createQueryClass(query,
				"testdata.TestQuantifiedExpressionEvaluator_existsExactly_false_noneExists")
				.execute(datagraph));
	}

	@Test
	public void testQuantifiedExpressionEvaluator_existsExactly_withNonBooleanPredicate()
			throws InstantiationException, IllegalAccessException {
		String query = "exists! n:list(1..9)@V{}";
		assertFalse((Boolean) evaluateQuery(query));
		assertFalse((Boolean) createQueryClass(
				query,
				"testdata.TestQuantifiedExpressionEvaluator_existsExactly_withNonBooleanPredicate")
				.execute(datagraph));
	}

	@Test
	public void testQuantifiedExpressionEvaluator_existsExactly_withNonBooleanPredicate_OnlyOneElem()
			throws InstantiationException, IllegalAccessException {
		String query = "exists! n:list(1..1)@V{}";
		assertTrue((Boolean) evaluateQuery(query));
		assertTrue((Boolean) createQueryClass(
				query,
				"testdata.TestQuantifiedExpressionEvaluator_existsExactly_withNonBooleanPredicate_OnlyOneElem")
				.execute(datagraph));
	}

	/*
	 * ConditionalExpression
	 */

	@Test
	public void testConditionalExpressionEvaluator_true()
			throws InstantiationException, IllegalAccessException {
		String query = "1=1?1:2";
		assertEquals(1, evaluateQuery(query));
		assertEquals(
				1,
				createQueryClass(query,
						"testdata.TestConditionalExpressionEvaluator_true")
						.execute(datagraph));
	}

	@Test
	public void testConditionalExpressionEvaluator_false()
			throws InstantiationException, IllegalAccessException {
		String query = "1=2?1:2";
		assertEquals(2, evaluateQuery(query));
		assertEquals(
				2,
				createQueryClass(query,
						"testdata.TestConditionalExpressionEvaluator_false")
						.execute(datagraph));
	}

	/*
	 * FWRExpression
	 */

	@Test
	public void testFWRExpression_reportSet() throws InstantiationException,
			IllegalAccessException {
		String queryText = "from n:list(1..3) with true reportSet n end";
		for (GreqlQuery query : new GreqlQuery[] {
				GreqlQuery.createQuery(queryText),
				(GreqlQuery) createQueryClass(queryText,
						"testdata.TestFWRExpression_reportSet") }) {
			Set<?> ergSet = (Set<?>) query.evaluate(datagraph);
			assertEquals(3, ergSet.size());
			assertTrue(ergSet.contains(1));
			assertTrue(ergSet.contains(2));
			assertTrue(ergSet.contains(3));
		}
	}

	@Test
	public void testFWRExpression_reportSet_2() throws InstantiationException,
			IllegalAccessException {
		String queryText = "from n, m:list(1..3) with true reportSet n, m end";
		for (GreqlQuery query : new GreqlQuery[] {
				GreqlQuery.createQuery(queryText),
				(GreqlQuery) createQueryClass(queryText,
						"testdata.TestFWRExpression_reportSet_2") }) {
			ArrayPSet<?> ergList = (ArrayPSet<?>) query.evaluate(datagraph);
			int numberOfErg = 0;
			for (int n = 1; n <= 3; n++) {
				for (int m = 1; m <= 3; m++) {
					Tuple tuple = (Tuple) ergList.get(numberOfErg++);
					assertEquals(2, tuple.size());
					assertEquals(n, tuple.get(0));
					assertEquals(m, tuple.get(1));
				}
			}
		}
	}

	@Test
	public void testFWRExpression_reportSetN() throws InstantiationException,
			IllegalAccessException {
		String queryText = "from n:list(10..100) with true reportSetN 10: n end";
		for (GreqlQuery query : new GreqlQuery[] {
				GreqlQuery.createQuery(queryText),
				(GreqlQuery) createQueryClass(queryText,
						"testdata.TestFWRExpression_reportSetN") }) {
			Set<?> ergSet = (Set<?>) query.evaluate(datagraph);
			assertEquals(10, ergSet.size());
			for (int i = 10; i < 20; i++) {
				assertTrue(ergSet.contains(i));
			}
		}
	}

	@Test
	public void testFWRExpression_reportList() throws InstantiationException,
			IllegalAccessException {
		String queryText = "from n:list(1..3) with true reportList n end";
		for (GreqlQuery query : new GreqlQuery[] {
				GreqlQuery.createQuery(queryText),
				(GreqlQuery) createQueryClass(queryText,
						"testdata.TestFWRExpression_reportList") }) {
			List<?> ergList = (List<?>) query.evaluate(datagraph);
			assertEquals(3, ergList.size());
			assertEquals(1, ergList.get(0));
			assertEquals(2, ergList.get(1));
			assertEquals(3, ergList.get(2));
		}
	}

	@Test
	public void testFWRExpression_reportList_2() throws InstantiationException,
			IllegalAccessException {
		String queryText = "from n, m:list(1..3) with true reportList n, m end";
		for (GreqlQuery query : new GreqlQuery[] {
				GreqlQuery.createQuery(queryText),
				(GreqlQuery) createQueryClass(queryText,
						"testdata.TestFWRExpression_reportList_2") }) {
			PVector<?> ergList = (PVector<?>) query.evaluate(datagraph);
			int numberOfErg = 0;
			for (int n = 1; n <= 3; n++) {
				for (int m = 1; m <= 3; m++) {
					Tuple tuple = (Tuple) ergList.get(numberOfErg++);
					assertEquals(2, tuple.size());
					assertEquals(n, tuple.get(0));
					assertEquals(m, tuple.get(1));
				}
			}
		}
	}

	@Test
	public void testFWRExpression_reportList_3() throws InstantiationException,
			IllegalAccessException {
		String queryText = "from n:list(1..3) with true reportList n, n end";
		for (GreqlQuery query : new GreqlQuery[] {
				GreqlQuery.createQuery(queryText),
				(GreqlQuery) createQueryClass(queryText,
						"testdata.TestFWRExpression_reportList_3") }) {
			PVector<?> ergList = (PVector<?>) query.evaluate(datagraph);
			int numberOfErg = 0;
			for (int n = 1; n <= 3; n++) {
				Tuple tuple = (Tuple) ergList.get(numberOfErg++);
				assertEquals(2, tuple.size());
				assertEquals(n, tuple.get(0));
				assertEquals(n, tuple.get(1));
			}
		}
	}

	@Test
	public void testFWRExpression_reportListN() throws InstantiationException,
			IllegalAccessException {
		String queryText = "from n:list(10..100) with true reportListN 10: n end";
		for (GreqlQuery query : new GreqlQuery[] {
				GreqlQuery.createQuery(queryText),
				(GreqlQuery) createQueryClass(queryText,
						"testdata.TestFWRExpression_reportListN") }) {
			List<?> ergList = (List<?>) query.evaluate(datagraph);
			assertEquals(10, ergList.size());
			for (int i = 0; i < ergList.size(); i++) {
				assertEquals(i + 10, ergList.get(i));
			}
		}
	}

	@Test
	public void testFWRExpression_reportMap() throws InstantiationException,
			IllegalAccessException {
		String queryText = "from n:list(1,2) with true reportMap n->getVertex(n) end";
		for (GreqlQuery query : new GreqlQuery[] {
				GreqlQuery.createQuery(queryText),
				(GreqlQuery) createQueryClass(queryText,
						"testdata.TestFWRExpression_reportMap") }) {
			Map<?, ?> ergMap = (Map<?, ?>) query.evaluate(datagraph);
			assertEquals(2, ergMap.size());
			assertEquals(datagraph.getVertex(1), ergMap.get(1));
			assertEquals(datagraph.getVertex(2), ergMap.get(2));
		}
	}

	@Test
	public void testFWRExpression_reportMap_2() throws InstantiationException,
			IllegalAccessException {
		String queryText = "from n,m:list(1,2) with true reportMap n->m end";
		for (GreqlQuery query : new GreqlQuery[] {
				GreqlQuery.createQuery(queryText),
				(GreqlQuery) createQueryClass(queryText,
						"testdata.TestFWRExpression_reportMap_2") }) {
			Map<?, ?> ergMap = (Map<?, ?>) query.evaluate(datagraph);
			assertEquals(2, ergMap.size());
			assertEquals(2, ergMap.get(1));
			assertEquals(2, ergMap.get(2));
		}
	}

	@Test
	public void testFWRExpression_reportMapN() throws InstantiationException,
			IllegalAccessException {
		String queryText = "from n:list(1..100) with true reportMapN 10: n->getVertex(n) end";
		for (GreqlQuery query : new GreqlQuery[] {
				GreqlQuery.createQuery(queryText),
				(GreqlQuery) createQueryClass(queryText,
						"testdata.TestFWRExpression_reportMapN") }) {
			Map<?, ?> ergMap = (Map<?, ?>) query.evaluate(datagraph);
			assertEquals(10, ergMap.size());
			for (int i = 1; i <= 10; i++) {
				assertEquals(datagraph.getVertex(i), ergMap.get(i));
			}
		}
	}

	@Test
	public void testFWRExpression_reportTable_oneNamedColumn()
			throws InstantiationException, IllegalAccessException {
		String queryText = "from n:list(1..3) report n as \"Column1\" end";
		for (GreqlQuery query : new GreqlQuery[] {
				GreqlQuery.createQuery(queryText),
				(GreqlQuery) createQueryClass(queryText,
						"testdata.TestFWRExpression_reportTable_oneNamedColumn") }) {
			Table<?> ergTable = (Table<?>) query.evaluate(datagraph);
			assertEquals(3, ergTable.size());
			assertEquals(1, ergTable.get(0));
			assertEquals(2, ergTable.get(1));
			assertEquals(3, ergTable.get(2));
			PVector<String> titles = ergTable.getTitles();
			assertEquals(1, titles.size());
			assertEquals("Column1", titles.get(0));
		}
	}

	@Test
	public void testFWRExpression_reportTable_twoNamedColumns()
			throws InstantiationException, IllegalAccessException {
		String queryText = "from x:list(1..3) report x as \"Column1\", x*x as \"Column2\" end";
		for (GreqlQuery query : new GreqlQuery[] {
				GreqlQuery.createQuery(queryText),
				(GreqlQuery) createQueryClass(queryText,
						"testdata.TestFWRExpression_reportTable_twoNamedColumns") }) {
			Table<?> ergTable = (Table<?>) query.evaluate(datagraph);
			assertEquals(3, ergTable.size());
			for (int i = 0; i < ergTable.size(); i++) {
				Tuple ergTuple = (Tuple) ergTable.get(i);
				int x = i + 1;
				assertEquals(x, ergTuple.get(0));
				assertEquals(x * x, ergTuple.get(1));
			}
			PVector<String> titles = ergTable.getTitles();
			assertEquals(2, titles.size());
			assertEquals("Column1", titles.get(0));
			assertEquals("Column2", titles.get(1));
		}
	}

	/*
	 * Missing FunctionApplication tests
	 */

	@Test
	public void testFunctionApplication_withEvaluatorParam()
			throws InstantiationException, IllegalAccessException {
		String query = "isReachable(getVertex(1),getVertex(2),<->*)";
		assertTrue((Boolean) evaluateQuery(query));
	}

	@Test
	public void testFunctionApplication_callSameFunctionSeveralTimes()
			throws InstantiationException, IllegalAccessException {
		String query = "and(isReachable(getVertex(1),getVertex(23),-->),isReachable(getVertex(1),getVertex(2),-->))";
		assertFalse((Boolean) evaluateQuery(query));
	}

	/*
	 * miscellaneous
	 */

	@Test
	public void testUseQuerySeveralTimes() throws InstantiationException,
			IllegalAccessException {
		SubGraphMarker oldResult = null;
		String queryText = "slice(getVertex(1),-->)";
		for (GreqlQuery query : new GreqlQuery[] {
				GreqlQuery.createQuery(queryText),
				(GreqlQuery) createQueryClass(queryText,
						"testdata.TestUseQuerySeveralTimes") }) {
			SubGraphMarker slice1 = (SubGraphMarker) query.evaluate(datagraph);
			SubGraphMarker slice2 = (SubGraphMarker) query.evaluate(datagraph);
			compareSubGraphs(slice1, slice2);
			if (oldResult != null) {
				compareSubGraphs(oldResult, slice1);
			}
			oldResult = slice1;
		}
	}

	private void compareSubGraphs(SubGraphMarker slice1, SubGraphMarker slice2) {
		assertEquals(slice1.getVCount(), slice2.getVCount());
		assertEquals(slice1.getECount(), slice2.getECount());
		for (GraphElement<?, ?> markedElement : slice1.getMarkedElements()) {
			assertTrue(slice2.isMarked(markedElement));
		}
		for (GraphElement<?, ?> markedElement : slice2.getMarkedElements()) {
			assertTrue(slice1.isMarked(markedElement));
		}
	}

	@Test
	public void testValueChangeOfUsedVariable_usingQuery()
			throws InstantiationException, IllegalAccessException {
		String queryText = "using x: x+3";
		for (GreqlQuery query : new GreqlQuery[] {
				GreqlQuery.createQuery(queryText),
				(GreqlQuery) createQueryClass(queryText,
						"testdata.TestValueChangeOfUsedVariable_usingQuery") }) {
			GreqlEnvironment environment = new GreqlEnvironmentAdapter();
			environment.setVariable("x", 3);
			assertEquals(6, query.evaluate(null, environment));

			environment.setVariable("x", 4);
			assertEquals(7, query.evaluate(null, environment));
		}
	}

	@Test
	public void testValueChangeOfUsedVariable_usingGreqlEvaluator()
			throws InstantiationException, IllegalAccessException {
		String queryText = "using x: x+3";
		for (GreqlQuery query : new GreqlQuery[] {
				GreqlQuery.createQuery(queryText),
				(GreqlQuery) createQueryClass(queryText,
						"testdata.TestValueChangeOfUsedVariable_usingGreqlEvaluator") }) {

			GreqlEnvironment environment = new GreqlEnvironmentAdapter();
			environment.setVariable("x", 3);
			assertEquals(6, query.evaluate(datagraph, environment));

			environment.setVariable("x", 4);
			assertEquals(7, query.evaluate(datagraph, environment));
		}
	}

	@Test
	public void testValueChangeOfUsedVariable_usingGreqlEvaluator2()
			throws InstantiationException, IllegalAccessException {
		String queryText = "using x: x+3";
		for (GreqlQuery query : new GreqlQuery[] {
				GreqlQuery.createQuery(queryText),
				(GreqlQuery) createQueryClass(queryText,
						"testdata.TestValueChangeOfUsedVariable_usingGreqlEvaluator2") }) {

			GreqlEnvironment environment = new GreqlEnvironmentAdapter();
			environment.setVariable("x", 3);
			assertEquals(6, query.evaluate(null, environment));

			environment.setVariable("x", 4);
			assertEquals(7, query.evaluate(null, environment));
		}
	}

}
