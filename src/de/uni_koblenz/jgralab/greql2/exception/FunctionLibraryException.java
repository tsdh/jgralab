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

package de.uni_koblenz.jgralab.greql2.exception;

import java.util.List;

import de.uni_koblenz.jgralab.greql2.schema.SourcePosition;

/**
 * Should be thrown if something went wrong with the functionlibary that is not
 * covered by the other exceptions
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class FunctionLibraryException extends QuerySourceException {

	static final long serialVersionUID = -1234562;

	public FunctionLibraryException(String functionName,
			List<SourcePosition> sourcePositions, Exception cause) {
		super("Error evaluating a function", functionName, sourcePositions,
				cause);
	}

	public FunctionLibraryException(String functionName,
			List<SourcePosition> sourcePositions) {
		super("Error evaluating a function", functionName, sourcePositions);
	}
}
