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
 * Adapter for PMD.
 * 
 * @author Holger Knoche
 */
public class PMDAdapter extends StylecheckToolAdapter {

	private static final String PREFIX = "pmd.";
	
	/**
	 * Property for the file names.
	 */
	private static final String FILE_NAMES_PROPERTY = PREFIX + "fileNames";
	
	public PMDAdapter(final Properties properties) {
		super(extractFileNames(FILE_NAMES_PROPERTY, properties));
	}

	@Override
	protected List<Issue> processFile(final String fileName) throws IOException {
		final Document document = XMLUtil.parseXMLFile(fileName);

		try {
			return this.processPMDDocument(document);
		} catch (final XPathExpressionException e) {
			throw new RuntimeException(e);
		}
	}
	
	private List<Issue> processPMDDocument(final Document document) throws XPathExpressionException {
		final XPath xPath = XPathFactory.newInstance().newXPath();
		final NodeList fileNodes = (NodeList) xPath.evaluate("/pmd/file", document.getDocumentElement(), XPathConstants.NODESET);
	
		final List<Issue> issues = new ArrayList<>();
		
		for(int fileIndex = 0; fileIndex < fileNodes.getLength(); fileIndex++) {
			final Node fileNode = fileNodes.item(fileIndex);
			
			final NamedNodeMap fileAttributes = fileNode.getAttributes();
			final String fileName = fileAttributes.getNamedItem("name").getTextContent();
			
			final NodeList errorNodes = (NodeList) xPath.evaluate("violation", fileNode, XPathConstants.NODESET);
			for(int errorIndex = 0; errorIndex < errorNodes.getLength(); errorIndex++) {
				 final Node errorNode = errorNodes.item(errorIndex);
				 
				 final NamedNodeMap errorAttributes = errorNode.getAttributes();
				 final int lineNumber = Integer.parseInt(errorAttributes.getNamedItem("beginline").getTextContent());				 
				 final IssueSeverity severity = parsePMDPriority(errorAttributes.getNamedItem("priority").getTextContent());
				 
				 final String message = errorNode.getTextContent().trim();
				 
				 final Issue issue = new Issue(fileName, lineNumber, severity, IssueType.PMD, message);
				 issues.add(issue);
			}
		}
			
		return issues;
	}
	
	private static IssueSeverity parsePMDPriority(final String value) {
		switch(value) {
		case "1":
		case "2":			
			return IssueSeverity.ERROR;
		case "3":
			return IssueSeverity.WARNING;
		default: 
			return IssueSeverity.INFO;
		}
	}

}
