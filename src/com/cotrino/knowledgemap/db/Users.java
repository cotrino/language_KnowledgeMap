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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONException;
import org.neo4j.ogm.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Users {
	
	final static Logger logger = LoggerFactory.getLogger(Users.class);
			
	private GraphDB db;
	private User user;
	private Map<Long, User> users;

	public Users(GraphDB db) {

		this.db = db;

		logger.debug("Getting list of users...");
		this.loadAllUsers();

	}
	
	public String getUser(Long id) {
		
		this.user = this.db.getUser(id);
		logger.debug("Selected user '"+user.getName());
		return this.user.getName()+" ("+this.user.getStatistics(this.db.getAmountOfPages())+")";
		
	}
	
	public String getUsers() {
		
		JSONArray userList = new JSONArray();
		
		for(Long userId : this.users.keySet()) {
			User user = this.users.get(userId);
			try {
				JSONObject userObject = new JSONObject();
				userObject.put("id", userId);
				userObject.put("name", user.getName());
				userList.put(userObject);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		return userList.toString();
		
	}
	
	public void save() {

		this.db.updateUser(user);
		
	}

	public void addAnswer(Page page, boolean correct) {

		this.user.knows(page, correct);

	}
	
	public String getKnowledgePages() {
		
		logger.debug("Loading knowledge of user '"+user.getName()+"'...");
		return this.db.getUserKnownPages(user.getId());
	    		
	}
	
	public String getKnowledgeCategories() {
		
		logger.debug("Loading knowledge of user '"+user.getName()+"'...");
		return this.db.getUserKnownCategories(user.getId());
	    
	}
	
	public String addUser(String userName) {

		logger.debug("Creating user "+userName+" in database...");
		this.user = this.db.createUser(userName);
		return this.user.getName();

	}
	
	private void loadAllUsers() {

		this.users = new HashMap<Long, User>();
		Set<User> usersSet = this.db.getAllUsers();

		for (User user : usersSet) {
			users.put(user.getId(), user);
			logger.debug("\t- " + user.getName());
			//logger.debug("\t- " + user.getName()+", "+user.getStatistics(this.db.getAmountOfPages()));
		}

	}

}
