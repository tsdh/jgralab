package de.uni_koblenz.jgralabtest.utilities.tg2dot;

import java.io.IOException;

import org.junit.Test;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ProgressFunction;
import de.uni_koblenz.jgralab.WorkInProgress;
import de.uni_koblenz.jgralab.utilities.common.dot.GraphVizOutputFormat;
import de.uni_koblenz.jgralab.utilities.common.dot.GraphVizProgram;
import de.uni_koblenz.jgralab.utilities.tg2dot.Tg2Dot;
import de.uni_koblenz.jgralab.utilities.tg2image.Tg2Image;

@WorkInProgress(responsibleDevelopers = "mmce@uni-koblenz.de", description = "More test have to be included. Every static method should be tested. Additionally the class itself should be tested.")
public class Tg2DotTest {

	@Test
	public void convertGraph() throws GraphIOException {
		Graph g = GraphIO.loadGraphFromFileWithStandardSupport(
				"testit/testgraphs/greqltestgraph.tg", (ProgressFunction) null);
		Tg2Dot.convertGraph(g, "testit/testoutput.dot", false);
	}

	@Test
	public void convertGraph2Svg() throws GraphIOException,
			InterruptedException, IOException {

		Graph g = GraphIO.loadGraphFromFileWithStandardSupport(

		"testit/testgraphs/greqltestgraph.tg", (ProgressFunction) null);

		GraphVizProgram program = new GraphVizProgram().path("").outputFormat(
				GraphVizOutputFormat.PNG);

		Tg2Image.convertGraph2ImageFile(g, program, "testit/testoutput.png",
				false);
	}
}