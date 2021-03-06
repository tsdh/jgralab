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
package de.uni_koblenz.jgralab.schema.impl.compilation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Logger;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

import de.uni_koblenz.jgralab.EclipseAdapter;
import de.uni_koblenz.jgralab.JGraLab;

/**
 * {@link ClassFileManager} redirects requests of the Java compiler in two
 * cases:
 * 
 * 1. In-memory-compilation: The output files of the compiler are
 * {@link InMemoryClassFile}s which save the generated bytecode in an array
 * using a {@link ByteArrayOutputStream}.
 * 
 * 2. When used as Eclipse plugin, JGraLab is not in the class path. In this
 * case, JGraLab classes requested by the compiler are handled by JGraLab
 * {@link EclipseAdapter} which locates class files using the bundle activator
 * and the file locator service.
 * 
 * @author ist@uni-koblenz.de
 */
public class ClassFileManager extends
		ForwardingJavaFileManager<JavaFileManager> {

	private final Logger logger = null; // JGraLab.getLogger(ClassFileManager.class);

	private final String qualifiedSchemaName;

	public ClassFileManager(ManagableArtifact ma, JavaFileManager fm) {
		super(fm);
		this.qualifiedSchemaName = ma.getManagedName();
	}

	@Override
	public boolean hasLocation(Location location) {
		return super.hasLocation(location)
				|| location.getName().equals("CLASS_PATH");
	}

	@Override
	public String inferBinaryName(Location location, JavaFileObject file) {
		if ((logger != null) && location.getName().equals("CLASS_PATH")) {
			logger.fine("(" + location + ", " + file + ")");
		}
		// handle ClassFileObjects specially
		if (location.getName().equals("CLASS_PATH")
				&& (file instanceof ClassFileObject)) {
			return ((ClassFileObject) file).getBinaryName();
		}
		// handle all other objects by delegation
		return super.inferBinaryName(location, file);
	}

	@Override
	public JavaFileObject getJavaFileForOutput(Location location,
			String className, Kind kind, FileObject sibling) {
		if (logger != null) {
			logger.fine("(" + location + ", " + className + ", " + kind + ", "
					+ sibling + ")");
		}
		// redirect compiler output to InMemoryClassFiles
		InMemoryClassFile cfa = new InMemoryClassFile(className);
		SchemaClassManager.instance(qualifiedSchemaName).putSchemaClass(
				className, cfa);
		// System.out.println("Registered class");
		return cfa;
	}

	@Override
	public Iterable<JavaFileObject> list(Location location, String packageName,
			Set<Kind> kinds, boolean recurse) throws IOException {
		if (logger != null) {
			logger.fine("(" + location + ", " + packageName + ", " + kinds
					+ ", " + recurse + ")");
		}

		EclipseAdapter ea = JGraLab.getEclipseAdapter();
		if ((ea == null)
				|| !((location.getName().equals("CLASS_PATH") && kinds
						.contains(Kind.CLASS)))) {
			// not in a plugin, or not looking for class files -> delegate to
			// the standard implementation
			return super.list(location, packageName, kinds, recurse);
		}

		// if run as Eclipse plugin, delegate to the JGraLab EclipseAdapter and
		// list class files inside the plugin bundle
		return ea.listJavaFileObjects(packageName, recurse);
	}
}
