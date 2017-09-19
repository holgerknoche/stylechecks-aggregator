package stylechecksaggregator.adapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import stylechecksaggregator.model.Issue;
import stylechecksaggregator.model.IssueSeverity;
import stylechecksaggregator.model.IssueType;
import stylechecksaggregator.util.XMLUtil;

/**
 * Adapter for CheckStyle.
 * 
 * @author Holger Knoche
 */
public class CheckstyleAdapter extends StylecheckToolAdapter {
	
	private static final String PREFIX = "checkstyle.";
	
	/**
	 * Property for the file names.
	 */
	private static final String FILE_NAMES_PROPERTY = PREFIX + "fileNames";
	
	public CheckstyleAdapter(final Properties properties) {
		super(extractFileNames(FILE_NAMES_PROPERTY, properties));
	}

	@Override
	public List<Issue> processFile(final String fileName) throws IOException {
		final Document document = XMLUtil.parseXMLFile(fileName);
		
		try {
			return this.processCheckstyleDocument(document);
		} catch (final XPathExpressionException e) {
			throw new RuntimeException(e);
		}
	}
	
	private List<Issue> processCheckstyleDocument(final Document document) throws XPathExpressionException {
		final XPath xPath = XPathFactory.newInstance().newXPath();
		final NodeList fileNodes = (NodeList) xPath.evaluate("/checkstyle/file", document.getDocumentElement(), XPathConstants.NODESET);
	
		final List<Issue> issues = new ArrayList<>();
			
		for(int fileIndex = 0; fileIndex < fileNodes.getLength(); fileIndex++) {
			final Node fileNode = fileNodes.item(fileIndex);
			
			final NamedNodeMap fileAttributes = fileNode.getAttributes();
			final String fileName = fileAttributes.getNamedItem("name").getTextContent();
			
			final NodeList errorNodes = (NodeList) xPath.evaluate("error", fileNode, XPathConstants.NODESET);
			for(int errorIndex = 0; errorIndex < errorNodes.getLength(); errorIndex++) {
				 final Node errorNode = errorNodes.item(errorIndex);
				 
				 final NamedNodeMap errorAttributes = errorNode.getAttributes();
				 final int lineNumber = Integer.parseInt(errorAttributes.getNamedItem("line").getTextContent());
				 final String message = errorAttributes.getNamedItem("message").getTextContent();
				 final IssueSeverity severity = parseCheckstyleSeverity(errorAttributes.getNamedItem("severity").getTextContent());
				 
				 final Issue issue = new Issue(fileName, lineNumber, severity, IssueType.CHECKSTYLE, message);
				 issues.add(issue);
			}
		}
		
		return issues;
	}
	
	private static IssueSeverity parseCheckstyleSeverity(final String value) {
		switch(value) {
		case "info":
			return IssueSeverity.INFO;
		case "warning":
			return IssueSeverity.WARNING;
		case "error":
			return IssueSeverity.ERROR;
		default:
			return IssueSeverity.ERROR;
		}
	}

}
