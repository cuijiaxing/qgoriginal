// Question Generation via Overgenerating Transformations and Ranking
// Copyright (c) 2008, 2009 Carnegie Mellon University.  All Rights Reserved.
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//
// For more information, bug reports, fixes, contact:
//    Michael Heilman
//	  Carnegie Mellon University
//	  mheilman@cmu.edu
//	  http://www.cs.cmu.edu/~mheilman

package edu.cmu.ark;

import java.io.*;
//import java.text.NumberFormat;
import java.util.*;

import edu.cmu.ravio.EnhancedQuestion;
import edu.cmu.ravio.EnhancedQuestionGenerator;
import edu.cmu.ravio.NERQuestionGenerator;
import edu.cmu.ravio.filter.RandomSentenceSelector;
import edu.cmu.ravio.ner.GlobalNER;
import edu.cmu.ravio.parser.Entity;
//import edu.cmu.ark.ranking.WekaLinearRegressionRanker;
import edu.stanford.nlp.trees.Tree;

/**
 * Wrapper class for outputting a (ranked) list of questions given an entire
 * document, not just a sentence. It wraps the three stages discussed in the
 * technical report and calls each in turn (along with parsing and other
 * preprocessing) to produce questions.
 * 
 * This is the typical class to use for running the system via the command line.
 * 
 * Example usage:
 * 
 * java -server -Xmx800m -cp
 * lib/weka-3-6.jar:lib/stanford-parser-2008-10-26.jar:
 * bin:lib/jwnl.jar:lib/commons
 * -logging.jar:lib/commons-lang-2.4.jar:lib/supersense
 * -tagger.jar:lib/stanford-ner-2008-05-07.jar:lib/arkref.jar \
 * edu/cmu/ark/QuestionAsker \ --verbose --simplify --group \ --model
 * models/linear-regression-ranker-06-24-2010.ser.gz \ --prefer-wh --max-length
 * 30 --downweight-pro
 * 
 * @author mheilman@cs.cmu.edu
 * 
 */
public class QuestionAsker {

	public QuestionAsker() {
	}

	/**
	 * @param args
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException {
		System.setErr(new PrintStream(new File("log.txt")));
		QuestionTransducer qt = new QuestionTransducer();
		InitialTransformationStep trans = new InitialTransformationStep();
		QuestionRanker qr = null;

		qt.setAvoidPronounsAndDemonstratives(false);

		// pre-load
		AnalysisUtilities.getInstance();

		String buf;
		Tree parsed;
		boolean printVerbose = false;
		String modelPath = null;

		List<Question> outputQuestionList = new ArrayList<Question>();
		boolean preferWH = false;
		boolean doNonPronounNPC = false;
		boolean doPronounNPC = true;
		Integer maxLength = 1000;
		boolean downweightPronouns = false;
		boolean avoidFreqWords = false;
		boolean dropPro = true;
		boolean justWH = false;
		int questionNumber = 0;
		String inputFileName = "";

		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("--debug")) {
				GlobalProperties.setDebug(true);
			} else if (args[i].equals("--verbose")) {
				printVerbose = true;
			} else if (args[i].equals("--model")) { // ranking model path
				modelPath = args[i + 1];
				i++;
			} else if (args[i].equals("--keep-pro")) {
				dropPro = false;
			} else if (args[i].equals("--downweight-pro")) {
				dropPro = false;
				downweightPronouns = true;
			} else if (args[i].equals("--downweight-frequent-answers")) {
				avoidFreqWords = true;
			} else if (args[i].equals("--properties")) {
				GlobalProperties.loadProperties(args[i + 1]);
			} else if (args[i].equals("--prefer-wh")) {
				preferWH = true;
			} else if (args[i].equals("--just-wh")) {
				justWH = true;
			} else if (args[i].equals("--full-npc")) {
				doNonPronounNPC = true;
			} else if (args[i].equals("--no-npc")) {
				doPronounNPC = false;
			} else if (args[i].equals("--max-length")) {
				maxLength = new Integer(args[i + 1]);
				i++;
			} else if (args[i].equals("--q-num")) {
				questionNumber = new Integer(args[i + 1]);
				++i;
			} else if (args[i].equals("--input-filename")) {
				inputFileName = args[i + 1];
				++i;
			}
		}

		qt.setAvoidPronounsAndDemonstratives(dropPro);
		trans.setDoPronounNPC(doPronounNPC);
		trans.setDoNonPronounNPC(doNonPronounNPC);

		if (modelPath != null) {
			System.err.println("Loading question ranking models from "
					+ modelPath + "...");
			qr = new QuestionRanker();
			qr.loadModel(modelPath);
		}
		long startTime = System.currentTimeMillis();
		Random random = new Random();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(inputFileName)));

			if (GlobalProperties.getDebug())
				System.err.println("\nInput Text:");
			String doc;

			while (true) {
				doc = "";
				buf = "";

				buf = br.readLine();
				if (buf == null) {
					break;
				}
				doc += buf;

				while (br.ready()) {
					buf = br.readLine();
					if (buf == null) {
						break;
					}
					if (buf.matches("^.*\\S.*$")) {
						doc += buf + " ";
					} else {
						doc += "\n";
					}
				}
				if (doc.length() == 0) {
					break;
				}

				List<String> sentencesList = AnalysisUtilities
						.getSentences(doc);
				List<Question> selectedQuestionsList = new ArrayList<Question>();

				/************* initialize global entity here *****************/
				GlobalNER.getAllEntities(doc);
				
				/*************************************************************/

				// select twice as many sentences as questions
				sentencesList = RandomSentenceSelector.selectQuestions(
						sentencesList, Math.min(10 * questionNumber, sentencesList.size()));
				

				for (String singleSentence : sentencesList) {
					outputQuestionList.clear();
					// iterate over each segmented sentence and generate
					// questions
					List<Tree> inputTrees = new ArrayList<Tree>();
					List<String> sentences = new ArrayList<String>();
					sentences.add(singleSentence);
					/********************* key step to generate because question ***********************************/
					EnhancedQuestionGenerator whyGenerator = new EnhancedQuestionGenerator();
					String becauseSentence = whyGenerator.extractBecause(sentences.get(0));
					if(becauseSentence != null){
						sentences.add(becauseSentence);
					}

					for (String sentence : sentences) {
						if (GlobalProperties.getDebug())
							System.err.println("Question Asker: sentence: "
									+ sentence);

						parsed = AnalysisUtilities.getInstance().parseSentence(
								sentence).parse;
						inputTrees.add(parsed);
					}

					if (GlobalProperties.getDebug())
						System.err
								.println("Seconds Elapsed Parsing:\t"
										+ ((System.currentTimeMillis() - startTime) / 1000.0));

					// step 1 transformations
					List<Question> transformationOutput = trans
							.transform(inputTrees);

					// step 2 question transducer
					for (Question t : transformationOutput) {
						if (GlobalProperties.getDebug())
							System.err.println("Stage 2 Input: "
									+ t.getIntermediateTree().yield()
											.toString());
						qt.generateQuestionsFromParse(t);
						outputQuestionList.addAll(qt.getQuestions());
					}

					// remove duplicates
					QuestionTransducer
							.removeDuplicateQuestions(outputQuestionList);

					// step 3 ranking
					if (qr != null) {
						qr.scoreGivenQuestions(outputQuestionList);
						boolean doStemming = true;
						QuestionRanker.adjustScores(outputQuestionList,
								inputTrees, avoidFreqWords, preferWH,
								downweightPronouns, doStemming);
						QuestionRanker.sortQuestions(outputQuestionList, false);
					}

					outputQuestionList = whyGenerator
							.selectQuestion(outputQuestionList);

					/******
					 * if we cannot generate why questions, then try to generate
					 * true/false questions
					 **********/
					
					if (!whyGenerator.hasGenerated()) {
						NERQuestionGenerator nerGenerator = new NERQuestionGenerator();
						outputQuestionList = nerGenerator
								.generateNERQuestion(outputQuestionList);
					}
					
					
					Collections.sort(outputQuestionList);
					Collections.reverse(outputQuestionList);
					
					//System.out.println();
					/*
					if(outputQuestionList.size() > 0 && selectedQuestionsList.size() < questionNumber){
						selectedQuestionsList.add(outputQuestionList.get(Math.abs(random.nextInt(outputQuestionList.size()))));
					}else
						if(selectedQuestionsList.size() == questionNumber){
							break;
						}
					*/
					
					//add the highest rank one
					if(outputQuestionList.size() > 0 && selectedQuestionsList.size() < questionNumber){
						selectedQuestionsList.add(outputQuestionList.get(0));
					}else
						if(selectedQuestionsList.size() == questionNumber){
							break;
						}
				}
				
				
				
				
				// now print the questions
				// double featureValue;
				for (Question question : selectedQuestionsList) {
					if (question.getTree() != null
							&& question.getTree().getLeaves().size() > maxLength) {
						continue;
					}
					if (justWH && question.getFeatureValue("whQuestion") != 1.0) {
						continue;
					}
					System.out.print(question.yield() + "\t");
					Tree ansTree = question.getAnswerPhraseTree();
					if (ansTree != null) {
						System.out.print(AnalysisUtilities
								.getCleanedUpYield(question
										.getAnswerPhraseTree()));
					} else {
						if (question instanceof EnhancedQuestion) {
							System.out.print(question.toString());
						} else {
							System.out
									.print(AnalysisUtilities
											.getCleanedUpYield(question
													.getSourceTree()));
						}
					}

					System.out.println();
				}
				
			}

			if (GlobalProperties.getDebug())
				System.err.println("Seconds Elapsed Total:\t"
						+ ((System.currentTimeMillis() - startTime) / 1000.0));
			// prompt for another piece of input text
			if (GlobalProperties.getDebug())
				System.err.println("\nInput Text:");

			br.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void printFeatureNames() {
		List<String> featureNames = Question.getFeatureNames();
		for (int i = 0; i < featureNames.size(); i++) {
			if (i > 0) {
				System.out.print("\n");
			}
			System.out.print(featureNames.get(i));
		}
		System.out.println();
	}

}
