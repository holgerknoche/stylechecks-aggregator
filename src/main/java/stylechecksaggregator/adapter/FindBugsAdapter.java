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
 * Adapter for FindBugs.
 * 
 * @author Holger Knoche
 */
public class FindBugsAdapter extends StylecheckToolAdapter {

	private static final String PREFIX = "findbugs.";
	
	/**
	 * Property for the file names.
	 */
	private static final String FILE_NAMES_PROPERTY = PREFIX + "fileNames";

	public FindBugsAdapter(final Properties properties) {
		super(extractFileNames(FILE_NAMES_PROPERTY, properties));
	}
	
	@Override
	protected List<Issue> processFile(final String fileName) throws IOException {
		final Document document = XMLUtil.parseXMLFile(fileName);

		try {
			return this.processFindBugsDocument(document);
		} catch (final XPathExpressionException e) {
			throw new RuntimeException(e);
		}
	}
	
	private List<Issue> processFindBugsDocument(final Document document) throws XPathExpressionException {
		final XPath xPath = XPathFactory.newInstance().newXPath();
		final NodeList bugNodes = (NodeList) xPath.evaluate("/BugCollection/BugInstance", document.getDocumentElement(), XPathConstants.NODESET);
	
		final List<Issue> issues = new ArrayList<>();
		
		for(int bugIndex = 0; bugIndex < bugNodes.getLength(); bugIndex++) {
			final Node bugNode = bugNodes.item(bugIndex);
			
			final NamedNodeMap bugAttributes = bugNode.getAttributes();
			final String bugType = bugAttributes.getNamedItem("type").getTextContent();
			final IssueSeverity severity = parseFindBugsPriority(bugAttributes.getNamedItem("priority").getTextContent());
			
			final NodeList sourceNodes = (NodeList) xPath.evaluate("SourceLine", bugNode, XPathConstants.NODESET);
			for(int sourceIndex = 0; sourceIndex < sourceNodes.getLength(); sourceIndex++) {
				 final Node sourceNode = sourceNodes.item(sourceIndex);
				 
				 final NamedNodeMap sourceAttributes = sourceNode.getAttributes();
				 final int lineNumber = Integer.parseInt(sourceAttributes.getNamedItem("start").getTextContent());				 				 
				 final String fileName = sourceAttributes.getNamedItem("sourcepath").getTextContent();				 
				 
				 final Issue issue = new Issue(fileName, lineNumber, severity, IssueType.FINDBUGS, bugType);
				 issues.add(issue);
			}
		}
			
		return issues;
	}
	
	private static IssueSeverity parseFindBugsPriority(final String value) {
		switch(value) {
		case "1":				
			return IssueSeverity.ERROR;
		case "2":
			return IssueSeverity.WARNING;
		case "3":
			return IssueSeverity.INFO;
		default: 
			return IssueSeverity.INFO;
		}
	}
	
}
