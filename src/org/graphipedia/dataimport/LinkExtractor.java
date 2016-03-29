//
// Copyright (c) 2012 Mirko Nasato
//
// Permission is hereby granted, free of charge, to any person obtaining a
// copy of this software and associated documentation files (the "Software"),
// to deal in the Software without restriction, including without limitation
// the rights to use, copy, modify, merge, publish, distribute, sublicense,
// and/or sell copies of the Software, and to permit persons to whom the
// Software is furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included
// in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
// THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
// OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
// ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
// OTHER DEALINGS IN THE SOFTWARE.
//
/**
 *  This is a patched version of Mirko Nasato's Graphipedia.
 *  https://github.com/mirkonasato/graphipedia
 *  Modifications: 2016 José Miguel Cotrino Benavides
 */
package org.graphipedia.dataimport;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import be.devijver.wikipedia.Visitor;
import be.devijver.wikipedia.parser.ast.Document;
import be.devijver.wikipedia.parser.ast.parser.DefaultASTParser;
import be.devijver.wikipedia.parser.wikitext.MarkupParser;

public class LinkExtractor extends SimpleStaxParser {

	private static final Pattern LINK_PATTERN = Pattern.compile("\\[\\[(.+?)\\]\\]");
	private static final Pattern CATEGORIES_PATTERN = Pattern.compile("(?im)\\s*(\\[\\[Category:.+\\]\\])+\\s*$");
	private static final Pattern CATEGORY_PATTERN = Pattern.compile("\\[\\[Category:([^\\|]+)\\|*.*\\]\\]");

	private final XMLStreamWriter writer;
	private final ProgressCounter pageCounter = new ProgressCounter();

	private String title;
	private String text;

	public LinkExtractor(XMLStreamWriter writer) {
		super(Arrays.asList("page", "title", "text"));
		this.writer = writer;
	}

	public int getPageCount() {
		return pageCounter.getCount();
	}

	@Override
	protected void handleElement(String element, String value) {
		if ("page".equals(element)) {
			if (!title.contains(":")) {
				try {
					writePage(title, text);
				} catch (XMLStreamException streamException) {
					throw new RuntimeException(streamException);
				}
			} else if (title.contains("Category:")) {
				title = title.substring(title.indexOf(':') + 1);
				try {
					writeCategory(title, text);
				} catch (XMLStreamException streamException) {
					throw new RuntimeException(streamException);
				}
				// System.out.println(title);
			}
			title = null;
			text = null;
		} else if ("title".equals(element)) {
			title = value;
		} else if ("text".equals(element)) {
			text = value;
		}
	}

	private void writePage(String title, String text) throws XMLStreamException {
		writer.writeStartElement("p");

		writer.writeStartElement("t");
		writer.writeCharacters(title);
		writer.writeEndElement();

		
		if (text != null) {
			String plainText = getPlainText(text);
			writer.writeStartElement("text");
			writer.writeCharacters(plainText);
			writer.writeEndElement();

			// remove special text of the kind {{ ... }}
			text = text.replaceAll("\\s*\\{\\{.+\\}\\}\\s*", "");
			
			// parse categories
			Set<String> categories = parseCategories(text);
			for (String category : categories) {
				writer.writeStartElement("c");
				writer.writeCharacters(category);
				writer.writeEndElement();
			}

			// parse links
			Set<String> links = parseLinks(text);
			links.remove(title);
			for (String link : links) {
				writer.writeStartElement("l");
				writer.writeCharacters(link);
				writer.writeEndElement();
			}

		}

		writer.writeEndElement();

		pageCounter.increment();
	}

	private void writeCategory(String title, String text) throws XMLStreamException {
		writer.writeStartElement("p");

		writer.writeStartElement("q");
		writer.writeCharacters(title);
		writer.writeEndElement();

		if (text != null) {
			// remove special text of the kind {{ ... }}
			text = text.replaceAll("\\s*\\{\\{.+\\}\\}\\s*", "");

			// parse categories
			Set<String> categories = parseCategories(text);
			categories.remove(title);
			for (String category : categories) {
				writer.writeStartElement("c");
				writer.writeCharacters(category);
				writer.writeEndElement();
			}

			// parse links
			Set<String> links = parseLinks(text);
			links.remove(title);
			for (String link : links) {
				writer.writeStartElement("l");
				writer.writeCharacters(link);
				writer.writeEndElement();
			}
		}

		writer.writeEndElement();

		pageCounter.increment();
	}

	private Set<String> parseCategories(String text) {
		Set<String> categories = new HashSet<String>();
		if (text != null) {
			Matcher matcher = CATEGORIES_PATTERN.matcher(text);
			while (matcher.find()) {
				String link = matcher.group(1);
				Matcher matcher2 = CATEGORY_PATTERN.matcher(link);
				if (matcher2.find()) {
					categories.add(matcher2.group(1));
				}
			}
		}
		return categories;
	}

	private Set<String> parseLinks(String text) {
		Set<String> links = new HashSet<String>();
		if (text != null) {
			Matcher matcher = LINK_PATTERN.matcher(text);
			while (matcher.find()) {
				String link = matcher.group(1);
				if (!link.contains(":")) {
					if (link.contains("|")) {
						link = link.substring(0, link.lastIndexOf('|'));
					}
					links.add(link);
				}
			}
		}
		return links;
	}

	/**
	 * Extract plain text from a wikipedia article, removing lists, tables, etc.
	 * @param text
	 * @return
	 */
	public static String getPlainText(String text) {
		StringWriter sw = new StringWriter();
		Visitor visitor = new PlainTextVisitor(sw, true);
		if (visitor != null) {
			try {
				Document document = new MarkupParser(text).parseDocument();
				DefaultASTParser parser = new DefaultASTParser(document);
				parser.parse(visitor);
			} catch (Exception e) {
				// System.err.println("Error: "+e);
			}
		}
		return sw.toString();
	}

}
