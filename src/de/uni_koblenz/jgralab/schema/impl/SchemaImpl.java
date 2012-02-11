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

package de.uni_koblenz.jgralab.schema.impl;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphFactory;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.ProgressFunction;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.codegenerator.CodeGenerator;
import de.uni_koblenz.jgralab.codegenerator.CodeGeneratorConfiguration;
import de.uni_koblenz.jgralab.codegenerator.EdgeCodeGenerator;
import de.uni_koblenz.jgralab.codegenerator.EnumCodeGenerator;
import de.uni_koblenz.jgralab.codegenerator.GraphCodeGenerator;
import de.uni_koblenz.jgralab.codegenerator.GraphFactoryGenerator;
import de.uni_koblenz.jgralab.codegenerator.RecordCodeGenerator;
import de.uni_koblenz.jgralab.codegenerator.ReversedEdgeCodeGenerator;
import de.uni_koblenz.jgralab.codegenerator.SchemaCodeGenerator;
import de.uni_koblenz.jgralab.codegenerator.VertexCodeGenerator;
import de.uni_koblenz.jgralab.impl.ConsoleProgressFunction;
import de.uni_koblenz.jgralab.impl.generic.GenericGraphFactoryImpl;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.BooleanDomain;
import de.uni_koblenz.jgralab.schema.CompositeDomain;
import de.uni_koblenz.jgralab.schema.Domain;
import de.uni_koblenz.jgralab.schema.DoubleDomain;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.EnumDomain;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.IntegerDomain;
import de.uni_koblenz.jgralab.schema.ListDomain;
import de.uni_koblenz.jgralab.schema.LongDomain;
import de.uni_koblenz.jgralab.schema.MapDomain;
import de.uni_koblenz.jgralab.schema.NamedElement;
import de.uni_koblenz.jgralab.schema.Package;
import de.uni_koblenz.jgralab.schema.RecordDomain;
import de.uni_koblenz.jgralab.schema.RecordDomain.RecordComponent;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.SetDomain;
import de.uni_koblenz.jgralab.schema.StringDomain;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.schema.exception.InvalidNameException;
import de.uni_koblenz.jgralab.schema.exception.SchemaClassAccessException;
import de.uni_koblenz.jgralab.schema.exception.SchemaException;
import de.uni_koblenz.jgralab.schema.impl.compilation.ClassFileManager;
import de.uni_koblenz.jgralab.schema.impl.compilation.InMemoryJavaSourceFile;
import de.uni_koblenz.jgralab.schema.impl.compilation.SchemaClassManager;

/**
 * @author ist@uni-koblenz.de
 */
public class SchemaImpl implements Schema {
	// we need a hard reference here, cause the SchemaClassManager uses only
	// weak
	// references. This way, when the schema gets collected, the class manager
	// is free for collection, too.
	private SchemaClassManager schemaClassManager = null;

	public SchemaClassManager getSchemaClassManager() {
		return this.schemaClassManager;
	}


	static final Class<?>[] GRAPHCLASS_CREATE_SIGNATURE = {
		ImplementationType.class, String.class, int.class, int.class };

	/**
	 * This is the name of the package into which the implementation classes for
	 * this schema are generated. The impl package is child of the package for
	 * the Schema.
	 */
	public static final String IMPL_PACKAGE_NAME = "impl";

	/**
	 * This is the name of the package into which the implementation classes for
	 * this schema are generated. The impl package is child of the package for
	 * Schema.
	 */
	public static final String IMPLSTDPACKAGENAME = "impl.std";
	public static final String IMPLTRANSPACKAGENAME = "impl.trans";
	public static final String IMPLDATABASEPACKAGENAME = "impl.db";

	static final Class<?>[] VERTEX_CLASS_CREATE_SIGNATURE = { int.class };

	/**
	 * Toggles if the schema allows lowercase enumeration constants
	 */
	private boolean allowLowercaseEnumConstants = true;

	private EdgeClass defaultEdgeClass;

	private GraphClass defaultGraphClass;

	private Package defaultPackage;

	private VertexClass defaultVertexClass;

	protected CodeGeneratorConfiguration config;

	/**
	 * Maps from qualified name to the {@link Domain}.
	 */
	private Map<String, Domain> domains = new HashMap<String, Domain>();

	private DirectedAcyclicGraph<Domain> domainsDag = new DirectedAcyclicGraph<Domain>();

	private boolean finished = false;

	/**
	 * Holds a reference to the {@link GraphClass} of this schema (not the
	 * default graph class {@link GraphClass})
	 */
	private GraphClass graphClass;

	/**
	 * The name of this schema without the package prefix.
	 */
	private String name;

	/**
	 * The package prefix of this schema.
	 */
	private String packagePrefix;

	/**
	 * Maps from simple names to a set of {@link NamedElement}s which have this
	 * simple name. Used for creation of unique names.
	 */
	private Map<String, AttributedElementClass<?, ?>> duplicateSimpleNames = new HashMap<String, AttributedElementClass<?, ?>>();

	/**
	 * Maps from qualified name to the {@link Package} with that qualified name.
	 */
	private Map<String, Package> packages = new TreeMap<String, Package>();

	/**
	 * The qualified name of this schema, that is {@link #packagePrefix} DOT
	 * {@link #name}
	 */
	private String qualifiedName;

	/**
	 * A set of all qualified names known to this schema.
	 */
	private Map<String, NamedElement> namedElements = new TreeMap<String, NamedElement>();

	private BooleanDomain booleanDomain;

	private DoubleDomain doubleDomain;

	private IntegerDomain integerDomain;

	private LongDomain longDomain;

	private StringDomain stringDomain;

	private static final Pattern SCHEMA_NAME_PATTERN = Pattern
			.compile("^\\p{Upper}(\\p{Alnum}|[_])*\\p{Alnum}$");

	private static final Pattern PACKAGE_PREFIX_PATTERN = Pattern
			.compile("^\\p{Lower}\\w*(\\.\\p{Lower}\\w*)*$");

	/**
	 * Creates a new <code>Schema</code>.
	 *
	 * @param name
	 *            Name of schema.
	 * @param packagePrefix
	 *            Package prefix of schema.
	 */
	public SchemaImpl(String name, String packagePrefix) {
		if (!SCHEMA_NAME_PATTERN.matcher(name).matches()) {
			this.throwInvalidSchemaNameException();
		}

		if (!PACKAGE_PREFIX_PATTERN.matcher(packagePrefix).matches()) {
			this.throwInvalidPackagePrefixNameException();
		}

		this.name = name;
		this.packagePrefix = packagePrefix;
		this.qualifiedName = packagePrefix + "." + name;
		this.schemaClassManager = SchemaClassManager.instance(this.qualifiedName);

		// Needs to be created before any NamedElement can be created
		this.defaultPackage = this.createDefaultPackage();

		// Creation of the BasicDomains
		this.booleanDomain = this.createBooleanDomain();
		this.doubleDomain = this.createDoubleDomain();
		this.integerDomain = this.createIntegerDomain();
		this.longDomain = this.createLongDomain();
		this.stringDomain = this.createStringDomain();

		/*
		 * Needs to be created before any GraphElementClass element can be
		 * created.
		 */
		this.defaultGraphClass = GraphClassImpl.createDefaultGraphClass(this);

		// Creation of default GraphElementClasses
		this.defaultVertexClass = VertexClassImpl.createDefaultVertexClass(this);
		this.defaultEdgeClass = EdgeClassImpl.createDefaultEdgeClass(this);
		this.config = this.createDefaultConfig();
	}

	protected Package createDefaultPackage() {
		return PackageImpl.createDefaultPackage(this);
	}

	private void throwInvalidSchemaNameException() {
		throw new InvalidNameException(
				"Invalid schema name '"
						+ this.name
						+ "'.\n"
						+ "The name must not be empty.\n"
						+ "The name must start with a capital letter.\n"
						+ "Any following character must be alphanumeric and/or a '_' character.\n"
						+ "The name must end with an alphanumeric character.");
	}

	private void throwInvalidPackagePrefixNameException() {
		throw new InvalidNameException(
				"Invalid schema package prefix '"
						+ this.packagePrefix
						+ "'.\n"
						+ "The packagePrefix must not be empty.\n"
						+ "The package prefix must start with a small letter.\n"
						+ "The first character after each '.' must be a small letter.\n"
						+ "Following characters may be alphanumeric and/or '_' characters.\n"
						+ "The last character before a '.' and the end of the line must be an alphanumeric character.");
	}

	private CodeGeneratorConfiguration createDefaultConfig() {
		CodeGeneratorConfiguration out = new CodeGeneratorConfiguration();
		if (java.lang.Package.getPackage(this.packagePrefix + ". "
				+ IMPLSTDPACKAGENAME) == null) {
			out.setStandardSupport(false);
		}
		if (java.lang.Package.getPackage(this.packagePrefix + ". "
				+ IMPLTRANSPACKAGENAME) != null) {
			out.setTransactionSupport(true);
		}
		if (java.lang.Package.getPackage(this.packagePrefix + ". "
				+ IMPLDATABASEPACKAGENAME) != null) {
			out.setDatabaseSupport(true);
		}

		return out;
		// TODO: Monte, check for the other values :-)
	}

	void addDomain(Domain dom) {
		assert !this.domains.containsKey(dom.getQualifiedName()) : "There already is a Domain with the qualified name: "
				+ dom.getQualifiedName() + " in the Schema!";
		this.domains.put(dom.getQualifiedName(), dom);
	}

	void addPackage(PackageImpl pkg) {
		assert !this.packages.containsKey(pkg.getQualifiedName()) : "There already is a Package with the qualified name '"
				+ pkg.getQualifiedName() + "' in the Schema!";
		this.packages.put(pkg.getQualifiedName(), pkg);
	}

	void addNamedElement(NamedElement namedElement) {
		assert !this.namedElements.containsKey(namedElement.getQualifiedName()) : "You are trying to add the NamedElement '"
				+ namedElement.getQualifiedName()
				+ "' to this Schema, but that does already exist!";

		this.namedElements.put(namedElement.getQualifiedName(), namedElement);

		if (!(namedElement instanceof AttributedElementClass)) {
			return;
		}

		AttributedElementClass<?, ?> aec = (AttributedElementClass<?, ?>) namedElement;


		if (this.duplicateSimpleNames.containsKey(aec.getSimpleName())) {
			AttributedElementClass<?, ?> other = this.duplicateSimpleNames.get(aec
					.getSimpleName());
			if (other != null) {
				((NamedElementImpl) other).changeUniqueName();
				this.duplicateSimpleNames.put(aec.getSimpleName(), null);
			}
			((NamedElementImpl) aec).changeUniqueName();
		} else {
			this.duplicateSimpleNames.put(aec.getSimpleName(), aec);
		}
	}

	@Override
	public NamedElement getNamedElement(String qualifiedName) {
		return this.namedElements.get(qualifiedName);
	}

	@Override
	public boolean allowsLowercaseEnumConstants() {
		return this.allowLowercaseEnumConstants;
	}

	private Vector<InMemoryJavaSourceFile> createClasses(
			CodeGeneratorConfiguration config) {
		Vector<InMemoryJavaSourceFile> javaSources = new Vector<InMemoryJavaSourceFile>();

		/* create code for graph */
		GraphCodeGenerator graphCodeGenerator = new GraphCodeGenerator(
				this.graphClass, this.packagePrefix, this.name, config);
		javaSources.addAll(graphCodeGenerator.createJavaSources());

		for (VertexClass vertexClass : this.graphClass.getVertexClasses()) {
			VertexCodeGenerator codeGen = new VertexCodeGenerator(vertexClass,
					this.packagePrefix, config);
			javaSources.addAll(codeGen.createJavaSources());
		}

		for (EdgeClass edgeClass : this.graphClass.getEdgeClasses()) {
			CodeGenerator codeGen = new EdgeCodeGenerator(edgeClass,
					this.packagePrefix, config);
			javaSources.addAll(codeGen.createJavaSources());

			if (!edgeClass.isAbstract()) {
				codeGen = new ReversedEdgeCodeGenerator(edgeClass,
						this.packagePrefix, config);
				javaSources.addAll(codeGen.createJavaSources());
			}
		}

		// build records and enums
		for (Domain domain : this.getRecordDomains()) {
			// also generate an abstract class for Records
			CodeGenerator rcode = new RecordCodeGenerator(
					(RecordDomain) domain, this.packagePrefix, config);
			javaSources.addAll(rcode.createJavaSources());
		}
		for (Domain domain : this.getEnumDomains()) {
			CodeGenerator ecode = new EnumCodeGenerator((EnumDomain) domain,
					this.packagePrefix);
			javaSources.addAll(ecode.createJavaSources());
		}

		return javaSources;
	}

	@Override
	public void createJAR(CodeGeneratorConfiguration config, String jarFileName)
			throws IOException, GraphIOException {
		File tmpFile = File.createTempFile("jar-creation", "tmp");
		tmpFile.deleteOnExit();
		File tmpDir = new File(tmpFile.getParent());
		File schemaDir = new File(tmpDir + File.separator + this.getName());
		if (!schemaDir.mkdir()) {
			System.err.println("Couldn't create " + schemaDir);
			return;
		}
		System.out.println("Committing schema classes to " + schemaDir);
		this.commit(schemaDir.getAbsolutePath(), config,
				new ConsoleProgressFunction("Committing"));

		this.compileClasses(schemaDir);

		// TODO: That should be doable without resorting to the cmd line, but
		// how? JarFile seems to provide only read access...
		Process proc = Runtime.getRuntime().exec(
				"jar cf " + jarFileName + " -C " + schemaDir.getAbsolutePath()
				+ " .");

		try {
			proc.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		this.deleteRecursively(schemaDir);
	}

	private void deleteRecursively(File file) {
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				this.deleteRecursively(f);
			}
			file.delete();
		} else {
			file.delete();
		}
	}

	private void compileClasses(File schemaDir) throws IOException {
		JavaCompiler c = ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager fileManager = c.getStandardFileManager(null,
				null, null);
		Iterable<? extends JavaFileObject> compilationUnits = fileManager
				.getJavaFileObjectsFromFiles(this.getJavaFiles(schemaDir));
		c.getTask(null, fileManager, null, null, null, compilationUnits).call();
		fileManager.close();
	}

	private List<File> getJavaFiles(File schemaDir) {
		LinkedList<File> sources = new LinkedList<File>();
		for (File f : schemaDir.listFiles()) {
			if (f.isDirectory()) {
				sources.addAll(this.getJavaFiles(f));
			} else if (f.getName().endsWith(".java")) {
				sources.add(f);
			} else {
				System.out.println("Skipping " + f + "...");
			}
		}
		return sources;
	}

	@Override
	public Vector<InMemoryJavaSourceFile> commit(
			CodeGeneratorConfiguration config) {
		if (!this.finished) {
			throw new SchemaException(
					"Schema must be finish before committing is allowed. "
							+ "Call finish() to finish the schema.");
		}

		Vector<InMemoryJavaSourceFile> javaSources = new Vector<InMemoryJavaSourceFile>();

		// generate schema class
		CodeGenerator schemaCodeGenerator = new SchemaCodeGenerator(this,
				this.packagePrefix, config);
		javaSources.addAll(schemaCodeGenerator.createJavaSources());

		// generate factory
		CodeGenerator factoryCodeGenerator = new GraphFactoryGenerator(this,
				this.packagePrefix, config);
		javaSources.addAll(factoryCodeGenerator.createJavaSources());

		// generate graph classes

		if (this.graphClass.getQualifiedName().equals("Graph")) {
			throw new SchemaException(
					"The defined GraphClass must not be named Graph!");
		}

		javaSources.addAll(this.createClasses(config));
		return javaSources;
	}

	private void createFiles(CodeGeneratorConfiguration config,
			String pathPrefix, ProgressFunction progressFunction,
			long schemaElements, long currentCount, long interval)
					throws GraphIOException {

		/* create code for graph */
		GraphCodeGenerator graphCodeGenerator = new GraphCodeGenerator(
				this.graphClass, this.packagePrefix, this.name, config);
		graphCodeGenerator.createFiles(pathPrefix);

		for (VertexClass vertexClass : this.graphClass.getVertexClasses()) {
			VertexCodeGenerator codeGen = new VertexCodeGenerator(vertexClass,
					this.packagePrefix, config);
			codeGen.createFiles(pathPrefix);
			if (progressFunction != null) {
				schemaElements++;
				currentCount++;
				if (currentCount == interval) {
					progressFunction.progress(schemaElements);
					currentCount = 0;
				}
			}
		}

		for (EdgeClass edgeClass : this.graphClass.getEdgeClasses()) {
			CodeGenerator codeGen = new EdgeCodeGenerator(edgeClass,
					this.packagePrefix, config);
			codeGen.createFiles(pathPrefix);

			if (!edgeClass.isAbstract()) {
				codeGen = new ReversedEdgeCodeGenerator(edgeClass,
						this.packagePrefix, config);
				codeGen.createFiles(pathPrefix);
			}
			if (progressFunction != null) {
				schemaElements++;
				currentCount++;
				if (currentCount == interval) {
					progressFunction.progress(schemaElements);
					currentCount = 0;
				}
			}
		}

		// build records and enums
		for (Domain domain : this.getRecordDomains()) {
			// also generate an abstract class for Records
			CodeGenerator rcode = new RecordCodeGenerator(
					(RecordDomain) domain, this.packagePrefix, config);
			rcode.createFiles(pathPrefix);
			if (progressFunction != null) {
				schemaElements++;
				currentCount++;
				if (currentCount == interval) {
					progressFunction.progress(schemaElements);
					currentCount = 0;
				}
			}
		}
		for (Domain domain : this.getEnumDomains()) {
			CodeGenerator ecode = new EnumCodeGenerator((EnumDomain) domain,
					this.packagePrefix);
			ecode.createFiles(pathPrefix);
		}
		if (progressFunction != null) {
			schemaElements++;
			currentCount++;
			if (currentCount == interval) {
				progressFunction.progress(schemaElements);
				currentCount = 0;
			}
		}
	}

	@Override
	public void commit(String pathPrefix, CodeGeneratorConfiguration config)
			throws GraphIOException {
		this.commit(pathPrefix, config, null);
	}

	@Override
	public void commit(String pathPrefix, CodeGeneratorConfiguration config,
			ProgressFunction progressFunction) throws GraphIOException {
		if (!this.finished) {
			throw new SchemaException(
					"Schema must be finish before committing is allowed. "
							+ "Call finish() to finish the schema.");
		}
		// progress bar for schema generation
		// ProgressFunctionImpl pf;
		long schemaElements = 0, currentCount = 0, interval = 1;
		if (progressFunction != null) {
			int elements = this.getNumberOfElements();
			if (config.hasTransactionSupport()) {
				elements *= 2;
			}
			progressFunction.init(elements);
			interval = progressFunction.getUpdateInterval();
		}

		// ********************* build code **********************
		if (!pathPrefix.endsWith(File.separator)) {
			pathPrefix += File.separator;
		}

		// generate schema class
		CodeGenerator schemaCodeGenerator = new SchemaCodeGenerator(this,
				this.packagePrefix, config);
		schemaCodeGenerator.createFiles(pathPrefix);

		// generate factory
		CodeGenerator factoryCodeGenerator = new GraphFactoryGenerator(this,
				this.packagePrefix, config);
		factoryCodeGenerator.createFiles(pathPrefix);

		// generate graph class
		if (this.graphClass.getQualifiedName().equals("Graph")) {
			throw new SchemaException(
					"The defined GraphClass must not be named Graph!");
		}

		this.createFiles(config, pathPrefix, progressFunction, schemaElements,
				currentCount, interval);

		// finish progress bar
		if (progressFunction != null) {
			progressFunction.finished();
		}
	}

	@Override
	public int compareTo(Schema other) {
		return this.qualifiedName.compareTo(other.getQualifiedName());
	}

	@Override
	public void compile(CodeGeneratorConfiguration config) {
		this.finish();
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			throw new SchemaException("Cannot compile schema " + this.qualifiedName
					+ ". Most probably you use a JRE instead of a JDK. "
					+ "The JRE does not provide a compiler.");

		}
		StandardJavaFileManager jfm = compiler.getStandardFileManager(null,
				null, null);
		ClassFileManager manager = new ClassFileManager(this, jfm);
		Vector<InMemoryJavaSourceFile> javaSources = this.commit(config);
		compiler.getTask(null, manager, null, null, null, javaSources).call();
	}

	@Override
	public Attribute createAttribute(String name, Domain dom,
			AttributedElementClass<?, ?> aec, String defaultValueAsString) {
		if (this.finished) {
			throw new SchemaException("No changes to finished schema!");
		}
		return new AttributeImpl(name, dom, aec, defaultValueAsString);
	}

	@Override
	public EnumDomain createEnumDomain(String qualifiedName) {
		return this.createEnumDomain(qualifiedName, new ArrayList<String>());
	}

	@Override
	public EnumDomain createEnumDomain(String qualifiedName,
			List<String> enumComponents) {
		if (this.finished) {
			throw new SchemaException("No changes to finished schema!");
		}
		String[] components = splitQualifiedName(qualifiedName);
		PackageImpl parent = (PackageImpl) this.createPackageWithParents(components[0]);
		String simpleName = components[1];
		EnumDomain ed = new EnumDomainImpl(simpleName, parent, enumComponents);
		return ed;
	}

	@Override
	public GraphClass createGraphClass(String simpleName) {
		if (this.finished) {
			throw new SchemaException("No changes to finished schema!");
		}
		if (this.graphClass != null) {
			throw new SchemaException(
					"Only one GraphClass (except DefaultGraphClass) is allowed in a Schema! '"
							+ this.graphClass.getQualifiedName()
							+ "' is already there.");
		}

		if (simpleName.equals(GraphClass.DEFAULTGRAPHCLASS_NAME)) {
			throw new InvalidNameException(
					"A GraphClass must not be named like the default GraphClass ("
							+ GraphClass.DEFAULTGRAPHCLASS_NAME + ")");
		}

		if (simpleName.contains(".")) {
			throw new InvalidNameException(
					"A GraphClass must always be in the default package!");
		}
		GraphClassImpl gc = new GraphClassImpl(simpleName, this);
		gc.addSuperClass(this.defaultGraphClass);
		return gc;
	}

	protected BooleanDomain createBooleanDomain() {
		if (this.booleanDomain != null) {
			throw new SchemaException(
					"The BooleanDomain for this Schema was already created!");
		}
		this.booleanDomain = new BooleanDomainImpl(this);
		return this.booleanDomain;
	}

	protected DoubleDomain createDoubleDomain() {
		if (this.doubleDomain != null) {
			throw new SchemaException(
					"The DoubleDomain for this Schema was already created!");
		}

		this.doubleDomain = new DoubleDomainImpl(this);
		return this.doubleDomain;
	}

	protected IntegerDomain createIntegerDomain() {
		if (this.integerDomain != null) {
			throw new SchemaException(
					"The IntegerDomain for this Schema was already created!");
		}

		this.integerDomain = new IntegerDomainImpl(this);
		return this.integerDomain;
	}

	protected LongDomain createLongDomain() {
		if (this.longDomain != null) {
			throw new SchemaException(
					"The LongDomain for this Schema was already created!");
		}

		this.longDomain = new LongDomainImpl(this);
		return this.longDomain;
	}

	protected StringDomain createStringDomain() {
		if (this.stringDomain != null) {
			throw new SchemaException(
					"The StringDomain for this Schema was already created!");
		}

		this.stringDomain = new StringDomainImpl(this);
		return this.stringDomain;
	}

	@Override
	public ListDomain createListDomain(Domain baseDomain) {
		if (this.finished) {
			throw new SchemaException("No changes to finished schema!");
		}
		String qn = "List<" + baseDomain.getQualifiedName() + ">";
		if (this.domains.containsKey(qn)) {
			return (ListDomain) this.domains.get(qn);
		}
		return new ListDomainImpl(this, baseDomain);
	}

	@Override
	public MapDomain createMapDomain(Domain keyDomain, Domain valueDomain) {
		if (this.finished) {
			throw new SchemaException("No changes to finished schema!");
		}
		String qn = "Map<" + keyDomain.getQualifiedName() + ", "
				+ valueDomain.getQualifiedName() + ">";
		if (this.domains.containsKey(qn)) {
			return (MapDomain) this.domains.get(qn);
		}
		return new MapDomainImpl(this, keyDomain, valueDomain);
	}

	protected Package createPackage(String sn, Package parentPkg) {
		return new PackageImpl(sn, parentPkg, this);
	}

	/**
	 * Creates a {@link Package} with given qualified name, or returns an
	 * existing package with this qualified name.
	 *
	 * @param qn
	 *            the qualified name of the package
	 * @return a new {@link Package} with the given qualified name, or an
	 *         existing package with this qualified name.
	 */
	protected Package createPackageWithParents(String qn) {
		if (this.packages.containsKey(qn)) {
			return this.packages.get(qn);
		}

		String[] components = splitQualifiedName(qn);
		String parent = components[0];
		String pkgSimpleName = components[1];

		assert !pkgSimpleName.contains(".") : "The package simple name '"
		+ pkgSimpleName + "' must not contain a dot!";

		Package currentParent = this.defaultPackage;
		String currentPkgQName = "";

		if (!this.packages.containsKey(parent)) {
			// the parent doesn't exist!

			for (String component : parent.split("\\.")) {
				if (currentParent != this.defaultPackage) {
					currentPkgQName = currentParent.getQualifiedName() + "."
							+ component;
				} else {
					currentPkgQName = component;
				}
				if (this.packages.containsKey(currentPkgQName)) {
					currentParent = this.packages.get(currentPkgQName);
					continue;
				}
				currentParent = this.createPackage(component, currentParent);
			}
		} else {
			currentParent = this.packages.get(parent);
		}

		// ok, parent existed or is created;
		assert currentParent.getQualifiedName().equals(parent) : "Something went wrong when creating a package with parents: "
		+ "parent should be \""
		+ parent
		+ "\" but created was \""
		+ currentParent.getQualifiedName() + "\".";
		assert (currentParent.getQualifiedName().isEmpty() ? currentParent == this.defaultPackage
				: true) : "The parent package of package '" + pkgSimpleName
				+ "' is empty, but not the default package.";
		return this.createPackage(pkgSimpleName, currentParent);
	}

	/**
	 * Given a qualified name like foo.bar.baz returns a string array with two
	 * components: the package prefix (foo.bar) and the simple name (baz).
	 *
	 * @param qualifiedName
	 *            a qualified name
	 * @return a string array with two components: the package prefix and the
	 *         simple name
	 */
	public static String[] splitQualifiedName(String qualifiedName) {
		int lastIndex = qualifiedName.lastIndexOf('.');
		String[] components = new String[2];
		if (lastIndex == -1) {
			components[0] = "";
			components[1] = qualifiedName;
		} else {
			components[0] = qualifiedName.substring(0, lastIndex);
			if ((components[0].length() >= 1)
					&& (components[0].charAt(0) == '.')) {
				components[0] = components[0].substring(1);
			}
			components[1] = qualifiedName.substring(lastIndex + 1);
		}
		return components;
	}

	@Override
	public RecordDomain createRecordDomain(String qualifiedName) {
		return this.createRecordDomain(qualifiedName, null);
	}

	@Override
	public RecordDomain createRecordDomain(String qualifiedName,
			Collection<RecordComponent> recordComponents) {
		if (this.finished) {
			throw new SchemaException("No changes to finished schema!");
		}
		String[] components = splitQualifiedName(qualifiedName);
		PackageImpl parent = (PackageImpl) this.createPackageWithParents(components[0]);
		String simpleName = components[1];
		RecordDomain rd = new RecordDomainImpl(simpleName, parent,
				recordComponents);
		return rd;
	}

	@Override
	public SetDomain createSetDomain(Domain baseDomain) {
		if (this.finished) {
			throw new SchemaException("No changes to finished schema!");
		}
		String qn = "Set<" + baseDomain.getQualifiedName() + ">";
		if (this.domains.containsKey(qn)) {
			return (SetDomain) this.domains.get(qn);
		}
		return new SetDomainImpl(this, baseDomain);
	}

	@Override
	public boolean equals(Object other) {
		if ((other == null) || !(other instanceof Schema)) {
			return false;
		}
		return this.qualifiedName.equals(((Schema) other).getQualifiedName());
	}

	@Override
	public int hashCode() {
		return this.qualifiedName.hashCode();
	}

	@SuppressWarnings("unchecked")
	@Override

	public <T extends AttributedElementClass<?, ?>> T getAttributedElementClass(
			String qualifiedName) {
		if (this.graphClass == null) {
			return null;
		} else if (this.graphClass.getQualifiedName().equals(qualifiedName)) {
			return (T) this.graphClass;
		} else {
			return (T) this.graphClass.getGraphElementClass(qualifiedName);
		}
	}

	@Override
	public List<CompositeDomain> getCompositeDomains() {
		ArrayList<CompositeDomain> topologicalOrderList = new ArrayList<CompositeDomain>();
		for (Domain dom : this.domainsDag.getNodesInTopologicalOrder()) {
			if (dom instanceof CompositeDomain) {
				topologicalOrderList.add((CompositeDomain) dom);
			}
		}

		return topologicalOrderList;
	}

	private Method getCreateMethod(String className, String graphClassName,
			Class<?>[] signature, ImplementationType implementationType) {
		Class<? extends Graph> schemaClass = null;
		AttributedElementClass<?, ?> aec = null;
		try {
			schemaClass = this.getGraphClassImpl(implementationType);
			if (className.equals(graphClassName)) {
				if (implementationType != ImplementationType.GENERIC) {
					// Graph create method is in the SchemaImpl specialization
					return this.getClass().getMethod("create" + graphClassName,
							signature);
				} else {
					// generic case
					return schemaClass.getMethod("createGraph", signature);
				}
			} else {
				// Element create methods are in the GraphImpl specialization
				aec = this.graphClass.getVertexClass(className);
				if (aec == null) {
					aec = this.graphClass.getEdgeClass(className);
					if (aec == null) {
						throw new SchemaClassAccessException("class "
								+ className + " does not exist in schema");
					}
				}
				if (implementationType != ImplementationType.GENERIC) {
					return schemaClass.getMethod(
							"create"
									+ CodeGenerator.camelCase(aec
											.getUniqueName()), signature);
				} else {
					if (signature[0].equals(VertexClass.class)) {
						return schemaClass.getMethod("createVertex", signature);
					} else {
						return schemaClass.getMethod("createEdge", signature);
					}
				}

			}
		} catch (SecurityException e) {
			throw new SchemaClassAccessException(
					"can't find create method in '" + schemaClass.getName()
					+ "' for '" + aec.getUniqueName() + "'", e);
		} catch (NoSuchMethodException e) {
			throw new SchemaClassAccessException(
					"can't find create method in '" + schemaClass.getName()
					+ "' for '" + aec.getUniqueName() + "'", e);
		}
	}

	@Override
	public EdgeClass getDefaultEdgeClass() {
		return this.defaultEdgeClass;
	}

	@Override
	public GraphClass getDefaultGraphClass() {
		return this.defaultGraphClass;
	}

	@Override
	public Package getDefaultPackage() {
		return this.defaultPackage;
	}

	@Override
	public VertexClass getDefaultVertexClass() {
		return this.defaultVertexClass;
	}

	@Override
	public Domain getDomain(String domainName) {
		return this.domains.get(domainName);
	}

	@Override
	public Map<String, Domain> getDomains() {
		return this.domains;
	}

	protected DirectedAcyclicGraph<Domain> getDomainsDag() {
		return this.domainsDag;
	}

	@Override

	public List<EdgeClass> getEdgeClasses() {
		List<EdgeClass> ec_top = new ArrayList<EdgeClass>();
		ec_top.add(this.defaultEdgeClass);
		for (EdgeClass ec : this.graphClass.getEdgeClasses()) {
			ec_top.add(ec);
		}
		return ec_top;
	}

	@Override
	public Method getEdgeCreateMethod(String edgeClassName,
			ImplementationType implementationType) {
		// Edge class create method cannot be found directly by its signature
		// because the vertex parameters are subclassed to match the to- and
		// from-class. Those subclasses are unknown in this method. Therefore,
		// we look for a method with correct name and 3 parameters
		// (int, vertex, Vertex).
		AttributedElementClass<?, ?> aec = this.getAttributedElementClass(edgeClassName);
		if ((aec == null) || !(aec instanceof EdgeClass)) {
			throw new SchemaException(
					"There's no EdgeClass with qualified name " + edgeClassName
					+ "!");
		}
		EdgeClass ec = (EdgeClass) aec;
		String methodName = "create"
				+ CodeGenerator.camelCase(ec.getUniqueName());
		Class<?> schemaClass = this.getGraphClassImpl(implementationType);
		if (implementationType != ImplementationType.GENERIC) {
			for (Method m : schemaClass.getMethods()) {
				if (m.getName().equals(methodName)
						&& (m.getParameterTypes().length == 3)) {
					return m;
				}
			}
		} else {
			try {
				return schemaClass.getMethod("createEdge",
						new Class[] { EdgeClass.class, int.class, Vertex.class,
						Vertex.class });
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}
		throw new SchemaClassAccessException("can't find create method '"
				+ methodName + "' in '" + schemaClass.getName() + "' for '"
				+ ec.getUniqueName() + "'");
	}

	@Override
	public List<EnumDomain> getEnumDomains() {
		ArrayList<EnumDomain> enumList = new ArrayList<EnumDomain>();

		for (Domain dl : this.domains.values()) {
			if (dl instanceof EnumDomain) {
				enumList.add((EnumDomain) dl);
			}
		}

		return enumList;
	}

	@Override
	public BooleanDomain getBooleanDomain() {
		return this.booleanDomain;
	}

	@Override
	public DoubleDomain getDoubleDomain() {
		return this.doubleDomain;
	}

	@Override
	public IntegerDomain getIntegerDomain() {
		return this.integerDomain;
	}

	@Override
	public LongDomain getLongDomain() {
		return this.longDomain;
	}

	@Override
	public StringDomain getStringDomain() {
		return this.stringDomain;
	}

	@Override
	public GraphClass getGraphClass() {
		return this.graphClass;
	}

	/**
	 *
	 * @param implementationType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Class<? extends Graph> getGraphClassImpl(
			ImplementationType implementationType) {
		String implClassName = this.packagePrefix + ".";
		// determine package
		switch (implementationType) {
		case STANDARD:
			implClassName += IMPLSTDPACKAGENAME;
			break;
		case TRANSACTION:
			implClassName += IMPLTRANSPACKAGENAME;
			break;
		case DATABASE:
			implClassName += IMPLDATABASEPACKAGENAME;
		case GENERIC:
			implClassName = "de.uni_koblenz.jgralab.impl.generic";
			break;
		default:
			throw new SchemaException("Implementation type "
					+ implementationType + " not supported yet.");
		}

		Class<? extends Graph> schemaClass;
		if (implementationType != ImplementationType.GENERIC) {
			implClassName = implClassName + "." + this.graphClass.getSimpleName()
					+ "Impl";
			try {
				schemaClass = (Class<? extends Graph>) Class.forName(
						implClassName, true,
						SchemaClassManager.instance(this.qualifiedName));
			} catch (ClassNotFoundException e) {
				throw new SchemaClassAccessException(
						"can't load implementation class '" + implClassName
						+ "'", e);
			}
			return schemaClass;
		} else {
			implClassName += "." + "GenericGraphImpl";
			try {
				return (Class<? extends Graph>) Class.forName(implClassName);
			} catch (ClassNotFoundException e) {
				throw new SchemaClassAccessException(
						"can't load implementation class '" + implClassName
						+ "'", e);
			}
		}
	}

	@Override
	public String getName() {
		return this.name;
	}

	/**
	 * only used internally
	 *
	 * @return number of graphelementclasses contained in graphclass
	 */
	private int getNumberOfElements() {
		return this.graphClass.getGraphElementClasses().size() + 1;
	}

	@Override
	public Package getPackage(String packageName) {
		return this.packages.get(packageName);
	}

	@Override
	public String getPackagePrefix() {
		return this.packagePrefix;
	}

	@Override
	public Map<String, Package> getPackages() {
		return this.packages;
	}

	@Override
	public String getQualifiedName() {
		return this.qualifiedName;
	}

	@Override
	public List<RecordDomain> getRecordDomains() {
		ArrayList<RecordDomain> recordList = new ArrayList<RecordDomain>();

		for (Domain dl : this.domains.values()) {
			if (dl instanceof RecordDomain) {
				recordList.add((RecordDomain) dl);
			}
		}

		return recordList;
	}

	@Override
	public List<VertexClass> getVertexClasses() {
		List<VertexClass> vc_top = new ArrayList<VertexClass>();
		vc_top.add(this.defaultVertexClass);
		for (VertexClass vc : this.graphClass.getVertexClasses()) {
			vc_top.add(vc);
		}
		return vc_top;
	}

	@Override
	public Method getVertexCreateMethod(String vertexClassName,
			ImplementationType implementationType) {
		if (implementationType != ImplementationType.GENERIC) {
			return this.getCreateMethod(vertexClassName, this.graphClass.getSimpleName(),
					VERTEX_CLASS_CREATE_SIGNATURE, implementationType);
		} else {
			return this.getCreateMethod(vertexClassName, this.graphClass.getSimpleName(),
					new Class[] { VertexClass.class, int.class },
					implementationType);
		}
	}

	@Override
	public boolean isValidEnumConstant(String name) {
		if (name.isEmpty()) {
			return false;
		}
		if (!this.allowLowercaseEnumConstants && !name.equals(name.toUpperCase())) {
			return false;
		}
		if (RESERVED_JAVA_WORDS.contains(name)) {
			return false;
		}
		if (!Character.isJavaIdentifierStart(name.charAt(0))) {
			return false;
		}
		for (char c : name.toCharArray()) {
			if (!Character.isJavaIdentifierPart(c)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean knows(String qn) {
		return (this.namedElements.containsKey(qn) || this.getQualifiedName().equals(qn));
	}

	@Override
	public void setAllowLowercaseEnumConstants(
			boolean allowLowercaseEnumConstants) {
		this.allowLowercaseEnumConstants = allowLowercaseEnumConstants;
	}


	void setGraphClass(GraphClass gc) {
		if (this.graphClass != null) {
			throw new SchemaException("There already is a GraphClass named: "
					+ this.graphClass.getQualifiedName() + "in the Schema!");
		}
		this.graphClass = gc;
	}

	/**
	 * @return the textual representation of the schema with all graph classes,
	 *         their edge and vertex classes, all attributes and the whole
	 *         hierarchy of those classes
	 */
	public String getDescriptionString() {
		return "GraphClass of schema '" + this.qualifiedName + "':\n\n\n"
				+ ((GraphClassImpl) this.graphClass).getDescriptionString();
	}

	@Override
	public String toString() {
		return this.getQualifiedName();
	}

	@Override
	public String toTGString() {
		String schemaDefinition = null;
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(byteOut);
		try {
			GraphIO.saveSchemaToStream(this, out);
			out.close();
			byteOut.close();
			schemaDefinition = new String(byteOut.toByteArray());
		} catch (GraphIOException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return schemaDefinition;
	}

	@Override
	public String getFileName() {
		return this.qualifiedName.replace('.', File.separatorChar);
	}

	@Override
	public String getPathName() {
		return this.packagePrefix.replace('.', File.separatorChar);
	}

	@Override
	public GraphFactory createDefaultGraphFactory(
			ImplementationType implementationType) {
		if (implementationType != ImplementationType.GENERIC) {
			throw new IllegalArgumentException(
					"Base implementation can't create a GraphFactory for implementation type "
							+ implementationType
							+ ". Only GENERIC is supported.");
		}
		return new GenericGraphFactoryImpl(this);
	}

	@Override
	public Graph createGraph(ImplementationType implementationType) {
		return this.createGraph(implementationType, null, 100, 100);
	}

	@Override
	public Graph createGraph(ImplementationType implementationType, String id,
			int vMax, int eMax) {
		GraphFactory factory = this.createDefaultGraphFactory(implementationType);
		return factory.createGraph(this.getGraphClass(), id, vMax, eMax);
	}

	/**
	 * @return whether the schema is finished
	 */
	@Override
	public boolean isFinished() {
		return this.finished;
	}

	/**
	 * Signals that the schema is finished. No more changes are allowed. To open
	 * the change mode call reopen
	 */
	@Override
	public void finish() {
		if (this.finished) {
			return;
		}
		((GraphClassImpl) this.graphClass).finish();
		this.finished = true;

	}

	/**
	 * Reopens the schema to allow changes. To finish the schema again, call
	 * finish
	 */
	@Override
	public void reopen() {
		if (!this.finished) {
			return;
		}
		((GraphClassImpl) this.graphClass).finish();
		this.finished = false;
	}

	@Override
	public void save(String filename) throws GraphIOException {
		GraphIO.saveSchemaToFile(this, filename);
	}

	@Override
	public void save(DataOutputStream out) throws GraphIOException {
		GraphIO.saveSchemaToStream(this, out);
	}

}
