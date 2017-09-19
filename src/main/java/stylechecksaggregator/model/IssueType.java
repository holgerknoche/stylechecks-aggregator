package stylechecksaggregator.model;

/**
 * This enumeration contains all known issue types, i.e. tools that are supported.
 * 
 * @author Holger Knoche
 */
public enum IssueType {
	/**
	 * Represents CheckStyle.
	 */
	CHECKSTYLE,
	/**
	 * Represents FindBugs.
	 */
	FINDBUGS,
	/**
	 * Represents PMD.
	 */
	PMD;
}
