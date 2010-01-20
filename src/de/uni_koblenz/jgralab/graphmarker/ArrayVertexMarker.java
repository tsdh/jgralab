package de.uni_koblenz.jgralab.graphmarker;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;

/**
 * This class is the generic vertex graph marker. It is used for temporary
 * attributes on vertices which can be of an arbitrary type.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class ArrayVertexMarker<O> extends ArrayGraphMarker<Vertex, O> {

	public ArrayVertexMarker(Graph graph) {
		super(graph, graph.getMaxVCount() + 1);
	}

	@Override
	public void edgeDeleted(Edge e) {
		// do nothing
	}

	@Override
	public void maxEdgeCountIncreased(int newValue) {
		// do nothing
	}

	@Override
	public void maxVertexCountIncreased(int newValue) {
		newValue++;
		if (newValue > temporaryAttributes.length) {
			expand(newValue);
		}
	}

	@Override
	public void vertexDeleted(Vertex v) {
		removeMark(v);
	}

	@Override
	public Iterable<Vertex> getMarkedElements() {
		return new Iterable<Vertex>() {

			@Override
			public Iterator<Vertex> iterator() {
				return new ArrayGraphMarkerIterator<Vertex>(version) {

					@Override
					public boolean hasNext() {
						return index < temporaryAttributes.length;
					}

					@Override
					protected void moveIndex() {
						int length = temporaryAttributes.length;
						while (index < length && temporaryAttributes[index] == null) {
							index++;
						}
					}

					@Override
					public Vertex next() {
						if(!hasNext()){
							throw new NoSuchElementException(NO_MORE_ELEMENTS_ERROR_MESSAGE);
						}
						if(version != ArrayVertexMarker.this.version){
							throw new ConcurrentModificationException(MODIFIED_ERROR_MESSAGE);
						}
						Vertex next = graph.getVertex(index++);
						moveIndex();
						return next;
					}		
				};
				
			}

		};
	}

}
