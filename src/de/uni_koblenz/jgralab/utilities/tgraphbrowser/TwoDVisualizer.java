package de.uni_koblenz.jgralab.utilities.tgraphbrowser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.util.HashMap;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSet;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSlice;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.utilities.tg2dot.Tg2Dot;
import de.uni_koblenz.jgralab.utilities.tgraphbrowser.StateRepository.State;

public class TwoDVisualizer {

	/**
	 * Creates the 2D-representation of <code>currentElement</code> and its
	 * environment, if their type is chosen to be shown. An element belongs to
	 * the environment if:<br>
	 * <ul>
	 * <li><code>currentElement</code> is an instance of Vertex then the element
	 * has to be in a path<br>
	 * <code>currentElement</code>&lt;-&gt;^x where 1&lt;=x&lt;=
	 * <code>pathLength</code></li>
	 * <li><code>currentElement</code> is an instance of Edge then the element
	 * has to be in a path<br>
	 * y&lt;-&gt;^x where 1&lt;=x&lt;= <code>pathLength</code> and y is an
	 * incident vertex of <code>currentElement</code></li>
	 * </ul>
	 * 
	 * @param code
	 * @param state
	 * @param sessionId
	 * @param currentElement
	 * @param showAttributes
	 * @param pathLength
	 */
	public void visualizeElements(StringBuilder code, State state,
			Integer sessionId, String workspace, JValue currentElement,
			Boolean showAttributes, Integer pathLength) {
		// set currentVertex or currentEdge to the current element
		if (currentElement.isVertex()) {
			code.append("current").append("Vertex = \"").append(
					currentElement.toVertex().getId()).append("\";\n");
		} else if (currentElement.isEdge()) {
			code.append("current").append("Edge = \"").append(
					currentElement.toEdge().getId()).append("\";\n");
		}
		// calculate environment
		JValueSet elementsToDisplay = new JValueSet();
		if (currentElement.isVertex()) {
			JValue slice = computeElements(currentElement, pathLength,
					state.graph);
			calculateElementsInSet(code, state, elementsToDisplay, slice);
		} else if (currentElement.isEdge()) {
			Edge current = currentElement.toEdge();
			JValue slice = computeElements(new JValueImpl(current.getAlpha()),
					pathLength, state.graph);
			calculateElementsInSet(code, state, elementsToDisplay, slice);
			slice = computeElements(new JValueImpl(current.getOmega()),
					pathLength, state.graph);
			calculateElementsInSet(code, state, elementsToDisplay, slice);
		} else {
			calculateElementsInSet(code, state, elementsToDisplay,
					currentElement.toJValueSet());
		}
		// create temp-folder
		File tempFolder = new File(System.getProperty("java.io.tmpdir")
				+ File.separator + "tGraphBrowser");
		if (!tempFolder.exists()) {
			if (!tempFolder.mkdir()) {
				TGraphBrowserServer.logger.warning(tempFolder
						+ " could not be created.");
			}
		}
		tempFolder.deleteOnExit();
		// create .dot-file
		// String dotFileName = workspace + "/" + sessionId +
		// "GraphSnippet.dot";
		String dotFileName = null;
		try {
			dotFileName = tempFolder.getCanonicalPath() + File.separator
					+ sessionId + "GraphSnippet.dot";
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		MyTg2Dot mtd = new MyTg2Dot(elementsToDisplay, dotFileName,
				showAttributes, currentElement, state.selectedEdgeClasses,
				state.selectedVertexClasses);
		mtd.printGraph();
		if (mtd.exception != null) {
			code
					.append("document.getElementById('divError').style.display = \"block\";\n");
			code
					.append(
							"document.getElementById('h2ErrorMessage').innerHTML = \"ERROR: ")
					.append("Could not create file ").append(dotFileName)
					.append("\";\n");
			code
					.append("document.getElementById('divNonError').style.display = \"none\";\n");
			return;
		}
		// create .svg-file
		String svgFileName = null;
		try {
			svgFileName = tempFolder.getCanonicalPath() + File.separator
					+ sessionId + "GraphSnippet.svg";
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		try {
			synchronized (StateRepository.dot) {
				String execStr = null;
				// on windows, we must quote the file names, and on UNIX, we
				// must not quote them... And this stupid ProcessBuilder doesn't
				// work either...
				if (System.getProperty("os.name").startsWith("Windows")) {
					execStr = StateRepository.dot + " -Tsvg -o \""
							+ svgFileName + "\" \"" + dotFileName + "\"";
				} else {
					execStr = StateRepository.dot + " -Tsvg -o " + svgFileName
							+ " " + dotFileName;
				}
				int exitCode = Runtime.getRuntime().exec(execStr).waitFor();
				if (exitCode != 0) {
					System.err.println("dot exited with code " + exitCode);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			code
					.append("document.getElementById('divError').style.display = \"block\";\n");
			code
					.append(
							"document.getElementById('h2ErrorMessage').innerHTML = \"ERROR: ")
					.append("Could not create file ").append(svgFileName)
					.append("\";\n");
			code
					.append("document.getElementById('divNonError').style.display = \"none\";\n");
			return;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (!new File(dotFileName).delete()) {
			TGraphBrowserServer.logger.warning(dotFileName
					+ " could not be deleted");
		}
		// determine the size of the svg-graphic
		String line = "";
		try {
			FileReader in = new FileReader(svgFileName);
			// FileReader in = new FileReader(workspace + "/" + svgFileName);
			LineNumberReader lnr = new LineNumberReader(in);
			do {
				line = lnr.readLine();
			} while ((line != null) && !line.startsWith("<svg"));
			lnr.close();
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String width = "200pt";
		String height = "200pt";
		if ((line != null) && !line.isEmpty()) {
			String[] lineparts = line.split("\"");
			width = lineparts[1];
			height = lineparts[3];
		}
		svgFileName = svgFileName.substring(svgFileName
				.lastIndexOf(File.separator) + 1);
		// svg in HTML-Seite einbinden
		code.append("/*@cc_on\n");
		code.append("/*@if (@_jscript_version > 5.6)\n");
		// code executed in Internet Explorer
		code.append("var div2D = document.getElementById(\"div2DGraph\");\n");
		code.append("var object = document.createElement(\"embed\");\n");
		code.append("object.id = \"embed2DGraph\";\n");
		code.append("object.src = \"_").append(svgFileName).append("\";\n");
		code.append("object.type = \"image/svg+xml\";\n");
		code.append("object.width = \"").append(width).append("\";\n");
		code.append("object.height = \"").append(height).append("\";\n");
		code.append("div2D.appendChild(object);\n");
		code.append("@else @*/\n");
		// code executed in other browsers
		code.append("var div2D = document.getElementById(\"div2DGraph\");\n");
		code.append("var object = document.createElement(\"object\");\n");
		code.append("object.data = \"").append(svgFileName).append("\";\n");
		code.append("object.type = \"image/svg+xml\";\n");
		code.append("object.width = \"").append(width).append("\";\n");
		code.append("object.height = \"").append(height).append("\";\n");
		code.append("div2D.appendChild(object);\n");
		code.append("object.onload = function(){\n");
		code.append("var svgDoc = object.getSVGDocument();\n");
		code.append("var svgRootElement = svgDoc.rootElement;\n");
		code.append("svgRootElement.currentScale = 0.8;");
		code.append("};\n");
		code.append("/*@end\n");
		code.append("@*/\n");
	}

	/**
	 * Puts all elements of <code>elements</code> into
	 * <code>elementsToDisplay</code>, if their type is selected to be shown.
	 * 
	 * @param state
	 * @param elementsToDisplay
	 * @param elements
	 */
	private void calculateElementsInSet(StringBuilder code, State state,
			JValueSet elementsToDisplay, JValue elements) {
		int totalElements = 0;
		int selectedElements = 0;
		if (elements.canConvert(JValueType.SLICE)) {
			JValueSlice slice = elements.toSlice();
			for (JValue v : slice.nodes()) {
				totalElements++;
				if (v.isVertex()
						&& state.selectedVertexClasses.get(v.toVertex()
								.getAttributedElementClass())) {
					elementsToDisplay.add(v);
					selectedElements++;
				}
			}
			for (JValue v : slice.edges()) {
				totalElements++;
				if (v.isEdge()
						&& state.selectedEdgeClasses.get(v.toEdge()
								.getAttributedElementClass())) {
					elementsToDisplay.add(new JValueImpl(v.toEdge()
							.getNormalEdge()));
					selectedElements++;
				}
			}
		} else {
			for (JValue v : elements.toJValueSet()) {
				totalElements++;
				if (v.isVertex()
						&& state.selectedVertexClasses.get(v.toVertex()
								.getAttributedElementClass())) {
					elementsToDisplay.add(v);
					selectedElements++;
				} else if (v.isEdge()
						&& state.selectedEdgeClasses.get(v.toEdge()
								.getAttributedElementClass())) {
					elementsToDisplay.add(new JValueImpl(v.toEdge()
							.getNormalEdge()));
					selectedElements++;
				}
			}
		}
		code.append("var div2D = document.getElementById(\"div2DGraph\");\n");
		code.append("div2D.innerHTML = \"\";\n");
		// print number of elements
		code
				.append(
						"document.getElementById(\"h3HowManyElements\").innerHTML = \"")
				.append(selectedElements).append(" of ").append(totalElements)
				.append(" elements selected.\";\n");
	}

	/**
	 * Finds all elements which are on a path of the kind:<br>
	 * <code>currentElement</code>&lt;-&gt;^<code>1</code> |<br>
	 * <code>currentElement</code>&lt;-&gt;^<code>2</code> |<br>
	 * ... |<br>
	 * <code>currentElement</code>&lt;-&gt;^<code>pathLength</code>
	 * 
	 * @param currentElement
	 * @param pathLength
	 * @param graph
	 * @return
	 */
	private JValue computeElements(JValue currentElement, Integer pathLength,
			Graph graph) {
		HashMap<String, JValue> boundVars = new HashMap<String, JValue>();
		boundVars.put("current", currentElement);
		StringBuilder query = new StringBuilder("using current: ");
		query.append("slice(current,<->^1");
		for (int i = 2; i <= pathLength; i++) {
			query.append("|<->^" + i);
		}
		query.append(")");
		return StateRepository
				.evaluateGReQL(query.toString(), graph, boundVars);
	}

	/**
	 * Creates the specific representation for the elements.
	 */
	private static class MyTg2Dot extends Tg2Dot {

		private static final double ranksep = 1.5;
		private static final boolean ranksepEqually = false;
		private static final double nodesep = 0.25;
		private static final String fontname = "Helvetica";
		private static final int fontsize = 14;

		/**
		 * The elements to be displayed.
		 */
		private final JValueSet elements;

		/**
		 * The current Element.
		 */
		private final GraphElement current;

		/**
		 * If true, the attributes are shown.
		 */
		private final boolean showAttributes;

		/**
		 * Stores the exception, if one occures.
		 */
		public Exception exception;

		/**
		 * Number for the unique name of the endnode of the edge, which shows,
		 * that there are further edges at a node.
		 */
		private int counter = 0;

		private final HashMap<EdgeClass, Boolean> selectedEdgeClasses;

		private final HashMap<VertexClass, Boolean> selectedVertexClasses;

		/**
		 * Creates a new MyTg2Dot object. Call printGraph() to create a
		 * .dot-file.
		 * 
		 * @param elements
		 * @param outputFileName
		 * @param showAttributes
		 * @param selectedEdgeClasses2
		 */
		public MyTg2Dot(JValueSet elements, String outputFileName,
				Boolean showAttributes, JValue currentElement,
				HashMap<EdgeClass, Boolean> selectedEdgeClasses2,
				HashMap<VertexClass, Boolean> selectedVertexClasses2) {
			this.selectedEdgeClasses = selectedEdgeClasses2;
			this.selectedVertexClasses = selectedVertexClasses2;
			this.elements = elements;
			outputName = outputFileName;
			this.showAttributes = showAttributes;
			setPrintIncidenceNumbers(true);
			if (currentElement.isVertex()) {
				current = currentElement.toVertex();
			} else if (currentElement.isEdge()) {
				current = currentElement.toEdge();
			} else {
				current = null;
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.uni_koblenz.jgralab.utilities.tg2whatever.Tg2Whatever#printGraph()
		 */
		@Override
		public void printGraph() {
			try {
				PrintStream out = new PrintStream(new FileOutputStream(
						outputName));
				graphStart(out);
				for (JValue v : elements) {
					if (v.isVertex()) {
						printVertex(out, v.toVertex());
					} else if (v.isEdge()) {
						printEdge(out, v.toEdge());
					}
				}
				graphEnd(out);
				out.flush();
				out.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				exception = e;
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.uni_koblenz.jgralab.utilities.tg2dot.Tg2Dot#graphStart(java.io
		 * .PrintStream)
		 */
		@Override
		public void graphStart(PrintStream out) {
			out.println("digraph \"" + outputName + "\"");
			out.println("{");

			// Set the ranksep
			if (ranksepEqually) {
				out.println("ranksep=\"" + ranksep + " equally\";");
			} else {
				out.println("ranksep=\"" + ranksep + "\";");
			}

			// Set the nodesep
			out.println("nodesep=\"" + nodesep + "\";");

			out.println("node [shape=\"record\" " + "style=\"filled\"  "
					+ "fillcolor=\"white\" " + "fontname=\"" + fontname + "\" "
					+ "fontsize=\"" + fontsize + "\" color=\"#999999\"];");
			out.println("edge [fontname=\"" + fontname + "\" fontsize=\""
					+ fontsize + "\" labelfontname=\"" + fontname
					+ "\" labelfontsize=\"" + fontsize + "\" color=\"#999999\""
					+ " penwidth=\"3\"  arrowsize=\"1.5\" " + "];");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.uni_koblenz.jgralab.utilities.tg2dot.Tg2Dot#printEdge(java.io.
		 * PrintStream, de.uni_koblenz.jgralab.Edge)
		 */
		@Override
		protected void printEdge(PrintStream out, Edge e) {
			if (!selectedEdgeClasses.get(e.getAttributedElementClass())) {
				return;
			}
			Vertex alpha = e.getAlpha();
			Vertex omega = e.getOmega();
			// hide deselected vertices
			if (!selectedVertexClasses.get(alpha.getAttributedElementClass())
					|| !selectedVertexClasses.get(omega
							.getAttributedElementClass())) {
				return;
			}

			out.print("v" + alpha.getId() + " -> v" + omega.getId() + " [");

			EdgeClass cls = (EdgeClass) e.getAttributedElementClass();

			out.print("label=\"e" + e.getId() + ": "
					+ cls.getUniqueName().replace('$', '.') + "");

			if (showAttributes && (cls.getAttributeCount() > 0)) {
				out.print("\\l");
				printAttributes(out, e);
			}
			out.print("\"");

			out.print(" tooltip=\"");
			if (!showAttributes) {
				printAttributes(out, e);
			}
			out.print(" \"");

			if (isPrintIncidenceNumbers()) {
				out
						.print(" taillabel=\"" + getIncidenceNumber(e, alpha)
								+ "\"");
				out
						.print(" headlabel=\""
								+ getIncidenceNumber(e.getReversedEdge(), omega)
								+ "\"");
			}

			out.print(" href=\"javascript:top.showElement('e" + e.getId()
					+ "');\"");

			if (e == current) {
				out.print(" color=\"red\"");
			}

			out.println("];");
		}

		private int getIncidenceNumber(Edge e, Vertex v) {
			int num = 1;
			for (Edge inc : v.incidences()) {
				if (inc == e) {
					return num;
				}
				num++;
			}
			return -1;
		}

		private void printAttributes(PrintStream out, AttributedElement elem) {
			AttributedElementClass cls = elem.getAttributedElementClass();
			StringBuilder value = new StringBuilder();
			for (Attribute attr : cls.getAttributeList()) {
				try {
					String current = attr.getName();
					Object attribute = elem.getAttribute(attr.getName());
					String attributeString = (attribute != null) ? attribute
							.toString() : "null";
					if (attribute instanceof String) {
						attributeString = '"' + attributeString + '"';
					}
					current += " = " + stringQuote(attributeString)
							+ (showAttributes ? "\\l" : ";");
					if (!showAttributes) {
						if (value.length() + current.length() < 400) {
							// if the title is too long dot produces nonsense
							// and the svg contains forbidden chars
							value.append(current);
						} else {
							value.append(" ...");
							break;
						}
					} else {
						value.append(current);
					}
				} catch (NoSuchFieldException e1) {
					exception = e1;
				}
			}
			out.print(value);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.uni_koblenz.jgralab.utilities.tg2dot.Tg2Dot#printVertex(java.io
		 * .PrintStream, de.uni_koblenz.jgralab.Vertex)
		 */
		@Override
		protected void printVertex(PrintStream out, Vertex v) {
			AttributedElementClass cls = v.getAttributedElementClass();
			out.print("v" + v.getId() + " [label=\"{{v" + v.getId() + "|"
					+ cls.getUniqueName().replace('$', '.') + "}");
			if (showAttributes && (cls.getAttributeCount() > 0)) {
				out.print("|");
				printAttributes(out, v);
			}
			out.print("}\"");
			out.print(" href=\"javascript:top.showElement('v" + v.getId()
					+ "');\"");
			if (v == current) {
				out.print(" fillcolor=\"#FFC080\"");
			}
			out.print(" tooltip=\"");
			if (!showAttributes) {
				printAttributes(out, v);
			}
			out.print(" \"");
			out.println("];");
			// check if this vertex has edges which will not be shown
			for (Edge e : v.incidences()) {
				if (!elements.contains(new JValueImpl(e.getNormalEdge()))
						&& selectedEdgeClasses.get(e.getNormalEdge()
								.getAttributedElementClass())) {
					// mark this vertex that it has further edges
					// print a new node, which is completely white
					out.println("nv" + counter
							+ " [shape=\"plaintext\" fontcolor=\"white\"]");
					// print the little arrow as a new edge
					out.println("nv" + counter++ + " -> v" + v.getId()
							+ " [style=\"dashed\"]");
					break;
				}
			}
		}
	}
}