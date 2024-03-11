package org.tecal.scheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tecal.scheduler.data.SQL_DATA;
import org.tecal.scheduler.types.GammeType;
import org.tecal.scheduler.types.ZoneType;


import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.IntVar;
import com.google.ortools.sat.IntervalVar;

public class JobType {
	List<Task> tasksJob;
	List<TaskOrdo> mTaskOrdoList;
	
	List<ListeZone> tasksNoOverlapPont;
	List<ListeZone> debutLonguesZonesPont;
	ArrayListeZonePonts bridgesMoves;
	
	List<List<int[]>> idZonesNoOverlapPont;
	
	TaskOrdo taskAnod;
	TaskOrdo taskColmatage;
	TaskOrdo taskDechargement;

	CpModel model;

	int horizon;

	int jobId;
	String name;
	int indexAnod=-1;
	int indexColmatage=-1;
	int indexDechargement=-1;
	
	int indexLastZoneP1=-1;
	int indexFirstZoneP2=-1;
	

	List<GammeType> zones;
	

	


	// groupement de zones non chevauchable sur le pont 1:
	// liste qui contient , par exemple, des tableaux du type [date arrivée C3, date
	// fin C4]
	// ou encore [ entrée en C17, départ de C21]
	
	
	

	JobType(int jobid, String inname,CpModel inM) {
		jobId = jobid;
		name = inname;
		model=inM;
		
		// dates d'entrée et de sortie du pont 2 sur toutes les zones après ANODISATION
		//tasksOverlapablePont =new ArrayList<ListeTaskOrdo>();		

		idZonesNoOverlapPont= new ArrayList<List<int[]>>();	
		
		tasksJob = new ArrayList<Task>();
		mTaskOrdoList= new ArrayList<TaskOrdo>();
		bridgesMoves = new ArrayListeZonePonts();
		
		
		
		
		
		
		tasksNoOverlapPont = new ArrayList<ListeZone> ();
		debutLonguesZonesPont = new ArrayList<ListeZone> ();		
		
		
		
	
		
		for(int pont=0;pont< CST.NB_PONTS;pont++){
			
			ListeZone bridgeMove= new ListeZone();
			bridgesMoves.add(bridgeMove);
			
			ListeZone tasksNoOverlapable= new ListeZone();
			tasksNoOverlapPont.add(tasksNoOverlapable);
			
			ListeZone debutLongue= new ListeZone();		
			debutLonguesZonesPont.add(debutLongue);
			
			
			
			ArrayList<int[]> idNoOverlap = new ArrayList<int[]>();
			idZonesNoOverlapPont.add(idNoOverlap);
			

			
			
	
	
		}

	}
	

	public void printNoOverlapZones() {
		int pont = 0;
		System.out.println("-----------------------------------------------------------");
		System.out.println("OVERLAP JOB: " + name);

		for (List<int[]> zr : idZonesNoOverlapPont) {
			System.out.println("-----------------------------------------------------------");
			System.out.println("pont" + (pont + 1));
			System.out.println("-----------------------------------------------------------");

			int cptZoneNoOverlap = 0;
			for (int[] z : zr) {

				System.out.println(" ******* zone: " + cptZoneNoOverlap + "************");
				System.out.println("deb: " + zones.get(z[0]).codezone);
				System.out.println("fin: " + zones.get(z[1]).codezone);

				cptZoneNoOverlap++;
			}

			pont++;
		}
	}


	
	
	// les zones regroupées avec le zone "marge" de CST.GAP_ZONE_NOOVERLAP secondes
	public void printZoneTimes(CpSolver solver) {

		System.out.println("\n-----------------------------------------------------------");
		System.out.println("TIME ZONE JOB: " + name);
		

		for (int p = 0; p < 2; p++) {
			ListeZone allBigZones = new ListeZone();
			allBigZones.addAll(tasksNoOverlapPont.get(p));
			allBigZones.addAll(debutLonguesZonesPont.get(p));


			Collections.sort(allBigZones,new Comparator<IntervalVar>() {
				public int compare(IntervalVar a1, IntervalVar a2) {
					long numligne1= solver.value(a1.getStartExpr());
					long numligne2=solver.value(a2.getStartExpr());
					return(int) (numligne1-numligne2);
				}
			});


			System.out.println("-----------------------------------------------------------");
			System.out.println("pont " + (p + 1));
			System.out.println("-----------------------------------------------------------");

			int cptZoneNoOverlap = 0;
			for (IntervalVar z : allBigZones) {

				System.out.println(" ******* zone: " + cptZoneNoOverlap + "************");
				System.out.println("deb zone: " + solver.value(z.getStartExpr()));
				System.out.println("fin zone: " + solver.value(z.getEndExpr()));

				cptZoneNoOverlap++;
			}

			// break;
		}

	}

	@Override
	public String toString() {

		return "";

	}

	void makeSafetyBetweenBridges() {
		
	}
	void makeBridgesMoves() {

		
		IntVar deb = null;
		IntVar fin= null;
		int bridge=0;
		//System.out.println("Job "+name);
		for (int taskID = 0; taskID < mTaskOrdoList.size(); ++taskID) {
			
						
			
			TaskOrdo taskOrdo = mTaskOrdoList.get(taskID);		
			TaskOrdo taskOrdoNext =null;
			if(taskID != mTaskOrdoList.size()-1) 
				taskOrdoNext = mTaskOrdoList.get(taskID+1);
		
			if(taskID >indexAnod) {
				bridge=1;								
			}
			ListeZone lBridgeMoves=bridgesMoves.get(bridge);
			
			if(taskID==0) {
				deb=TecalOrdo.getBackward(model, taskOrdoNext.startBDD,taskOrdo.tempsDeplacement+30);
				continue;
			}
			
			
			if(taskOrdo.isOverlapable || taskID ==indexAnod ||  taskID == mTaskOrdoList.size()-1 ) {
				fin=TecalOrdo.getForeward(model, taskOrdo.startBDD,CST.TEMPS_MVT_PONT);
				
				lBridgeMoves.add(model.newIntervalVar(deb, model.newIntVar(0, horizon, ""), fin ,""));
				
				if(taskID != mTaskOrdoList.size()-1)
					deb=TecalOrdo.getBackward(model, taskOrdoNext.startBDD,taskOrdo.tempsDeplacement+30);
				
				
				if(taskID ==indexAnod) {
					//bridgesMoves.get(0).add(TecalOrdo.getNoOverlapZone(model, taskOrdo.endBDD,0,40));
				}
				
				
			}
			
			
			
			
			
		}
	}

	void addIntervalForModel(Map<List<Integer>, TaskOrdo> allTasks,Map<Integer, List<IntervalVar>> zoneToIntervals,Map<Integer, 
			List<IntervalVar>> zoneCumulToIntervals,int jobID,HashMap<Integer,ZoneType>  zonesBDD) {

		//System.out.println("Job "+name);
		
		int minDebut=0;
		for (int taskID = 0; taskID < tasksJob.size(); ++taskID) {
			Task task = tasksJob.get(taskID);
			String suffix = "_" + jobID + "_" + taskID;

			ZoneType  zt=zonesBDD.get(task.numzone);
			if(task.numzone == CST.DECHARGEMENT_NUMZONE )
				task.duration=CST.TEMPS_DECHARGEMENT;
			
			
			if( task.numzone == CST.CHARGEMENT_NUMZONE)
				task.duration=CST.TEMPS_CHARGEMENT;
			
			
			TaskOrdo taskOrdo;
			if(task.numzone == CST.DECHARGEMENT_NUMZONE ) {
				taskOrdo = new TaskOrdo(model,horizon,task.duration,zt.derive, minDebut,0,suffix);   
			}
			else {
				Task taskSuivante = tasksJob.get(taskID+1);
				//System.out.println("------------------------------------------------");
				//System.out.println("task.numzone "+task.numzone);
				//System.out.println("task.numzone "+taskSuivante.numzone);
				int tps=SQL_DATA.getTempsDeplacement(task.numzone,taskSuivante.numzone,1);
				taskOrdo = new TaskOrdo(model,horizon,task.duration,zt.derive, minDebut,tps,suffix);   
			}
			
			minDebut+=task.duration;


			if(zt.cumul>1) {
				zoneCumulToIntervals.computeIfAbsent(task.numzone, (Integer k) -> new ArrayList<>());   
				zoneCumulToIntervals.get(task.numzone).add(taskOrdo.intervalReel);
			}
			else {
				zoneToIntervals.computeIfAbsent(task.numzone, (Integer k) -> new ArrayList<>());              
				zoneToIntervals.get(task.numzone).add(taskOrdo.intervalReel);


			}

			List<Integer> key = Arrays.asList(jobID, taskID);
			allTasks.put(key, taskOrdo);     
			mTaskOrdoList.add(taskOrdo);


		}
	}



	// on ajoute les ID BDD pour les zones par poste et les zonesde rincage non
	// chevauchable
	void addZones(List<GammeType> inzones) {



		boolean newZoneZincage[] = { false, false };
		if(CST.PrintGroupementZones) System.out.println("---------------------------------------");
		if(CST.PrintGroupementZones) System.out.println("---------------------------------------");
		if(CST.PrintGroupementZones) System.out.println("JOB: "+name);
		
		zones = inzones;

		int debZone = -1;
		int finZone = -1;
		for (int i = 0; i < zones.size(); i++) {
			GammeType gt = zones.get(i);
			tasksJob.add(new Task(gt.time, gt.numzone));
			//System.out.println("debZone: "+gt.codezone);
			
			boolean bridgeChanged=false;

			int p = 0;
			if (gt.numzone > CST.NUMZONE_DEBUT_PONT_2) {
				p = 1;
				if(i < zones.size() -1) {
					GammeType next = zones.get(i+1);
					if (next.numzone < CST.NUMZONE_DEBUT_PONT_2) 
						bridgeChanged=true;
				}
					
			}
				
			else{ //PONT 1 
				
				if(i == (zones.size() - 1))// on est avec le pont 1 mais la gamme ne va pas plus loin que l'ano
					bridgeChanged=true;
				else {
					GammeType next = zones.get(i+1);
					//la zone d'après est sur lepont 2 on doit changer quoi qu'il arrive
					if (next.numzone > CST.NUMZONE_DEBUT_PONT_2) 
						bridgeChanged=true;
				}
			}		
			
			
			if (gt.numzone == CST.ANODISATION_NUMZONE) {
				indexAnod=i;
				indexLastZoneP1=i-1;
				indexFirstZoneP2=i+1;
			}
			if (gt.numzone == CST.COLMATAGE_NUMZONE) {
				indexColmatage=i;
			}
			if (gt.numzone == CST.DECHARGEMENT_NUMZONE) {
				indexDechargement=i;
			}

			boolean zoneCumulOrFinChaine = (// arret si p1 arrive en ano
					(p == 0 && gt.numzone == CST.ANODISATION_NUMZONE) 
					
				) ;

			
			if (!newZoneZincage[p] && (gt.time + gt.derive) < CST.TEMPS_ZONE_OVERLAP_MIN) {
				debZone = i;
				newZoneZincage[p] = true;
				
				
				if(debZone ==zones.size()-1) {
					if(CST.PrintGroupementZones) System.out.println("---------------------------------------");
					if(CST.PrintGroupementZones) System.out.println("Pont: "+p+", zone déchargement: "+gt.codezone);
					int coordDecharge[] = {i};
					
					idZonesNoOverlapPont.get(p).add(coordDecharge);
				}
				else {
					if(CST.PrintGroupementZones) System.out.println("---------------------------------------");
					if(CST.PrintGroupementZones) System.out.println("Pont: "+p+", zone regroupée deb: "+gt.codezone);
				}
				
			}
			else if ((gt.time + gt.derive >= CST.TEMPS_ZONE_OVERLAP_MIN && newZoneZincage[p])
					|| zoneCumulOrFinChaine || bridgeChanged) {
				
				// on arrive sur une grosse zone, on établit le regroupement des zones PRECEDENTES
				finZone = i - 1;
				if(CST.PrintGroupementZones) System.out.println("Pont: "+p+", zone regroupée fin: "+zones.get(i-1).codezone);
				if(CST.PrintGroupementZones) System.out.println("----------------------------------------------------");
				// TODO:
				// CHECK PB SI debzone==finzone
				int coord[] = { debZone, finZone };
				
				idZonesNoOverlapPont.get(p).add(coord);

				if (!zoneCumulOrFinChaine) {
					// on est sur une zone chevauchable
					if(CST.PrintGroupementZones) System.out.println("-------------------------------------------------");
					if(CST.PrintGroupementZones) System.out.println("Pont: "+p+", zone grosse: "+zones.get(i).codezone);
					int coordBig[] = {i};
					
					idZonesNoOverlapPont.get(p).add(coordBig);
				}

				newZoneZincage[p] = false;

			}
			else if (gt.time + gt.derive >= CST.TEMPS_ZONE_OVERLAP_MIN && !newZoneZincage[p]){
				int coordBig[] = { i };				
				idZonesNoOverlapPont.get(p).add(coordBig);
				if(CST.PrintGroupementZones) if(CST.PrintGroupementZones) System.out.println("---------------------------------------");
				if(CST.PrintGroupementZones) if(CST.PrintGroupementZones) System.out.println("Pont: "+p+", zone grosse: "+zones.get(i).codezone);
			}


		}
		

	}
	

	void ComputeZonesNoOverlap(int jobID, Map<List<Integer>, TaskOrdo> allTasks) {

		taskAnod = allTasks.get(Arrays.asList(jobID, indexAnod));
		taskColmatage = allTasks.get(Arrays.asList(jobID, indexColmatage));
		taskDechargement = allTasks.get(Arrays.asList(jobID, indexDechargement));
		//System.out.println("job:"+name);	

		
		for (int pont = 0; pont < idZonesNoOverlapPont.size(); pont++) {
			
			int nbZones=idZonesNoOverlapPont.get(pont).size();
				
			// zones non chevauchables
			for (int zoneID = 0; zoneID < nbZones; ++zoneID) {
				
				// !!
				//on commence après la zone 0 de chargement
				if(pont==0 && zoneID==0) continue;

				int ids []=idZonesNoOverlapPont.get(pont).get(zoneID);
				
				int idDebZone = ids[0];
				int idFinZone =-1;
			
				
				boolean groupe=false;
				if(ids.length==2) {			
					//zones regroupées 
					idFinZone = ids[1];
					groupe=true;
				}
				else {
					idFinZone = idDebZone;
				}
				//if(jobID==1 && pont==1 && zoneID==2) {int a=2;}
					
					

				IntVar start = null;
				IntVar end = null;
				

				for (int i = idDebZone; i <= idFinZone; i++) {
					List<Integer> key = Arrays.asList(jobID, i);
					TaskOrdo taskOrdo = allTasks.get(key);

					if (i == idDebZone ) {			
						
						if(indexAnod >0 && idDebZone-1==indexAnod ) {							
							start = taskAnod.endBDD;						
						}
						else {
							//TODO: gérer le cas où cette zone regroupée est juste après le chargement => décaler le start sur gauche							
							if(i==1) {
								//on est juste après le chargement:
								//on doit prendre en compte le fait d'aller chercher la charge à la fin de la zone de chargement
								start =TecalOrdo.getBackward(model,taskOrdo.startBDD,CST.TEMPS_MVT_PONT*2);
							}
							else {
								start = taskOrdo.startBDD;
							}
						}
								
						if(groupe==false) {
							if(i==1) {
								// on est juste après le chargement, on ajoute du temps pour inclure la prise au chargement précédente
								debutLonguesZonesPont.get(pont).add(TecalOrdo.getNoOverlapZone(model, taskOrdo.startBDD,CST.TEMPS_MVT_PONT*2,CST.TEMPS_MVT_PONT));
							}
							else 
								debutLonguesZonesPont.get(pont).add(TecalOrdo.getNoOverlapZone(model, taskOrdo.startBDD));					
						}
					}
						
					if (i == idFinZone ) {
						
						
						//dernière zone avant l'anod, on ajoute un dernier mvt de pont pour y aller justement
						if(indexAnod >0 && idFinZone+1==indexAnod) {							
							end = taskAnod.startBDD;						
						} 
						
						else {
							end = taskOrdo.fin;
						}
						
						if(groupe) {						
							// on ajoute la zone non chevauchable
							tasksNoOverlapPont.get(pont).add(TecalOrdo.getNoOverlapZone(model,start,end));											
						}
						//TODO
						//to test
						if(idFinZone+1==indexAnod) {
							//debutLonguesZonesPont.get(pont).add(TecalOrdo.getNoOverlapZone(model, taskAnod.endBDD,0,40));
						}
						
					}
						
				}

			}			

		}

	}

}
