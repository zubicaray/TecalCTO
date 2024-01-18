package org.tecal.scheduler;

import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.IntVar;
import com.google.ortools.sat.IntervalVar;
import com.google.ortools.sat.LinearExpr;

class TaskOrdo {


	IntVar startBDD;
	IntVar endBDD;
	IntVar derive;
	IntVar arriveePont;
	//interval théorique
	IntervalVar intervalBDD;
	
	//réel , qui inttègre les contraintes du pont
	IntVar deb;
	IntVar fin;
	IntervalVar intervalReel;

	//dérive aux postes entre deux task d'un même job
	IntervalVar deriveInt;  
	// temps incompresible d'arrivée du pont
	IntervalVar intArriveePont;
	
	
	boolean isOverlapable=false;



	TaskOrdo(CpModel model,int horizon,int duration,int inderive,String suffix){
		startBDD = model.newIntVar(0, horizon, "start" + suffix);        
		endBDD = model.newIntVar(0, horizon, "end" + suffix);
		derive = model.newIntVar(0, horizon,  "derive_" + suffix);
		arriveePont = model.newIntVar(0, horizon,  "pontArrive_" + suffix);
		intervalBDD = model.newIntervalVar(
				startBDD, LinearExpr.constant(duration),endBDD, "interval" + suffix);

		
		inderive-=Constantes.TEMPS_MVT_PONT_MIN;
		inderive=Math.max(Constantes.TEMPS_MVT_PONT_MIN,inderive);

		
		intArriveePont = model.newIntervalVar(endBDD, LinearExpr.constant(Constantes.TEMPS_MVT_PONT_MIN), arriveePont, "intPontArrive" + suffix);
		
		
		deriveInt = model.newIntervalVar(
					arriveePont, LinearExpr.constant(inderive), derive, "derive" + suffix);

		
		//  !!
		// avec un temps d'arrivée au pont de 15 secondes
		// et une dérive théorique 15 secondes minimum
		// cela fait une dérive réelle de 15 secondes au minimum 
		// et une dérive réelle de 30 secondes max pour une dérive théorique nullu


		deb=model.newIntVar(0, horizon, "deb_nooverlap");
		fin=model.newIntVar(0, horizon, "fin_nooverlap");

		model.newIntervalVar(deb,LinearExpr.constant(Constantes.TEMPS_MVT_PONT_MIN),startBDD,"");             
		model.newIntervalVar(endBDD,LinearExpr.constant(Constantes.TEMPS_MVT_PONT_MIN),fin,"");           
		intervalReel=model.newIntervalVar(deb,model.newIntVar(0, horizon,  ""),fin,"");
		


	}


}
