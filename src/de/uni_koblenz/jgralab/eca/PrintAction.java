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
package de.uni_koblenz.jgralab.eca;

import de.uni_koblenz.jgralab.eca.events.Event;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;

public class PrintAction<AEC extends AttributedElementClass<AEC, ?>> implements
		Action<AEC> {

	/**
	 * Message to print on console
	 */
	private String message;

	// +++++++++++++++++++++++++++++++++++++++++++++++++

	/**
	 * Creates a PrintAction with the given message
	 * 
	 * @param message
	 *            the message to print
	 */
	public PrintAction(String message) {
		this.message = message;
	}

	// +++++++++++++++++++++++++++++++++++++++++++++++++

	/**
	 * Executes the action, in this case print the message on console
	 */
	@Override
	public void doAction(Event<AEC> event) {
		System.out.println(this.message);
	}

	// +++++++++++++++++++++++++++++++++++++++++++++++++

	/**
	 * @return Message, this action prints on Console
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Sets the message to print on Console
	 * 
	 * @param message
	 *            to print
	 */
	public void setMessage(String message) {
		this.message = message;
	}

}
