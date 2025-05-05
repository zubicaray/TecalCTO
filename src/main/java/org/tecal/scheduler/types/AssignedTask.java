package org.tecal.scheduler.types;



public class AssignedTask {
	//id OF/GAMME propre à G.OR
	public int barreID;
	//id zone  propre à G.OR
	public int taskID;  
	// numzone de la table ZONE
	public int numzone;
	public long start;
	public long end;
	public int duration;    
	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
	public int finDerive;    
	// offset pour les zones cumul
	public int IdPosteZoneCumul;
	// Ctor
	public AssignedTask(int barreID, int taskID, int numzone,long start, int duration,int derive) {
		this.barreID = barreID;
		this.taskID = taskID;
		this.start = start;
		this.duration = duration;
		this.end = duration+start;
		this.numzone=numzone;
		this.finDerive=derive;
	}
}