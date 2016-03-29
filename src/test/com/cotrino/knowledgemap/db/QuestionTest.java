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
package test.com.cotrino.knowledgemap.db;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cotrino.knowledgemap.db.Question;

public class QuestionTest {

	final static Logger logger = LoggerFactory.getLogger(QuestionTest.class);

	@Test
	public void questionGeneratorTest() throws IOException {

		String article = this.getArticle("article.txt");
		List<String> sentences = Question.tokenize(article, "en", "US");
		
		for(String sentence : sentences) {
			logger.debug(sentence);
		}
		logger.debug("Found "+sentences.size()+" sentences.");
		assertTrue(sentences.size() == 30);

	}
	
	private String getArticle(String file) throws IOException {
		InputStream stream = this.getClass().getResourceAsStream(file);
		StringWriter writer = new StringWriter();
		IOUtils.copy(stream, writer, "UTF-8");
		return writer.toString();
	}
}
