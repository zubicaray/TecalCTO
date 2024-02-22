package org.tecal.scheduler;


import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.IntVar;
import com.google.ortools.sat.IntervalVar;
import com.google.ortools.sat.LinearExpr;

class TaskOrdo {


	IntVar startBDD;
	IntVar endBDD;
	//interval théorique
	IntervalVar intervalBDD;
		
	//réel , qui intègre les contraintes du pont
	IntVar deb;
	IntVar fin;
	IntVar debutDerive;
	IntVar finDerive;
	IntervalVar intervalReel;
	IntervalVar minimumDerive;
	

	boolean isOverlapable=false;


	TaskOrdo(CpModel model,int horizon,int duration,int inderive,int minDebut,String suffix){
	
		
		//TODO : enlever le temps de mvt de pont pour la zone de chargement
		/*
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
		
		if(minDebut==0) {
			startBDD = model.newIntVar(0, horizon, "start" + suffix); 
		}
		else {
			startBDD = model.newIntVar(0, horizon, "start" + suffix); 
		}
		*/
		
		//if(CST.MODE_ECO) {
			
		//}
	
		     
		
		//endBDD = model.newIntVar(0, horizon, "end" + suffix);		

		

		
		if(inderive+duration>=CST.TEMPS_ZONE_OVERLAP_MIN){
			isOverlapable=true;
		}		
		
		
		inderive-=CST.TEMPS_MVT_PONT_MIN_JOB;
		inderive=Math.max(CST.TEMPS_MVT_PONT_MIN_JOB,inderive);

		
		startBDD = model.newIntVar(0, horizon, "start" + suffix); 
		deb=model.newIntVar(0, horizon, "deb_nooverlap");
		endBDD = model.newIntVar(0+0, horizon, "end" + suffix);		
		fin=model.newIntVar(duration, horizon, "fin_nooverlap");
		debutDerive=model.newIntVar(0+0, horizon, "fin_nooverlap");
		finDerive=model.newIntVar(duration+inderive, horizon, "fin_nooverlap");
		
		
		
		
		
		model.newIntervalVar(deb,LinearExpr.constant(CST.TEMPS_MVT_PONT_MIN_JOB),startBDD,"");             
		intervalBDD = model.newIntervalVar(startBDD, LinearExpr.constant(duration),endBDD, "interval" + suffix);
		
		//model.newIntervalVar(endBDD, LinearExpr.constant(CST.TEMPS_MVT_PONT_MIN_JOB), arriveePont, "");       
		intervalReel=model.newIntervalVar(
				deb,
				//model.newIntVar(CST.TEMPS_MVT_PONT_MIN_JOB+duration, CST.TEMPS_MVT_PONT_MIN_JOB+duration+inderive, ""),
				LinearExpr.constant(CST.TEMPS_MVT_PONT_MIN_JOB+duration+inderive),
				fin,"");
		
		model.newIntervalVar(
				finDerive,
				LinearExpr.constant(CST.TEMPS_MVT_PONT_MIN_JOB),
				fin,"");
		
		
		//arriveePontDerive= model.newIntVar(0, horizon, "");
		//model.newIntervalVar(arriveePontDerive,				LinearExpr.constant(CST.TEMPS_MVT_PONT_MIN_JOB+10),				fin,"");
		
		minimumDerive=model.newIntervalVar(
				endBDD,
				LinearExpr.constant(CST.TEMPS_MVT_PONT_MIN_JOB),
				debutDerive,"");
		
		
		
		
		


	}
	


}
