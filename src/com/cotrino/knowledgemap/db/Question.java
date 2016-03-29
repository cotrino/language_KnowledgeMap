/**
 *  Copyright (C) 2016 José Miguel Cotrino Benavides
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.cotrino.knowledgemap.db;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;

public class Question {

	private static final Pattern LINK_PATTERN = Pattern.compile("\\[\\[(.+?)\\]\\]");
	private static final int QUESTION_SENTENCES = 3;

	public Page page;
	public String question;
	public List<String> answers = new LinkedList<String>();
	
	public Question(Page page) {
		String realText = StringEscapeUtils.unescapeHtml3(page.getText());
		List<String> rawSentences = Question.tokenize(realText, "en", "US");
		if (rawSentences.size() >= QUESTION_SENTENCES) {
			//System.out.println("Asking about '" + title + "'");
			generateQuestion(page, rawSentences);
		}
	}
	
	public boolean matches(String userAnswer) {
		
		for(String answer : answers) {
			if( answer.toLowerCase().equals(userAnswer.toLowerCase()) ) {
				return true;
			}
		}
		return false;
		
	}
	
	public String getExpectedAnswer() {
		
		StringJoiner sj = new StringJoiner(", ");
		for(String answer : answers) {
			sj.add("'"+answer+"'");
		}
		return sj.toString();
		
	}
	
	private void generateQuestion(Page page, List<String> sentences) {

		int firstSentence = (int) Math.round(Math.random() * (sentences.size() - QUESTION_SENTENCES));
		String question = "";
		for (int i = firstSentence; i < firstSentence + QUESTION_SENTENCES; i++) {
			question += sentences.get(i);
		}
		Matcher matcher = LINK_PATTERN.matcher(question);
		List<Integer> matchStart = new LinkedList<Integer>();
		List<Integer> matchEnd = new LinkedList<Integer>();
		List<String> matchLink = new LinkedList<String>();
		while (matcher.find()) {
			String link = matcher.group(1);
			if (!link.contains(":")) {
				matchStart.add(matcher.start());
				matchEnd.add(matcher.end());
				matchLink.add(link);
			}
		}
		if (matchLink.size() > 0) {
			int linkToBeReplaced = (int) Math.round(Math.random() * (matchLink.size() - 1));
			int start = matchStart.get(linkToBeReplaced);
			int end = matchEnd.get(linkToBeReplaced);
			String link = matchLink.get(linkToBeReplaced);
			String[] answers = link.split("\\|");
			for(String answer : answers) {
				this.answers.add(answer);
			}
			String placeholder = link.replaceAll("[^ \\|]", ".");
			if( placeholder.contains("|") ) {	
				placeholder = placeholder.split("\\|")[1];
			}
			String text = question.substring(0, start) + "<span class='placeholder'>"+placeholder+"</span>"
					+ question.substring(end);
			// replace other links of the kind [[A|B]] with A 
			text = text.replaceAll("\\[\\[[^\\]\\|]+\\|", "");
			// replace other links of the kind [[A]] with A
			text = text.replaceAll("\\[\\[", "").replaceAll("\\]\\]", "");
			//System.out.println("Question: "+text);
			this.question = text;
			this.page = page;
		}

	}
	
	/**
	 * http://stackoverflow.com/questions/2103598/java-simple-sentence-parser
	 * @param text
	 * @param language
	 * @param country
	 * @return
	 */
	public static List<String> tokenize(String text, String language, String country){
	    List<String> sentences = new ArrayList<String>();
	    Locale currentLocale = new Locale(language, country);
	    BreakIterator sentenceIterator = BreakIterator.getSentenceInstance(currentLocale);      
	    sentenceIterator.setText(text);
	    int boundary = sentenceIterator.first();
	    int lastBoundary = 0;
	    while (boundary != BreakIterator.DONE) {
	        boundary = sentenceIterator.next();         
	        if(boundary != BreakIterator.DONE){
	            sentences.add(text.substring(lastBoundary, boundary));
	        }
	        lastBoundary = boundary;            
	    }
	    return sentences;
	}
}
