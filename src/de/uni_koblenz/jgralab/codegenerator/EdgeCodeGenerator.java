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

package de.uni_koblenz.jgralab.codegenerator;

import java.util.TreeSet;

import de.uni_koblenz.jgralab.schema.AggregationKind;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.VertexClass;

/**
 * TODO add comment
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class EdgeCodeGenerator extends AttributedElementCodeGenerator {

	public EdgeCodeGenerator(EdgeClass edgeClass, String schemaPackageName,
			CodeGeneratorConfiguration config) {
		super(edgeClass, schemaPackageName, config);
		rootBlock.setVariable("graphElementClass", "Edge");
		rootBlock.setVariable("schemaElementClass", "EdgeClass");
	}

	@Override
	protected CodeBlock createHeader() {
		CodeList code = new CodeList();
		EdgeClass ec = (EdgeClass) aec;
		code.setVariable("fromVertexClass", ec.getFrom().getVertexClass()
				.getQualifiedName());
		code.setVariable("toVertexClass", ec.getTo().getVertexClass()
				.getQualifiedName());
		code.setVariable("fromRoleName", ec.getFrom().getRolename());
		code.setVariable("toRoleName", ec.getTo().getRolename());
		code.setVariable("ecName", ec.getQualifiedName());
		CodeSnippet snippet = new CodeSnippet();
		snippet.add("/**");
		snippet.add(" * FromVertexClass: #fromVertexClass#");
		snippet.add(" * FromRoleName : #fromRoleName#");
		snippet.add(" * ToVertexClass: #toVertexClass#");
		snippet.add(" * ToRoleName : #toRoleName#");
		snippet.add(" */");
		code.addNoIndent(snippet);
		code.addNoIndent(super.createHeader());
		return code;
	}

	@Override
	protected CodeBlock createConstructor() {
		CodeList code = new CodeList();
		addImports("#jgPackage#.Vertex");
		code.addNoIndent(new CodeSnippet(
				true,
				"public #simpleClassName#Impl(int id, #jgPackage#.Graph g, Vertex alpha, Vertex omega) {",
				"\tsuper(id, g, alpha, omega);"));
		if (hasDefaultAttributeValues()) {
			code.addNoIndent(new CodeSnippet(
					"\tinitializeAttributesWithDefaultValues();"));
		}
		code.add(createSpecialConstructorCode());
		code.addNoIndent(new CodeSnippet("}"));
		return code;
	}

	@Override
	protected CodeBlock createBody() {
		CodeList code = (CodeList) super.createBody();
		if (currentCycle.isStdOrDbImplOrTransImpl()) {
			rootBlock.setVariable("baseClassName", "EdgeImpl");
			if (currentCycle.isStdImpl()) {
				addImports("#jgImplStdPackage#.#baseClassName#");
			}
			if (currentCycle.isTransImpl()) {
				addImports("#jgImplTransPackage#.#baseClassName#");
			}
			if (currentCycle.isDbImpl()) {
				addImports("#jgImplDbPackage#.#baseClassName#");
			}
		}
		if (config.hasTypeSpecificMethodsSupport()
				&& !currentCycle.isClassOnly()) {
			code.add(createNextEdgeMethods());
			code.add(createNextIncidenceMethods());
		}
		if (currentCycle.isStdOrDbImplOrTransImpl()) {
			code.add(createGetAggregationKindMethod());
			code.add(createGetAlphaAggregationKindMethod());
			code.add(createGetOmegaAggregationKindMethod());
			code.add(createReversedEdgeMethod());
		}
		code.add(createGetAlphaOmegaOverrides());
		return code;
	}

	private CodeBlock createGetAlphaOmegaOverrides() {
		CodeSnippet b = new CodeSnippet();
		EdgeClass ec = (EdgeClass) aec;
		VertexClass from = ec.getFrom().getVertexClass();
		VertexClass to = ec.getTo().getVertexClass();
		b.setVariable("fromVertexClass", from.getSimpleName());
		b.setVariable("toVertexClass", to.getSimpleName());
		if (!from.isInternal()) {
			addImports(schemaRootPackageName + "." + from.getQualifiedName());
		}
		if (!to.isInternal()) {
			addImports(schemaRootPackageName + "." + to.getQualifiedName());
		}
		if (currentCycle.isAbstract()) {
			if (!from.isInternal()) {
				b.add("public #fromVertexClass# getAlpha();");
			}
			if (!to.isInternal()) {
				b.add("public #toVertexClass# getOmega();");
			}
		} else {
			if (!from.isInternal()) {
				b.add("public #fromVertexClass# getAlpha() {");
				b.add("\treturn (#fromVertexClass#) super.getAlpha();");
				b.add("}");
			}
			if (!to.isInternal()) {
				b.add("public #toVertexClass# getOmega() {");
				b.add("\treturn (#toVertexClass#) super.getOmega();");
				b.add("}");
			}
		}
		return b;
	}

	/**
	 * 
	 * @return
	 */
	private CodeBlock createReversedEdgeMethod() {
		CodeSnippet code = new CodeSnippet(true,
				"protected #jgImplPackage#.ReversedEdgeBaseImpl createReversedEdge() {");
		if (currentCycle.isStdImpl()) {
			addImports("#schemaImplStdPackage#.Reversed#simpleClassName#Impl");
		}
		if (currentCycle.isTransImpl()) {
			addImports("#schemaImplTransPackage#.Reversed#simpleClassName#Impl");
		}
		if (currentCycle.isDbImpl()) {
			addImports("#schemaImplDbPackage#.Reversed#simpleClassName#Impl");
		}
		code.add("\treturn new Reversed#simpleClassName#Impl(this, graph);");
		code.add("}");
		return code;
	}

	private CodeBlock createNextEdgeMethods() {
		CodeList code = new CodeList();
		TreeSet<AttributedElementClass> superClasses = new TreeSet<AttributedElementClass>();
		superClasses.addAll(aec.getAllSuperClasses());
		superClasses.add(aec);

		if (config.hasTypeSpecificMethodsSupport()) {
			for (AttributedElementClass ec : superClasses) {
				if (ec.isInternal()) {
					continue;
				}
				EdgeClass ecl = (EdgeClass) ec;
				code.addNoIndent(createNextEdgeMethod(ecl));
			}
		}
		return code;
	}

	private CodeBlock createNextEdgeMethod(EdgeClass ec) {
		CodeSnippet code = new CodeSnippet(true);
		code.setVariable("ecQualifiedName",
				schemaRootPackageName + "." + ec.getQualifiedName());
		code.setVariable("ecCamelName", camelCase(ec.getUniqueName()));
		code.setVariable("formalParams", "");
		code.setVariable("actualParams", "");

		if (currentCycle.isAbstract()) {
			code.add("/**",
					" * @return the next #ecQualifiedName# edge in the global edge sequence");
			code.add(" */",
					"public #ecQualifiedName# getNext#ecCamelName#InGraph(#formalParams#);");
		}
		if (currentCycle.isStdOrDbImplOrTransImpl()) {
			code.add(
					"public #ecQualifiedName# getNext#ecCamelName#InGraph(#formalParams#) {",
					"\treturn (#ecQualifiedName#)getNextEdge(#ecQualifiedName#.class#actualParams#);",
					"}");
		}
		return code;
	}

	private CodeBlock createNextIncidenceMethods() {
		CodeList code = new CodeList();

		TreeSet<AttributedElementClass> superClasses = new TreeSet<AttributedElementClass>();
		superClasses.addAll(aec.getAllSuperClasses());
		superClasses.add(aec);

		if (config.hasTypeSpecificMethodsSupport()) {
			for (AttributedElementClass ec : superClasses) {
				if (ec.isInternal()) {
					continue;
				}
				addImports("#jgPackage#.EdgeDirection");
				EdgeClass ecl = (EdgeClass) ec;
				code.addNoIndent(createNextIncidenceMethod(ecl, false));
				code.addNoIndent(createNextIncidenceMethod(ecl, true));
			}
		}
		return code;
	}

	private CodeBlock createNextIncidenceMethod(EdgeClass ec,
			boolean withOrientation) {

		CodeSnippet code = new CodeSnippet(true);
		code.setVariable("ecQualifiedName",
				schemaRootPackageName + "." + ec.getQualifiedName());
		code.setVariable("ecCamelName", camelCase(ec.getUniqueName()));
		code.setVariable("formalParams",
				(withOrientation ? "EdgeDirection orientation" : ""));

		code.setVariable("actualParams", (withOrientation ? ", orientation"
				: ""));
		if (currentCycle.isAbstract()) {
			code.add("/**",
					" * @return the next edge of class #ecQualifiedName# at the \"this\" vertex");

			if (withOrientation) {
				code.add(" * @param orientation the orientation of the edge");
			}
			code.add(" */",
					"public #ecQualifiedName# getNext#ecCamelName#Incidence(#formalParams#);");
		}
		if (currentCycle.isStdOrDbImplOrTransImpl()) {
			code.add(
					"public #ecQualifiedName# getNext#ecCamelName#Incidence(#formalParams#) {",
					"\treturn (#ecQualifiedName#)getNextIncidence(#ecQualifiedName#.class#actualParams#);",
					"}");
		}
		return code;
	}

	private CodeBlock createGetAggregationKindMethod() {
		CodeSnippet code = new CodeSnippet(true);
		EdgeClass ec = (EdgeClass) aec;
		String val = "NONE";

		if ((ec.getTo().getAggregationKind() == AggregationKind.COMPOSITE)
				|| (ec.getFrom().getAggregationKind() == AggregationKind.COMPOSITE)) {
			val = "COMPOSITE";
		} else if ((ec.getTo().getAggregationKind() == AggregationKind.SHARED)
				|| (ec.getFrom().getAggregationKind() == AggregationKind.SHARED)) {
			val = "SHARED";
		}
		code.setVariable("semantics", val);
		code.add(
				"public de.uni_koblenz.jgralab.schema.AggregationKind getAggregationKind() {",
				"\treturn de.uni_koblenz.jgralab.schema.AggregationKind.#semantics#;",
				"}");
		return code;
	}

	private CodeBlock createGetAlphaAggregationKindMethod() {
		CodeSnippet code = new CodeSnippet(true);
		EdgeClass ec = (EdgeClass) aec;
		code.setVariable("semantics", ec.getFrom().getAggregationKind()
				.toString());
		code.add(
				"@Override",
				"public de.uni_koblenz.jgralab.schema.AggregationKind getAlphaAggregationKind() {",
				"\treturn de.uni_koblenz.jgralab.schema.AggregationKind.#semantics#;",
				"}");
		return code;
	}

	private CodeBlock createGetOmegaAggregationKindMethod() {
		CodeSnippet code = new CodeSnippet(true);
		EdgeClass ec = (EdgeClass) aec;
		code.setVariable("semantics", ec.getTo().getAggregationKind()
				.toString());
		code.add(
				"@Override",
				"public de.uni_koblenz.jgralab.schema.AggregationKind getOmegaAggregationKind() {",
				"\treturn de.uni_koblenz.jgralab.schema.AggregationKind.#semantics#;",
				"}");
		return code;
	}
}
