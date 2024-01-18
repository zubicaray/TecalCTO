package org.tecal.scheduler;

class GammeType {
    String numgamme;
    String codezone;   
    int numligne;
    int numzone;
    int idzonebdd;
    int time;
    int start;
    int derive;   
    int nonChevauchementPont;
    int cumul;
    GammeType(String numgamme,String codezone,int numligne,int numzone,
    		int time,int idzone,int derive,   int nonChevauchementPont,int cumul) {
        this.numgamme = numgamme;
        this.codezone = codezone;
        this.numligne = numligne;
        this.numzone = numzone;        
        this.time = time;
        this.idzonebdd = idzone;
        this.derive=derive;
        this.nonChevauchementPont=nonChevauchementPont;
        this.cumul=cumul;
        
      }
  }