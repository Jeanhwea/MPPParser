package net.jeanhwea;

import java.awt.Component;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import net.jeanhwea.in.Reader;
import net.sf.mpxj.MPXJException;

public class MPPParser {
	
	private Reader reader;
	private String tmp_dir = "./tmp/";

	MPPParser() {
		reader = new Reader();
	}
	
	public boolean renameFile(String src, String des) {
		boolean ret;
		File src_file = new File(src);
		File des_file = new File(des);
		
		if (src_file.exists()) {
			ret = src_file.renameTo(des_file);
		} else {
			ret = false;
		}
		
		return ret;
	}
	
	public boolean moveFileToPath(String full_filename, String pathname) {
		String filename;
		String[] path_list = full_filename.split("\\\\");
		filename = path_list[path_list.length-1];
		return renameFile(full_filename, pathname+filename);
	}
	
	public void executeSync(String cmd) {
		System.out.println("cmd> " + cmd);
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
//		reader.printTasks();
//		reader.printResources();
//		reader.buildGraphWithAllTasks();
		reader.buildGraphWithLeafTasks();
//		reader.printNodes();
//		reader.printEdges();
		reader.printGraphInfo();
	}
	
	public void testDotFile() throws IOException, InterruptedException {
		reader.genDotFile();
		
		String cmd, input, output;
		input = reader.getDotFilename();
		output = reader.getFilePrefix() + ".pdf";
		
		// do more removing file work and call graphviz to generate PDF file
		cmd = String.format("dot -Tpdf %s -o %s", input, output);
		executeSync(cmd);
		
		moveFileToPath(input, tmp_dir);
		moveFileToPath(output, tmp_dir);
		
		// display PDF file, if supported
		if (Desktop.isDesktopSupported()) {
			String[] path_list = output.split("\\\\");
			File file = new File(tmp_dir + path_list[path_list.length-1]);
			Desktop.getDesktop().open(file);
		} else {
			System.err.println("Cannot open pdf for no desktop support!");
		}
	}
	
	public void testXmlFile() throws ParserConfigurationException, TransformerException {
		reader.genXmlFile();
		
		// remove file to temporary directory
		String output;
		output = reader.getXmlFilename();
		moveFileToPath(output, tmp_dir);
	}

	public static void main(String[] args) throws MPXJException, IOException, InterruptedException, ParserConfigurationException, TransformerException {
		MPPParser parser = new MPPParser();
		JFileChooser chooser = new JFileChooser("mpps");
//		JFileChooser chooser = new JFileChooser(".");
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Microsoft Project File (*.mpp)", "mpp");
		chooser.setFileFilter(filter);
		
		int returnVal = chooser.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String full_filename = chooser.getSelectedFile().getAbsolutePath();
			System.out.println("Try to parse " + full_filename);
			parser.parse(full_filename);
			parser.testXmlFile();
			parser.testDotFile();
		} else {
			System.err.println("Canceled ???");
		}
	}

}
