package org.tecal.scheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.tecal.scheduler.data.SQL_DATA;
import org.tecal.scheduler.types.AssignedTask;
import org.tecal.scheduler.types.GammeType;
import org.tecal.scheduler.types.ZoneType;


import com.google.ortools.sat.IntVar;
import com.google.ortools.sat.IntervalVar;

public class JobType {
	List<Task> tasksJob;
	List<TaskOrdo> mTaskOrdoList;
	

	ArrayListeZonePonts bridgesMoves;
	ListeZone mNoOverlapP1P2;
	
	TaskOrdo taskAnod;


	
	protected boolean isFixed=false;
	

	public boolean isFixed() {
		return isFixed;
	}


	public void setFixed(boolean isFixed) {
		this.isFixed = isFixed;
	}
	protected int mBarreId;
	public int getBarreID() {
		return mBarreId;
	}
	private String name;
	public String getName() {
		return name;
	}
	


	int indexAnod=-1;


	List<GammeType> zones;
	
	

	// groupement de zones non chevauchable sur le pont 1:
	// liste qui contient , par exemple, des tableaux du type [date arrivée C3, date
	// fin C4]
	// ou encore [ entrée en C17, départ de C21]
	
	
	

	JobType(int barre, String inname) {
		mBarreId = barre;
		name = inname;
		
		
		
		tasksJob = new ArrayList<Task>();
		mTaskOrdoList= new ArrayList<TaskOrdo>();
		bridgesMoves = new ArrayListeZonePonts();
			
		mNoOverlapP1P2 = new ListeZone();
		
	
		
		for(int pont=0;pont< CST.NB_PONTS;pont++){
			
			ListeZone bridgeMove= new ListeZone();
			bridgesMoves.add(bridgeMove);	
	
		}

	}
	



	
	@Override
	public String toString() {

		return "";

	}

	void makeSafetyBetweenBridges() {
		
		IntVar deb = null;
		IntVar fin= null;
		
	
		//System.out.println("Job "+name);
		for (int taskID = 0; taskID < mTaskOrdoList.size(); ++taskID) {
						
			if(mTaskOrdoList.get(taskID).zoneSecu) {
				deb=(IntVar) mTaskOrdoList.get(taskID).getStart();
				
				if(indexAnod > 0 && taskID-1 == indexAnod) {
					deb=TecalOrdo.getBackward(TecalOrdo.model,(IntVar) mTaskOrdoList.get(indexAnod).getEndBDD(),CST.TEMPS_ANO_ENTRE_P1_P2);
				}
				
				if(indexAnod > 0 && taskID+1 == indexAnod) {
					fin=(IntVar) mTaskOrdoList.get(indexAnod).getStart();
					
				}else {
					fin=(IntVar) mTaskOrdoList.get(taskID).getEndBDD();
				}
				
				int taskID2 = taskID+1;
				
				while(taskID2 < mTaskOrdoList.size() && mTaskOrdoList.get(taskID2).zoneSecu )
				{
					taskID2++;
				}
				
				if(taskID2>taskID+1) {
					if(indexAnod > 0 && taskID2 == indexAnod) {
						fin=TecalOrdo.getForeward(TecalOrdo.model,(IntVar) mTaskOrdoList.get(indexAnod).getStart(),CST.TEMPS_ANO_ENTRE_P1_P2);;
						
					}else {
						fin=(IntVar) mTaskOrdoList.get(taskID2-1).getEndBDD();
					}
					
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
	void simulateBridgesMoves() {

		
		IntVar deb = null;
		IntVar fin= null;
		int bridge=0;
		
		
		
		//System.out.println("Job "+name);
		for (int taskID = 0; taskID < mTaskOrdoList.size(); ++taskID) {
			
						
			
			TaskOrdo taskOrdo = mTaskOrdoList.get(taskID);		
			TaskOrdo taskOrdoNext =null;
			if(taskID != mTaskOrdoList.size()-1) 
				taskOrdoNext = mTaskOrdoList.get(taskID+1);
		
			if(indexAnod > 0 && taskID >indexAnod) {
				bridge=1;								
			}
			ListeZone lBridgeMoves=bridgesMoves.get(bridge);
			
			if(taskID==0) {
				deb=TecalOrdo.getBackward(TecalOrdo.model, (IntVar) taskOrdoNext.getStart(),taskOrdo.tempsDeplacement+CST.TEMPS_ANO_ENTRE_P1_P2);
				continue;
			}
			
			
			if(taskOrdo.isOverlapable || taskID ==indexAnod ||  taskID == mTaskOrdoList.size()-1 ) {
				fin=TecalOrdo.getForeward(TecalOrdo.model, (IntVar) taskOrdo.getStart(),CST.TEMPS_MVT_PONT);
				
				lBridgeMoves.add(TecalOrdo.model.newIntervalVar(deb, TecalOrdo.model.newIntVar(0, TecalOrdo.horizon, ""), fin ,""));
				
				if(taskID != mTaskOrdoList.size()-1)
					deb=TecalOrdo.getBackward(TecalOrdo.model, (IntVar) taskOrdoNext.getStart(),taskOrdo.tempsDeplacement+CST.TEMPS_ANO_ENTRE_P1_P2);
				
				
				
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
			
			if(zt.cumul>1) {
				multiZoneIntervals.computeIfAbsent(task.numzone, (Integer k) -> new ArrayList<>());   
				multiZoneIntervals.get(task.numzone).add(mTaskOrdoList.get(taskID).intervalReel);
			}
			else {
				zoneToIntervals.computeIfAbsent(task.numzone, (Integer k) -> new ArrayList<>());              
				zoneToIntervals.get(task.numzone).add(mTaskOrdoList.get(taskID).intervalReel);
				
				if(SQL_DATA.getInstance().relatedZones.containsKey(task.numzone)) {
					int zoneToAdd=SQL_DATA.getInstance().relatedZones.get(task.numzone);
					//cumulDemands.add
					if(!cumulDemands.containsKey(zoneToAdd)) {
						cumulDemands.put(zoneToAdd,new ArrayList<IntervalVar>());
					}
					cumulDemands.get(zoneToAdd).add(mTaskOrdoList.get(taskID).intervalReel);
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
			
			if(task.numzone == CST.DECHARGEMENT_NUMZONE  || taskID==  tasksJob.size()-1) {
				taskOrdo = new TaskOrdo(TecalOrdo.model,task.duration,zt.derive, 0,task.egouttage,suffix);   
			}
			else {
				
				Task taskSuivante = tasksJob.get(taskID+1);
				
				int tps=SQL_DATA.getInstance().getTempsDeplacement(task.numzone,taskSuivante.numzone,CST.VITESSE);
				if (tps==0) {
					System.out.println("---------TPS NUL !!!!---------------");
					System.out.println("task.numzone "+task.numzone);
					System.out.println("task.numzone "+taskSuivante.numzone);
				}
				taskOrdo = new TaskOrdo(TecalOrdo.model,task.duration,zt.derive, tps,task.egouttage,suffix);   
				
				
			}
			
			//minDebut+=task.duration;
			
			
			
			mTaskOrdoList.add(taskOrdo);
			
			if(SQL_DATA.getInstance().zonesSecu.contains(task.numzone)) {
				taskOrdo.zoneSecu=true;
			}
		}
		else {
			mTaskOrdoList.get(taskID).createFixedIntervals();
		}
		
		List<Integer> key = Arrays.asList(mBarreId, taskID);
		allTasks.put(key, mTaskOrdoList.get(taskID));     
			


		
	}



	void buildTaskList(List<GammeType> inzones) {
		
		if(CST.PrintGroupementZones) System.out.println("---------------------------------------");
		if(CST.PrintGroupementZones) System.out.println("---------------------------------------");
		if(CST.PrintGroupementZones) System.out.println("JOB: "+name);
		
		zones = inzones;

		for (int i = 0; i < zones.size(); i++) {
			GammeType gt = zones.get(i);
			tasksJob.add(new Task(gt.time, gt.numzone,gt.egouttage));
			if(CST.PrintGroupementZones) 
				System.out.println("debZone: "+gt.codezone+", gt.time="+gt.time);
			
			
			
			if (gt.numzone == CST.ANODISATION_NUMZONE) {
				indexAnod=i;
			
			}


		}
		

	}
	



	public int getmBarreId() {
		return mBarreId;
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
