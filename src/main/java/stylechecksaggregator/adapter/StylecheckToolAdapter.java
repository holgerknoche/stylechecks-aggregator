package stylechecksaggregator.adapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.google.common.collect.ImmutableList;

import stylechecksaggregator.model.Issue;

/**
 * Abstract superclass for all stylecheck tool adapters.
 * 
 * @author Holger Knoche
 */
public abstract class StylecheckToolAdapter {
	
	/**
	 * Separator used in the file name strings. Note that this is a regular expression.
	 */
	private static final String FILE_NAME_SEPARATOR = ";";
	
	/**
	 * Contains the relative file names (to the given root path) to be analyzed by this adapter.
	 */
	protected final List<String> relativeFileNames;
	
	/**
	 * Creates a new adapter using the given data.
	 * @param relativeFileNames The relative file names to be analyzed by this adapter.
	 */
	protected StylecheckToolAdapter(final List<String> relativeFileNames) {
		this.relativeFileNames = ImmutableList.copyOf(relativeFileNames);
	}
	
	/**
	 * Processes the files in the given root path and returns the found issues.
	 * @param rootPath The root path in which the files are stored
	 * @return The issues from the files in the root path
	 * @throws IOException If an I/O error occurs
	 */
	public List<Issue> processPath(final String rootPath) throws IOException {
		final List<Issue> allIssues = new ArrayList<>();
		
		for(final String relativeFileName : this.relativeFileNames) {
			final String fileName = rootPath + File.separatorChar + relativeFileName;
			
			final List<Issue> issues = this.processFile(fileName);
			allIssues.addAll(issues);
		}
		
		return allIssues;
	}
	
	protected abstract List<Issue> processFile(String fileName) throws IOException;

	/**
	 * Extracts file names from the property with the given name from the given properties.
	 * @param propertyName The name of the property containing the file name string
	 * @param properties The properties containing the desired property
	 * @return The (possibly empty) set of extracted file names
	 */
	protected static List<String> extractFileNames(final String propertyName, final Properties properties) {
		final String fileNameString = properties.getProperty(propertyName, null);
		
		// Return an empty set if no such property exists
		if(fileNameString == null) {
			return ImmutableList.of();
		}
		
		final String[] parts = fileNameString.split(FILE_NAME_SEPARATOR);		
		return ImmutableList.copyOf(parts);
	}
	
}
