package net.jeanhwea;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;

import net.stixar.graph.BasicDigraph;
import net.stixar.graph.Edge;
import net.stixar.graph.Node;

public class DotFileWriter {
	
	protected int indent = 0;
	
	private File file;
	private BufferedWriter wter;
	private BasicDigraph graph;
	private Reader rder;
	private FileWriter file_writer;
	
	DotFileWriter(String filename, Reader reader) throws IOException {
		rder = reader;
		file = new File(filename);
		file_writer = new FileWriter(file.getAbsolutePath());
		wter = new BufferedWriter(file_writer);
	}
	

	void write(BasicDigraph dgraph) throws IOException {
		graph = dgraph;
		drawDigraph();
		wter.close();
	}
	
	private void drawDigraph() throws IOException {
		indentPrint("digraph G {");
		indent++;
		indentPrint("ranksep=0.75; ratio=compress; size= \"27.5,20!\"; compound=true; node[fontsize=54]");
		drawTasks();
		indent--;
		indentPrint("}");
	}
	
	private void drawTasks() throws IOException {
		indentPrint("subgraph cluster_tasks {");
		indent++;
		indentPrint("label = \"Tasks\";");
		indentPrint("style = filled;");
		indentPrint("color = white;");
		indentPrint("fontsize = 54;");
		for (Node task : graph.nodes()) {
			MyTask my_task;
			my_task = rder.getTaskByNid(task.nodeId());
			String line = "T" + task.nodeId() + "[label=\"" + my_task.getName() + "-" + my_task.getDuration() + "\", shape=circle];";
			indentPrint(line);
		}
		for (Edge rela : graph.edges()) {
			String line = "T"+rela.source().nodeId() + " -> " + "T"+rela.target().nodeId() + ";";
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
//		wter.flush();
	}
}
