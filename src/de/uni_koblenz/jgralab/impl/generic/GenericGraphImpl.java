package de.uni_koblenz.jgralab.impl.generic;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.pcollections.POrderedSet;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.NoSuchAttributeException;
import de.uni_koblenz.jgralab.Record;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.EdgeIterable;
import de.uni_koblenz.jgralab.impl.VertexIterable;
import de.uni_koblenz.jgralab.impl.std.GraphImpl;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.BasicDomain;
import de.uni_koblenz.jgralab.schema.BooleanDomain;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.DoubleDomain;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.EnumDomain;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.IntegerDomain;
import de.uni_koblenz.jgralab.schema.LongDomain;
import de.uni_koblenz.jgralab.schema.RecordDomain;
import de.uni_koblenz.jgralab.schema.VertexClass;

/**
 *
 * @author Bernhard
 *
 */
public class GenericGraphImpl extends GraphImpl {

	private GraphClass type;
	private Map<String, Object> attributes;

	protected GenericGraphImpl(GraphClass type, String id) {
		super(id, type, 100, 100);
	}

	protected GenericGraphImpl(GraphClass type, String id, int vmax, int emax) {
		super(id, type, vmax, emax);
		this.type = type;
		attributes = GenericGraphImpl.initializeAttributes(type);
		GenericGraphImpl.initializeGenericAttributeValues(this);
	}

	/**
	 * Creates a new instance of a generic Graph. This method isn't supposed to
	 * be called manually. Use
	 * <code>Schema.createGraph(ImplementationType.Generic)</code> instead!
	 */
	public static Graph createGraph(GraphClass type, String id, int vmax,
			int emax) {
		return new GenericGraphImpl(type, id, vmax, emax);
	}

	/**
	 * Creates a new {@link GenericVertexImpl} in the graph that conforms to a
	 * given {@Link VertexClass} from the Schema.
	 */
	@Override
	public <T extends Vertex> T createVertex(VertexClass vc) {
		return graphFactory.createVertex(vc, 0, this);
	}

	/**
	 * Creates a new {@Link GenericEdgeImpl} in the Graph that conforms
	 * to a given {@link EdgeClass} from the Schema.
	 */
	@Override
	public <T extends Edge> T createEdge(EdgeClass ec, Vertex alpha,
			Vertex omega) {
		return graphFactory.createEdge(ec, 0, this, alpha, omega);
	}

	@Override
	public GraphClass getAttributedElementClass() {
		return type;
	}

	@Override
	public void readAttributeValueFromString(String attributeName, String value)
			throws GraphIOException, NoSuchAttributeException {
		if ((attributes != null) && attributes.containsKey(attributeName)) {
			attributes.put(
					attributeName,
					type.getAttribute(attributeName)
							.getDomain()
							.parseGenericAttribute(
									GraphIO.createStringReader(value,
											getSchema())));
			return;
		}
		throw new NoSuchAttributeException(this + " doesn't have an attribute "
				+ attributeName);
	}

	@Override
	public void readAttributeValues(GraphIO io) throws GraphIOException {
		for (Attribute a : type.getAttributeList()) {
			attributes
					.put(a.getName(), a.getDomain().parseGenericAttribute(io));
		}
	}

	@Override
	public String writeAttributeValueToString(String attributeName)
			throws IOException, GraphIOException, NoSuchAttributeException {
		GraphIO io = GraphIO.createStringWriter(getSchema());
		type.getAttribute(attributeName).getDomain()
				.serializeGenericAttribute(io, getAttribute(attributeName));
		return io.getStringWriterResult();
	}

	@Override
	public void writeAttributeValues(GraphIO io) throws IOException,
			GraphIOException {
		for (Attribute a : type.getAttributeList()) {
			a.getDomain().serializeGenericAttribute(io,
					attributes.get(a.getName()));
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAttribute(String name) throws NoSuchAttributeException {
		if ((attributes == null) || !attributes.containsKey(name)) {
			throw new NoSuchAttributeException(type.getSimpleName()
					+ " doesn't contain an attribute " + name);
		} else {
			return (T) attributes.get(name);
		}
	}

	@Override
	public <T> void setAttribute(String name, T data)
			throws NoSuchAttributeException {
		if ((attributes == null) || !attributes.containsKey(name)) {
			throw new NoSuchAttributeException(type.getSimpleName()
					+ " doesn't contain an attribute " + name);
		} else {
			if (!type.getAttribute(name).getDomain().genericIsConform(data)) {
				throw new ClassCastException();
			} else {
				attributes.put(name, data);
			}
		}

	}

	@Override
	public Vertex getFirstVertex(VertexClass vertexClass) {
		Vertex v = getFirstVertex();
		if (v == null) {
			return null;
		}
		if (v.getAttributedElementClass().equals(vertexClass)
				|| v.getAttributedElementClass().getAllSuperClasses()
						.contains(vertexClass)) {
			return v;
		}
		return v.getNextVertex(vertexClass);
	}

	@Override
	public Edge getFirstEdge(EdgeClass edgeClass) {
		Edge e = getFirstEdge();
		if (e == null) {
			return null;
		}
		if (e.getAttributedElementClass().equals(edgeClass)
				|| e.getAttributedElementClass().getAllSuperClasses()
						.contains(edgeClass)) {
			return e;
		}
		return e.getNextEdge(edgeClass);
	}

	@Override
	public Iterable<Vertex> vertices(VertexClass vc) {
		return new VertexIterable<Vertex>(this, vc);
	}

	@Override
	public Iterable<Edge> edges(EdgeClass ec) {
		return new EdgeIterable<Edge>(this, ec);
	}

	@Override
	public void initializeAttributesWithDefaultValues() {
		initializeGenericAttributeValues(this);
	}

	/**
	 * Returns the default value for attributes in the generic implementation if
	 * there is no explicitly defined default value, according to the
	 * attribute's domain.
	 *
	 * @param domain
	 *            The attribute's domain.
	 * @return The default value for attributes of the domain.
	 */
	public static Object genericAttributeDefaultValue(Domain domain) {
		if (domain instanceof BasicDomain) {
			if (domain instanceof BooleanDomain) {
				return Boolean.valueOf(false);
			} else if (domain instanceof IntegerDomain) {
				return Integer.valueOf(0);
			} else if (domain instanceof LongDomain) {
				return Long.valueOf(0);
			} else if (domain instanceof DoubleDomain) {
				return Double.valueOf(0.0);
			} else { // StringDomain
				return null;
			}
		} else {
			return null;
		}
	}

	static Map<String, Object> initializeAttributes(
			AttributedElementClass<?, ?> aec) {
		Map<String, Object> attributes = null;
		if (aec.getAttributeCount() > 0) {
			attributes = new HashMap<String, Object>();
			for (Attribute a : aec.getAttributeList()) {
				attributes.put(a.getName(), null);
			}
		}
		return attributes;
	}

	static void initializeGenericAttributeValues(AttributedElement<?, ?> ae) {
		for (Attribute attr : ae.getAttributedElementClass().getAttributeList()) {
			if ((attr.getDefaultValueAsString() != null)
					&& !attr.getDefaultValueAsString().isEmpty()) {
				try {
					attr.setDefaultValue(ae);
				} catch (GraphIOException e) {
					e.printStackTrace();
				}
			} else {
				ae.setAttribute(attr.getName(),
						genericAttributeDefaultValue(attr.getDomain()));
			}
		}
	}

	// ************** unsupported methods ***************/
	@Override
	public Class<? extends Graph> getSchemaClass() {
		throw new UnsupportedOperationException(
				"This method is not supported by the generic implementation");
	}

	@Override
	public Vertex getFirstVertex(Class<? extends Vertex> vertexClass) {
		throw new UnsupportedOperationException(
				"This method is not supported by the generic implementation");
	}

	@Override
	public Iterable<Vertex> vertices(Class<? extends Vertex> vertexClass) {
		throw new UnsupportedOperationException(
				"This method is not supported by the generic implementation");
	}

	@Override
	public Edge getFirstEdge(Class<? extends Edge> edgeClass) {
		throw new UnsupportedOperationException(
				"This method is not supported by the generic implementation");
	}

	@Override
	public Iterable<Edge> edges(Class<? extends Edge> edgeClass) {
		throw new UnsupportedOperationException(
				"This method is not supported by the generic implementation");
	}

	@Override
	public <T extends Vertex> POrderedSet<T> reachableVertices(
			Vertex startVertex, String pathDescription, Class<T> vertexType) {
		throw new UnsupportedOperationException(
				"This method is not supported by the generic implementation");
	}

	@Override
	public boolean isInstanceOf(GraphClass cls) {
		// Needs to be overridden from the base variant, because that relies on
		// code generation.
		return type.equals(cls) || type.isSubClassOf(cls);
	}

	@Override
	public Object getEnumConstant(EnumDomain enumDomain, String constantName) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException(
				"Not yet implemented!  Bug Bernhard to do it!");
	}

	@Override
	public Record createRecord(RecordDomain recordDomain,
			Map<String, Object> values) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException(
				"Not yet implemented!  Bug Bernhard to do it!");
	}
}
