/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2013 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
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
package de.uni_koblenz.jgralab.utilities.tg2dot.greql.funlib;

import de.uni_koblenz.jgralab.greql.funlib.AcceptsUndefinedArguments;
import de.uni_koblenz.jgralab.greql.funlib.Description;
import de.uni_koblenz.jgralab.greql.funlib.Function;
import de.uni_koblenz.jgralab.greql.types.Undefined;


@AcceptsUndefinedArguments
public class ToDotString extends Function {

	@Description(params = "arg", description = "Returns a converted DOT string representation of the given string.", categories = Category.STRINGS)
	public ToDotString() {
		super(2, 1, 1.0);
	}

	public String evaluate(Object arg) {
		if (arg == Undefined.UNDEFINED) {
			return "null";
		}
		String string = arg.toString();
		if (arg instanceof String) {
			string = "\"" + string + "\"";
		}
		string = string.replace("\\", "\\\\");
		string = string.replace("|", "\\|");
		string = string.replace("{", "\\{");
		string = string.replace("}", "\\}");
		string = string.replace("\n", "\\n");
		string = string.replace("\"", "\\\"");
		string = string.replace("<", "\\<");
		string = string.replace(">", "\\>");
		return string;
	}
}
