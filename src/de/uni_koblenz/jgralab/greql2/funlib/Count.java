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

package de.uni_koblenz.jgralab.greql2.funlib;

import java.util.ArrayList;


import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.graphmarker.SubGraphMarker;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.WrongFunctionParameterException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

/**
 * Returns the number of elements in a given Object. If the object is not a
 * collection or map, 1 is returned. For a map the number of keys is returned.
 * 
 * <dl>
 * <dt><b>GReQL-signature</b></dt>
 * <dd><code>INT count(obj:OBJECT)</code></dd>
 * <dd>&nbsp;</dd>
 * </dl>
 * <dl>
 * <dt></dt>
 * <dd>
 * <dl>
 * <dt><b>Parameters:</b></dt>
 * <dd><code>obj</code> - object to count elements in</dd>
 * <dt><b>Returns:</b></dt>
 * <dd>the amount of elements in the given object. If the object is not a
 * collection, 1 is returned.</dd>
 * <dd><code>Null</code> if one of the parameters is <code>Null</code></dd>
 * </dl>
 * </dd>
 * </dl>
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class Count extends Greql2Function {

	{
		JValueType[][] x = { { JValueType.COLLECTION, JValueType.INT },
				{ JValueType.OBJECT, JValueType.INT },
				{ JValueType.MAP, JValueType.INT } };
		signatures = x;
		description = "Returns the number of elements in the given object.\n"
				+ "For a map the number of entries is returned."
				+ "Returns 1 iff no collection or map is given.\n";

		Category[] c = { Category.COLLECTIONS_AND_MAPS };
		categories = c;
	}

	@Override
	public JValue evaluate(Graph graph,
			SubGraphMarker subgraph, JValue[] arguments)
			throws EvaluateException {

		if (isAnyArgumentNull(arguments)) {
			return new JValueImpl();
		}

		JValueImpl result = null;
		switch (checkArguments(arguments)) {
		case 0:
			result = new JValueImpl(arguments[0].toCollection().size());
			break;
		case 1:
			result = new JValueImpl(1);
			break;
		case 2:
			result = new JValueImpl(arguments[0].toJValueMap().size());
			break;
		default:
			throw new WrongFunctionParameterException(this, arguments);
		}
		return result;
	}

	@Override
	public long getEstimatedCosts(ArrayList<Long> inElements) {
		return 2;
	}

	@Override
	public double getSelectivity() {
		return 1;
	}

	@Override
	public long getEstimatedCardinality(int inElements) {
		return 1;
	}

}
