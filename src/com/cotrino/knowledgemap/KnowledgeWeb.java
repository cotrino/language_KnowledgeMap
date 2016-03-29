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

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * KnowledgeMap is an application to understand the scope of your own knowledge
 * based on Wikipedia content.
 * 
 * Main class to start the web server Jetty and embed our servlet.
 * @author cotrino
 *
 */
public class KnowledgeWeb {

	final static Logger logger = LoggerFactory.getLogger(KnowledgeWeb.class);
	
	private static final int port = 8080;

	public static void main(String[] args) throws Exception {

		Server server = new Server(port);

		ServletHandler serverHandler = new ServletHandler();
		serverHandler.addServletWithMapping(KnowledgeServlet.class, "/*");

		ResourceHandler resourceHandler = new ResourceHandler();
		resourceHandler.setDirectoriesListed(true);
		resourceHandler.setWelcomeFiles(new String[] { "client.html" });
		resourceHandler.setResourceBase("./client");

		HandlerList handlers = new HandlerList();
		handlers.setHandlers(new Handler[] { resourceHandler, serverHandler, new DefaultHandler() });

		server.setHandler(handlers);

		logger.debug("Starting KnowledgeMap server at http://localhost:" + port + "/");
		server.start();
		server.join();

	}

}