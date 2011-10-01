package de.uni_koblenz.jgralab.greql2.funlib.misc;

import de.uni_koblenz.jgralab.greql2.funlib.AcceptsUndefinedArguments;
import de.uni_koblenz.jgralab.greql2.funlib.Function;
import de.uni_koblenz.jgralab.greql2.types.Undefined;

@AcceptsUndefinedArguments
public class IsDefined extends Function {

	public IsDefined() {
		super("Returns true if $val$ is defined.", Category.DEBUGGING);
	}

	public Object evaluate(Object val) {
		return !(val instanceof Undefined);
	}
}
