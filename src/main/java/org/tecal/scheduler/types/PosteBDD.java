package org.tecal.scheduler.types;



public class PosteBDD implements Comparable<PosteBDD> {
	
	public int numligne;	
	public String nom;
	
	public PosteBDD(int numligne, String nom) {
	      this.numligne = numligne;	   
	      this.nom = nom;
	    }
	
	@Override
	public int compareTo(PosteBDD e) {
		return this.numligne-e.numligne;
	}
	
}