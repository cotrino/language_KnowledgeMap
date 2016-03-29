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
package com.cotrino.knowledgemap;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cotrino.knowledgemap.db.GraphDB;
import com.cotrino.knowledgemap.db.Page;
import com.cotrino.knowledgemap.db.Question;
import com.cotrino.knowledgemap.db.Users;

/**
 * Servlet to offer the KnowledgeMap web service.
 * @author cotrino
 *
 */
public class KnowledgeServlet extends HttpServlet {

	final static Logger logger = LoggerFactory.getLogger(KnowledgeServlet.class);

	private static final long serialVersionUID = 2513550631842146742L;

	private static final int PAGE_SET_SIZE = 10;

	private GraphDB db;
	private Users users;
	private List<Page> pages;
	private Question lastQuestion;

	/**
	 * 1) Ask username. If new, create as node.
	 * 2) Find nodes with high NodeRank and start asking questions around.
	 * 3) For each answer, update a link from user node to Page.
	 * 4) Draw a simplified network with overlapped heat-map.
	 * 
	 */
	public KnowledgeServlet() {

		this.pages = new LinkedList<Page>();
		this.connect();
		
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		if( !this.isConnected() ) {
			logger.error("Database not available!");
			return;
		}
		
		response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter writer = response.getWriter();
		
		String action = request.getParameter("action");
		logger.debug("Action: "+action);
		
		if( action == null ) {
			
			writer.println("");
			
		} else if( action.equals("userList") ) {
			
			response.setContentType("application/json");
			//this.users = new Users(db);
			writer.println(this.users.getUsers()); 
			
		} else if( action.equals("userSelect") ) {
			
			Long userId = Long.parseLong( request.getParameter("userId") );
			String userName = request.getParameter("userName");
			
			response.setContentType("text/html");
			
			if( userId == 0 ) {
			
				writer.println( this.users.addUser(userName) );
				
			} else {
				
				writer.println( this.users.getUser(userId) );
				
			}
			
		} else if( action.equals("ask") ) {

			response.setContentType("text/html");
			while (this.ask() != true);
			writer.println("<h2>Asking about '" + lastQuestion.page.getTitle() + "'</h2>");
			writer.println("<p>Question: " + lastQuestion.question + "</p>");
			writer.println("<p>Your answer?</p><input type='text' id='userAnswer' name='userAnswer'/>");
			writer.println("<button id='sendAnswer' onclick='submitAnswer();'>Send</button>"); 
			
		} else if( action.equals("answer") ) {
			
			response.setContentType("text/html");
			String answer = request.getParameter("answer");
			writer.println("<p>"+this.answer(answer)+"</p>");
			writer.println("<p><button onclick='askQuestion();'>New question</button></p>"); 
			
		} else if( action.equals("pageGraph") ) {
			
			response.setContentType("application/json");
			writer.println(this.pageKnowledge()); 
			
		} else if( action.equals("categoryGraph") ) {
			
			response.setContentType("application/json");
			writer.println(this.categoryKnowledge()); 
			
		} else {

			writer.println("Unknown action "+action);
			
		}
		
	}
	
	private void connect() {
		
		try {
			
			this.db = new GraphDB();
			this.users = new Users(db);
			
		} catch(Exception e) {
			logger.error("Database not accessible!");
			e.printStackTrace();
		}
		
	}
	
	private boolean isConnected() {
		
		return this.db != null;
		
	}
	
	private String pageKnowledge() {
		
		// All knowledge relationships:
		// MATCH ()-[r]->() WHERE type(r)="Knows" OR type(r)="Ignores" RETURN r LIMIT 25
		return this.users.getKnowledgePages();

	}
	
	private String categoryKnowledge() {
		
		return this.users.getKnowledgeCategories();

	}

	private boolean ask() {
		
		while(this.pages.size() == 0) {
			this.loadPages();
		}
		Page page = this.pages.remove(0);
		
		Question question = page.getQuestion();
		if (question != null && question.question != null) {
			this.lastQuestion = question;
			return true;
		} 
		return false;
		
	}
	
	private String answer(String userAnswer) {
		
		String response = "";
		if (lastQuestion.matches(userAnswer)) {
			users.addAnswer(lastQuestion.page,true);
			response = "<p class='correct'>CORRECT!</p>";
		} else {
			users.addAnswer(lastQuestion.page,false);
			response = "<p class='wrong'>WRONG!</p>";
		}
		response += "<p>Expected answer: " + lastQuestion.getExpectedAnswer() + "</p>";
		this.users.save();
		return response;
		
	}

	private void loadPages() {
		
		this.pages = this.db.getPages(PAGE_SET_SIZE);
		logger.debug(pages.size() + " pages for questions found");

	}

	public static void main(String[] args) throws Exception {

		long userId = 193773;
		KnowledgeServlet quiz = new KnowledgeServlet();
		quiz.users.getUser(userId);
		//logger.debug(quiz.pageKnowledge());
		logger.debug(quiz.categoryKnowledge());
		
	}

}
