package rusch.megan5server;


/**A single parameter that comes with a http request
 * 
 * 
 * 
 * @author Hans-Joachim Ruscheweyh
 * 10:20:47 AM - Oct 29, 2014
 *
 */
public class Parameter {
	
	public String parameterName;
	public boolean required;
	public String defaultValue;
	
	
	
	
	public Parameter(String parameterName, boolean required,
			String defaultValue) {
		super();
		this.parameterName = parameterName;
		this.required = required;
		this.defaultValue = defaultValue;
	}
	public String getParameterName() {
		return parameterName;
	}
	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}
	public boolean isRequired() {
		return required;
	}
	public void setRequired(boolean required) {
		this.required = required;
	}
	public String getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	

}
