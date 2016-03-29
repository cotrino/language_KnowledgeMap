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
public class Category extends Entity {

	private String title;
	private Long nodeRank;
	@Relationship(type = "Link", direction = Relationship.OUTGOING)
    private Set<Category> categories;
	
	/**
	 * Constructor just for Neo4j-OGM.
	 */
	public Category() {
		categories = new HashSet<Category>();
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getTitle() {
		return this.title;
	}
	
	public Long getNodeRank() {
		return this.nodeRank;
	}
	
	@Override
	public String toString() {
		return this.title;
	}
	
	@Override
	public boolean equals(Object o) {
		if( o instanceof String ) {
			return ((String)o).equals(title);
		} else if( o instanceof Category ) {
			return ((Category)o).getTitle().equals(title);
		} else {
			return o.equals(title);
		}
	}

}
