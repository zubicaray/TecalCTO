package org.tecal.scheduler;


import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.IntVar;
import com.google.ortools.sat.IntervalVar;
import com.google.ortools.sat.LinearExpr;

class TaskOrdo {


	IntVar startBDD;
	IntVar endBDD;
	//IntVar derive;
	IntVar arriveePont;
	//interval théorique
	IntervalVar intervalBDD;
	
	//réel , qui intègre les contraintes du pont
	IntVar deb;
	IntVar fin;
	IntervalVar intervalReel;
	IntervalVar finDerive;

	//dérive aux postes entre deux task d'un même job
	//IntervalVar deriveInt;  
	// temps incompresible d'arrivée du pont
	//IntervalVar intArriveePont;
	

	boolean isOverlapable=false;


	TaskOrdo(CpModel model,int horizon,int duration,int inderive,int minDebut,String suffix){
	
		
		// 6 secondes , 15ks => WEIRD ???
		if(minDebut==0) {
			startBDD = model.newIntVar(duration, horizon, "start" + suffix); 
		}
		else {
			startBDD = model.newIntVar(duration, horizon, "start" + suffix); 
		}
		
		//5 secondes , 15ks
		if(minDebut==0) {
			startBDD = model.newIntVar(0, horizon, "start" + suffix); 
		}
		else {
			startBDD = model.newIntVar(duration, horizon, "start" + suffix); 
		}	
		

		
		//53 secondes , 13.8ks
		if(minDebut==0) {
			startBDD = model.newIntVar(0, horizon, "start" + suffix); 
		}
		else {
			startBDD = model.newIntVar(minDebut, horizon, "start" + suffix); 
		}

		//82 secondes , 13.8ks
		/*
		if(minDebut==0) {
			startBDD = model.newIntVar(0, horizon, "start" + suffix); 
		}
		else {
			startBDD = model.newIntVar(0, horizon, "start" + suffix); 
		}
		*/
		
		if(CST.MODE_FAST) {
			startBDD = model.newIntVar(0, horizon, "start" + suffix); 
		}
	
		
		  
		
		     
		endBDD = model.newIntVar(minDebut+duration, horizon, "end" + suffix);
		//derive = model.newIntVar(0, horizon,  "derive_" + suffix);
		arriveePont = model.newIntVar(minDebut+duration, horizon,  "pontArrive_" + suffix);
		intervalBDD = model.newIntervalVar(startBDD, LinearExpr.constant(duration),endBDD, "interval" + suffix);

		
		if(inderive+duration>=CST.TEMPS_ZONE_OVERLAP_MIN){
			isOverlapable=true;
		}		
		
		
		inderive-=CST.TEMPS_MVT_PONT_MIN_JOB;
		inderive=Math.max(CST.TEMPS_MVT_PONT_MIN_JOB,inderive);

		
		//intArriveePont = model.newIntervalVar(endBDD, LinearExpr.constant(CST.TEMPS_MVT_PONT_MIN), arriveePont, "intPontArrive" + suffix);
		
		
		// !!!!!!!!!!!
		// NE PAS ajouter cet interval à la méthode addNoOverlap
		// il sera ajouter mais pourra être réduit à nul si besoin
		// cf model.addLessOrEqual(allTasks.get(nextKey).deb, allTasks.get(prevKey).derive);
		//deriveInt = model.newIntervalVar(arriveePont, LinearExpr.constant(inderive), derive, "derive" + suffix);

		
		//  !!
		// avec un temps d'arrivée au pont de 15 secondes
		// et une dérive théorique 15 secondes minimum
		// cela fait une dérive réelle de 15 secondes au minimum 
		// et une dérive réelle de 30 secondes max pour une dérive théorique nullu


		deb=model.newIntVar(minDebut, horizon, "deb_nooverlap");
		fin=model.newIntVar(minDebut+duration, horizon, "fin_nooverlap");

		model.newIntervalVar(deb,LinearExpr.constant(CST.TEMPS_MVT_PONT_MIN_JOB),startBDD,"");             
		model.newIntervalVar(endBDD, LinearExpr.constant(CST.TEMPS_MVT_PONT_MIN_JOB), arriveePont, "");       
		intervalReel=model.newIntervalVar(
				startBDD,
				model.newIntVar(CST.TEMPS_MVT_PONT_MIN_JOB+duration, CST.TEMPS_MVT_PONT_MIN_JOB+duration+inderive, ""),
				fin,"");
		finDerive=model.newIntervalVar(
				model.newIntVar(minDebut+duration, horizon, ""),
				LinearExpr.constant(CST.TEMPS_MVT_PONT_MIN_JOB),
				fin,"");
		


	}
	


}
