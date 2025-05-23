package org.tecal.scheduler.types;

public class ElementGamme implements Cloneable {
    public String numgamme;
    public String codezone;   
    public int numligne;
    public int numzone;
    public int idzonebdd;
    public int duree;
    public int start;
    public int derive;   
    public int egouttage;
    public boolean BloquePont;
   
    public ElementGamme(String numgamme,String codezone,int numligne,int numzone,
    		int duree,int idzone,int derive,   int egouttage) {
        this.numgamme = numgamme;
        this.codezone = codezone;
        this.numligne = numligne;
        this.numzone = numzone;        
        this.duree = duree;
        this.idzonebdd = idzone;
        this.derive=derive;
      
        this.egouttage=egouttage;
        
        
      }
    // Implémentation de la méthode clone
    @Override
    public ElementGamme clone() {
        try {
            // Appel de super.clone() pour une copie superficielle
            return (ElementGamme) super.clone();
        } catch (CloneNotSupportedException e) {
            // Cette exception ne devrait pas se produire si la classe implémente Cloneable
            throw new AssertionError("Clonage non supporté", e);
        }
    }
  }
