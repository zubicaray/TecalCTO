package org.tecal.scheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tecal.scheduler.data.SQL_DATA;
import org.tecal.scheduler.types.AssignedTask;
import org.tecal.scheduler.types.Barre;
import org.tecal.scheduler.types.ElementGamme;
import org.tecal.scheduler.types.ZoneType;

import com.google.ortools.sat.IntVar;
import com.google.ortools.sat.IntervalVar;


public class JobType {
	
	protected static final Logger logger = LogManager.getLogger(JobType.class);
	protected  TecalOrdoParams mParams=TecalOrdoParams.getInstance();
	List<Task> tasksJob;
	List<TaskOrdo> mTaskOrdoList;	

	ArrayListeZonePonts mBridgeMoves;
	ListeZone mNoOverlapP1P2;
	
	TaskOrdo taskAnod;
	
	private Barre mBarre;
	
	protected boolean isFixed=false;
	

	public boolean isFixed() { return isFixed;	}


	public void setFixed(boolean isFixed) { 		this.isFixed = isFixed;	}
	protected int mBarreId;
	public int getBarreID() {
		return mBarreId;
	}
	protected String name;
	public String getName() {
		return name;
	}
	


	int indexAnod=-1;


	List<ElementGamme> zones;
	
		

	JobType(int barreID, final Barre barre, String inname) {
		
		
		mBarreId = barreID;
		name = inname;
		
		mBarre =barre;
		
		tasksJob = new ArrayList<Task>();
		mTaskOrdoList= new ArrayList<TaskOrdo>();
		mBridgeMoves = new ArrayListeZonePonts();
			
		mNoOverlapP1P2 = new ListeZone();
		
	
		
		for(int pont=0;pont< CST.NB_PONTS;pont++){
			
			ListeZone bridgeMove= new ListeZone();
			mBridgeMoves.add(bridgeMove);	
	
		}

	}
	
	JobType(int barreID,  String inname) {
		mBarreId = barreID;
		name = inname;
		
		
		
		tasksJob = new ArrayList<Task>();
		mTaskOrdoList= new ArrayList<TaskOrdo>();
		mBridgeMoves = new ArrayListeZonePonts();
			
		mNoOverlapP1P2 = new ListeZone();
		
	
		
		for(int pont=0;pont< CST.NB_PONTS;pont++){
			
			ListeZone bridgeMove= new ListeZone();
			mBridgeMoves.add(bridgeMove);	
	
		}

	}
	

	
	@Override
	public String toString() {

		return "";

	}


	void clear() {
		mNoOverlapP1P2.clear();
		for(ListeZone t :mBridgeMoves) {
			t.clear();
		}
	}

	void simulateBridgesMoves(long time) {

	
		int bridge=0;
		
		for (int taskID = 0; taskID < mTaskOrdoList.size(); ++taskID) {
			
			TaskOrdo taskOrdo = mTaskOrdoList.get(taskID);			
		
			if(indexAnod > 0 && taskID >indexAnod) {
				bridge=1;								
			}
			// si pas de zone d'ano
			if(tasksJob.get(taskID).numzone >=mParams.getNUMZONE_ANODISATION()) {
				bridge=1;								
			}
			ListeZone lBridgeMoves=mBridgeMoves.get(bridge);

			
			
			List<Integer> listeSecuP1P2 = Arrays.asList(12, 13, 14, 16, 17);
			
			if(listeSecuP1P2.contains(taskOrdo.mTask.numzone) ) {
				mNoOverlapP1P2.add(taskOrdo.intervalBDD);
			}
			
			List<Integer> liste = Arrays.asList(1,35);
			if(! taskOrdo.isOverlapable || taskOrdo.BloquePont() || liste.contains(taskOrdo.mTask.numzone) ) {
			//if( taskOrdo.mTask.duration<180) {
				lBridgeMoves.add(taskOrdo.intervalBDD);
			}
			else
			{
				IntervalVar interval_var_deb = TecalOrdo.getForewardZone( (IntVar) taskOrdo.getStartBDD(),40);
				IntervalVar interval_var_fin = TecalOrdo.getBackwardZone( (IntVar) taskOrdo.getFinBDD(),40);
				
				if(taskID ==indexAnod ) {
					//mNoOverlapP1P2.add(interval_var_deb);
					//mNoOverlapP1P2.add(interval_var_fin);
					mBridgeMoves.get(0).add(interval_var_deb);
					mBridgeMoves.get(1).add(interval_var_fin);
				}
				else {
					lBridgeMoves.add(interval_var_deb);
					lBridgeMoves.add(interval_var_fin);
				}
			}
					
			
			
		}
	}

	

	void addIntervalForModel (Map<List<Integer>, TaskOrdo> inAllTasks,Map<Integer, List<IntervalVar>> mZoneToIntervals,
			Map<Integer,List<IntervalVar>> multiZoneIntervals,
			Map<Integer, List<IntervalVar>> mCumulDemands) {

		//System.out.println("Job "+name);
		for (int taskID = 0; taskID < tasksJob.size(); ++taskID) {
			Task task = tasksJob.get(taskID);
			String suffix = "_" + mBarreId + "_" + taskID;
			ZoneType  zt=SQL_DATA.getInstance().getZones().get(task.numzone);
			buildTaskOrdo(inAllTasks,   taskID, task, suffix, zt);
		}
		
		for (int taskID = 0; taskID < tasksJob.size(); ++taskID) {
			Task task = tasksJob.get(taskID);
			ZoneType  zt=SQL_DATA.getInstance().getZones().get(task.numzone);
			
				
		
			//todo check cas chargement
			if(zt.cumul>1) {
				multiZoneIntervals.computeIfAbsent(task.numzone, (Integer k) -> new ArrayList<>());   
				multiZoneIntervals.get(task.numzone).add(mTaskOrdoList.get(taskID).intervalBDD);
			}
			else {
				mZoneToIntervals.computeIfAbsent(task.numzone, (Integer k) -> new ArrayList<>());              
				mZoneToIntervals.get(task.numzone).add(mTaskOrdoList.get(taskID).intervalBDD);
				
				if(SQL_DATA.getInstance().getRelatedZones().containsKey(task.numzone)) {
					int zoneToAdd=SQL_DATA.getInstance().getRelatedZones().get(task.numzone);
					//mCumulDemands.add
					if(!mCumulDemands.containsKey(zoneToAdd)) {
						//mCumulDemands.put(zoneToAdd,new ArrayList<IntervalVar>());
					}
					//mCumulDemands.get(zoneToAdd).add(inter);
				}

			}

		}
	}


	private void buildTaskOrdo(Map<List<Integer>, TaskOrdo> allTasks,  int taskID, Task task,
			String suffix, ZoneType zt) {
		
	
		if(!isFixed) {

			if(task.numzone == CST.DECHARGEMENT_NUMZONE )
				task.duration=CST.TEMPS_DECHARGEMENT;
						
			if( task.numzone == CST.CHARGEMENT_NUMZONE)
				task.duration=CST.TEMPS_CHARGEMENT;
			
			
			TaskOrdo taskOrdo;
			
			int derive=zt.derive;
			if(mBarre.isPrioritaire()) {
				derive=Math.min(derive, 180);
			}
			
			if(task.numzone == CST.DECHARGEMENT_NUMZONE  || taskID==  tasksJob.size()-1) {
				//TODO !! CHECk pourquoi 0 par defaut juste avant s
				//new TaskOrdo(TecalOrdo.model,task.duration,derive, 0,task.egouttage,suffix);   
				taskOrdo = new TaskOrdo(TecalOrdo.model,task,0,suffix);   
			}
			else {
				
				Task taskSuivante = tasksJob.get(taskID+1);
				
				int tps=SQL_DATA.getInstance().getTempsDeplacement(task.numzone,taskSuivante.numzone,CST.VITESSE);
				if (tps==0) {
					//System.out.println("---------TPS NUL !!!!---------------");
					//System.out.println("task.numzone "+task.numzone);
					//System.out.println("task.numzone "+taskSuivante.numzone);
					logger.info("TPS NUL !!!!  entre idzone: "+task.numzone+" et "+taskSuivante.numzone+"  -------------------");
				}
				else {					
					tps = gestionVitesseManutention(tps);										
				}
				taskOrdo = new TaskOrdo(TecalOrdo.model,task,tps,suffix);   
				
			}
			
			//minDebut+=task.duration;	
			
			
			mTaskOrdoList.add(taskOrdo);
			
			if(SQL_DATA.getInstance().getZonesSecu().contains(task.numzone)) {
				taskOrdo.zoneSecu=true;
			}
		}
		else {
			mTaskOrdoList.get(taskID).createFixedIntervals();
		}
		
		List<Integer> key = Arrays.asList(mBarreId, taskID);
		allTasks.put(key, mTaskOrdoList.get(taskID));     

		
	}


	private int gestionVitesseManutention(int tps) {
		switch(mBarre.getVitesseDescente()){
		   
		   case CST.VITESSE_LENTE: 
		       tps+=CST.VITESSE_LENTE_DESCENTE;
		       break;		
		   case CST.VITESSE_NORMALE: 
		      break;	   
		   case CST.VITESSE_RAPIDE:
		       tps-=CST.VITESSE_RAPIDE_DESCENTE;
		       break;
		   default:
		       System.out.println("ERREUR gestionVitesseManutention descente");
		       break;
		}
		switch(mBarre.getVitesseMontee()){
		   
		   case CST.VITESSE_LENTE: 		     
		       tps+=CST.VITESSE_LENTE_MONTEE;
		       break;  
		   case CST.VITESSE_NORMALE: 
		       break;	
		   case CST.VITESSE_RAPIDE:		    
		       tps-=CST.VITESSE_RAPIDE_MONTEE;
		       break;
		   default:
		       System.out.println("ERREUR gestionVitesseManutention montée");
		       break;
		}
		return tps;
	}

	void buildTaskList(List<ElementGamme> inzones) {
		
		if(CST.PrintGroupementZones) System.out.println("---------------------------------------");
		if(CST.PrintGroupementZones) System.out.println("---------------------------------------");
		if(CST.PrintGroupementZones) System.out.println("JOB: "+name);
		
		zones = inzones;

		for (int i = 0; i < zones.size(); i++) {
			ElementGamme gt = zones.get(i);
			
			tasksJob.add(new Task(gt.time, gt.numzone,gt.egouttage,gt.derive,gt.BloquePont));
			if(CST.PrintGroupementZones) 
				System.out.println("debZone: "+gt.codezone+", gt.time="+gt.time);			
			
			if (gt.numzone == mParams.getNUMZONE_ANODISATION()) {
				indexAnod=i;
			
			}


		}
		

	}
	
	public int getmBarreId() {
		return mBarreId;
	}
	public Barre getBarre() {
		return mBarre;
	}
	public JobTypeFixed  makeFixedJob(List<AssignedTask> listTasks) {
		
		JobTypeFixed jf=new JobTypeFixed(mBarreId, name);
		
		jf.indexAnod=indexAnod;		
		jf.taskAnod=taskAnod;		

		jf.zones=zones;
		int cpt=0;
		for (TaskOrdo t : mTaskOrdoList) {
			t.fixeTime(listTasks.get(cpt));
			jf.mTaskOrdoList.add(t);
			cpt++;
		}
		for (Task t : tasksJob) {			
			jf.tasksJob.add(t);
		}
		
		return jf;
	}

}
