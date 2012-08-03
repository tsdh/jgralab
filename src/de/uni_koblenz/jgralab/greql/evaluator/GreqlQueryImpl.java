/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2012 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         https://github.com/jgralab/jgralab
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses>.
 * 
 * Additional permission under GNU GPL version 3 section 7
 * 
 * If you modify this Program, or any covered work, by linking or combining
 * it with Eclipse (or a modified version of that program or an Eclipse
 * plugin), containing parts covered by the terms of the Eclipse Public
 * License (EPL), the licensors of this Program grant you additional
 * permission to convey the resulting work.  Corresponding Source for a
 * non-source form of such a combination shall include the source code for
 * the parts of JGraLab used as well as that of the covered work.
 */
package de.uni_koblenz.jgralab.greql.evaluator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.pcollections.PSet;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.GraphStructureChangedListener;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.ProgressFunction;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.GraphMarker;
import de.uni_koblenz.jgralab.greql.GreqlEnvironment;
import de.uni_koblenz.jgralab.greql.GreqlQuery;
import de.uni_koblenz.jgralab.greql.OptimizerInfo;
import de.uni_koblenz.jgralab.greql.evaluator.vertexeval.VertexEvaluator;
import de.uni_koblenz.jgralab.greql.optimizer.DefaultOptimizer;
import de.uni_koblenz.jgralab.greql.optimizer.Optimizer;
import de.uni_koblenz.jgralab.greql.optimizer.OptimizerUtility;
import de.uni_koblenz.jgralab.greql.parser.GreqlParser;
import de.uni_koblenz.jgralab.greql.schema.GreqlExpression;
import de.uni_koblenz.jgralab.greql.schema.GreqlGraph;
import de.uni_koblenz.jgralab.greql.schema.GreqlVertex;
import de.uni_koblenz.jgralab.greql.schema.Identifier;
import de.uni_koblenz.jgralab.greql.schema.Variable;
import de.uni_koblenz.jgralab.impl.ConsoleProgressFunction;
import de.uni_koblenz.jgralab.impl.GraphBaseImpl;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.Schema;

public class GreqlQueryImpl extends GreqlQuery implements
		GraphStructureChangedListener {
	private final String queryText;
	private GreqlGraph queryGraph;
	private PSet<String> usedVariables;
	private PSet<String> storedVariables;
	private final boolean optimize;
	private long optimizationTime = -1;
	private long parseTime = -1;
	private final OptimizerInfo optimizerInfo;
	private Optimizer optimizer;
	// private final boolean useSavedOptimizedSyntaxGraph = true;
	private GreqlExpression rootExpression;

	/**
	 * Print the text representation of the optimized query after optimization.
	 */
	public static boolean DEBUG_OPTIMIZATION = Boolean.parseBoolean(System
			.getProperty("greqlDebugOptimization", "false"));

	/**
	 * The {@link Map} of SimpleName to Type of types that is known in the
	 * evaluator by import statements in the greql query
	 */
	protected Map<Schema, Map<String, AttributedElementClass<?, ?>>> knownTypes = new HashMap<Schema, Map<String, AttributedElementClass<?, ?>>>();

	/**
	 * The {@link GraphMarker} that stores all vertex evaluators
	 */
	private GraphMarker<VertexEvaluator<? extends GreqlVertex>> vertexEvaluators;

	public GreqlQueryImpl(String queryText) {
		this(queryText, true);
	}

	public GreqlQueryImpl(String queryText, boolean optimize) {
		this(queryText, optimize, OptimizerUtility.getDefaultOptimizerInfo());
	}

	public GreqlQueryImpl(String queryText, OptimizerInfo optimizerInfo) {
		this(queryText, true, optimizerInfo);
	}

	public GreqlQueryImpl(String queryText, Optimizer optimizer) {
		this(queryText, optimizer != null, OptimizerUtility
				.getDefaultOptimizerInfo(), optimizer);
	}

	public GreqlQueryImpl(String queryText, boolean optimize,
			OptimizerInfo optimizerInfo) {
		this.queryText = queryText;
		this.optimize = optimize;
		this.optimizerInfo = optimizerInfo == null ? OptimizerUtility
				.getDefaultOptimizerInfo() : optimizerInfo;
		knownTypes = new HashMap<Schema, Map<String, AttributedElementClass<?, ?>>>();
		initializeQueryGraph();
	}

	public GreqlQueryImpl(String queryText, boolean optimize,
			OptimizerInfo optimizerInfo, Optimizer optimizer) {
		this.queryText = queryText;
		this.optimize = optimize;
		this.optimizerInfo = optimizerInfo == null ? OptimizerUtility
				.getDefaultOptimizerInfo() : optimizerInfo;
		knownTypes = new HashMap<Schema, Map<String, AttributedElementClass<?, ?>>>();
		this.optimizer = optimizer == null ? new DefaultOptimizer() : optimizer;
		initializeQueryGraph();
	}

	@Override
	public GreqlGraph getQueryGraph() {
		return queryGraph;
	}

	@SuppressWarnings("unchecked")
	public synchronized <V extends GreqlVertex> VertexEvaluator<V> getVertexEvaluator(
			V vertex) {
		return (VertexEvaluator<V>) vertexEvaluators.get(vertex);
	}

	private void initializeQueryGraph() {
		if (queryGraph == null) {
			long t0 = System.currentTimeMillis();

			queryGraph = GreqlParserWithVertexEvaluatorUpdates.parse(queryText,
					this, new HashSet<String>());
			long t1 = System.currentTimeMillis();
			parseTime = t1 - t0;
			if (optimize) {
				(optimizer == null ? new DefaultOptimizer() : optimizer)
						.optimize(this);
				optimizationTime = System.currentTimeMillis() - t1;
				if (DEBUG_OPTIMIZATION) {
					System.out
							.println("#########################################################");
					System.out
							.println("################## Unoptimized Query ####################");
					System.out
							.println("#########################################################");
					String name = "__greql-query.";
					try {
						queryGraph.save(name + "tg",
								new ConsoleProgressFunction(
										"Saving broken GReQL graph:"));
						printGraphAsDot(queryGraph, true, name + "dot");
					} catch (GraphIOException e) {
						e.printStackTrace();
					}
					System.out.println("Saved query graph to " + name
							+ "tg/dot.");
					System.out
							.println("#########################################################");
				}
			}
			((GraphBaseImpl) queryGraph).defragment();
			rootExpression = queryGraph.getFirstGreqlExpression();
			initializeVertexEvaluatorsMarker(queryGraph);
			// queryGraphCache.put(queryText, optimize, queryGraph,
			// vertexEvaluators, knownTypes, optimizationTime, parseTime);
		}
	}

	private void printGraphAsDot(Graph graph, boolean reversedEdges,
			String outputFilename) {

		try {
			Class<?> tg2DotClass = Class
					.forName("de.uni_koblenz.jgralab.utilities.tg2dot.Tg2Dot");
			Method printMethod = tg2DotClass.getMethod("convertGraph",
					Graph.class, String.class, boolean.class);
			printMethod.invoke(tg2DotClass, new Object[] { graph,
					outputFilename, reversedEdges });
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	private void initializeVertexEvaluatorsMarker(GreqlGraph graph) {
		if (vertexEvaluators == null) {
			vertexEvaluators = new GraphMarker<VertexEvaluator<?>>(graph);
		}
	}

	/**
	 * clears the tempresults that are stored in the GreqlEvaluators-Objects at
	 * the syntaxgraph nodes
	 * 
	 * @param optimizer
	 */
	void resetVertexEvaluators(InternalGreqlEvaluator evaluator) {
		GreqlGraph queryGraph = getQueryGraph();
		GreqlVertex currentVertex = (GreqlVertex) queryGraph.getFirstVertex();
		while (currentVertex != null) {
			VertexEvaluator<?> vertexEval = vertexEvaluators
					.getMark(currentVertex);
			if (vertexEval != null) {
				vertexEval.resetToInitialState(evaluator);
			}
			currentVertex = (GreqlVertex) currentVertex.getNextVertex();
		}
	}

	@Override
	public Set<String> getUsedVariables() {
		if (usedVariables == null) {
			usedVariables = JGraLab.set();
			GreqlExpression expr = getRootExpression();
			if (expr != null) {
				for (Variable v : expr.get_boundVar()) {
					usedVariables = usedVariables.plus(v.get_name());
				}
			}
		}
		return usedVariables;
	}

	@Override
	public Set<String> getStoredVariables() {
		if (storedVariables == null) {
			storedVariables = JGraLab.set();
			GreqlExpression expr = getRootExpression();
			if (expr != null) {
				Identifier id = expr.get_identifier();
				if (id != null) {
					storedVariables = storedVariables.plus(id.get_name());
				}
			}
		}
		return storedVariables;
	}

	@Override
	public String getQueryText() {
		return queryText;
	}

	@Override
	public GreqlExpression getRootExpression() {
		getQueryGraph();
		return rootExpression;
	}

	/**
	 * @return the time needed for optimizing the query or -1 if no optimization
	 *         was done.
	 */
	@Override
	public long getOptimizationTime() {
		return optimizationTime;
	}

	/**
	 * @return the time needed for parsing the query.
	 */
	@Override
	public long getParseTime() {
		return parseTime;
	}

	/**
	 * @param typeSimpleName
	 *            {@link String} the simple name of the needed
	 *            {@link AttributedElementClass}
	 * @return {@link AttributedElementClass} of the datagraph with the name
	 *         <code>name</code>
	 */
	public synchronized AttributedElementClass<?, ?> getKnownType(
			Schema schema, String typeSimpleName) {
		return knownTypes.get(schema).get(typeSimpleName);
	}

	/**
	 * @param elem
	 *            {@link AttributedElementClass} which will be added to the
	 *            {@link #knownTypes} with its simple name as key.
	 * @return @see {@link Map#put(Object, Object)}
	 */
	public synchronized AttributedElementClass<?, ?> addKnownType(
			Schema schema, AttributedElementClass<?, ?> elem) {
		Map<String, AttributedElementClass<?, ?>> kTypes = knownTypes
				.get(schema);
		if (kTypes == null) {
			kTypes = new HashMap<String, AttributedElementClass<?, ?>>();
			knownTypes.put(schema, kTypes);
		}
		return kTypes.put(elem.getSimpleName(), elem);
	}

	@Override
	public void vertexAdded(Vertex v) {
		try {
			vertexEvaluators.mark(v, VertexEvaluator.createVertexEvaluator(
					(GreqlVertex) v, this));
		} catch (RuntimeException e) {
			if (!(e.getCause() instanceof ClassNotFoundException)) {
				// Some vertices of the query graph do not have an Evaluator
				// e.g. Definition
				throw e;
			}
		}
	}

	@Override
	public void vertexDeleted(Vertex v) {
		vertexEvaluators.removeMark(v);
	}

	@Override
	public void edgeAdded(Edge e) {
	}

	@Override
	public void edgeDeleted(Edge e) {
	}

	@Override
	public void maxEdgeCountIncreased(int newValue) {
	}

	@Override
	public void maxVertexCountIncreased(int newValue) {
	}

	public OptimizerInfo getOptimizerInfo() {
		return optimizerInfo;
	}

	private static class GreqlParserWithVertexEvaluatorUpdates extends
			GreqlParser {

		public GreqlParserWithVertexEvaluatorUpdates(String source,
				Set<String> subQueryNames, GreqlQueryImpl gscl) {
			super(source, subQueryNames);
			if (gscl != null) {
				graph.addGraphStructureChangedListener(gscl);
				gscl.initializeVertexEvaluatorsMarker(graph);
			}
		}

		public static GreqlGraph parse(String query, GreqlQueryImpl gscl,
				Set<String> subQueryNames) {
			return parse(query, subQueryNames, gscl);
		}

		public static GreqlGraph parse(String query,
				Set<String> subQueryNames, GreqlQueryImpl gscl) {
			GreqlParser parser = new GreqlParserWithVertexEvaluatorUpdates(
					query, subQueryNames, gscl);
			parser.parse();
			return parser.getGraph();
		}

	}

	@Override
	public Object evaluate() {
		return evaluate(null, new GreqlEnvironmentAdapter(), null);
	}

	@Override
	public Object evaluate(Graph datagraph) {
		return evaluate(datagraph, new GreqlEnvironmentAdapter(), null);
	}

	@Override
	public Object evaluate(Graph datagraph, GreqlEnvironment environment) {
		return evaluate(datagraph, environment, null);
	}

	@Override
	public Object evaluate(Graph datagraph, ProgressFunction progressFunction) {
		return evaluate(datagraph, new GreqlEnvironmentAdapter(),
				progressFunction);
	}

	@Override
	public Object evaluate(Graph datagraph, GreqlEnvironment environment,
			ProgressFunction progressFunction) {
		Object result = new GreqlEvaluatorImpl(this, datagraph, environment,
				progressFunction).getResult();
		return result;
	}

	@Override
	public String toString() {
		return queryText;
	}
}
