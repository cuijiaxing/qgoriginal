package edu.cmu.ravio.parser;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class NER {
	
	public List<Entity> getEntities(String inputSentence){
		Runtime run = Runtime.getRuntime();
		String cmd = "java -jar runnable/NERParser.jar " + inputSentence;
		List<Entity> resultList = new ArrayList<Entity>();
		try{
			Process p = run.exec(cmd);
			BufferedInputStream in = new BufferedInputStream(p.getInputStream());
			BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
			String lineStr;
			String result = "";
			while((lineStr = inBr.readLine()) != null){
				result += lineStr;
			}
			if(p.waitFor() != 0){
				if(p.exitValue() == 1){
					System.err.println("Java -Jar failed");
				}
			}
			inBr.close();
			in.close();
			resultList = Entity.getEntityList(result);
		}catch(Exception e){
			System.err.println("invode java -jar failed");
		}
		return resultList;
	}
	
}
