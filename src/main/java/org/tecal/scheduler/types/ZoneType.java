package org.tecal.scheduler.types;

public class ZoneType {
	
	public String codezone;
    public int numzone;
    public int cumul;
    public int idzonebdd;
    public  int derive;
   
    public ZoneType(int numzone,String codezone,int cumul,int derive,int idzone) {
        this.codezone = codezone;
        this.numzone = numzone;
        this.cumul = cumul;
        this.idzonebdd = idzone;
        this.derive=derive;
        
      }
    
  }
