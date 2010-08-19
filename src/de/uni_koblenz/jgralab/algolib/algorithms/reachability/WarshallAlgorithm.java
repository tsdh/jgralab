package de.uni_koblenz.jgralab.algolib.algorithms.reachability;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmStates;
import de.uni_koblenz.jgralab.algolib.algorithms.GraphAlgorithm;
import de.uni_koblenz.jgralab.algolib.algorithms.reachability.visitors.TransitiveVisitorComposition;
import de.uni_koblenz.jgralab.algolib.algorithms.search.BreadthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.SearchAlgorithm;
import de.uni_koblenz.jgralab.algolib.functions.ArrayBinaryFunction;
import de.uni_koblenz.jgralab.algolib.functions.ArrayRelation;
import de.uni_koblenz.jgralab.algolib.functions.BinaryFunction;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.functions.IntFunction;
import de.uni_koblenz.jgralab.algolib.functions.Permutation;
import de.uni_koblenz.jgralab.algolib.functions.Relation;
import de.uni_koblenz.jgralab.algolib.problems.ReachabilitySolver;
import de.uni_koblenz.jgralab.algolib.problems.SimplePathsSolver;
import de.uni_koblenz.jgralab.algolib.visitors.Visitor;

public class WarshallAlgorithm extends GraphAlgorithm implements
		ReachabilitySolver, SimplePathsSolver {

	private TransitiveVisitorComposition visitors;
	private EdgeDirection searchDirection;
		private IntFunction<Vertex> indexMapping;
	private Permutation<Vertex> vertexOrder;
	private int vertexCount;
	private boolean reachable[][];
	private Edge[][] successor;

	public WarshallAlgorithm(Graph graph) {
		super(graph);
	}

	public WarshallAlgorithm(Graph graph, BooleanFunction<GraphElement> subgraph) {
		super(graph, subgraph);
	}

	@Override
	public void addVisitor(Visitor visitor) {
		checkStateForSettingParameters();
		visitors.addVisitor(visitor);
	}

	@Override
	public void disableOptionalResults() {
	}

	@Override
	protected void done() {
		state = AlgorithmStates.FINISHED;
	}

	@Override
	public boolean isDirected() {
		return searchDirection != EdgeDirection.INOUT;
	}

	@Override
	public boolean isHybrid() {
		return true;
	}

	@Override
	public void removeVisitor(Visitor visitor) {
		checkStateForSettingParameters();
		visitors.removeVisitor(visitor);
	}

	@Override
	public void setDirected(boolean directed) {
		checkStateForSettingParameters();
		searchDirection = directed ? EdgeDirection.OUT : EdgeDirection.INOUT;
	}

	@Override
	public void reset() {
		super.reset();
		SearchAlgorithm search = new BreadthFirstSearch(graph).withNumber();
		search.execute();
		indexMapping = search.getNumber();
		vertexOrder = search.getVertexOrder();
		vertexCount = graph.getVCount();
		reachable = reachable == null ? new boolean[vertexCount + 1][vertexCount + 1]
				: reachable;
		successor = successor == null ? new Edge[vertexCount + 1][vertexCount + 1]
				: successor;
	}

	@Override
	public void resetParameters() {
		super.resetParameters();
		searchDirection = EdgeDirection.OUT;
		visitors = new TransitiveVisitorComposition();
	}

	@Override
	public WarshallAlgorithm execute() {
		startRunning();

		// clear and initialize arrays
		for (int vId = 1; vId < vertexCount; vId++) {
			for (int wId = 1; wId < vertexCount; wId++) {
				reachable[vId][wId] = false;
				successor[vId][wId] = null;
			}
			reachable[vId][vId] = true;
		}
		for (Edge e : graph.edges()) {
			int vId = indexMapping.get(e.getAlpha());
			int wId = indexMapping.get(e.getOmega());
			switch (searchDirection) {
			case OUT:
				reachable[vId][wId] = true;
				successor[vId][wId] = e;
				break;
			case INOUT:
				reachable[vId][wId] = true;
				reachable[wId][vId] = true;
				successor[vId][wId] = e;
				successor[wId][wId] = e.getReversedEdge();
				break;
			case IN:
				reachable[wId][vId] = true;
				successor[wId][vId] = e.getReversedEdge();
			}
		}

		// main loop
		for (int vId = 1; vId <= vertexCount; vId++) {
			for (int uId = 1; uId <= vertexCount; uId++) {
				for (int wId = 1; wId <= vertexCount; wId++) {
					if (reachable[uId][vId] && reachable[vId][wId]
							&& !reachable[uId][wId]) {
						reachable[uId][wId] = true;
						successor[uId][wId] = successor[uId][vId];
						visitors.visitVertexTriple(vertexOrder.get(uId),
								vertexOrder.get(vId), vertexOrder.get(wId));
					}
				}
			}
		}

		done();
		return this;
	}

	@Override
	public Relation<Vertex, Vertex> getReachabilityRelation() {
		checkStateForResult();
		return new ArrayRelation<Vertex>(reachable, indexMapping);
	}

	@Override
	public BinaryFunction<Vertex, Vertex, Edge> getSuccessor() {
		return new ArrayBinaryFunction<Vertex, Edge>(successor, indexMapping);
	}
	
	public EdgeDirection getSearchDirection() {
		checkStateForSettingParameters();
		return searchDirection;
	}

	public void setSearchDirection(EdgeDirection searchDirection) {
		checkStateForSettingParameters();
		this.searchDirection = searchDirection;
	}
	
	public Permutation<Vertex> getVertexOrder(){
		checkStateForResult();
		return vertexOrder;
	}

}
