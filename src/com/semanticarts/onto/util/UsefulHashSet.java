/**
 * 
 */
package com.semanticarts.onto.util;

import java.io.BufferedReader;
import java.util.HashSet;

/**
 * @author thill
 *
 */
public class UsefulHashSet {
	HashSet<String> UsefulEntities;

	/**
	 * The constructor reads lines from br. Each line contains the prefixed name of an entity to be
	 * considered "useful". It loads the names into a hash set for later reference.
	 */
	public UsefulHashSet(BufferedReader br) {
		try {
			UsefulEntities= new HashSet<String>();

			String line;
			while((line= br.readLine()) != null) {
				UsefulEntities.add(line);
			}
			br.close();
		}
		catch(Exception e) {
	         e.printStackTrace();
		}
	}

	public boolean isUseful(String Entity) {
		return UsefulEntities.contains(Entity);
	}

}
