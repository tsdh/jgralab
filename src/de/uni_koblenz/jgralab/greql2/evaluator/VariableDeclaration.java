/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2009 Institute for Software Technology
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

package de.uni_koblenz.jgralab.greql2.evaluator;

import java.util.ArrayList;
import java.util.Iterator;

import de.uni_koblenz.jgralab.graphmarker.BooleanGraphMarker;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VariableEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexEvaluator;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.JValueInvalidTypeException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueCollection;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSet;
import de.uni_koblenz.jgralab.greql2.schema.SimpleDeclaration;
import de.uni_koblenz.jgralab.greql2.schema.Variable;

/**
 * This class models the declaration of one variable. It allowes the iteration
 * over all possible values using the method iterate(). THe current value of the
 * variable is stored as temporary attribute at the variable vertex
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class VariableDeclaration implements Comparable<VariableDeclaration> {

	/**
	 * Holds the set of possible values the variable may have
	 */
	private JValueCollection definitionSet;

	private BooleanGraphMarker subgraph;

	/**
	 * Holds the variable-vertex of this declaration.
	 */
	private VariableEvaluator variableEval;

	private VertexEvaluator definitionSetEvaluator;

	/**
	 * Used for simple Iteration over the possible values
	 */
	private Iterator<JValue> iter = null;

	/**
	 * Holds all Vertices in the greql-syntaxgraph whose result depends on this
	 * variable
	 */
	private ArrayList<VertexEvaluator> dependingExpressions;

	/**
	 * Creates a new VariableDeclaration for the given Variable and the given
	 * JValue
	 * 
	 * @param var
	 *            the Variable-Vertex in the GReQL-Syntaxgraph to create a
	 *            VariableDeclaration for
	 * @param definitionSet
	 *            the set of possible values this variable may have
	 * @param decl
	 *            the SimpleDeclaration which declares the variable
	 * @param eval
	 *            the GreqlEvaluator which is used to evaluate the query
	 */
	public VariableDeclaration(Variable var,
			VertexEvaluator definitionSetEvaluator,
			BooleanGraphMarker subgraph, SimpleDeclaration decl,
			GreqlEvaluator eval) {
		variableEval = (VariableEvaluator) eval.getVertexEvaluatorGraphMarker()
				.getMark(var);
		this.definitionSetEvaluator = definitionSetEvaluator;
		this.subgraph = subgraph;
		dependingExpressions = new ArrayList<VertexEvaluator>();
	}

	/**
	 * Iterates over all possible values for this variable. Returns true if
	 * another value was found, false otherwise
	 */
	public boolean iterate() {
		if ((iter != null) && (iter.hasNext())) {
			deleteDependingResults();
			variableEval.setValue(iter.next());
			return true;
		}
		return false;
	}

	/**
	 * returns the current value of the represented variable. used only for
	 * debugging
	 */
	public JValue getVariableValue() {
		return variableEval.getValue();
	}

	/**
	 * Resets the iterator to the first element
	 */
	protected void reset() {
		JValue tempAttribute = definitionSetEvaluator.getResult(subgraph);
		if (tempAttribute.isCollection()) {
			try {
				JValueCollection col = tempAttribute.toCollection();
				definitionSet = col.toJValueSet();
				if (col.size() > definitionSet.size()) {
					throw new EvaluateException(
							"A collection that doesn't fulfill the set property is used as variable range definition");
				}
			} catch (JValueInvalidTypeException exception) {
				throw new EvaluateException(
						"Error evaluating a SimpleDeclaration : "
								+ exception.toString());
			}
		} else {
			definitionSet = new JValueSet();
			definitionSet.add(tempAttribute);
		}
		iter = definitionSet.iterator();
	}

	/**
	 * deletes all intermediate results that depend on this variable
	 */
	private void deleteDependingResults() {
		for (VertexEvaluator eval : dependingExpressions) {
			eval.clear();
		}
	}

	public int compareTo(VariableDeclaration d) {
		return variableEval.getVertex().getId()
				- d.variableEval.getVertex().getId();
	}

	/**
	 * Returns the cardinality of the collection this variable is bound to
	 * 
	 * @return the cardinality of the collection this variable is bound to
	 */
	public int getDefinitionCardinality() {
		// return definitionSet.size();
		return 40;
	}

}
