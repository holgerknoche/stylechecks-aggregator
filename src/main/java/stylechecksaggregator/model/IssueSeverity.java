package stylechecksaggregator.model;

/**
 * This enumeration contains all known issue severities.
 * 
 * @author Holger Knoche
 */
public enum IssueSeverity {
	/**
	 * Denotes a severity that has not been mapped to the unified model.
	 */
	UNCLASSIFIED,
	/**
	 * Denotes an "info"-level severity, i.e. an issue that could be fixed.
	 */
	INFO,
	/**
	 * Denotes a "warning"-level severity, i.e. an issue that should be fixed.
	 */
	WARNING,
	/**
	 * Denotes an "error"-level severity, i.e. an issue that must be fixed.
	 */
	ERROR;
}
