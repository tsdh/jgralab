package de.uni_koblenz.jgralabtest.temporary;

import org.junit.Test;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.TemporaryVertex;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralabtest.schemas.citymap.CityMapSchema;
import de.uni_koblenz.jgralabtest.schemas.citymap.impl.std.CityMapFactoryImpl;
import de.uni_koblenz.jgralabtest.schemas.citymap.impl.std.CityMapImpl;

public class TemporaryGraphElementsTest {

	@Test
	public void test(){
		Schema schema = CityMapSchema.instance();
		Graph g = schema.createGraph(ImplementationType.GENERIC);
		
		Vertex v1 = g.createVertex(schema.getGraphClass().getVertexClass("ParkingGarage"));
		
		Vertex v2 = g.createVertex(schema.getGraphClass().getVertexClass("Intersection"));
		
		Edge e1 = g.createEdge(schema.getGraphClass().getEdgeClass("Street"), v1, v2);
		
		TemporaryVertex tempv = g.createTemporaryVertex();
		
		Edge e2 = g.createEdge(schema.getGraphClass().getEdgeClass("Street"), v1, tempv);
		
		for(Edge e : v1.incidences())
			System.out.println(e + " from "+e.getAlpha() + " to "+ e.getOmega());
		
		Edge tempe = g.createTemporaryEdge(v1, v2);
		System.out.println(tempe + " from "+ tempe.getAlpha() + " to "+ tempe.getOmega());
		
		System.out.println(v1.getDegree());
		
		System.out.println(tempv.getDegree());
		
		
	}
	
}
