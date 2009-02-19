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
package de.uni_koblenz.jgralabtest.graphvalidatortest;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.graphvalidator.BrokenGReQLConstraintViolation;
import de.uni_koblenz.jgralab.graphvalidator.ConstraintViolation;
import de.uni_koblenz.jgralab.graphvalidator.GReQLConstraintViolation;
import de.uni_koblenz.jgralab.graphvalidator.GraphValidator;
import de.uni_koblenz.jgralab.graphvalidator.MultiplicityConstraintViolation;
import de.uni_koblenz.jgralabtest.schemas.constrained.ConstrainedGraph;
import de.uni_koblenz.jgralabtest.schemas.constrained.ConstrainedLink;
import de.uni_koblenz.jgralabtest.schemas.constrained.ConstrainedNode;
import de.uni_koblenz.jgralabtest.schemas.constrained.ConstrainedSchema;
import de.uni_koblenz.jgralabtest.schemas.constrained.OtherConstrainedNode;

/**
 * @author Tassilo Horn <horn@uni-koblenz.de>
 *
 */
public class GraphValidatorTest {
	private ConstrainedGraph g = null;
	private GraphValidator validator = null;

	{
		JGraLab.setLogLevel(Level.OFF);
	}

	@Before
	public void setup() {
		g = ConstrainedSchema.instance().createConstrainedGraph();
		validator = new GraphValidator(g);
	}

	@Test
	public void validate1() throws IOException {
		g.createConstrainedNode();
		Set<ConstraintViolation> brokenConstraints = validator.validate();

		// Only one isolated node, so the following constraints cannot be met:
		// 1. uid is not > 0. 2. multiplicity is broken twice, cause there has
		// to be at least one ConstrainedLink between ConstrainedNodes. 3. The
		// name attribute is not set.

		// each ConstrainedNode must have (1,*) in and outgoing ConstrainedLink
		assertEquals(2, getNumberOfBrokenConstraints(
				MultiplicityConstraintViolation.class, brokenConstraints));
		// uid should be > 0 and name has to be set
		assertEquals(2, getNumberOfBrokenConstraints(
				GReQLConstraintViolation.class, brokenConstraints));
		// The graph class has to invalid constraints
		assertEquals(2, getNumberOfBrokenConstraints(
				BrokenGReQLConstraintViolation.class, brokenConstraints));
	}

	@Test
	public void validate2() {
		ConstrainedNode n1 = g.createConstrainedNode();
		n1.setName("n1");
		n1.setUid(n1.getId());
		ConstrainedNode n2 = g.createConstrainedNode();
		n2.setName("n2");
		n2.setUid(n2.getId());
		ConstrainedLink l1 = g.createConstrainedLink(n1, n2);
		l1.setUid(Integer.MAX_VALUE - l1.getId());

		Set<ConstraintViolation> brokenConstraints = validator.validate();

		// This one is fine, except the broken GReQL query...
		assertEquals(2, getNumberOfBrokenConstraints(
				BrokenGReQLConstraintViolation.class, brokenConstraints));
	}

	@Test
	public void validate3() {
		OtherConstrainedNode n1 = g.createOtherConstrainedNode();
		n1.setName("n1");
		n1.setUid(n1.getId());
		// This should be between 0 and 20.
		n1.setNiceness(-17);

		Set<ConstraintViolation> brokenConstraints = validator
				.validateConstraints(n1.getAttributedElementClass());

		// This one is fine, except that niceness should be between 0 and 20.
		assertEquals(1, getNumberOfBrokenConstraints(
				GReQLConstraintViolation.class, brokenConstraints));
	}

	private static int getNumberOfBrokenConstraints(
			Class<? extends ConstraintViolation> type,
			Set<ConstraintViolation> set) {
		int number = 0;
		for (ConstraintViolation ci : set) {
			if (type.isInstance(ci)) {
				number++;
			}
		}
		return number;
	}
}
