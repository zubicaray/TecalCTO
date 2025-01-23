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
	private IntVar endReel;
	private IntVar deriveNulle;

	IntervalVar intervalReel;


	int tempsDeplacement;

	boolean zoneSecu=false;
	Task mTask;
	
	
	private long fixedStartBDD;
	private long fixedEndBDD;
	private long fixedDeriveNulle;	
	private long fixedDeriveMax;
	private  long fixedFinReel;
	
	

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
	public long getFixedFinReel() {
		return fixedFinReel;
	}
	public long getFixedDeriveNulle() {
		return fixedDeriveNulle;
	}
	
	public boolean BloquePont() {
		return mTask.BloquePont;
	}
	public IntVar getStartBDD() {
		return startBDD;
	}
	public long getStartBDDValue() {
		return TecalOrdo.solver.value(intervalBDD.getStartExpr());
	}
	
	public long getEndBDDValue() {
		return TecalOrdo.solver.value(intervalBDD.getEndExpr());
	}
	
	public long getFinReelValue() {
		return TecalOrdo.solver.value(intervalReel.getEndExpr());
	}
	


	public IntVar getDeriveNulle() { return deriveNulle ;}
	
	public IntVar getEndBDD() {
		return endBDD;
	}
	public IntVar getFinReel() {
		return endReel;
	}
	public IntVar getFinBDD() {
		return endBDD;
	}
	
	
	
	
	
	public void fixeTime(AssignedTask task) {
		
		
		fixedStartBDD=task.start;
		
		fixedEndBDD=task.end;
				
		fixedFinReel=task.derive+tempsDeplacement+mTask.egouttage;
		
	
	}
	
	void createFixedIntervals() {
		intervalBDD = TecalOrdo.model.newFixedInterval(fixedStartBDD,mTask.duration, "intervalFixeReel" );
		
			
		intervalReel=TecalOrdo.model.newFixedInterval(
					fixedStartBDD,
					fixedFinReel-fixedStartBDD,
					"intervalReel fixe");
	}

	TaskOrdo(CpModel model,Task task,int tps,String suffix){
	
		mTask=task;
		if( mTask.duration>70)
			mTask.duration-=40;
		
		tempsDeplacement=mTask.egouttage+tempsDeplacement+tps;
		if(mTask.duration>=TecalOrdoParams.getInstance().getTEMPS_ZONE_OVERLAP_MIN()){
			isOverlapable=true;
		}		
	
		//int tempsIncompresible=mTask.egouttage+tempsDeplacement+mTask.duration;
		startBDD 	= model.newIntVar(0, TecalOrdo.horizon, "start" + suffix); 
		endBDD 		= model.newIntVar(0, TecalOrdo.horizon, "end"   + suffix);		
		endReel			= model.newIntVar(0, TecalOrdo.horizon, "fin_nooverlap");
		deriveNulle= model.newIntVar(0, TecalOrdo.horizon, "deriveNulle");
		//deriveVar	= model.newIntVar(tempsIncompresible,tempsIncompresible+inderive, "deriveVar");
		
		//intervalBDD2 = model.newIntervalVar(startBDD, LinearExpr.constant(mTask.duration),endBDD, "interval" + suffix);
		  
		intervalBDD=model.newIntervalVar(
				startBDD,
				LinearExpr.constant(mTask.duration),
				//TODO best solution to finish ?
				//deriveVar,
				endBDD,"");
		intervalReel=model.newIntervalVar(
				startBDD,
				//+1 car que ce soit strictement supérieur dans jobConstraints
				LinearExpr.constant(mTask.duration+tps+1),
				//TODO best solution to finish ?
				//deriveVar,
				endReel,"");
		
	
		
	
		//minimumDerive=model.newIntervalVar(	startBDD,LinearExpr.constant(tempsIncompresible),deriveNulle,"");
		

	}
	public int getDuration() {
		
		return (int) (fixedEndBDD-fixedStartBDD);
		
	}




}
