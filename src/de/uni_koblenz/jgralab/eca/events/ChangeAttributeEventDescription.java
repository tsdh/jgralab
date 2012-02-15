/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2012 Institute for Software Technology
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
package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.eca.ECARule;

public class ChangeAttributeEventDescription extends EventDescription {

	/**
	 * Name of the Attribute, this EventDescription monitors changes
	 */
	private String concernedAttribute;

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	/**
	 * Creates a ChangeAttributeEventDescription with the given parameters
	 * 
	 * @param time
	 *            the EventTime, BEFORE or AFTER
	 * @param type
	 *            the Class of elements, this EventDescription monitors for
	 *            Attribute changes
	 * @param attributeName
	 *            the name of the observed Attribute
	 */
	public ChangeAttributeEventDescription(EventTime time,
			Class<? extends AttributedElement> type, String attributeName) {
		super(time, type);
		this.concernedAttribute = attributeName;
	}

	/**
	 * Creates a ChangeAttributeEventDescription with the given parameters
	 * 
	 * @param time
	 *            the EventTime, BEFORE or AFTER
	 * @param contextExpr
	 *            the GReQuL-Expression that represents the context of this
	 *            EventDescription
	 * @param attributeName
	 *            the name of the observed Attribute
	 */
	public ChangeAttributeEventDescription(EventTime time, String contextExpr,
			String attributeName) {
		super(time, contextExpr);
		this.concernedAttribute = attributeName;
	}

	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	/**
	 * Compares the Attribute names and context or type, triggers the rules if
	 * the EventDescription matches the Event
	 * 
	 * @param element
	 *            the AttributedElement an Attribute will change or changed for
	 * @param attributeName
	 *            the name of the changing or changed Attribute
	 */
	public void fire(AttributedElement element, String attributeName,
			Object oldValue, Object newValue) {
		if (concernedAttribute.equals(attributeName)) {
			if (super.checkContext(element)) {
				int nested = this.getActiveECARules().get(0)
						.getECARuleManager().getNestedTriggerCalls();
				Graph graph = this.getActiveECARules().get(0)
						.getECARuleManager().getGraph();
				for (ECARule rule : activeRules) {
					rule.trigger(new ChangeAttributeEvent(nested, this
							.getTime(), graph, element, attributeName,
							oldValue, newValue));
				}
			}
		}
	}

	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	/**
	 * @return the name of the monitored Attribute
	 */
	public String getConcernedAttribute() {
		return concernedAttribute;
	}

}
