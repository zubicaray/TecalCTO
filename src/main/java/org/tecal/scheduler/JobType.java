package org.tecal.scheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tecal.scheduler.types.GammeType;
import org.tecal.scheduler.types.ZoneType;


import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.IntVar;
import com.google.ortools.sat.IntervalVar;

public class JobType {
	List<Task> tasksJob;
	
	List<ListeZone> tasksNoOverlapPont;
	List<ListeZone> debutLonguesZonesPont;
	
	List<List<int[]>> idZonesNoOverlapPont;
	
	TaskOrdo taskAnod;
	TaskOrdo taskColmatage;

	CpModel model;

	int horizon;

	int jobId;
	String name;
	int indexAnod=-1;
	int indexColmatage=-1;


	List<GammeType> zones;
	
	// groupement de zones non chevauchable sur le pont 2
	
	List<List<int[]>> idLonguePont;

	


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
		

		idLonguePont = new ArrayList<List<int[]>>();				
		
		
		
		
		tasksNoOverlapPont = new ArrayList<ListeZone> ();
		debutLonguesZonesPont = new ArrayList<ListeZone> ();		
		
		
		
	
		
		for(int pont=0;pont< CST.NB_PONTS;pont++){
			
			ListeZone tasksNoOverlapable= new ListeZone();
			tasksNoOverlapPont.add(tasksNoOverlapable);
			
			ListeZone debutLongue= new ListeZone();		
			debutLonguesZonesPont.add(debutLongue);
			
			
			
			ArrayList<int[]> idNoOverlap = new ArrayList<int[]>();
			idZonesNoOverlapPont.add(idNoOverlap);
			

			
			ArrayList<int[]> idLongue = new ArrayList<int[]>();
			idLonguePont.add(idLongue);		
			
	
	
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

	
	// le regroupement se fait
	// par id
	void addZonesBDD(List<GammeType> inzones) {

		zones = inzones;
		List<Task> lTasksJob =new ArrayList<Task>();

		List<HashMap<Integer,ArrayList<Integer>>> zoneGroupement =new ArrayList<HashMap<Integer,ArrayList<Integer>>>();
		
		zoneGroupement.add(new HashMap<Integer,ArrayList<Integer>>());
		zoneGroupement.add(new HashMap<Integer,ArrayList<Integer>>());


		for (int i = 0; i < zones.size(); i++) {
			// les idtask partent de zéro et ce suivent sans trou
			Task task=new Task(zones.get(i).time,zones.get(i).numzone);
			lTasksJob.add(task);
			
			if(task.numzone==CST.ANODISATION_NUMZONE) 
				continue;
			int pont=1;
			if(task.numzone<CST.NUMZONE_DEBUT_PONT_2) 
				pont=0;
			int idgroupe=zones.get(i).id_regroupement_bdd;
			if(idgroupe!=0) {
				
				if(zoneGroupement.get(pont).containsKey(idgroupe)==false) {
					zoneGroupement.get(pont).put(idgroupe, new ArrayList<Integer>());
				}
				zoneGroupement.get(pont).get(idgroupe).add(i);
				
			}
			else {
				int coord[] = { i };
				idLonguePont.get(pont).add(coord);
			}
			
		}/*
		for(int p =0;p<CST.NB_PONTS;p++) {
			for (Map.Entry<Integer,ArrayList<Integer>> entry : zoneGroupement.get(p).entrySet()) {
				//List<Integer> listeZone=entry.getValue();
				
				//int coord[]={listeZone.get(0),listeZone.get(listeZone.size()-1)};
				//idRegroupeesPont.get(p).add(coord);
			}
		}
	*/
		

		tasksJob=lTasksJob;
	}


	void addIntervalForModel(Map<List<Integer>, TaskOrdo> allTasks,Map<Integer, List<IntervalVar>> zoneToIntervals,Map<Integer, 
			List<IntervalVar>> zoneCumulToIntervals,int jobID,HashMap<Integer,ZoneType>  zonesBDD) {

		
		int minDebut=0;
		for (int taskID = 0; taskID < tasksJob.size(); ++taskID) {
			Task task = tasksJob.get(taskID);
			String suffix = "_" + jobID + "_" + taskID;

			ZoneType  zt=zonesBDD.get(task.numzone);
			if(task.numzone == CST.DECHARGEMENT_NUMZONE )
					task.duration=CST.TEMPS_DECHARGEMENT;
			
			
			if( task.numzone == CST.CHARGEMENT_NUMZONE)
				task.duration=CST.TEMPS_CHARGEMENT;
			
			
			TaskOrdo taskOrdo = new TaskOrdo(model,horizon,task.duration,zt.derive, minDebut,suffix);   
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


		}
	}



	// on ajoute les ID BDD pour les zones par poste et les zonesde rincage non
	// chevauchable
	void addZones(List<GammeType> inzones) {

		List<Task> lTasksJob = new ArrayList<Task>();

		boolean newZoneZincage[] = { false, false };
		if(CST.PrintGroupementZones) System.out.println("---------------------------------------");
		if(CST.PrintGroupementZones) System.out.println("---------------------------------------");
		if(CST.PrintGroupementZones) System.out.println("JOB: "+name);
		
		zones = inzones;

		int debZone = -1;
		int finZone = -1;
		for (int i = 0; i < zones.size(); i++) {
			GammeType gt = zones.get(i);
			lTasksJob.add(new Task(gt.time, gt.numzone));
			//System.out.println("debZone: "+gt.codezone);

			int p = 0;
			if (gt.numzone > CST.NUMZONE_DEBUT_PONT_2)
				p = 1;
			
			
			if (gt.numzone == CST.ANODISATION_NUMZONE) {
				indexAnod=i;
			}
			if (gt.numzone == CST.COLMATAGE_NUMZONE) {
				indexColmatage=i;
			}

			boolean zoneCumulOrFinChaine = (// arret si p1 arrive en ano
					(p == 0 && gt.numzone == CST.ANODISATION_NUMZONE) ||
					// arret si P2 est au bout de ligne
					(p == 1 && i == (zones.size() - 1) && gt.numzone == CST.COLMATAGE_NUMZONE))
					// arret si zone de cumul
					 ;

			if (!newZoneZincage[p] && (gt.time + gt.derive) < CST.TEMPS_ZONE_OVERLAP_MIN) {
				debZone = i;
				newZoneZincage[p] = true;
				if(CST.PrintGroupementZones) System.out.println("---------------------------------------");
				if(CST.PrintGroupementZones) System.out.println("Pont: "+p+", zone regroupée deb: "+gt.codezone);
				
			}
			else if ((gt.time + gt.derive >= CST.TEMPS_ZONE_OVERLAP_MIN && newZoneZincage[p])
					|| zoneCumulOrFinChaine) {
				
				// on arrive sur une grosse zone, on étzablit le regroupement des zones précédentes
				finZone = i - 1;
				if(CST.PrintGroupementZones) System.out.println("Pont: "+p+", zone regroupée fin: "+zones.get(i-1).codezone);
				if(CST.PrintGroupementZones) System.out.println("---------------------------------------");
				// TODO:
				// CHECK PB SI debzone==finzone
				int coord[] = { debZone, finZone };
				
				idZonesNoOverlapPont.get(p).add(coord);

				if (!zoneCumulOrFinChaine) {
					// on est sur une zone chevauchable
					if(CST.PrintGroupementZones) System.out.println("---------------------------------------");
					if(CST.PrintGroupementZones) System.out.println("Pont: "+p+", zone grosse: "+zones.get(i).codezone);
					int coordBig[] = {i};
					idLonguePont.get(p).add(coordBig);
					idZonesNoOverlapPont.get(p).add(coordBig);
				}

				newZoneZincage[p] = false;

			}
			else if (gt.time + gt.derive >= CST.TEMPS_ZONE_OVERLAP_MIN && !newZoneZincage[p]){
				int coordBig[] = { i };
				idLonguePont.get(p).add(coordBig);
				idZonesNoOverlapPont.get(p).add(coordBig);
				if(CST.PrintGroupementZones) if(CST.PrintGroupementZones) System.out.println("---------------------------------------");
				if(CST.PrintGroupementZones) if(CST.PrintGroupementZones) System.out.println("Pont: "+p+", zone grosse: "+zones.get(i).codezone);
			}


		}
		tasksJob = lTasksJob;

	}
	

	void ComputeZonesNoOverlap(int jobID, Map<List<Integer>, TaskOrdo> allTasks) {

		taskAnod = allTasks.get(Arrays.asList(jobID, indexAnod));
		taskColmatage = allTasks.get(Arrays.asList(jobID, indexColmatage));
		

		
		for (int pont = 0; pont < idZonesNoOverlapPont.size(); pont++) {
			
				
			// zones non chevauchables
			for (int zoneID = 0; zoneID < idZonesNoOverlapPont.get(pont).size(); ++zoneID) {

				int ids []=idZonesNoOverlapPont.get(pont).get(zoneID);
				
				int idDebZone = ids[0];
				int idFinZone =-1;
			
				
				boolean groupe=false;
				if(ids.length==2) {			
					//zone sregroupées 
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
						//TODO fixe bug 
						if(indexAnod >0 && idDebZone-1==indexAnod ) {							
							start = taskAnod.endBDD;						
						}
						else {
							start = taskOrdo.deb;
						}
								
						if(groupe==false) {
							if(i==1) {
								debutLonguesZonesPont.get(pont).add(TecalOrdo.getMvt(model,start,horizon,30));
							}
							else 
								debutLonguesZonesPont.get(pont).add(TecalOrdo.getMvt(model,start,horizon));					
						}
					}
						
					if (i == idFinZone ) {
						
						
						//dernière zone avant l'anod, on ajoute un dernier mvt de pont pour y aller justement
						if(indexAnod >0 && idFinZone+1==indexAnod) {							
							end = taskAnod.deb;						
						}
						else {
							end = taskOrdo.fin;
						}
						
						if(groupe) {						
							// on ajoute la zone non chevauchable
							tasksNoOverlapPont.get(pont).add(TecalOrdo.getMvt(model,start,end,horizon));											
						}
						
					}
						
				}

			}			

		}

	}

}
