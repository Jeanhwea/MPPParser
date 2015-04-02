package net.jeanhwea.out;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class XmlFileWriter {
	DocumentBuilderFactory doc_builder_factory;
	DocumentBuilder doc_builder;
	TransformerFactory trans_factory;
	Transformer trans;
	String filename;
	DOMSource source;
	StreamResult result;
	
	Document doc;
	Element root;
	
	public XmlFileWriter () throws ParserConfigurationException, TransformerConfigurationException {
		doc_builder_factory = DocumentBuilderFactory.newInstance();
		doc_builder = doc_builder_factory.newDocumentBuilder();
		doc = doc_builder.newDocument();
	
		trans_factory = TransformerFactory.newInstance();
		trans = trans_factory.newTransformer();
		// pretty print
		trans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		trans.setOutputProperty(OutputKeys.INDENT, "yes");
		
	}
	
	public void buildXML() {
		root = doc.createElement("Project");
		doc.appendChild(root);
		Element task;
		task = doc.createElement("Task");
		task.setTextContent("textContent");
		task.setAttribute("Duration", "32");
		root.appendChild(task);
	}
	
	public void write() throws TransformerException {
		source = new DOMSource(doc);
		// Output to console for testing
		result = new StreamResult(System.out);
		// save to a file
//		result = new StreamResult(filename);
		trans.transform(source, result);
	}
}
