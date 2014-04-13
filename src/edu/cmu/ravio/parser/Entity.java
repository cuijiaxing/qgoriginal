package edu.cmu.ravio.parser;

import java.util.ArrayList;
import java.util.List;

public class Entity {
	private String text;
	private String ner;
	
	public Entity(String text, String ner){
		this.text = text;
		this.ner = ner;
	}
	
	public static List<Entity> getEntityList(String inlineXML) throws Exception{
		int index;
		List<Entity> resultList = new ArrayList<Entity>();
		while((index = inlineXML.indexOf("<")) != -1){
			int postIndex = inlineXML.indexOf(">");
			String ner = inlineXML.substring(index + 1, postIndex);
			String subString = inlineXML.substring(postIndex + 1);
			index = subString.indexOf("<") + postIndex + 1;
			String text = inlineXML.substring(postIndex + 1, index);
			postIndex = subString.indexOf(">") + postIndex + 1;
			inlineXML = inlineXML.substring(postIndex + 1);
			Entity entity = new Entity(text.trim(), ner.trim());
			resultList.add(entity);
		}
		return resultList;
	}
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getNer() {
		return ner;
	}

	public void setNer(String ner) {
		this.ner = ner;
	}
	
	public String toString(){
		return ner + "\t" + text;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return ner.trim().hashCode() * 10  + text.trim().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return this.toString().equals(obj.toString());
	}
	
	
	
	
}
