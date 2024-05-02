package org.tecal.scheduler.types;



public class AssignedTask {
	//id OF/GAMME propre à G.OR
	public int jobID;
	//id zone  propre à G.OR
	public int taskID;  
	// numzone de la table ZONE
	public int numzone;
	public int start;
	public int end;
	public int duration;    
	public int derive;    
	// offset pour les zones cumul
	public int IdPosteZoneCumul;
	// Ctor
	public AssignedTask(int jobID, int taskID, int numzone,int start, int duration,int derive) {
		this.jobID = jobID;
		this.taskID = taskID;
		this.start = start;
		this.duration = duration;
		this.end = duration+start;
		this.numzone=numzone;
		this.derive=derive;
	}
}