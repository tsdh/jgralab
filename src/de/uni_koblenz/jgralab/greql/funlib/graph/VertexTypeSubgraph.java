/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2013 Institute for Software Technology
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
package de.uni_koblenz.jgralab.greql.funlib.graph;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.SubGraphMarker;
import de.uni_koblenz.jgralab.greql.funlib.Description;
import de.uni_koblenz.jgralab.greql.funlib.Function;
import de.uni_koblenz.jgralab.greql.funlib.NeedsGraphArgument;
import de.uni_koblenz.jgralab.greql.types.TypeCollection;

@NeedsGraphArgument
public class VertexTypeSubgraph extends Function {

	@Description(params = { "graph", "typeCollection" }, description = "Returns the subgraph induced by the vertex types in typeCollection, i.e. all vertices specified by typeCollection together with all edges between those vertices.", categories = Category.GRAPH)
	public VertexTypeSubgraph() {
		super(7, 1, 1.0);
	}

	public SubGraphMarker evaluate(Graph graph, TypeCollection typeCollection) {
		SubGraphMarker subgraphMarker = new SubGraphMarker(graph);
		Vertex currentVertex = graph.getFirstVertex();
		while (currentVertex != null) {
			if (typeCollection.acceptsType(currentVertex
					.getAttributedElementClass())) {
				subgraphMarker.mark(currentVertex);
			}
			currentVertex = currentVertex.getNextVertex();
		}
		// add all edges
		Edge currentEdge = graph.getFirstEdge();
		while (currentEdge != null) {
			if (subgraphMarker.isMarked(currentEdge.getAlpha())
					&& subgraphMarker.isMarked(currentEdge.getOmega())) {
				subgraphMarker.mark(currentEdge);
			}
			currentEdge = currentEdge.getNextEdge();
		}
		return subgraphMarker;
	}

}
