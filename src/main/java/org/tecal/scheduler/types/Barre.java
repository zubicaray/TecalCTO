package org.tecal.scheduler.types;

	public class Barre {
		public String gamme;
		public int idbarre;
		public int vitesseDescente;
		public int vitesseMontee;
		public boolean prioritaire;

		public Barre(int idbarre,String gamme, int vitesseMontee, int vitesseDescente,boolean prio) {
			this.idbarre = idbarre;
			this.gamme = gamme;
			this.vitesseDescente = vitesseDescente;
			this.vitesseMontee = vitesseMontee;
			this.prioritaire=prio;
		}
	}