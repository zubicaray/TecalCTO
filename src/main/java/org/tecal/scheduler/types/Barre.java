package org.tecal.scheduler.types;

	public class Barre {
		public String gamme;
		public String barreNom;
		public int idbarre;
		public int vitesseDescente;
		public int vitesseMontee;
		public boolean prioritaire;

		public Barre(int idbarre,String barreNom,String gamme, int vitesseMontee, int vitesseDescente,boolean prio) {
			this.idbarre = idbarre;
			this.gamme = gamme;
			this.barreNom=barreNom;
			this.vitesseDescente = vitesseDescente;
			this.vitesseMontee = vitesseMontee;
			this.prioritaire=prio;
		}
	}