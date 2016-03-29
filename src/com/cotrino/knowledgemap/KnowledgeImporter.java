/**
 *  This importer is a patched version of Mirko Nasato's Graphipedia.
 *  Original code: Copyright (c) 2012 Mirko Nasato
 *  https://github.com/mirkonasato/graphipedia
 */
package com.cotrino.knowledgemap;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.graphipedia.dataimport.ExtractLinks;
import org.graphipedia.dataimport.neo4j.ImportGraph;

/**
 * Class to import Wikipedia content into a Neo4j graph database.
 * @author cotrino
 *
 */
public class KnowledgeImporter {

	private final static String SOURCE_WIKIPEDIA_XML = "./data/simplewiki-latest-pages-articles.xml";
	private final static String EXTRACTED_WIKIPEDIA_XML = "./data/simplewiki-links.xml";
	private final static String NEO4J_DATABASE = "./database/data/graphipedia.db";
	
	private final static boolean EXTRACT_LINKS = true;
	private final static boolean IMPORT_GRAPH = true;
	
	public static void main(String[] args) throws Exception {

		if( EXTRACT_LINKS ) {
			File targetXml = new File(EXTRACTED_WIKIPEDIA_XML);
			targetXml.delete();
			ExtractLinks self = new ExtractLinks();
	        self.extract(SOURCE_WIKIPEDIA_XML, EXTRACTED_WIKIPEDIA_XML);
		}
        
		if( IMPORT_GRAPH ) {
			File databaseDirectory = new File(NEO4J_DATABASE);
			FileUtils.deleteDirectory(databaseDirectory);
	        ImportGraph importer = new ImportGraph(NEO4J_DATABASE);
	        importer.createNodes(EXTRACTED_WIKIPEDIA_XML);
	        importer.createRelationships(EXTRACTED_WIKIPEDIA_XML);
	        importer.finish();
		}
        
		System.out.println("Finished!");
		
	}

}
