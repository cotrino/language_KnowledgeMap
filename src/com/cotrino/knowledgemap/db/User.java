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

import java.util.HashSet;
import java.util.Set;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity
public class User extends Entity {

	private String name;
	@Relationship(type = "Knows", direction = Relationship.OUTGOING)
    private Set<Knows> knows;
	@Relationship(type = "Ignores", direction = Relationship.OUTGOING)
    private Set<Ignores> ignores;
	
	/**
	 * Constructor just for Neo4j-OGM.
	 */
	public User() {
		this.knows = new HashSet<Knows>();
		this.ignores = new HashSet<Ignores>();
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public Set<Knows> getKnown() {
		return this.knows;
	}
	
	public Set<Ignores> getIgnored() {
		return this.ignores;
	}
	
	@Override
	public String toString() {
		return this.name;
	}
	
	@Override
	public boolean equals(Object o) {
		if( o instanceof String ) {
			return ((String)o).equals(name);
		} else if( o instanceof User ) {
			return ((User)o).getName().equals(name);
		} else {
			return o.equals(name);
		}
	}
	
	public String getStatistics(long totalPages) {
		
		long pages = 0, correctAnswers = 0, wrongAnswers = 0;
		for (Knows knownPage : knows) {
			pages++;
			correctAnswers += knownPage.getWeight();
		}
		for (Ignores ignoredPage : ignores) {
			pages++;
			wrongAnswers += ignoredPage.getWeight();
		}
		double seenPagesPercentage = 100.0 * pages/totalPages;
		return "seen pages: " + pages 
				+ ", correct answers: " + correctAnswers 
				+ ", wrong answers: " + wrongAnswers
				+ ", percentage of pages seen: " + String.format("%.2f%%", seenPagesPercentage);
		
	}
	
	public void knows(Page page, boolean correct) {
		
		if( correct ) {
			Knows link = null;
			if( !knows.contains(page) ) {
				link = new Knows(this, page);
				this.knows.add(link);
			} else {
				for(Knows knownPage : knows) {
					if( knownPage.getPage().equals(page) ) {
						link = knownPage;
					}
				}
			}
			link.addWeight();
		} else {
			Ignores link = null;
			if( !ignores.contains(page) ) {
				link = new Ignores(this, page);
				this.ignores.add(link);
			} else {
				for(Ignores ignoredPage : ignores) {
					if( ignoredPage.getPage().equals(page) ) {
						link = ignoredPage;
					}
				}
			}
			link.addWeight();
		}
		
	}
}
