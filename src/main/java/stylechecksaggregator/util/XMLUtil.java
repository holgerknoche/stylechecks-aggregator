package stylechecksaggregator.util;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Utility functions for working with XML files.
 * 
 * @author Holger Knoche
 */
public class XMLUtil {

	/**
	 * Parses the given XML file and returns the contained DOM tree.
	 * @param fileName The file name to parse
	 * @return The DOM tree
	 * @throws IOException If an I/O error occurs
	 */
	public static Document parseXMLFile(final String fileName) throws IOException {
		try {			
			final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
			final Document document = documentBuilder.parse(new File(fileName));
			
			return document;
		} catch (ParserConfigurationException | SAXException e) {
			throw new RuntimeException(e);
		}
	}

}
