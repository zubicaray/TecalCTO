package org.tecal.scheduler.types;

public class GammeType {
    public String numgamme;
    public String codezone;   
    public int numligne;
    public int numzone;
    public int idzonebdd;
    public int time;
    public int start;
    public int derive;   
    public int egouttage;
   
    public GammeType(String numgamme,String codezone,int numligne,int numzone,
    		int time,int idzone,int derive,   int egouttage) {
        this.numgamme = numgamme;
        this.codezone = codezone;
        this.numligne = numligne;
        this.numzone = numzone;        
        this.time = time;
        this.idzonebdd = idzone;
        this.derive=derive;
      
        this.egouttage=egouttage;
        
        
      }
  }
