/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
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
package de.uni_koblenz.jgralab.impl.std;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.EdgeBase;
import de.uni_koblenz.jgralab.impl.VertexBase;

/**
 * The implementation of an <code>Edge</code> accessing attributes without
 * versioning.
 * 
 * @author Jose Monte(monte@uni-koblenz.de)
 */
public abstract class EdgeImpl extends de.uni_koblenz.jgralab.impl.EdgeBaseImpl {
	// global edge sequence
	private EdgeBase nextEdge;
	private EdgeBase prevEdge;

	// the this-vertex
	private VertexBase incidentVertex;

	// incidence list
	private EdgeBase nextIncidence;
	private EdgeBase prevIncidence;

	@Override
	public EdgeBase getNextBaseEdge() {
		assert isValid();
		return nextEdge;
	}

	@Override
	public EdgeBase getPrevBaseEdge() {
		assert isValid();
		return prevEdge;
	}

	@Override
	public VertexBase getIncidentVertex() {
		return incidentVertex;
	}

	@Override
	public EdgeBase getNextBaseIncidence() {
		return nextIncidence;
	}

	@Override
	public EdgeBase getPrevBaseIncidence() {
		return prevIncidence;
	}

	@Override
	public void setNextEdgeInGraph(Edge nextEdge) {
		this.nextEdge = (EdgeBase) nextEdge;
	}

	@Override
	public void setPrevEdgeInGraph(Edge prevEdge) {
		this.prevEdge = (EdgeBase) prevEdge;
	}

	@Override
	public void setIncidentVertex(Vertex v) {
		incidentVertex = (VertexBase) v;
	}

	@Override
	public void setNextIncidenceInternal(EdgeBase nextIncidence) {
		this.nextIncidence = nextIncidence;
	}

	@Override
	public void setPrevIncidenceInternal(EdgeBase prevIncidence) {
		this.prevIncidence = prevIncidence;
	}

	/**
	 * 
	 * @param anId
	 * @param graph
	 * @throws Exception
	 */
	protected EdgeImpl(int anId, Graph graph, Vertex alpha, Vertex omega) {
		super(anId, graph);
		((GraphImpl) graph).addEdge(this, alpha, omega);
	}

	@Override
	public void setId(int id) {
		assert id >= 0;
		this.id = id;
	}
}
