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

import net.jeanhwea.ds.MyAssignment;
import net.jeanhwea.ds.MyResource;
import net.jeanhwea.ds.MyTask;
import net.jeanhwea.in.Reader;
import net.stixar.graph.Node;
import net.stixar.graph.attr.ByteNodeMatrix;
import net.stixar.graph.conn.Transitivity;

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
		
		Element resources = doc.createElement("Resources");
		root.appendChild(resources);
		resources.setAttribute("size", String.valueOf(rder.getResources().size()));
		for (MyResource mr : rder.getResources()) {
			Element resource = doc.createElement("Resource");
			resources.appendChild(resource);
			resource.setAttribute("id", String.valueOf(mr.getId()));
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
			task.setAttribute("id", String.valueOf(mt.getId()));
			task.setAttribute("uid", String.valueOf(mt.getUid()));
			task.setAttribute("duration", String.valueOf(mt.getDuration()));
			task.setAttribute("unit", mt.getUnit());
			task.setAttribute("name", mt.getName());
		}
		
		Element dependencies = doc.createElement("Dependencies");
		root.appendChild(dependencies);
		int dep_size = 0;
		ByteNodeMatrix closure = Transitivity.acyclicClosure(rder.getDgraph());
		for (Node u : rder.getDgraph().nodes()) {
			for (Node v : rder.getDgraph().nodes()) {
				if (u == v) continue;
				byte canReach = closure.get(u, v);
				// a matrix whose entries (i,j) are 1 if i can reach j in the graph dg, and 0 otherwise.
				if (canReach == 1) {
					dep_size ++;
					int nid_src, nid_des;
					nid_src = u.nodeId();
					nid_des = v.nodeId();
					MyTask mt_src, mt_des;
					mt_src = rder.getTaskByNid(nid_src);
					mt_des = rder.getTaskByNid(nid_des);
					
					Element dependency = doc.createElement("Dependency");
					dependencies.appendChild(dependency);
					dependency.setAttribute("predecessor", String.valueOf(mt_src.getId()));
					dependency.setAttribute("successor", String.valueOf(mt_des.getId()));
				}
			}
		}
		dependencies.setAttribute("size", String.valueOf(dep_size));
		
//		for (Edge edge : rder.getDgraph().edges()) {
//		int nid_src, nid_des;
//		nid_src = edge.source().nodeId();
//		nid_des = edge.target().nodeId();
//		
//		MyTask mt_src, mt_des;
//		mt_src = rder.getTaskByNid(nid_src);
//		mt_des = rder.getTaskByNid(nid_des);
//		
//		Element dependency = doc.createElement("Dependency");
//		dependencies.appendChild(dependency);
//		dependency.setAttribute("predecessor", String.valueOf(mt_src.getId()));
//		dependency.setAttribute("successor", String.valueOf(mt_des.getId()));
//	}
	
		Element assigns = doc.createElement("Assignments");
		root.appendChild(assigns);
		assigns.setAttribute("size", String.valueOf(rder.getAssigns().size()));
		for (MyAssignment as : rder.getAssigns()) {
			Element assign = doc.createElement("Assignment");
			assigns.appendChild(assign);
			
			assign.setAttribute("task", String.valueOf(as.getTask().getId()));
			assign.setAttribute("resource", String.valueOf(as.getResource().getId()));
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
