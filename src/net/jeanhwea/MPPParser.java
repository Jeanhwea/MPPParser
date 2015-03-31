package net.jeanhwea;

import java.io.FileNotFoundException;
import java.io.IOException;

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
		reader.removeZeroDurationTasks();
		reader.loadTasks();
//		reader.printTasks();
//		reader.printNodes();
//		reader.printEdges();
	}
	
	public void test() throws IOException, InterruptedException {
		reader.genDotFile();
		String cmd, input, output;
		input = reader.getDot_filename();
		output = reader.getDot_filename().split("\\.")[0] + ".pdf";
		
		
		cmd = String.format("dot -Tpdf %s -o %s", input, output);
		executeSync(cmd);
		cmd = String.format("cmd /c move %s d:\\tmp", input);
		executeSync(cmd);
		cmd = String.format("cmd /c move %s d:\\tmp", output);
		executeSync(cmd);
		cmd = String.format("cmd /c start d:\\tmp\\%s", output);
		executeSync(cmd);
	}

	public static void main(String[] args) throws MPXJException, IOException, InterruptedException {
		MPPParser parser = new MPPParser();
		parser.parse("mpps/input.mpp");
//		parser.parse("mpps/C-softchoice.com.mpp");
		parser.test();
	}

}
