package stylechecksaggregator.model;

/**
 * Represents an issue located by one of the style checkers.
 * 
 * @author Holger Knoche
 */
public class Issue {
	
	/**
	 * The name of the source file in which the issue was detected.
	 */
	public final String fileName;
	
	/**
	 * The line number in which the issue was detected. 
	 */
	public final int lineNumber;
	
	/**
	 * The severity of the issue.
	 */
	public final IssueSeverity severity;
	
	/**
	 * The type of the issue (i.e. the tool which found the issue).
	 */
	public final IssueType type;
	
	/**
	 * The message associated with this issue.
	 */
	public final String message;
	
	/**
	 * Creates a new issue from the given data.
	 * @param fileName see {@link #fileName}
	 * @param lineNumber see {@link #lineNumber}
	 * @param severity see {@link #severity}
	 * @param type see {@link #type}
	 * @param message see {@link #message}
	 */
	public Issue(final String fileName, final int lineNumber, final IssueSeverity severity, final IssueType type, final String message) {
		this.fileName = fileName;
		this.lineNumber = lineNumber;
		this.severity = severity;
		this.type = type;
		this.message = message;
	}

	@Override
	public String toString() {
		return String.format("%s (%s) - %s, line %d: %s", this.severity, this.type, this.fileName, this.lineNumber, this.message);
	}
	
}
