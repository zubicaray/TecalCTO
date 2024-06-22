package org.tecal.scheduler.types;


public class ZoneType {
	
	public String codezone;
    public int numzone;
    public int cumul;
    public int idzonebdd;
    public int derive;
    public int idPosteDeb;
    public int idPosteFin;
   
    public ZoneType(int numzone,String codezone,int cumul,int derive,int idzone, int idPosteDeb, int idPosteFin) {
        this.codezone = codezone;
        this.numzone = numzone;
        this.cumul = cumul;
        this.idzonebdd = idzone;
        this.derive=derive;
        this.idPosteDeb=idPosteDeb;
        this.idPosteFin=idPosteFin;
        
        
        
      }
    
  }
