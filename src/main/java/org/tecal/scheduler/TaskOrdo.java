package org.tecal.scheduler;


import org.tecal.scheduler.types.AssignedTask;

import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.IntVar;
import com.google.ortools.sat.IntervalVar;
import com.google.ortools.sat.LinearExpr;

public class TaskOrdo {

	
	private IntVar startBDD;
	private IntVar endBDD;
	//interval théorique
	IntervalVar intervalBDD;
		
	//réel , qui intègre les contraintes du pont
	//IntVar deb;
	private IntVar fin;
	private IntVar deriveNulle;

	IntervalVar intervalReel;
	IntervalVar intervalMax;
	IntervalVar minimumDerive;
	IntervalVar maximumDerive;
	int tempsDeplacement;

	boolean zoneSecu=false;
	Task mTask;
	
	
	private long fixedStartBDD;
	private long fixedEndBDD;
	private long fixedDeriveNulle;	
	private long fixedDeriveMax;
	private  long fixedFin;
	
	

	boolean isOverlapable=false;

	public long getFixedStartBDD() {
		return fixedStartBDD;
	}
	public long getFixedDeriveMax() {
		return fixedDeriveMax;
	}
	
	public long getFixedEndBDD() {
		return fixedEndBDD;
	}
	public long getFixedFin() {
		return fixedFin;
	}
	public long getFixedDeriveNulle() {
		return fixedDeriveNulle;
	}
	
	public boolean BloquePont() {
		return mTask.BloquePont;
	}
	public IntVar getStart() {
		return startBDD;
	}
	public long getStartValue() {
		return TecalOrdo.solver.value(intervalBDD.getStartExpr());
	}
	
	public long getEndBDDValue() {
		return TecalOrdo.solver.value(intervalBDD.getEndExpr());
	}
	
	public long getFinValue() {
		return TecalOrdo.solver.value(intervalReel.getEndExpr());
	}
	


	public IntVar getDeriveNulle() { return deriveNulle ;}
	
	public IntVar getEndBDD() {
		return endBDD;
	}
	public IntVar getFin() {
		return fin;
	}
	
	
	
	
	public void fixeTime(AssignedTask task) {
		
		
		fixedStartBDD=task.start;
		
		fixedEndBDD=task.end;
				
		fixedFin=task.derive+tempsDeplacement+mTask.egouttage;
		
	
	}
	
	void createFixedIntervals() {
		intervalBDD = TecalOrdo.model.newFixedInterval(fixedStartBDD,mTask.duration, "intervalFixeReel" );
		
			
		intervalReel=TecalOrdo.model.newFixedInterval(
					fixedStartBDD,
					fixedFin-fixedStartBDD,
					"intervalReel fixe");
	}

	TaskOrdo(CpModel model,Task task,int tps,String suffix){
	
		mTask=task;
		
		tempsDeplacement=tps;
		int duree=mTask.duration;
		int derive=mTask.derive;
		derive=1; 
		int overlap=TecalOrdoParams.getInstance().getTEMPS_ZONE_OVERLAP_MIN();
		/*
		if(duree<overlap  && duree+derive>=overlap) {
			derive-=TecalOrdoParams.getInstance().getTEMPS_ZONE_OVERLAP_MIN()-duree;
			duree=TecalOrdoParams.getInstance().getTEMPS_ZONE_OVERLAP_MIN();
		}
		if(mTask.duration+mTask.derive>=TecalOrdoParams.getInstance().getTEMPS_ZONE_OVERLAP_MIN()){
			isOverlapable=true;
		}	
		*/
		if(mTask.duration>=TecalOrdoParams.getInstance().getTEMPS_ZONE_OVERLAP_MIN()){
			isOverlapable=true;
		}		
	
		
		startBDD 	= model.newIntVar(0, TecalOrdo.horizon, "start" + suffix); 
		endBDD 		= model.newIntVar(0, TecalOrdo.horizon, "end"   + suffix);		
		fin			= model.newIntVar(0, TecalOrdo.horizon, "fin_nooverlap");
		deriveNulle= model.newIntVar(0, TecalOrdo.horizon, "deriveNulle");
		//deriveVar	= model.newIntVar(tempsIncompresible,tempsIncompresible+inderive, "deriveVar");
		
		
		int tempsIncompresible=mTask.egouttage+tempsDeplacement+duree;
		
		intervalBDD = model.newIntervalVar(startBDD, LinearExpr.constant(mTask.duration),endBDD, "interval" + suffix);
		  
		intervalReel=model.newIntervalVar(
				startBDD,
				LinearExpr.constant(duree+mTask.egouttage+derive+tempsDeplacement),
				//TODO best solution to finish ?
				//deriveVar,
				fin,"");
		
	
		
	
		minimumDerive=model.newIntervalVar(	startBDD,LinearExpr.constant(tempsIncompresible),deriveNulle,"");
		

	}
	public int getDuration() {
		
		return (int) (fixedEndBDD-fixedStartBDD);
		
	}




}
