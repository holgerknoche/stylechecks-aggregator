package stylechecksaggregator.adapter;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.xml.xpath.XPathException;

import org.w3c.dom.Document;

import stylechecksaggregator.model.Issue;
import stylechecksaggregator.util.XMLUtil;

/**
 * Abstract adapter for tools with XML output.
 * 
 * @author Holger Knoche
 */
public abstract class XMLBasedToolAdapter extends StylecheckToolAdapter {
	
	/**
	 * Creates a new adapter using the given data.
	 * @param relativeFileNames The relative file names to be analyzed by this adapter.
	 */
	protected XMLBasedToolAdapter(final List<String> relativeFileNames) {
		super(relativeFileNames);
	}

	@Override
	protected final List<Issue> processFile(final String fileName) throws IOException {
		final File file = new File(fileName);
		
		// Treat missing files as empty 
		if (!file.exists()) {
			return Collections.emptyList();
		}
		
		// Parse the XML file and pass the result to the appropriate method for analysis
		final Document document = XMLUtil.parseXMLFile(file);
		
		try {
			return this.processDocument(document);
		} catch (final XPathException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Extracts issues from the given document.
	 * @param document The document to process
	 * @return The issues found in the document
	 * @throws XPathException If an exception during XPath processing occurs
	 */
	protected abstract List<Issue> processDocument(Document document) throws XPathException;
	
}
