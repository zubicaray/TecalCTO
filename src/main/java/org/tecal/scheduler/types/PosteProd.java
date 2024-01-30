package org.tecal.scheduler.types;



public class PosteProd implements Comparable<PosteProd>{
	public int[] arrMinutes;
	public int numposte;
	public int numligneBDD;
	public int start;
	public int stop;
	public String nom;
	
	public PosteProd(int numposte,int numligne, String nom,int start, int stop) {
	      this.numposte = numposte;
	      this.numligneBDD = numligne;
	      this.stop = stop;
	      this.start = start;
	      this.nom = nom;
	    }
	

	@Override
	public int compareTo(PosteProd e) {
		return this.start-e.start;
	}
	
}