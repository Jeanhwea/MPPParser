package net.jeanhwea.out;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Vector;

import net.jeanhwea.ds.MyResource;
import net.jeanhwea.ds.MyTask;
import net.jeanhwea.in.Reader;
import net.stixar.graph.BasicDigraph;
import net.stixar.graph.Edge;
import net.stixar.graph.Node;

public class DotFileWriter {
	
	protected int indent = 0;
	
	private String filename;
	private File file;
	private BufferedWriter wter;
	private BasicDigraph graph;
	private Reader rder;
	private FileOutputStream file_output_stream;
	private OutputStreamWriter file_writer;
	
	public DotFileWriter(String filename, Reader reader) throws IOException {
		rder = reader;
		this.filename = filename;
	}
	

	public void write(BasicDigraph dgraph) throws IOException {
		file = new File(filename);
		file_output_stream = new FileOutputStream(file.getAbsoluteFile());
		file_writer = new OutputStreamWriter(file_output_stream, "UTF-8");
		wter = new BufferedWriter(file_writer);
		graph = dgraph;
		drawDigraph();
		wter.close();
		file_output_stream.close();
	}
	
	private void drawDigraph() throws IOException {
		indentPrint("digraph G {");
		indent++;
		indentPrint("compound=true; rankdir=LR; ratio=compress;");
		drawResources();
		drawTasks();
		indent--;
		indentPrint("}");
	}
	
	private void drawTasks() throws IOException {
		String line;
		indentPrint("subgraph cluster_tasks {");
		indent++;
		
		indentPrint("node[shape=box, color=black];");
		
		for (Node task : graph.nodes()) {
			MyTask my_task;
			my_task = rder.getTaskByNid(task.nodeId());
//			line = String.format("T%d[label=\"%s_%.0f%s\"];", task.nodeId(), my_task.getName(), my_task.getDuration(), my_task.getUnit());
			line = String.format("T%d[label=\"T%s_%.0f%s\"];", task.nodeId(), my_task.getNid(), my_task.getDuration(), my_task.getUnit());
			indentPrint(line);
		}
		
		for (Edge rela : graph.edges()) {
			line = String.format("T%d -> T%d;", rela.source().nodeId(), rela.target().nodeId());
			indentPrint(line);
		}
		
		indent--;
		indentPrint("}");
	}
	
	private void drawResources() throws IOException {
		String line;
		indentPrint("subgraph cluser_resources {");
		indent++;
		
		indentPrint("node[shape=folder, color=blue, fontcolor=blue];");
		
		Vector<MyResource> resources = rder.getResources();
		for (MyResource resource : resources) {
			if (resource.getName() == null)
				continue;
//			line = String.format("R%d[label=\"%s\"];", resource.getUid(), resource.getName());
			line = String.format("R%d[label=\"R%s\"];", resource.getUid(), resource.getUid());
			indentPrint(line);
		}
		
		/* construct some like this
		 * R1 -> R2 -> R3 -> R4 -> R5[style=invis];
		 */
		if (!resources.isEmpty()) {
			line ="R" + resources.get(0).getUid();
			for (int i = 1; i < resources.size(); i++) {
				MyResource resource = resources.get(i);
				line += " -> " + "R" + resource.getUid();
			}
			line += "[style=invis];";
			indentPrint(line);
		}
		indent--;
		indentPrint("}");
	}
	
	protected void indentPrint(String line) throws IOException {
		for (int i = 0; i < indent; i++) {
			wter.write("\t");
		}
		wter.write(line);
		wter.newLine();
	}
}
