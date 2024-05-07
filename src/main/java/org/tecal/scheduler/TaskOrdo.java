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
	private IntVar deriveMax;
	IntervalVar intervalReel;
	IntervalVar minimumDerive;
	IntervalVar maximumDerive;
	int tempsDeplacement;
	int egouttage;
	int duration;
	boolean zoneSecu=false;
	
	
	private long fixedStartBDD;
	private long fixedEndBDD;
	private long fixedDeriveNulle;
	@SuppressWarnings("unused")
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
	
	public IntVar getDeriveMax() {
		return deriveMax;
	}
	
	public long getDeriveMaxValue() {
		return TecalOrdo.solver.value(maximumDerive.getStartExpr());
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
		
		
		fixedFin=task.derive+tempsDeplacement+egouttage;
		
		
	
		
		
		
		
	
	}
	
	void createFixedIntervals() {
		intervalBDD = TecalOrdo.model.newFixedInterval(fixedStartBDD,duration, "intervalFixeReel" );
		
			
			intervalReel=TecalOrdo.model.newFixedInterval(
					fixedStartBDD,
					fixedFin-fixedStartBDD,
					"intervalReel fixe");
	}

	TaskOrdo(CpModel model,int induration,int inderive,int minDebut,int intempsDeplacement,int egouttage,String suffix){
	
	
		tempsDeplacement=intempsDeplacement;
		duration=induration;
		this.egouttage=egouttage;
		
		if(inderive+duration>=CST.TEMPS_ZONE_OVERLAP_MIN){
			isOverlapable=true;
		}		
		
		
		startBDD 	= model.newIntVar(0, TecalOrdo.horizon, "start" + suffix); 
		endBDD 		= model.newIntVar(0, TecalOrdo.horizon, "end"   + suffix);		
		fin			= model.newIntVar(0, TecalOrdo.horizon, "fin_nooverlap");
		deriveNulle	= model.newIntVar(0, TecalOrdo.horizon, "deriveNulle");
		deriveMax	= model.newIntVar(0, TecalOrdo.horizon, "deriveMax");		
		      
		intervalBDD = model.newIntervalVar(startBDD, LinearExpr.constant(duration),endBDD, "interval" + suffix);
		  
		intervalReel=model.newIntervalVar(
				startBDD,
				LinearExpr.constant(duration+egouttage+inderive+tempsDeplacement),
				fin,"");
		
		maximumDerive=model.newIntervalVar(
				deriveMax,
				LinearExpr.constant(tempsDeplacement+egouttage),
				fin,"");
		
		
		minimumDerive=model.newIntervalVar(
				endBDD,
				LinearExpr.constant(tempsDeplacement+egouttage),
				deriveNulle,"");
		

	}




}
