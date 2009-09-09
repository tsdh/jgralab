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

package de.uni_koblenz.jgralab.schema.impl;

import de.uni_koblenz.jgralab.codegenerator.CodeBlock;
import de.uni_koblenz.jgralab.codegenerator.CodeGenerator;
import de.uni_koblenz.jgralab.codegenerator.CodeSnippet;
import de.uni_koblenz.jgralab.schema.BooleanDomain;
import de.uni_koblenz.jgralab.schema.Package;
import de.uni_koblenz.jgralab.schema.Schema;

public final class BooleanDomainImpl extends BasicDomainImpl implements
		BooleanDomain {

	BooleanDomainImpl(Schema schema) {
		super(BOOLEANDOMAIN_NAME, schema.getDefaultPackage());
	}

	@Override
	public String getJavaAttributeImplementationTypeName(
			String schemaRootPackagePrefix) {
		return BOOLEANDOMAIN_NAME.toLowerCase();
	}

	@Override
	public String getJavaClassName(String schemaRootPackagePrefix) {
		return BOOLEANDOMAIN_NAME;
	}

	@Override
	public CodeBlock getReadMethod(String schemaPrefix, String variableName,
			String graphIoVariableName) {
		return new CodeSnippet(variableName + " = " + graphIoVariableName
				+ ".matchBoolean();");
	}

	@Override
	public String getTGTypeName(Package pkg) {
		return BOOLEANDOMAIN_NAME;
	}

	@Override
	public CodeBlock getWriteMethod(String schemaRootPackagePrefix,
			String variableName, String graphIoVariableName) {
		return new CodeSnippet(graphIoVariableName + ".writeBoolean("
				+ variableName + ");");
	}
	
	@Override
	public CodeBlock getTransactionReadMethod(String schemaPrefix,
			String variableName, String graphIoVariableName) {
		return new CodeSnippet(
				getJavaAttributeImplementationTypeName(schemaPrefix) + " tmp"
						+ variableName + " = " + graphIoVariableName
						+ ".matchBoolean();");
	}

	@Override
	public CodeBlock getTransactionWriteMethod(String schemaRootPackagePrefix,
			String variableName, String graphIoVariableName) {
		return getWriteMethod(schemaRootPackagePrefix, "is"
				+ CodeGenerator.camelCase(variableName) + "()",
				graphIoVariableName);
	}

	@Override
	public String getTransactionJavaAttributeImplementationTypeName(
			String schemaRootPackagePrefix) {
		return "Boolean";
	}

	@Override
	public String getTransactionJavaClassName(String schemaRootPackagePrefix) {
		return getJavaClassName(schemaRootPackagePrefix);
	}

	@Override
	public String getVersionedClass(String schemaRootPackagePrefix) {
		return "de.uni_koblenz.jgralab.impl.trans.VersionedReferenceImpl<"
				+ getTransactionJavaClassName(schemaRootPackagePrefix) + ">";
	}

	@Override
	public String getInitialValue() {
		return "false";
	}
}
