package net.jeanhwea;

import java.awt.Component;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import net.jeanhwea.in.Reader;
import net.jeanhwea.out.XmlFileWriter;
import net.sf.mpxj.MPXJException;

public class MPPParser {
	
	private Reader reader;
	XmlFileWriter xml_writer;

	MPPParser() {
		reader = new Reader();
	}
	
	public void executeSync(String cmd) {
		System.out.println("> " + cmd);
		Runtime runtime = Runtime.getRuntime();
		try {
			Process process = runtime.exec(cmd);
			process.waitFor();
			if (process.exitValue() != 0) {
				System.err.println("Bad execute value = " + process.exitValue());
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
	
	public void parse(String filename) throws MPXJException {
		reader.readFile(filename);
		reader.loadTasks();
		reader.loadResources();
		reader.printTasks();
	}
	
	public void testDotFile() throws IOException, InterruptedException {
//		reader.buildGraphWithAllTasks();
		reader.buildGraphWithLeafTasks();
		reader.genDotFile();
//		reader.printNodes();
//		reader.printEdges();
		reader.printGraphInfo();
		
		String cmd, input, output;
		input = reader.getDot_filename();
		output = reader.getDot_filename().split("\\.")[0] + ".pdf";
		
		
		cmd = String.format("dot -Tpdf %s -o %s", input, output);
		executeSync(cmd);
		cmd = String.format("cmd /c move %s d:\\tmp", input);
		executeSync(cmd);
		cmd = String.format("cmd /c move %s d:\\tmp", output);
		executeSync(cmd);
		String[] path_list = output.split("\\\\");
		cmd = String.format("cmd /c start d:\\tmp\\%s", path_list[path_list.length-1]);
		executeSync(cmd);
	}
	
	public void testXmlFile() throws ParserConfigurationException, TransformerException {
		xml_writer = new XmlFileWriter();
		xml_writer.buildXML();
		xml_writer.write();
	}

	public static void main(String[] args) throws MPXJException, IOException, InterruptedException, ParserConfigurationException, TransformerException {
		MPPParser parser = new MPPParser();
		JFileChooser chooser = new JFileChooser("mpps");
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Microsoft Project File (*.mpp)", "mpp");
		chooser.setFileFilter(filter);
		
		Component parent = null;
		int returnVal = chooser.showOpenDialog(parent);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String full_filename = chooser.getSelectedFile().getAbsolutePath();
			System.out.println("Try to parse " + full_filename);
			parser.parse(full_filename);
			parser.testDotFile();
		} else {
			System.err.println("Ha, ha, You canceled!!!");
			parser.testXmlFile();
		}
	}

}
