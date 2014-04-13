package edu.cmu.ravio.ner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import edu.cmu.ravio.parser.Entity;
import edu.cmu.ravio.parser.NER;

public class GlobalNER{
	public static Set<Entity> entityList = new HashSet<Entity>();
	
	private static GlobalNER instance  = null;
	
	private static final int NUMBER = 10;
	
	private static final int MAX_ITERATION = 20;
	
	private GlobalNER(){
		/*
		Entity entity = new Entity("David Bamman", "PERSON");
		entityList.add(entity);
		entity = new Entity("Mannal", "PERSON");
		entityList.add(entity);
		entity = new Entity("CMU", "LOCATION");
		entityList.add(entity);
		*/
	}
	
	private void getEntities(List<String> sentencesList){
		Random random = new Random();
		NER ner = new NER();
		HashMap<Integer, Integer> hashMap = new HashMap<Integer, Integer>();
		int currentIteration = 0;
		while(entityList.size() < NUMBER && currentIteration < MAX_ITERATION){
			int index = Math.abs(random.nextInt()) % sentencesList.size();
			++currentIteration;
			if(hashMap.containsKey(index)){
				continue;
			}else{
				hashMap.put(index, 1);
			}
			entityList.addAll(ner.getEntities(sentencesList.get(index)));
		}
	}
	
	private void getEntities(String doc){
		NER ner = new NER();
		entityList.addAll(ner.getEntities(doc));
	}
	
	public static List<Entity> getAllEntities(List<String> sentencesList){
		if(instance == null){
			instance = new GlobalNER();
		}
		instance.getEntities(sentencesList);
		return new ArrayList<Entity>(GlobalNER.entityList);
	}

	public static List<Entity> getEntityList() {
		if(GlobalNER.entityList == null){
			System.err.println("You haven't initialized the entityList yet");
		}
		return new ArrayList<Entity>(GlobalNER.entityList);
	}

	public static void setEntityList(Set<Entity> entityList) {
		GlobalNER.entityList = entityList;
	}
	
}
