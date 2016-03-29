/**
 *  This is a patched version of Mirko Nasato's Graphipedia.
 *  https://github.com/mirkonasato/graphipedia
 *  Modifications: 2016 José Miguel Cotrino Benavides
 */
package org.graphipedia.dataimport;

import java.io.IOException;
import java.io.Writer;

import org.apache.commons.lang3.StringEscapeUtils;

import be.devijver.wikipedia.Visitor;
import be.devijver.wikipedia.html.HtmlEncoder;
import be.devijver.wikipedia.parser.ast.AttributeList;

public class PlainTextVisitor implements Visitor {

	private class Output {

		private final boolean flush;
		private Writer writer;

		private Output(Writer writer, boolean flush) {
			this.writer = writer;
			this.flush = flush;
		}

		private void append(String s) {
			try {
				writer.append(s);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		private void finished() {
			writer = null;
		}

		private void flush() {
			if (!flush)
				return;
			try {
				writer.flush();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private static class SimpleCharacterEncoder implements HtmlEncoder {

		public String encode(String s) {
			String result = StringEscapeUtils.unescapeHtml3(s);
			return StringEscapeUtils.escapeHtml3(result);
		}

	}

	private String blockingOutput = "";
	private int caption = 0;
	protected final HtmlEncoder characterEncoder;
	protected final Output output;

	public PlainTextVisitor(Writer writer) {
		this(writer, false);
	}

	public PlainTextVisitor(Writer writer, boolean flush) {
		this(writer, new SimpleCharacterEncoder(), flush);
	}

	public PlainTextVisitor(Writer writer, HtmlEncoder characterEncoder, boolean flush) {
		this.output = new Output(writer, flush);
		this.characterEncoder = characterEncoder;
	}

	public void endBold() {
	}

	public void endCaption() {
	}

	public void endDocument() {
		output.flush();
		output.finished();
	}

	public void endHeading1() {
		output.flush();
		if (blockingOutput.equals("h1")) {
			blockingOutput = "";
		}
	}

	public void endHeading2() {
		output.flush();
		if (blockingOutput.equals("h2")) {
			blockingOutput = "";
		}
	}

	public void endHeading3() {
		output.flush();
		if (blockingOutput.equals("h3")) {
			blockingOutput = "";
		}
	}

	public void endHeading4() {
		output.flush();
		if (blockingOutput.equals("h4")) {
			blockingOutput = "";
		}
	}

	public void endHeading5() {
		output.flush();
		if (blockingOutput.equals("h5")) {
			blockingOutput = "";
		}
	}

	public void endHeading6() {
		output.flush();
		if (blockingOutput.equals("h6")) {
			blockingOutput = "";
		}
	}

	public void endIndent() {
		output.flush();
		if (blockingOutput.equals("quote")) {
			blockingOutput = "";
		}
	}

	public void endItalics() {
	}

	public void endLiteral() {
		output.flush();
	}

	public void endNormalLinkWithCaption() {
		this.handleString("]]");
		this.caption --;
		//System.out.println("Caption: "+this.caption);
		if (blockingOutput.equals("caption") && this.caption == 0) {
			blockingOutput = "";
		}
	}

	public void endOrderedList() {
		output.flush();
		if (blockingOutput.equals("ol")) {
			blockingOutput = "";
		}
	}

	public void endOrderedListItem() {
		if (blockingOutput.equals("li")) {
			blockingOutput = "";
		}
	}

	public void endParagraph() {
		output.flush();
	}

	public void endPre() {
		if (blockingOutput.equals("pre")) {
			blockingOutput = "";
		}
	}

	public void endSmartLinkWithCaption() {
		this.handleString("]]");
		this.caption --;
		//System.out.println("Caption: "+this.caption);
		if (blockingOutput.equals("caption") && this.caption == 0) {
			blockingOutput = "";
		}
	}

	public void endTable() {
		if (blockingOutput.equals("table")) {
			blockingOutput = "";
		}
	}

	public void endTableData() {
	}

	public void endTableHeader() {
	}

	public void endTableRecord() {
	}

	public void endUnorderedList() {
		output.flush();
		if (blockingOutput.equals("ul")) {
			blockingOutput = "";
		}
	}

	public void endUnorderedListItem() {
		if (blockingOutput.equals("li")) {
			blockingOutput = "";
		}
	}

	public void handleNormalLinkWithoutCaption(String string) {
		this.handleString("[["+string+"]]");
	}

	public void handleNowiki(String nowiki) {
		output.append(nowiki);
	}

	public void handleSmartLinkWithoutCaption(String string) {
		this.handleString("[["+string+"]]");
	}

	public void handleString(String s) {
		boolean containsBoth = s.indexOf("[[") >= 0 && s.indexOf("]]") > 0;
		boolean justClosure = s.equals("]]");
		if( s.indexOf("[[") > 0 && !containsBoth ) {
			String sublink = s.substring(s.indexOf("[[")+2);
			//System.out.println("Sublink start: "+sublink);
			this.startNormalLinkWithCaption(sublink);
		}
		if (blockingOutput.equals("")) {
			//System.out.println("OK <"+this.blockingOutput+"><"+this.caption+"> "+s);
			output.append(characterEncoder.encode(s));
		} else {
			//System.out.println("NO <"+this.blockingOutput+"><"+this.caption+"> "+s);
		}
		if( s.indexOf("]]") > 0 && !justClosure && !containsBoth ) {
			//System.out.println("Sublink end");
			this.endNormalLinkWithCaption();
		}
	}

	public void startBold() {
	}

	public void startCaption(AttributeList captionOptions) {
	}

	public void startDocument() {
	}

	public void startHeading1() {
		if (blockingOutput.equals("")) {
			blockingOutput = "h1";
		}
	}

	public void startHeading2() {
		if (blockingOutput.equals("")) {
			blockingOutput = "h2";
		}
	}

	public void startHeading3() {
		if (blockingOutput.equals("")) {
			blockingOutput = "h3";
		}
	}

	public void startHeading4() {
		if (blockingOutput.equals("")) {
			blockingOutput = "h4";
		}
	}

	public void startHeading5() {
		if (blockingOutput.equals("")) {
			blockingOutput = "h5";
		}
	}

	public void startHeading6() {
		if (blockingOutput.equals("")) {
			blockingOutput = "h6";
		}
	}

	public void startIndent() {
		if (blockingOutput.equals("")) {
			blockingOutput = "quote";
		}
	}

	public void startItalics() {
	}

	public void startLiteral() {
		if (blockingOutput.equals("")) {
			blockingOutput = "pre";
		}
	}

	public void startNormalLinkWithCaption(String string) {
		this.caption ++;
		this.handleString("[[");
	}

	public void startOrderedList() {
		if (blockingOutput.equals("")) {
			blockingOutput = "ol";
		}
	}

	public void startOrderedListItem() {
		if (blockingOutput.equals("")) {
			blockingOutput = "li";
		}
	}

	public void startParagraph() {
	}

	public void startPre() {
		if (blockingOutput.equals("")) {
			blockingOutput = "pre";
		}
	}

	public void startSmartLinkWithCaption(String string) {
		this.caption ++;
		//System.out.println("Caption: "+this.caption);
		if( string.contains(":") ) { // ignore "Category.", "Foto:", "Image:"...
			if (blockingOutput.equals("")) {
				blockingOutput = "caption";
			}
		}
		this.handleString("[["+string+"|");
	}

	public void startTable(AttributeList tableOptions) {
		if (blockingOutput.equals("")) {
			blockingOutput = "table";
		}
	}

	public void startTableData(AttributeList options) {
	}

	public void startTableHeader(AttributeList list) {
	}

	public void startTableRecord(AttributeList rowOptions) {
	}

	public void startUnorderedList() {
		if (blockingOutput.equals("")) {
			blockingOutput = "ul";
		}
	}

	public void startUnorderedListItem() {
		if (blockingOutput.equals("")) {
			blockingOutput = "li";
		}
	}

}
