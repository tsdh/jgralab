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

package de.uni_koblenz.jgralab.schema;

import java.util.Collection;
import java.util.Set;
import java.util.SortedSet;

import de.uni_koblenz.jgralab.Attribute;
import de.uni_koblenz.jgralab.AttributedElement;

/**
 * This is the base class of any <code>GraphClass</code>/
 * <code>VertexClass</code>/<code>EdgeClass</code>/<code>AggregationClass</code>
 * /<code>CompositionClass</code>.
 * 
 * <p>
 * In the following, <code>attrElement</code>, and <code>attrElement'</code>, will
 * represent the states of the given <code>AttributedElementClass</code> before,
 * respectively after, any operation.
 * </p>
 * 
 * @author ist@uni-koblenz.de
 */
public interface AttributedElementClass extends NamedElement,
		Comparable<AttributedElementClass> {
	/**
	 * Sets the package of this Domain to <code>p</code>.
	 * 
	 * @param p
	 *            the package of this Domain.
	 */
	public void setPackage(Package p);

	/**
	 * @return the package of this Domain, may be <code>null</code> for domains
	 *         not associated to a package.
	 */
	public Package getPackage();

	/**
	 * @param anAttributedElementClass
	 *            the class to search for
	 * @return true, if anAttributedElementClass is a direct or indirect
	 *         superclass of this class
	 */
	public boolean isSuperClassOf(
			AttributedElementClass anAttributedElementClass);

	/**
	 * @param anAttributedElementClass
	 *            the class to search for
	 * @return true, if anAttributedElementClass is a direct superclass of this
	 *         class
	 */
	public boolean isDirectSuperClassOf(
			AttributedElementClass anAttributedElementClass);

	/**
	 * @param anAttributedElementClass
	 *            the class to search for
	 * @return true, if anAttributedElementClass is a direct subclass of this
	 *         class
	 */
	public boolean isDirectSubClassOf(
			AttributedElementClass anAttributedElementClass);

	/**
	 * @param anAttributedElementClass
	 *            the class to search for
	 * @return true, if anAttributedElementClass is a direct or indirect
	 *         subclass of this class
	 */
	public boolean isSubClassOf(AttributedElementClass anAttributedElementClass);

	/**
	 * Tests if the given Class is a subclass of this class or equal to this
	 * class
	 * 
	 * @param anAttributedElementClass
	 *            the class to test
	 * @return true iff the given AttributedElementClass is a subclass of this
	 *         class or is this class itself
	 */
	public boolean isSuperClassOfOrEquals(
			AttributedElementClass anAttributedElementClass);

	/**
	 * @return the set of direct superclasses this class is subclass of
	 */
	public Set<AttributedElementClass> getDirectSuperClasses();

	/**
	 * @return the set of direct and indirect superclasses
	 */
	public Set<AttributedElementClass> getAllSuperClasses();

	/**
	 * @return the unique name of the element in the package without the fully
	 *         qualified package name
	 */
	public String getSimpleName();

	/**
	 * @return the unique name of the element in the schema, if there is only
	 *         one class in the schema with this short name, the short name is
	 *         returned. Otherwise, the fully qualified package name is returned
	 *         in a camel-cased underscored manner
	 */
	public String getUniqueName();

	/**
	 * @return the fully qualified name of this element in the schema. This is
	 *         the fully qualified name of the package the element is located in
	 *         together with the short name of the element
	 */
	public String getQualifiedName();

	/**
	 * Adds a new <code>Attribute</code> by the given <code>name</code> and
	 * <code>domain</code> to this <code>AttributedElement</code>.
	 * 
	 * 
	 * <p>
	 * <b>Pattern:</b> <code>attrElement.addAttribute(name, domain);</code>
	 * </p>
	 * 
	 * <p>
	 * <b>Preconditions:</b>
	 * <ul>
	 * <li>The <code>name</code> must not contain
	 * {@link de.uni_koblenz.jgralab.schema.Schema#reservedTGWords reserved TG
	 * words} and/or
	 * {@link de.uni_koblenz.jgralab.schema.Schema#reservedJavaWords reserved
	 * Java words}.</li>
	 * <li>The <code>name</code> must be distinct from all other
	 * <code>Attribute</code> names in <code>attrElement</code>, and in
	 * <code>attrElement´s</code> direct and indirect superclasses.</li>
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * <b>Postcondition:</b> In addition to the <code>Attribute(s)</code>
	 * already contained in <code>attrElement</code>, <code>attrElement'</code> holds
	 * the new <code>Attribute</code> with the given <code>name</code> and
	 * <code>domain</code>.
	 * </p>
	 * 
	 * @param name
	 *            a unique <code>name</code> in the list of
	 *            <code>Attributes</code> of <code>attrElement</code>
	 * @param domain
	 *            the <code>Domain</code> of the new <code>Attribute</code>
	 * 
	 * @throws SchemaException
	 *             if:
	 *             <ul>
	 *             <li>the <code>name</code> contains reserved TG/Java words</li>
	 *             <li>there is an <code>Attribute</code> with the same
	 *             <code>name</code> in <code>attrElement</code></li>
	 *             </ul>
	 */
	public void addAttribute(String name, Domain domain);

	/**
	 * Adds a new <code>Attribute</code> to this <code>AttributedElement</code>.
	 * 
	 * <p>
	 * <b>Pattern:</b> <code>attrElement.addAttribute(anAttribute);</code>
	 * </p>
	 * 
	 * <p>
	 * <b>Precondition:</b> The <code>name</code> of <code>anAttribute</code>
	 * must be distinct from all other <code>Attribute</code> names in
	 * <code>attrElement</code>, and in <code>attrElement´s</code> direct and
	 * indirect superclasses.
	 * </p>
	 * 
	 * <p>
	 * <b>Postcondition:</b> In addition to the <code>Attribute(s)</code>
	 * already contained in <code>attrElement</code>, <code>attrElement'</code> holds
	 * the new <code>Attribute</code>.
	 * </p>
	 * 
	 * @param anAttribute
	 *            the new <code>Attribute</code> to be added to
	 *            <code>attrElement</code>
	 * 
	 * @throws SchemaException
	 *             if there is an <code>Attribute</code> with the same
	 *             <code>name</code> in <code>attrElement</code>
	 */
	public void addAttribute(Attribute anAttribute);

	/**
	 * Adds multiple <code>Attributes</code> to this
	 * <code>AttributedElement</code>.
	 * 
	 * <p>
	 * <b>Pattern:</b> <code>attrElement.addAttributes(attrs);</code>
	 * </p>
	 * 
	 * <p>
	 * <b>Preconditions:</b>
	 * <ul>
	 * <li>Each <code>Attribute´s</code> <code>name</code> in <code>attrs</code>
	 * must be distinct from the <code>name</code> of every
	 * <code>Attribute</code> held by <code>attrElement</code>, and by
	 * <code>attrElement´s</code> direct and indirect superclasses.</li>
	 * <li>Different <code>Attributes</code> in <code>attrs</code> must have
	 * distinct <code>names</code>.</li>
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * <b>Postcondition:</b> In addition to the <code>Attributes</code> already
	 * contained in <code>attrElement</code>, <code>attrElement'</code> holds as many
	 * new <code>Attributes</code> as were in <code>attrs</code>.
	 * </p>
	 * 
	 * @param attrs
	 *            the list of new <code>Attributes</code> to be appended to
	 *            <code>attrElement</code>
	 * 
	 * @throws SchemaException
	 *             if:
	 *             <ul>
	 *             <li><code>attrElement</code> already contains an
	 *             <code>Attribute</code> bearing the same <code>name</code></li>
	 *             <li>different <code>Attributes</code> in <code>attrs</code>
	 *             are named equally</li>
	 *             In either case, <b>none</b> of the <code>Attributes</code>
	 *             from <code>attrs</code> will be added to
	 *             <code>attrElement</code>.
	 */
	public void addAttributes(Collection<Attribute> attrs);

	/**
	 * @param name
	 *            the name of the attribute
	 * @return the attribute with the specified name
	 */
	public Attribute getAttribute(String name);

	/**
	 * @param name
	 *            the name of the attribute
	 * @return the attribute with the specified name
	 */
	public Attribute getOwnAttribute(String name);

	/**
	 * @return the list of attributes of this element without inherited
	 *         attributes
	 */
	public SortedSet<Attribute> getOwnAttributeList();

	/**
	 * @return the list of attributes of this element and all inherited
	 *         attributes
	 */
	public SortedSet<Attribute> getAttributeList();

	/**
	 * Checks if this element has an attribute with the given <code>name</code>.
	 * 
	 * <p>
	 * <b>Pattern:</b> <code>e.containsAttribute(name);</code>
	 * </p>
	 * 
	 * <p>
	 * <b>Precondition:</b> The name of the attribute must not be empty.
	 * </p>
	 * 
	 * <p>
	 * <b>Postconditions:</b> none
	 * </p>
	 * 
	 * @param name
	 *            the name of the attribute to search for
	 * 
	 * @return <code>true</code>, if the element or its super-classes contains
	 *         an attribute with the specified name
	 */
	public boolean containsAttribute(String name);

	/**
	 * @return the number of attributes this element holds including inherited
	 *         attributes
	 */
	public int getAttributeCount();

	/**
	 * @return the number of attributes this element holds without inherited
	 *         attributes
	 */
	public int getOwnAttributeCount();

	/**
	 * this method has to be implemented by each specialised class, it has to
	 * return a textual representation of that class
	 */
	public abstract String toString();

	/**
	 * @return true, if the element may not have any instances
	 */
	public boolean isAbstract();

	/**
	 * defines if the element may or may not have any instances
	 * 
	 * @param isAbstract
	 *            true or false
	 */
	public void setAbstract(boolean isAbstract);

	/**
	 * @return all direct subclasses of this class
	 */
	public Set<AttributedElementClass> getDirectSubClasses();

	/**
	 * Lists all direct and indirect subclasses of this element.
	 * 
	 * <p>
	 * <b>Pattern:</b> <code>subClasses = e.getAllSubClasses();</code>
	 * </p>
	 * 
	 * <p>
	 * <b>Preconditions:</b> none
	 * </p>
	 * 
	 * <p>
	 * <b>Postconditions:</b>
	 * <ul>
	 * <li><code>subClasses</code> holds all direct and indirect subclasses of
	 * this element</li>
	 * <li>The number of classes in <code>subClasses</code> equals the number of
	 * all direct and indirect subclasses</li>
	 * <li></li>
	 * </ul>
	 * </p>
	 * 
	 * @return all direct and indirect subclasses of this class
	 */
	public Set<AttributedElementClass> getAllSubClasses();

	/**
	 * @return true, if the element has own or inherited attributes, false
	 *         otherwise
	 */
	public boolean hasAttributes();

	/**
	 * Checks if this AttributedElementClass has own attributes
	 * 
	 * @return true if this AttributedElementClass contains at least one
	 *         non-inherited attributed, false otherwise
	 */
	public boolean hasOwnAttributes();

	/**
	 * @return true, if this AttributedElementClass is only for internal use
	 */
	public boolean isInternal();

	/**
	 * sets the <code>internal</code>-flag of this AttributedElementClass, if
	 * this is set, the AttributedElementClass is only for internal use (for
	 * instance, the AttributedElementClasses 'Graph', 'Vertex', 'Edge',
	 * 'Composition' and 'Aggregation' are such AttributedElementClasses only
	 * for internal use
	 * 
	 * @param internal
	 */
	public void setInternal(boolean internal);

	/**
	 * @return the schema this AttributedElementClass belongs to
	 */
	public Schema getSchema();

	/**
	 * @return the M1 interface class for this AttributedElementClass if
	 *         reflection exceptions occures
	 */
	public Class<? extends AttributedElement> getM1Class();

	/**
	 * @return the M1 implementation class for this AttributedElementClass if
	 *         this AttributedElementClass is abstract, or upon reflection
	 *         exceptions
	 */
	public Class<? extends AttributedElement> getM1ImplementationClass();

	/**
	 * Returns the least common superclass of this class and the given class
	 * <code>other</code>
	 * 
	 * @return the least common superclass
	 */
	public AttributedElementClass getLeastCommonSuperclass(
			AttributedElementClass other);

	/**
	 * Returns the least common superclass of this class and the classes in the
	 * set <code>other</code>
	 * 
	 * @return the least common superclass
	 */
	public AttributedElementClass getLeastCommonSuperclass(
			Set<? extends AttributedElementClass> other);
}
