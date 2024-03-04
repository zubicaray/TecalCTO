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
	IntVar debutDerive;
	IntVar finDerive;
	IntervalVar intervalReel;
	IntervalVar minimumDerive;
	int tempsDeplacement;
	

	boolean isOverlapable=false;


	TaskOrdo(CpModel model,int horizon,int duration,int inderive,int minDebut,int intempsDeplacement,String suffix){
	
	
		tempsDeplacement=intempsDeplacement;

		
		if(inderive+duration>=CST.TEMPS_ZONE_OVERLAP_MIN){
			isOverlapable=true;
		}		
		
			
		startBDD = model.newIntVar(0, horizon, "start" + suffix); 
		endBDD = model.newIntVar(0, horizon, "end" + suffix);		
		fin=model.newIntVar(0, horizon, "fin_nooverlap");
		debutDerive=model.newIntVar(0, horizon, "fin_nooverlap");
		finDerive=model.newIntVar(0, horizon, "fin_nooverlap");
		
		      
		intervalBDD = model.newIntervalVar(startBDD, LinearExpr.constant(duration),endBDD, "interval" + suffix);
		  
		intervalReel=model.newIntervalVar(
				startBDD,
				LinearExpr.constant(duration+inderive+tempsDeplacement),
				fin,"");
		
		model.newIntervalVar(
				finDerive,
				LinearExpr.constant(tempsDeplacement),
				fin,"");
		
		
		minimumDerive=model.newIntervalVar(
				endBDD,
				LinearExpr.constant(tempsDeplacement),
				debutDerive,"");
		

	}
	


}
