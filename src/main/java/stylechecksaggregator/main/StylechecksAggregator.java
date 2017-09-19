package stylechecksaggregator.main;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import stylechecksaggregator.adapter.CheckstyleAdapter;
import stylechecksaggregator.adapter.StylecheckToolAdapter;
import stylechecksaggregator.model.Issue;
import stylechecksaggregator.model.IssueSeverity;

public class StylechecksAggregator {	
	
	/**
	 * Contains constructors for all registered tool adapters.
	 */
	private static final List<Function<Properties, StylecheckToolAdapter>> KNOWN_ADAPTER_CONSTRUCTORS = ImmutableList.of(CheckstyleAdapter::new);
	
	private static final Class<?> THIS_CLASS = StylechecksAggregator.class;
	
	public static void main(final String[] arguments) throws IOException {		
		final StylechecksAggregatorParams parameters;
		
		// Parse the command line parameters
		try {
			parameters = parseParameters(arguments);
		} catch (final IllegalArgumentException e) {
			System.err.println(e.getMessage());
			printUsage();
			return;
		}		
	
		// Initialize the adapters
		final List<StylecheckToolAdapter> adapters = initializeAdapters(parameters);
		
		// Aggregate the issues using the adapters
		final List<Issue> issues = aggregateIssues(parameters, adapters);

		// Filter and output the issues
		outputIssues(issues, IssueSeverity.ERROR);
	}

	private static List<StylecheckToolAdapter> initializeAdapters(final StylechecksAggregatorParams parameters) throws IOException {
		final boolean fromClasspath = parameters.fromClasspath;
		final String configFileName = parameters.configFileName;
				
		final InputStream inputStream;
		
		// Create appropriate input stream
		if(fromClasspath) {
			final ClassLoader classLoader = THIS_CLASS.getClassLoader();
			inputStream = classLoader.getResourceAsStream(configFileName);
		} else {
			inputStream = new FileInputStream(configFileName);
		}
		
		// Load properties from the given stream
		final Properties properties = new Properties();
		properties.load(inputStream);
		
		// Initialize all adapters with the loaded properties
		final List<Function<Properties, StylecheckToolAdapter>> constructors = KNOWN_ADAPTER_CONSTRUCTORS;
		final List<StylecheckToolAdapter> adapters = new ArrayList<>(constructors.size());
		
		for(final Function<Properties, StylecheckToolAdapter> constructor : constructors) {
			final StylecheckToolAdapter adapter = constructor.apply(properties);
			adapters.add(adapter);
		}
		
		return adapters;
	}
	
	private static List<Issue> aggregateIssues(final StylechecksAggregatorParams parameters, final List<StylecheckToolAdapter> adapters) throws IOException {
		final String rootPath = parameters.rootPath;
		final List<Issue> allIssues = new ArrayList<>();

		// Aggregate issues from all adapters
		for(final StylecheckToolAdapter adapter : adapters) {
			final List<Issue> issues = adapter.processPath(rootPath);
			allIssues.addAll(issues);
		} 
		
		return allIssues;
	}
	
	private static void outputIssues(final List<Issue> issues, final IssueSeverity minSeverity) {
		// Filter issues by severity
		final List<Issue> filteredIssues = 
				issues
				.stream()
				.filter((issue) -> (issue.severity.compareTo(minSeverity) >= 0))
				.collect(Collectors.toCollection(ArrayList::new));
		
		// Sort the filtered issues by file name and line number
		Collections.sort(filteredIssues, new SortIssueByFileNameAndLineNumberComparator());
		
		// Print the issues to stdout
		for(final Issue issue : filteredIssues) {
			System.out.println(issue);
		}
	}
	
	private static void printUsage() {
		final String className = THIS_CLASS.getName();
		
		System.err.println("Usage: " + className + " [--from-classpath] -config <config file name> <root path>");
	}
	
	private static StylechecksAggregatorParams parseParameters(final String[] arguments) {
		String configFileName = null;
		String rootPath = null;
		boolean fromClasspath = false;
		
		// Parse arguments
		int argumentIndex = 0;
		while(argumentIndex < arguments.length) {
			final String currentArgument = arguments[argumentIndex];
			
			switch(currentArgument) {
			case "--config":
				argumentIndex++;
				configFileName = arguments[argumentIndex];
				break;
			case "--from-classpath":
				fromClasspath = true;
				break;
			default:
				if(rootPath == null) {
					rootPath = currentArgument;
				} else {
					throw new IllegalArgumentException("Superfluous argument: " + currentArgument);
				}
				break;
			}
			
			argumentIndex++;
		}
		
		// Check if all required arguments were supplied
		if(configFileName == null) {
			throw new IllegalArgumentException("No configuration file was given.");
		}
		if(rootPath == null) {
			throw new IllegalArgumentException("No root path was given.");
		}
		
		return new StylechecksAggregatorParams(fromClasspath, configFileName, rootPath);
	}
	
	/**
	 * Class to hold the parameters for the stylechecks aggregator.
	 * 
	 * @author Holger Knoche
	 */
	private static class StylechecksAggregatorParams {
		
		public final boolean fromClasspath;
		
		public final String configFileName;
		
		public final String rootPath;

		public StylechecksAggregatorParams(final boolean fromClasspath, final String configFileName, final String rootPath) {
			this.fromClasspath = fromClasspath;
			this.configFileName = configFileName;
			this.rootPath = rootPath;
		}
				
	}
	
	/**
	 * Comparator to sort issues by file name and line number.
	 * 
	 * @author Holger Knoche
	 */
	private static class SortIssueByFileNameAndLineNumberComparator implements Comparator<Issue> {

		@Override
		public int compare(final Issue issue1, final Issue issue2) {
			int result;
			
			// Compare by file name first
			final String fileName1 = issue1.fileName;
			final String fileName2 = issue2.fileName;
			result = fileName1.compareTo(fileName2);
			 
			if(result != 0) {
				return result;
			}
			
			// If names are equal, compare by line number
			final int lineNumber1 = issue1.lineNumber;
			final int lineNumber2 = issue2.lineNumber;
			
			return Integer.compare(lineNumber1, lineNumber2);
		}
		
	}
	
}
