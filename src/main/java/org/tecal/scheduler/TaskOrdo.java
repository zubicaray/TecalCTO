package org.tecal.scheduler;


import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.IntVar;
import com.google.ortools.sat.IntervalVar;
import com.google.ortools.sat.LinearExpr;

public class TaskOrdo {

	
	IntVar startBDD;
	IntVar endBDD;
	//interval théorique
	IntervalVar intervalBDD;
		
	//réel , qui intègre les contraintes du pont
	//IntVar deb;
	IntVar fin;
	IntVar deriveNulle;
	IntVar deriveMax;
	IntervalVar intervalReel;
	IntervalVar minimumDerive;
	int tempsDeplacement;
	int egouttage;
	

	boolean isOverlapable=false;


	TaskOrdo(CpModel model,int horizon,int duration,int inderive,int minDebut,int intempsDeplacement,int egouttage,String suffix){
	
	
		tempsDeplacement=intempsDeplacement;
		this.egouttage=egouttage;
		
		if(inderive+duration>=CST.TEMPS_ZONE_OVERLAP_MIN){
			isOverlapable=true;
		}		
		
		
		startBDD = model.newIntVar(0, horizon, "start" + suffix); 
		endBDD = model.newIntVar(0, horizon, "end" + suffix);		
		fin=model.newIntVar(0, horizon, "fin_nooverlap");
		deriveNulle=model.newIntVar(0, horizon, "fin_nooverlap");
		deriveMax=model.newIntVar(0, horizon, "fin_nooverlap");
		
		
		      
		intervalBDD = model.newIntervalVar(startBDD, LinearExpr.constant(duration),endBDD, "interval" + suffix);
		  
		intervalReel=model.newIntervalVar(
				startBDD,
				LinearExpr.constant(duration+egouttage+inderive+tempsDeplacement),
				fin,"");
		
		model.newIntervalVar(
				deriveMax,
				LinearExpr.constant(tempsDeplacement+egouttage),
				fin,"");
		
		
		minimumDerive=model.newIntervalVar(
				endBDD,
				LinearExpr.constant(tempsDeplacement+egouttage),
				deriveNulle,"");
		

	}
	


}
