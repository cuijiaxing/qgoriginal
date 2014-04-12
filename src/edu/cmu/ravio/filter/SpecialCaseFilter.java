package edu.cmu.ravio.filter;

public class SpecialCaseFilter {
	
	
	/*
	 * rewite "it's" to generate gramaticall right question.
	 * */
	public static String itsFilter(String sentence){
		int index = -1;
		while((index = sentence.indexOf("it 's")) != -1){
			sentence = sentence.substring(0, index) + "it is" + sentence.substring(index + 5);
		}
		while((index = sentence.indexOf("It 's")) != -1){
			sentence = sentence.substring(0, index) + "It is" + sentence.substring(index + 5);
		}
		return sentence;
	}
}
