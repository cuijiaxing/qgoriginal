package edu.cmu.ravio;

import edu.cmu.ark.Question;
import edu.stanford.nlp.trees.Tree;

public class EnhancedQuestion extends Question{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7471544470319133729L;
	private String question;
	private String answer;
	public EnhancedQuestion(){
		super();
		question = null;
		answer = null;
		this.setScore(1);
	}
	
	public EnhancedQuestion(String question, String answer){
		super();
		this.question = question;
		this.answer = answer;
	}

	@Override
	public Tree getTree() {
		// TODO Auto-generated method stub
		return super.getTree();
	}

	@Override
	public String yield() {
		// TODO Auto-generated method stub
		return question;
	}
	
	@Override
	public String toString(){
		return answer;
	}
	
	

	@Override
	public double getScore() {
		// TODO Auto-generated method stub
		return getSimilarity();
	}

	@Override
	public Tree getAnswerPhraseTree() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public boolean isReasonableQuestion(){
		
		if( getSimilarity() > 0.5){
			return false;
		}else{
			return true;
		}
	}
	
	private double getSimilarity(){
		String[] questionWords = this.question.split("\\s+");
		String[] answerWords = this.answer.split("\\s+");
		int similarity = 0;
		for(int i = 0; i < questionWords.length; ++i){
			for(int j = 0; j < answerWords.length; ++j){
				if(questionWords[i].equals(answerWords[j])){
					++similarity;
				}
			}
		}
		return similarity * 2.0 / (questionWords.length + answerWords.length);
	}
	

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}
}
