/**
 *  This is a patched version of Mirko Nasato's Graphipedia.
 *  https://github.com/mirkonasato/graphipedia
 *  Modifications: 2016 José Miguel Cotrino Benavides
 */
package test.org.graphipedia.dataimport;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.graphipedia.dataimport.LinkExtractor;
import org.junit.Test;

public class LinkExtractorTest {

	@Test
	public void plainTextTest() throws IOException {
		
		String article1 = this.getArticle("article1.txt");
		article1 = LinkExtractor.getPlainText(article1);
		assertTrue(article1.length() > 0);
		
		String article2 = this.getArticle("article2.txt");
		article2 = LinkExtractor.getPlainText(article2);
		assertTrue(article2.length() > 0);

	}

	private String getArticle(String file) throws IOException {
		InputStream stream = this.getClass().getResourceAsStream(file);
		StringWriter writer = new StringWriter();
		IOUtils.copy(stream, writer, "UTF-8");
		return writer.toString();
	}
	
}
