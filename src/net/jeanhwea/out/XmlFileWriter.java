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

import net.jeanhwea.ds.MyResource;
import net.jeanhwea.ds.MyTask;
import net.jeanhwea.in.Reader;
import net.stixar.graph.Edge;
import net.stixar.graph.Node;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class XmlFileWriter {
	Reader rder;
	String filename;
	
	DocumentBuilderFactory doc_builder_factory;
	DocumentBuilder doc_builder;
	TransformerFactory trans_factory;
	Transformer trans;
	DOMSource source;
	StreamResult result;
	
	Document doc;
	Element root;

	public XmlFileWriter(String filename, Reader reader) throws TransformerConfigurationException, ParserConfigurationException {
		doc_builder_factory = DocumentBuilderFactory.newInstance();
		doc_builder = doc_builder_factory.newDocumentBuilder();
		doc = doc_builder.newDocument();
	
		trans_factory = TransformerFactory.newInstance();
		trans = trans_factory.newTransformer();
		// pretty print
		trans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
//		trans.setOutputProperty(OutputKeys.INDENT, "yes");
		
		rder = reader;
		this.filename = filename;
	}

	public void buildXML() {
		root = doc.createElement("Project");
		doc.appendChild(root);
		
		Element size = doc.createElement("Size");
		root.appendChild(size);
		size.setAttribute("ResourceSize", String.valueOf(rder.getResources().size()));
		size.setAttribute("TaskSize", String.valueOf(rder.getDgraph().nodeSize()));
		size.setAttribute("DependSize", String.valueOf(rder.getDgraph().edgeSize()));
		
		Element resources = doc.createElement("Resources");
		root.appendChild(resources);
		resources.setAttribute("size", String.valueOf(rder.getResources().size()));
		for (MyResource mr : rder.getResources()) {
			Element resource = doc.createElement("Resource");
			resources.appendChild(resource);
			resource.setAttribute("uid", String.valueOf(mr.getUid()));
			resource.setAttribute("cost", String.valueOf(mr.getCost()));
			resource.setAttribute("max_unit", String.valueOf(mr.getMaxUnit()));
			resource.setAttribute("name", mr.getName());
		}
		
		Element tasks = doc.createElement("Tasks");
		root.appendChild(tasks);
		tasks.setAttribute("size", String.valueOf(rder.getDgraph().nodeSize()));
		for (Node node : rder.getDgraph().nodes()) {
			MyTask mt = rder.getTaskByNid(node.nodeId());
			Element task = doc.createElement("Task");
			tasks.appendChild(task);
			task.setAttribute("name", mt.getName());
			task.setAttribute("uid", String.valueOf(mt.getUid()));
			task.setAttribute("duration", String.valueOf(mt.getDuration()));
			task.setAttribute("unit", mt.getUnit());
			Element resources_needed = doc.createElement("ResourcesNeeded");
			task.appendChild(resources_needed);
			resources_needed.setAttribute("size", String.valueOf(mt.getResource().size()));
			for (Integer res : mt.getResource()) {
				Element resource_needed = doc.createElement("ResourceNeeded");
				resources_needed.appendChild(resource_needed);
				resource_needed.setAttribute("resource_uid", String.valueOf(res));
			}
		}
		
		Element dependencies = doc.createElement("Dependencies");
		root.appendChild(dependencies);
		dependencies.setAttribute("size", String.valueOf(rder.getDgraph().edgeSize()));
		for (Edge edge : rder.getDgraph().edges()) {
			int nid_src, nid_des;
			nid_src = edge.source().nodeId();
			nid_des = edge.target().nodeId();
			
			MyTask mt_src, mt_des;
			mt_src = rder.getTaskByNid(nid_src);
			mt_des = rder.getTaskByNid(nid_des);
			
			Element dependency = doc.createElement("Dependency");
			dependencies.appendChild(dependency);
			dependency.setAttribute("predecessor", String.valueOf(mt_src.getUid()));
			dependency.setAttribute("successor", String.valueOf(mt_des.getUid()));
		}
	}
	
	public void write() throws TransformerException {
		source = new DOMSource(doc);
		// Output to console for testing
//		result = new StreamResult(System.out);
//		trans.transform(source, result);
		// save to a file
		result = new StreamResult(filename);
		trans.transform(source, result);
	}
}
