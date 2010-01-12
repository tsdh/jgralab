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

package de.uni_koblenz.jgralab.greql2.funlib;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.graphmarker.BooleanGraphMarker;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;

/**
 * Calculates a >= b for given scalar values a and b or s1 and s2. In case of
 * strings a lexicographical order is used.
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>BOOL grEqual(a: INT, b: INT)</code></dd>
 * <dd><code>BOOL grEqual(a: INT, b: LONG)</code></dd>
 * <dd><code>BOOL grEqual(a: INT, b: DOUBLE)</code></dd>
 * <dd><code>BOOL grEqual(a: LONG, b: INT)</code></dd>
 * <dd><code>BOOL grEqual(a: LONG, b: LONG)</code></dd>
 * <dd><code>BOOL grEqual(a: LONG, b: DOUBLE)</code></dd>
 * <dd><code>BOOL grEqual(a: DOUBLE, b: INT)</code></dd>
 * <dd><code>BOOL grEqual(a: DOUBLE, b: LONG)</code></dd>
 * <dd><code>BOOL grEqual(a: DOUBLE, b: DOUBLE)</code></dd>
 * <dd><code>BOOL grEqual(s1: STRING, s2: STRING)</code></dd>
 * <dd>&nbsp;</dd>
 * <dd>This function can be used with the (>=)-Operator: <code>a >= b</code></dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>a: INT</code> - first number to compare</dd>
 * <dd><code>a: LONG</code> - first number to compare</dd>
 * <dd><code>a: DOUBLE</code> - first number to compare</dd>
 * <dd><code>s1: STRING</code> - first string to compare</dd>
 * <dd><code>b: INT</code> - second number to compare</dd>
 * <dd><code>b: LONG</code> - second number to compare</dd>
 * <dd><code>b: DOUBLE</code> - second number to compare</dd>
 * <dd><code>s2: STRING</code> - second string to compare</dd>
 * <dt><b>Returns:</b></dt>
 * <dd><code>true</code> if a >= b or s1 >= s2</dd>
 * <dd><code>Null</code> if one of the parameters is <code>Null</code></dd>
 * <dd><code>false</code> otherwise</dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @author ist@uni-koblenz.de
 * 
 */

public class GrEqual extends CompareFunction {

	{
		description = "Returns true iff $a\\geq b$. \nAlternative usage: a >= b.";
	}

	@Override
	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph,
			JValue[] arguments) throws EvaluateException {
		return evaluate(arguments, CompareOperator.GR_EQUAL);
	}

}
