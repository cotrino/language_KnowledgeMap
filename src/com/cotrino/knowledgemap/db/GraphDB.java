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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.service.Components;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONException;
import org.neo4j.ogm.json.JSONObject;

public class GraphDB {

	final static Logger logger = LoggerFactory.getLogger(GraphDB.class);

	private final static String NEO4J_DATABASE_URI = "http://neo4j:neo4jneo4j@localhost:7474";
	private final static int MAX_CATEGORY_DEPTH = 4;

	private Long numPages;
	private Long maxNodeRank;
	private Long minId;
	private Long maxId;
	private Session session;

	public GraphDB() {

		Configuration configuration = Components.configuration();

		configuration.driverConfiguration().setDriverClassName("org.neo4j.ogm.drivers.http.driver.HttpDriver")
				.setURI(NEO4J_DATABASE_URI);

		SessionFactory sessionFactory = new SessionFactory(configuration, "com.cotrino.knowledgemap.db");
		this.session = sessionFactory.openSession();

		this.numPages = this.getNumber("MATCH (n:Page) RETURN COUNT(n) AS numPages");
		logger.debug("Amount of pages: " + numPages);
		this.maxNodeRank = this.getNumber("MATCH (n:Page) RETURN MAX(n.nodeRank) AS maxRank");
		logger.debug("Max node rank: " + maxNodeRank);
		this.minId = this.getNumber("MATCH (n:Page) RETURN MIN(id(n)) AS minId");
		logger.debug("Min ID: " + minId);
		this.maxId = this.getNumber("MATCH (n:Page) RETURN MAX(id(n)) AS maxId");
		logger.debug("Max ID: " + maxId);
		
	}

	public Set<User> getAllUsers() {

		Set<User> users = new HashSet<User>();
		Collection<User> userList = this.session.loadAll(User.class);
		for (User user : userList) {
			users.add(user);
		}
		return users;

	}

	public User getUser(Long id) {

		User user = this.session.load(User.class, id, 1);
		return user;

	}

	public String getUserKnownPages(Long userId) {

		User user = this.session.load(User.class, userId, 2);

		String jsonText = "";
		try {

			JSONArray nodes = new JSONArray();
			JSONArray edges = new JSONArray();

			JSONObject message = new JSONObject();
			Set<Knows> knows = user.getKnown();
			Set<Ignores> ignores = user.getIgnored();

			// get all known or ignored pages
			HashMap<Page, Integer> pages = new HashMap<Page, Integer>();
			for (Knows know : knows) {
				pages.put(know.getPage(), know.getWeight());
			}
			for (Ignores ignore : ignores) {
				int weight = -ignore.getWeight();
				if (pages.containsKey(ignore.getPage())) {
					weight += pages.get(ignore.getPage());
				}
				pages.put(ignore.getPage(), weight);
			}

			// add all pages as nodes
			HashMap<Page, Integer> pageIndex = new HashMap<Page, Integer>();
			for (Page page : pages.keySet()) {

				JSONObject node = new JSONObject();
				node.put("id", page.getId());
				node.put("name", page.getTitle());
				node.put("weight", pages.get(page));
				node.put("rank", page.getNodeRank());
				nodes.put(node);
				pageIndex.put(page, nodes.length() - 1);

			}

			// add edges between printed pages
			for (Page page : pages.keySet()) {

				// logger.debug("Links in page '"+page.getTitle()+"':
				// "+page.getLinks().size());

				for (Page linkedPage : page.getLinks()) {
					if (pageIndex.containsKey(linkedPage)) {
						JSONObject edge = new JSONObject();
						edge.put("source", pageIndex.get(page));
						edge.put("target", pageIndex.get(linkedPage));
						edges.put(edge);
					}
				}

			}

			message.put("nodes", nodes);
			message.put("edges", edges);

			jsonText = message.toString();
			System.out.println("Nodes: " + nodes.length());
			System.out.println("Edges: " + edges.length());
			// logger.debug(jsonText);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return jsonText;
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	public String getUserKnownCategories(Long userId) {

		String jsonText = "";
		Map<String, String> parameters = new HashMap<String, String>();
		Result result = this.session.query("MATCH (u:User)-[k]->(n:Page) WHERE id(u)=" + userId + " WITH n,k "
				+ "MATCH p=shortestPath((a:Category)<-[r:In_Category*]-(n)) "
				+ "WHERE a.title='Articles' RETURN nodes(p) AS path,"
				+ "type(k) AS knowledgetype,k.weight as knowledgeweight", parameters, true);
		Iterable<Map<String, Object>> results = result.queryResults();

		JSONObject rootNode = null;
		Map<Long, JSONObject> categories = new HashMap<Long, JSONObject>();
		Map<Long, Integer> categoryWeights = new HashMap<Long, Integer>();

		try {

			for (Map<String, Object> map : results) {

				String knowledgeType = (String) map.get("knowledgetype");
				Integer knowledgeWeight = (Integer) map.get("knowledgeweight");
				@SuppressWarnings("unchecked")
				List<Object> path = (List<Object>) map.get("path");
				JSONObject parentNode = null;
				int depth = 0;
				for (Object o : path) {
					if (o instanceof Category) {

						Category c = (Category) o;
						Long cid = c.getId();

						/**
						 * Calculate category weight as the sum of all contained
						 * known (positive) and ignored (negative) pages.
						 */
						int weight = 0;
						if (categoryWeights.containsKey(cid)) {
							weight = categoryWeights.get(cid);
						}
						if (knowledgeType.equals("Knows")) {
							weight += knowledgeWeight;
						} else if (knowledgeType.equals("Ignores")) {
							weight -= knowledgeWeight;
						}
						categoryWeights.put(cid, weight);

						/**
						 * Build a hierarchical JSON tree, nodes containing
						 * nodes.
						 */
						JSONObject node = null;
						if (!categories.containsKey(cid)) {
							node = new JSONObject();
							node.put("id", cid);
							node.put("name", c.getTitle());
							node.put("rank", c.getNodeRank());
							node.put("children", new JSONArray());
							categories.put(cid, node);
							if (parentNode != null) {
								parentNode.getJSONArray("children").put(node);
							} else {
								rootNode = node;
							}
						} else {
							node = categories.get(cid);
						}
						node.put("weight", weight);
						// logger.debug("Category "+c.getTitle()+" => "+weight);

						parentNode = node;
						depth++;

					} else if (o instanceof Page) {

						// Page c = (Page) o;
						// logger.debug("Page: "+c.getTitle()+",
						// "+knowledgeType+" "+knowledgeWeight);

					} else {
						logger.error("Object " + o.getClass() + " unknown!");
					}
					if (depth >= MAX_CATEGORY_DEPTH) {
						break;
					}
					// logger.debug(key+": "+path.get(key).getClass());
				}

			}
			logger.debug("Found " + categories.size() + " categories");

			jsonText = rootNode.toString();

		} catch (JSONException e) {
			logger.error(e.toString());
		}

		return jsonText;

	}

	public List<Page> getPages(long limit) {

		StringJoiner ids = new StringJoiner(",");
		for (int i = 0; i < limit; i++) {
			long id = (long) (Math.random() * (maxId - minId) + minId);
			ids.add(id + "");
		}

		List<Page> pages = new LinkedList<Page>();
		Map<String, String> parameters = new HashMap<String, String>();
		Iterable<Page> pageList = this.session.query(Page.class,
				"MATCH (n:Page) WHERE id(n) IN [" + ids.toString() + "] RETURN n", parameters);
		for (Page page : pageList) {
			if (page.isPageValidForQuestioning()) {
				pages.add(page);
				// logger.debug(page.getTitle());
			}
		}
		return pages;

	}

	public User createUser(String name) {

		User user = new User();
		user.setName(name);
		this.session.save(user);
		return user;

	}

	public void updateUser(User user) {

		this.session.save(user);

	}

	public Long getNumber(String query) {

		Long value = null;
		Map<String, String> parameters = new HashMap<String, String>();
		value = this.session.queryForObject(Long.class, query, parameters);
		return value;

	}

	public Long getAmountOfPages() {

		return this.numPages;

	}

}
