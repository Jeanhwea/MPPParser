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
		graph = rder.getDgraph();
		this.filename = filename;
	}
	

	public void write() throws IOException {
		file = new File(filename);
		file_output_stream = new FileOutputStream(file.getAbsoluteFile());
		file_writer = new OutputStreamWriter(file_output_stream, "UTF-8");
		wter = new BufferedWriter(file_writer);
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
			line = String.format("T%d[label=\"T%s\\n%.1f%s\"];", my_task.getId(), my_task.getId(), my_task.getDuration(), my_task.getUnit());
			indentPrint(line);
		}
//		ByteNodeMatrix closure = Transitivity.acyclicClosure(rder.getDgraph());
//		for (Node u : rder.getDgraph().nodes()) {
//			for (Node v : rder.getDgraph().nodes()) {
//				byte canReach = closure.get(u, v);
//				// a matrix whose entries (i,j) are 1 if i can reach j in the graph dg, and 0 otherwise.
//				if (canReach == 1) {
//					MyTask src_task = rder.getTaskByNid(u.nodeId());
//					MyTask des_task = rder.getTaskByNid(v.nodeId());
//					line = String.format("T%d -> T%d;", src_task.getId(), des_task.getId());
//					indentPrint(line);
//				}
//			}
//		}
		for (Edge rela : graph.edges()) {
			MyTask src_task = rder.getTaskByNid(rela.source().nodeId());
			MyTask des_task = rder.getTaskByNid(rela.target().nodeId());
			line = String.format("T%d -> T%d;", src_task.getId(), des_task.getId());
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
			line = String.format("R%d[label=\"R%s\\n%s\"];", resource.getId(), resource.getId(), resource.getName());
			indentPrint(line);
		}
		
		/* construct some like this
		 * R1 -> R2 -> R3 -> R4 -> R5[style=invis];
		 */
		if (!resources.isEmpty()) {
			line ="R" + resources.get(0).getId();
			for (int i = 1; i < resources.size(); i++) {
				MyResource resource = resources.get(i);
				line += " -> " + "R" + resource.getId();
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
