package edu.cmu.ravio.enhanced;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import edu.cmu.ravio.ner.GlobalNER;
import edu.cmu.ravio.parser.Entity;

public class EnhancedFeature {
	public static void Facade(String inputFileName, List<String> sentenceList){
		//get all the named entities from the passage
		List<Entity> entityList = GlobalNER.getAllEntities(sentenceList);
		for(int i = 0; i < entityList.size(); ++i){
			System.out.println(entityList.get(i));
		}
	}
	
	
	/**
	 * @param sentenceList: all the sentences
	 * @param qNumber: the number of sentences we want to use
	 * @param maxLength: the maximum number of sentence we wan to use
	 * */
	public static List<String> getRandomSentence(List<String> sentenceList, int qNumber, final int maxLength){
		List<String> resultList = new ArrayList<String>();
		Random random = new Random();
		int count = 0;
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		qNumber = Math.min(qNumber, sentenceList.size());
		while(count < qNumber){
			int index = Math.abs(random.nextInt() % sentenceList.size());
			if(map.containsKey(index) || sentenceList.get(index).length() >= maxLength){
				continue;
			}else{
				++count;
				resultList.add(sentenceList.get(index));
			}
		}
		return resultList;
	}
}
