package edu.cmu.ravio;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.ark.Question;

public class EnhancedQuestionGenerator {
	private String answer;
	public String extractBecause(String sentence){
		String becauseRegex = "because";
		String becauseOfRegex = "because of";
		//we have to judge "because" first, otherwise, it would fail.
		if(sentence.contains(becauseOfRegex)){
			return extractBecauseOfSentence(sentence);
		}else//then we deal with "because of" sentences
			if(sentence.contains(becauseRegex)){
				return extractBecauseSentence(sentence);
			}else{
				answer = null;
				return null;
			}
	}
	
	/*
	 * deal with the sentence that contains "because of"
	 * */
	private String extractBecauseOfSentence(String sentence){
		//find the index of because
		int index = sentence.indexOf("because");
		answer = "It is " + sentence.substring(index);
		return sentence.substring(0, index);
	}
	
	/*
	 * deal with the sentence that only contains "because"
	 * */
	private String extractBecauseSentence(String sentence){
		int index = sentence.indexOf("because");
		answer = "It is " + sentence.substring(index);
		return sentence.substring(0, index);
	}
	
	public List<Question> selectQuestion(List<Question> questionList){
		if(answer == null){
			return questionList;
		}
		List<Question> enhancedQuestionList = new ArrayList<Question>();
		String keywordsArray[] = {"Does", "Did", "Is", "Was", "Do", "Were", "Are"};
		for(int i = 0; i < questionList.size(); ++i){
			String questionStr = questionList.get(i).yield();
			for(String keyword : keywordsArray){
				if(questionStr.startsWith(keyword) && !questionStr.contains("because")){
					if(keyword == "Do" && questionStr.startsWith("Does")){
						continue;
					}
					EnhancedQuestion q = new EnhancedQuestion();
					String revisedQuestion = "Why " + keyword.toLowerCase() + " " + questionStr.substring(keyword.length());
					q.setQuestion(revisedQuestion);
					q.setAnswer(this.answer);
					enhancedQuestionList.add(q);
				}
			}
		}
		//clear the answer record
		this.answer = null;
		enhancedQuestionList = eliminateDuplicateQuestions(enhancedQuestionList);
		return mergeQuestionList(enhancedQuestionList, questionList);
	}
	
	/**
	 * merge two question list
	 * */
	public static List<Question> mergeQuestionList(List<Question> first, List<Question> second){
		//we also have to make sure that there is no duplicate
		List<Question> resultList = new ArrayList<Question>();
		resultList.addAll(first);
		for(int i = 0; i < second.size(); ++i){
			boolean noDuplicate = true;
			for(int j = 0;j < first.size(); ++j){
				if(first.get(j).yield().equals(second.get(i).yield())){
					noDuplicate = false;
					break;
				}
			}
			if(noDuplicate){
				resultList.add(second.get(i));
			}
		}
		return resultList;
	}
	
	/*
	 * Can only be used on enhanced questions, you cannot apply it on normal questions.
	 * */
	public static List<Question> eliminateDuplicateQuestions(List<Question> questionList){
		List<Question> resultList = new ArrayList<Question>();
		int i, j;
		for(i = 0; i < questionList.size(); ++i){
			for(j = 0; j < i; ++j){
				if(questionList.get(i).yield().equals(questionList.get(j).yield())){
					break;
				}
			}
			if(j == i && ((EnhancedQuestion)questionList.get(i)).isReasonableQuestion()){
				resultList.add(questionList.get(i));
			}
		}
		return resultList;
	}
	
	public boolean hasGenerated(){
		return this.answer != null;
	}
}
