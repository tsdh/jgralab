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
package de.uni_koblenz.jgralab.utilities.tg2dot.greql2.funlib;

import java.util.ArrayList;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.graphmarker.AbstractGraphMarker;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.funlib.Greql2Function;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

/**
 * Shortens a given String to a specific length and adds three dots ('...').
 * 
 * <dl>
 * <dt><b>GReQL-signatures</b></dt>
 * <dd><code>STRING shortenString(string:STRING, maxLength:INTEGER)</code></dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>string</code> - String which is shortened.</dd>
 * <dd><code>maxLength</code> - Maximum length of the String.</dd>
 * <dt><b>Returns:</b></dt>
 * <dd>Shortened String.</dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class ShortenString extends Greql2Function {

	{
		JValueType[][] x = { { JValueType.STRING, JValueType.INT,
				JValueType.STRING } };
		signatures = x;

		description = "Returns a String shortened to the maximum allowed length.";

		Category[] c = { Category.STRINGS };
		categories = c;
	}

	@Override
	public JValue evaluate(Graph graph,
			AbstractGraphMarker<AttributedElement> subgraph, JValue[] arguments)
			throws EvaluateException {

		switch (checkArguments(arguments)) {
		case 0:
			String string = arguments[0].toString();
			Integer maxLength = arguments[1].toInteger();

			boolean isString = string.startsWith("\"") && string.endsWith("\"");
			boolean isToLong = string.length() > maxLength;

			string = isToLong ? string.substring(0, maxLength) + "..." : string;
			string += isString ? "\"" : "";

			return new JValueImpl(string);
		default:
			throw new RuntimeException();
		}
	}

	@Override
	public long getEstimatedCardinality(int inElements) {
		return 1;
	}

	@Override
	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return 1;
	}

	@Override
	public double getSelectivity() {
		return 1;
	}

}
