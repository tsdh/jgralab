/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2008 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 *
 *               ist@uni-koblenz.de
 *
 * Please report bugs to http://serres.uni-koblenz.de/bugzilla
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package de.uni_koblenz.jgralab.greql2.evaluator.fa;

import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexEvaluator;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueInvalidTypeException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueTypeCollection;
import de.uni_koblenz.jgralab.BooleanGraphMarker;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;

/**
 * This transition accepts only one edge. Because this edge may be a variable or
 * even the result of an expression containing a variable, a reference to the
 * VertexEvaluator which evaluates this variable/expression is stored in this
 * transition and the result of this evaluator is acceptes.
 * 
 * This transition accepts the greql2 syntax: --{edge}->
 * 
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> Summer 2006, Diploma Thesis
 * 
 */
public class EdgeTransition extends SimpleTransition {

	/**
	 * In GReQL 2 it is possible to specify an explicit edge. Cause this edge
	 * may be a variable or the result of an expression containing a variable,
	 * the VertexEvalutor which evaluates this edge expression is stored here so
	 * the result can be used as allowed edge
	 */
	private VertexEvaluator allowedEdgeEvaluator;

	/**
	 * returns a string which describes the edge
	 */
	public String edgeString() {
		String desc = "EdgeTransition";
		// String desc = "EdgeTransition ( Dir:" + validDirection.toString() + "
		// "
		// + edgeTypeRestriction.toString() + " Edge: " +
		// allowedEdgeEvaluator.toString() + " )";
		return desc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see greql2.evaluator.fa.Transition#equalSymbol(greql2.evaluator.fa.EdgeTransition)
	 */
	public boolean equalSymbol(Transition t) {
		if (!(t instanceof EdgeTransition))
			return false;
		EdgeTransition et = (EdgeTransition) t;
		if (!typeCollection.equals(et.typeCollection))
			return false;
		if (validEdgeRole != et.validEdgeRole)
			return false;
		if (allowedEdgeEvaluator != et.allowedEdgeEvaluator)
			return false;
		if (validDirection != et.validDirection)
			return false;
		return true;
	}

	/**
	 * Copy-constructor, creates a copy of the given transition
	 */
	protected EdgeTransition(EdgeTransition t, boolean addToStates) {
		super(t, addToStates);
		allowedEdgeEvaluator = t.allowedEdgeEvaluator;
	}

	/**
	 * returns a copy of this transition
	 */
	public Transition copy(boolean addToStates) {
		return new EdgeTransition(this, addToStates);
	}

	/**
	 * Creates a new transition from start state to end state. The Transition
	 * accepts all edges that have the right direction, role, startVertexType,
	 * endVertexType, edgeType and even it's possible to define a specific edge.
	 * This constructor creates a transition to accept a EdgePathDescription
	 * 
	 * @param start
	 *            The state where this transition starts
	 * @param end
	 *            The state where this transition ends
	 * @param dir
	 *            The direction of the accepted edges, may be EdeDirection.IN,
	 *            EdgeDirection.OUT or EdgeDirection.ANY
	 * @param typeCollection
	 *            The types which restrict the possible edges
	 * @param role
	 *            The accepted edge role, or null if any role is accepted
	 * @param edgeEval
	 *            If this is set, only the resulting edge of this evaluator will
	 *            be accepted
	 */
	public EdgeTransition(State start, State end, AllowedEdgeDirection dir,
			JValueTypeCollection typeCollection, String role,
			VertexEvaluator edgeEval) {
		super(start, end, dir, typeCollection, role);
		allowedEdgeEvaluator = edgeEval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see greql2.evaluator.fa.Transition#accepts(jgralab.Vertex, jgralab.Edge,
	 *      greql2.evaluator.SubgraphTempAttribute)
	 */
	public boolean accepts(Vertex v, Edge e, BooleanGraphMarker subgraph)
			throws EvaluateException {
		if (!super.accepts(v, e, subgraph)) {
			return false;
		}
		// GreqlEvaluator.println("Checking edge path for Edge: " + e.toString());
		// checks if only one edge is allowed an if e is this allowed edge
		if (allowedEdgeEvaluator != null) {
			try {
				Edge allowedEdge = allowedEdgeEvaluator.getResult(subgraph)
						.toEdge().getNormalEdge();
				// GreqlEvaluator.println("Allowed Edge is: " +
				// allowedEdge.toString());
				if (e.getNormalEdge() != allowedEdge)
					return false;
			} catch (JValueInvalidTypeException ex) {
				throw new EvaluateException(
						"EdgeExpression in EdgePathDescription doesn't evaluate to edge",
						ex);
			}
		}
		return true;
	}

}
