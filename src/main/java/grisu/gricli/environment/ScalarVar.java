package grisu.gricli.environment;

import grisu.gricli.GricliSetValueException;

public abstract class ScalarVar<T> extends GricliVar<T> {
	
	private boolean nullable;

	public ScalarVar(String name, T value, boolean nullable){
		super(name);
		this.nullable = nullable;
		try {
			set(value);
		} catch (GricliSetValueException ex){
			throw new NullPointerException("variable cannot be null");
		}
	}
	

	public ScalarVar(String name, T value) {
		this(name, value, false);
	}

	@Override
	protected T fromStrings(String[] args) throws GricliSetValueException {
		if (args == null || args.length != 1){
			throw new GricliSetValueException(getName(), "", "accepts only single argument");
		} else {
			return fromString(args[0]);
		}
	}
	
	protected abstract T fromString(String arg) throws GricliSetValueException;
	
	@Override
	public void set(T value) throws GricliSetValueException {
		if (!this.nullable && (value == null)){
			throw new GricliSetValueException(getName(), null, "variable cannot be unset");
		}
		super.set(value);
	}
	

}