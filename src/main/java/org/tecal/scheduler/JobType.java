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
import com.google.ortools.sat.LinearExpr;

public class JobType {
	
	protected static final Logger logger = LogManager.getLogger(JobType.class);
	protected  TecalOrdoParams mParams=TecalOrdoParams.getInstance();
	List<Task> tasksJob;
	List<TaskOrdo> mTaskOrdoList;	

	ArrayListeZonePonts bridgesMoves;
	ListeZone mNoOverlapP1P2;
	
	ListeZone mOverlapZones;
	
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
		bridgesMoves = new ArrayListeZonePonts();
			
		mNoOverlapP1P2 = new ListeZone();
		mOverlapZones = new ListeZone();
	
		
		for(int pont=0;pont< CST.NB_PONTS;pont++){
			
			ListeZone bridgeMove= new ListeZone();
			bridgesMoves.add(bridgeMove);	
	
		}

	}
	
	JobType(int barreID,  String inname) {
		mBarreId = barreID;
		name = inname;
		
		
		
		tasksJob = new ArrayList<Task>();
		mTaskOrdoList= new ArrayList<TaskOrdo>();
		bridgesMoves = new ArrayListeZonePonts();
			
		mNoOverlapP1P2 = new ListeZone();
		mOverlapZones= new ListeZone();
	
		
		for(int pont=0;pont< CST.NB_PONTS;pont++){
			
			ListeZone bridgeMove= new ListeZone();
			bridgesMoves.add(bridgeMove);	
	
		}

	}
	

	
	@Override
	public String toString() {

		return "";

	}

	void makeSafetyBetweenBridges(long time) {
		

		
		IntVar deb = null;
		IntVar fin= null;
		

		for (int taskID = 0; taskID < mTaskOrdoList.size(); ++taskID) {
						
			if(mTaskOrdoList.get(taskID).zoneSecu) {
				deb=(IntVar) mTaskOrdoList.get(taskID).getStart();
				
				if(indexAnod > 0 && taskID-1 == indexAnod) {
					deb=TecalOrdo.getBackward((IntVar) mTaskOrdoList.get(indexAnod).getEndBDD(),
							mParams.getTEMPS_ANO_ENTRE_P1_P2());
				}
				
				if(indexAnod > 0 && taskID+1 == indexAnod) {
					fin=(IntVar) mTaskOrdoList.get(indexAnod).getStart();
					
				}else {
					fin=(IntVar) mTaskOrdoList.get(taskID).getEndBDD();
				}
				
				int taskID2 = taskID+1;
				
				while(taskID2 < mTaskOrdoList.size() && mTaskOrdoList.get(taskID2).zoneSecu)
				{
					taskID2++;
				}
				
				if(taskID2>taskID+1) {
					if(indexAnod > 0 && taskID2 == indexAnod) {
						fin=TecalOrdo.getForeward((IntVar) mTaskOrdoList.get(indexAnod).getStart(),mParams.getTEMPS_ANO_ENTRE_P1_P2());
					}else {
						fin=(IntVar) mTaskOrdoList.get(taskID2-1).getEndBDD();
					}
					//on recommence après avec taskID=taskID2 ( +1 par la boucle )
					taskID=taskID2;
				}
				mNoOverlapP1P2.add(TecalOrdo.model.newIntervalVar( deb,TecalOrdo.model.newIntVar(0,TecalOrdo.horizon, "") ,fin, ""));
								
				
			}
			
						
		}
		
	}

	void clear() {
		mNoOverlapP1P2.clear();
		for(ListeZone t :bridgesMoves) {
			t.clear();
		}
	}
	void simulateBridgesMoves(long time) {

		
		IntVar deb = null;
		IntVar fin= null;
		int bridge=0;
		
		for (int taskID = 0; taskID < mTaskOrdoList.size(); ++taskID) {
			
			TaskOrdo taskOrdo = mTaskOrdoList.get(taskID);		
			TaskOrdo taskOrdoNext =null;
			if(taskID != mTaskOrdoList.size()-1) 
				taskOrdoNext = mTaskOrdoList.get(taskID+1);
		
			if(indexAnod > 0 && taskID >indexAnod) {
				bridge=1;								
			}
			// si pas de zone d'ano
			if(indexAnod < 0 && tasksJob.get(taskID).numzone >=mParams.getNUMZONE_ANODISATION()) {
				bridge=1;								
			}
			ListeZone lBridgeMoves=bridgesMoves.get(bridge);
			
			if(taskID==0) {
				deb=TecalOrdo.getBackward((IntVar) taskOrdoNext.getStart(),
						taskOrdo.tempsDeplacement+mParams.getTEMPS_ANO_ENTRE_P1_P2());
				continue;
			}
			
			
			if(taskOrdo.isOverlapable || taskID ==indexAnod ||  taskID == mTaskOrdoList.size()-1 ) {
				if(taskOrdo.BloquePont()) {
					logger.info("Coloration en "+SQL_DATA.getInstance().getZones().get(taskOrdo.mTask.numzone).codezone+ ", job: "+name);
					fin=taskOrdo.getEndBDD();
				}
				else
					fin=TecalOrdo.getForeward( (IntVar) taskOrdo.getStart(),mParams.getTEMPS_ANO_ENTRE_P1_P2());
				
				//System.out.println("deb:"+deb+", fin-deb="+ fin);
				lBridgeMoves.add(TecalOrdo.model.newIntervalVar(deb, TecalOrdo.model.newIntVar(0, TecalOrdo.horizon, ""), fin ,""));
				
				if(taskID != mTaskOrdoList.size()-1) {
					if(taskOrdo.BloquePont())
						deb=TecalOrdo.getBackward( (IntVar) taskOrdoNext.getStart(),taskOrdo.tempsDeplacement);
					else
						deb=TecalOrdo.getBackward((IntVar) taskOrdoNext.getStart(),taskOrdo.tempsDeplacement
								+mParams.getTEMPS_ANO_ENTRE_P1_P2());
				}					
						
			}			
			
		}
	}

	void addIntervalForModel (Map<List<Integer>, TaskOrdo> inAllTasks,Map<Integer, List<IntervalVar>> zoneToIntervals,
			Map<Integer,List<IntervalVar>> multiZoneIntervals,
			Map<Integer, List<IntervalVar>> cumulDemands) {

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
			TaskOrdo taskOrdo=mTaskOrdoList.get(taskID);
			
			LinearExpr deb=taskOrdo.intervalReel.getStartExpr();
			LinearExpr end;
			
			if(taskOrdo.isOverlapable) {
				mOverlapZones.add(taskOrdo.intervalReel);
			}
			
			if(taskID == tasksJob.size()-1 ) {
				end=taskOrdo.intervalReel.getEndExpr();					
			}
			else
				end=mTaskOrdoList.get(taskID+1).intervalReel.getStartExpr();
			
			IntervalVar inter=TecalOrdo.model.newIntervalVar(deb,TecalOrdo.model.newIntVar(0, TecalOrdo.horizon, "") ,end,"");
			//todo check cas chargement
			if(zt.cumul>1 &&  zt.numzone!=35) {
				multiZoneIntervals.computeIfAbsent(task.numzone, (Integer k) -> new ArrayList<>());   
				multiZoneIntervals.get(task.numzone).add(inter);
			}
			else {
				zoneToIntervals.computeIfAbsent(task.numzone, (Integer k) -> new ArrayList<>());              
				zoneToIntervals.get(task.numzone).add(inter);
				
				if(SQL_DATA.getInstance().getRelatedZones().containsKey(task.numzone)) {
					int zoneToAdd=SQL_DATA.getInstance().getRelatedZones().get(task.numzone);
					//cumulDemands.add
					if(!cumulDemands.containsKey(zoneToAdd)) {
						cumulDemands.put(zoneToAdd,new ArrayList<IntervalVar>());
					}
					cumulDemands.get(zoneToAdd).add(inter);
				}
			}
		}
	}


	private void buildTaskOrdo(Map<List<Integer>, TaskOrdo> allTasks,  int taskID, Task task,
			String suffix, ZoneType zt) {
		
		//System.out.println("taskID:"+taskID);
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

				taskOrdo = new TaskOrdo(TecalOrdo.model,task,0,suffix);   
			}
			else {
				
				Task taskSuivante = tasksJob.get(taskID+1);
				
				int tps=SQL_DATA.getInstance().getTempsDeplacement(task.numzone,taskSuivante.numzone,CST.VITESSE);
				if (tps==0) {
					//System.out.println("---------TPS NUL !!!!---------------");
					//System.out.println("task.numzone "+task.numzone);
					//System.out.println("task.numzone "+taskSuivante.numzone);
					String zone1=SQL_DATA.getInstance().getZones().get(task.numzone).codezone;
					String zone2=SQL_DATA.getInstance().getZones().get(taskSuivante.numzone).codezone;
					if(task.numzone==taskSuivante.numzone)
						tps=CST.TEMPS_PONT_MEME_CUVE;
					else {
						int ecart=Math.abs(task.numzone-taskSuivante.numzone);
						tps=CST.TEMPS_PONT_BASE+CST.TEMPS_PONT_PAR_CUVE*ecart;
					}					
					logger.info("------------------------------------------------------------");	
					logger.info("TPS NUL !!!!  entre idzone: "+zone1+" et "+zone2);
					logger.info("TPS inféré temporairement: "+tps);
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
