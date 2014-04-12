package edu.cmu.ravio;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.cmu.ark.Question;
import edu.cmu.ravio.ner.GlobalNER;
import edu.cmu.ravio.parser.Entity;
import edu.cmu.ravio.parser.NER;

public class NERQuestionGenerator {
	
	
	public EnhancedQuestion generateNERQuestion(String sentence){
		//only deal with the following cases
		//I think we need to construct a substitution table at first
		String[] rules = {"Does", "Did", "Is", "Was", "Were", "Do", "Are"};
		String result = sentence;
		for(int i = 0; i < rules.length; ++i){
			if(sentence.startsWith(rules[i])){
				result = replace(sentence);
				break;
			}
		}
		return new EnhancedQuestion(result, "False");
	}
	
	public List<Question> generateNERQuestion(List<Question> questionList){
		List<Question> generatedList = new ArrayList<Question>();
		for(int i = 0; i < questionList.size(); ++i){
			EnhancedQuestion q = generateNERQuestion(questionList.get(i).yield());
			generatedList.add(q);
		}
		
		
		//eliminate duplication
		generatedList = EnhancedQuestionGenerator.eliminateDuplicateQuestions(generatedList);
		
		//merge question list
		generatedList = EnhancedQuestionGenerator.mergeQuestionList(questionList, generatedList);
		
		return generatedList;
	}
	
	private String replace(String question){
		NER ner = new NER();
		List<Entity> qNERList = ner.getEntities(question);
		List<Entity> reservedEntity = GlobalNER.getEntityList();
		for(int i = 0; i < reservedEntity.size(); ++i){
			System.out.println(reservedEntity.get(i));
		}
		if(qNERList.size() > 0){
			Random random = new Random();
			for(int i = 0; i < reservedEntity.size(); ++i){
				int index = Math.abs(random.nextInt()) % reservedEntity.size();
				if(!question.contains(reservedEntity.get(index).getText())){
					int q_index = Math.abs(random.nextInt()) % qNERList.size();
					for(int k = 0; k < qNERList.size(); ++k){
						int temp_index = (q_index + k) % qNERList.size();
						if(qNERList.get(temp_index).getNer().equals(reservedEntity.get(index).getNer())){
							question = question.replace(qNERList.get(temp_index).getText(), reservedEntity.get(index).getText());
							return question;
						}
					}
				}
			}
		}
		return question;
	}
	
}
