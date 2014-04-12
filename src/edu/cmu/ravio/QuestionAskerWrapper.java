package edu.cmu.ravio;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.ark.AnalysisUtilities;
import edu.cmu.ark.GlobalProperties;
import edu.cmu.ark.InitialTransformationStep;
import edu.cmu.ark.Question;
import edu.cmu.ark.QuestionRanker;
import edu.cmu.ark.QuestionTransducer;
import edu.stanford.nlp.trees.Tree;

public class QuestionAskerWrapper{
	//generate question sentence by sentence
	public List<Question> getQuestions(List<String> sentences, InitialTransformationStep trans, QuestionRanker qr, QuestionTransducer qt){
		long startTime = System.currentTimeMillis();
		List<Question> outputQuestionList = new ArrayList<Question>();
		//iterate over each segmented sentence and generate questions
		List<Tree> inputTrees = new ArrayList<Tree>();
		
		for(String singleSentence: sentences){
			if(GlobalProperties.getDebug()) System.err.println("Question Asker: sentence: "+singleSentence);
			
			Tree parsed = AnalysisUtilities.getInstance().parseSentence(singleSentence).parse;
			inputTrees.add(parsed);
		}
		
		if(GlobalProperties.getDebug()) System.err.println("Seconds Elapsed Parsing:\t"+((System.currentTimeMillis()-startTime)/1000.0));
		
		//step 1 transformations
		List<Question> transformationOutput = trans.transform(inputTrees);
		
		//step 2 question transducer
		for(Question t: transformationOutput){
			if(GlobalProperties.getDebug()) System.err.println("Stage 2 Input: "+t.getIntermediateTree().yield().toString());
			qt.generateQuestionsFromParse(t);
			outputQuestionList.addAll(qt.getQuestions());
		}
		return outputQuestionList;
	}
}
