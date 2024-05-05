package org.tecal.scheduler;


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

	public LinearExpr getFixedStart() {
		return intervalBDD.getStartExpr();
	}
	public LinearExpr getFixedDeriveMax() {
		return maximumDerive.getStartExpr();
	}
	
	public LinearExpr getFixedEndBDD() {
		return intervalBDD.getEndExpr();
	}
	public LinearExpr getFixedFin() {
		return intervalReel.getEndExpr();
	}
	public LinearExpr getFixedDeriveNulle() {
		return minimumDerive.getEndExpr();
	}
	

	public IntVar getStart() {
		return startBDD;
	}
	public IntVar getDeriveMax() {
		return deriveMax;
	}
	public IntVar getDeriveNulle() { return deriveNulle ;}
	
	public IntVar getEndBDD() {
		return endBDD;
	}
	public IntVar getFin() {
		return fin;
	}
	
	
	
	
	public void fixeTime() {
		
		
		fixedStartBDD=TecalOrdo.solver.value(startBDD);
		fixedEndBDD=TecalOrdo.solver.value(endBDD);
		fixedDeriveNulle=TecalOrdo.solver.value(deriveNulle);		
		fixedDeriveMax=TecalOrdo.solver.value(deriveMax);
		fixedFin=TecalOrdo.solver.value(fin);
		
		
		intervalBDD = TecalOrdo.model.newFixedInterval(fixedStartBDD,duration, "intervalFixeReel" );
	
		
		intervalReel=TecalOrdo.model.newFixedInterval(
				fixedStartBDD,
				fixedFin-fixedStartBDD,
				"intervalReel fixe");
		
		
		
		maximumDerive=TecalOrdo.model.newFixedInterval(
				fixedFin-(tempsDeplacement+egouttage),
				tempsDeplacement+egouttage,
				"derive max fixed" );
		
		minimumDerive=TecalOrdo.model.newFixedInterval(
				fixedEndBDD,
				fixedEndBDD-fixedDeriveNulle,
				"minimumDerive fixef" );
		
	
	}

	TaskOrdo(CpModel model,int horizon,int induration,int inderive,int minDebut,int intempsDeplacement,int egouttage,String suffix){
	
	
		tempsDeplacement=intempsDeplacement;
		duration=induration;
		this.egouttage=egouttage;
		
		if(inderive+duration>=CST.TEMPS_ZONE_OVERLAP_MIN){
			isOverlapable=true;
		}		
		
		
		startBDD 	= model.newIntVar(0, horizon, "start" + suffix); 
		endBDD 		= model.newIntVar(0, horizon, "end"   + suffix);		
		fin			= model.newIntVar(0, horizon, "fin_nooverlap");
		deriveNulle	= model.newIntVar(0, horizon, "deriveNulle");
		deriveMax	= model.newIntVar(0, horizon, "deriveMax");		
		      
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
