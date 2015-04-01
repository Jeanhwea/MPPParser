package net.jeanhwea;

import java.awt.Component;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.sf.mpxj.MPXJException;

public class MPPParser {
	
	private Reader reader;

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
	
	public void test() throws IOException, InterruptedException {
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

	public static void main(String[] args) throws MPXJException, IOException, InterruptedException {
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
			parser.test();
		} else {
			System.err.println("Ha, ha, You canceled!!!");
		}
	}

}
