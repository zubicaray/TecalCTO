package org.tecal.scheduler.types;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.tecal.scheduler.CST;
import org.tecal.scheduler.data.SQL_DATA;

public class Barre {
		private String gamme;
		private String barreNom;
		private int idbarre;
		private int vitesseDescente;
		private int vitesseMontee;
		
		private boolean prioritaire;
		private LocalDateTime mHeureLimite;
		
		private List<ElementGamme>  gammeArray;

		public Barre(int idbarre,String barreNom,String gamme, int vitesseMontee, int vitesseDescente,boolean prio) {
			this.idbarre = idbarre;
			this.gamme = gamme;
			this.barreNom=barreNom;
			this.vitesseDescente = vitesseDescente;
			this.vitesseMontee = vitesseMontee;
			this.prioritaire=prio;
			ArrayList<ElementGamme> source=SQL_DATA.getInstance().getLignesGammesAll().get(gamme);
			
			gammeArray = source.stream()
				    .map(ElementGamme::clone) // Assurez-vous que clone() est défini et public
				    .collect(Collectors.toList());
			
		}
		public String getGamme() {
			return gamme;
		}

		public void setGamme(String gamme) {
			this.gamme = gamme;
		}

		public String getBarreNom() {
			return barreNom;
		}

		public void setBarreNom(String barreNom) {
			this.barreNom = barreNom;
		}

		public int getIdbarre() {
			return idbarre;
		}

		public void setIdbarre(int idbarre) {
			this.idbarre = idbarre;
		}

		public int getVitesseDescente() {
			return vitesseDescente;
		}

		public void setVitesseDescente(int vitesseDescente) {
			this.vitesseDescente = vitesseDescente;
		}

		public int getVitesseMontee() {
			return vitesseMontee;
		}

		public void setVitesseMontee(int vitesseMontee) {
			this.vitesseMontee = vitesseMontee;
		}

		public boolean isPrioritaire() {
			return prioritaire;
		}

		public void setPrioritaire(boolean prioritaire) {
			this.prioritaire = prioritaire;
		}

		public List<ElementGamme> getGammeArray() {
			return gammeArray;
		}

		public void setGammeArray(ArrayList<ElementGamme> gammeArray) {
			this.gammeArray = gammeArray;
		}
		public LocalDateTime getHeureLimite() {
			return mHeureLimite;
		}
		public void setHeureLimite(LocalDateTime mHeureLimite) {
			this.mHeureLimite = mHeureLimite;
		}
		public int getDuree() {
			int duree=0;
			for( ElementGamme g:gammeArray) {
				duree+=g.duree+CST.TEMPS_MVT_PONT;
			}
			return duree;
		}

	}