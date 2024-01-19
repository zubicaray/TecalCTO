package org.tecal.scheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tecal.scheduler.SQL_Anodisation.ZoneType;

import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.IntVar;
import com.google.ortools.sat.IntervalVar;
import com.google.ortools.sat.LinearExpr;

public class JobType {
	List<Task> tasksJob;

	CpModel model;

	int horizon;

	int jobId;
	String name;
	int indexAnod=-1;;


	// task qui permettent au pont se s'en aller ailleurs
	// !!!!!!!!!!
	// elles comprennent les zone slongues et des portions de zones regroupées "non overlapable"
	List<ListeTaskOrdo> tasksOverlapablePont;

	// dates d'entrée et de sortie du pont 1 sur toutes les zones avant anodisation
	List<Coord> mvts;

	List<GammeType> zones;
	
	// groupement de zones non chevauchable sur le pont 2
	
	List<List<int[]>> idLonguePont;

	
	ArrayListeZonePonts zonesLonguesPonts;

	// groupement de zones non chevauchable sur le pont 1:
	// liste qui contient , par exemple, des tableaux du type [date arrivée C3, date
	// fin C4]
	// ou encore [ entrée en C17, départ de C21]
	List<List<int[]>> idRegroupeesPont;

	ArrayCoordsRincagePonts coordsRegroupeesPonts;

	ArrayListeZonePonts zonesRegroupeesPonts;
	
	

	JobType(int jobid, String inname, CpModel inModel) {

		model = inModel;
		jobId = jobid;
		name = inname;
		
		
		// dates d'entrée et de sortie du pont 2 sur toutes les zones après ANODISATION
		tasksOverlapablePont =new ArrayList<ListeTaskOrdo>();		

		idRegroupeesPont = new ArrayList<List<int[]>>();		

		idLonguePont = new ArrayList<List<int[]>>();				
		
		mvts = new ArrayList<Coord>();		
		
		coordsRegroupeesPonts = new ArrayCoordsRincagePonts();

		zonesRegroupeesPonts = new ArrayListeZonePonts();			
		
		zonesLonguesPonts = new ArrayListeZonePonts();
	
		
		for(int pont=0;pont< CST.NB_PONTS;pont++){
			
			ListeTaskOrdo tasksOverlapable= new ListeTaskOrdo();
			tasksOverlapablePont.add(tasksOverlapable);
			
			ArrayList<int[]> idRegroupees = new ArrayList<int[]>();
			idRegroupeesPont.add(idRegroupees);
			
			ArrayList<int[]> idLongue = new ArrayList<int[]>();
			idLonguePont.add(idLongue);
			
			
			Coord mvt = new Coord();
			mvts.add(mvt);
			
			CoordsRincage coordsRegroupees = new CoordsRincage();
			coordsRegroupeesPonts.add(coordsRegroupees);
			
			ListeZone zonesRegroupees = new ListeZone();
			zonesRegroupeesPonts.add(zonesRegroupees);
			
			ListeZone zonesLongues = new ListeZone();			
			zonesLonguesPonts.add(zonesLongues);
		}

	}
	

	public void printNoOverlapZones() {
		int pont = 0;
		System.out.println("-----------------------------------------------------------");
		System.out.println("OVERLAP JOB: " + name);

		for (List<int[]> zr : idRegroupeesPont) {
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

	public void printMvtsPonts(CpSolver solver) {

		System.out.println("\n-----------------------------------------------------------");
		System.out.println(" MVTS PONT JOB: " + name);
		

		for (int p = 0; p < 1; p++) {

			List<IntVar> mvt = mvts.get(p);			


			Collections.sort(mvt,new Comparator<IntVar>() {
				public int compare(IntVar a1, IntVar a2) {
					long numligne1= solver.value(a1);
					long numligne2=solver.value(a2);
					return(int) (numligne1-numligne2);
				}
			});


			System.out.println("-----------------------------------------------------------");
			System.out.println("pont" + (p + 1));
			System.out.println("-----------------------------------------------------------");

			int cptZoneNoOverlap = 0;
			for (IntVar z : mvt) {

				System.out.println(" ******* zone: " + cptZoneNoOverlap + "************");
				System.out.println("at : " + solver.value(z));

				cptZoneNoOverlap++;
			}

			// break;
		}

	}
	
	
	// les zones regroupées avec le zone "marge" de CST.GAP_ZONE_NOOVERLAP secondes
	public void printZoneTimes(CpSolver solver) {

		System.out.println("\n-----------------------------------------------------------");
		System.out.println("TIME ZONE JOB: " + name);
		

		for (int p = 0; p < 2; p++) {
			ListeZone allBigZones = new ListeZone();
			allBigZones.addAll(zonesRegroupeesPonts.get(p));
			allBigZones.addAll(zonesLonguesPonts.get(p));


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


	ArrayListeZonePonts mvtsToInterval() {
		ArrayListeZonePonts res= new ArrayListeZonePonts();
		
		for(Coord mvtPont: mvts) {
			ListeZone listePont=new ListeZone();
			for(IntVar coord:mvtPont) {
				
				IntervalVar deb= model.newIntervalVar(
						model.newIntVar(0, horizon,  ""),
						LinearExpr.constant(CST.TEMPS_MVT_PONT), 
						coord, 
						"");
				IntervalVar fin= model.newIntervalVar(
						model.newIntVar(0, horizon,  ""),
						LinearExpr.constant(CST.TEMPS_MVT_PONT), 
						coord, 
						"");
				
				IntervalVar inter= model.newIntervalVar(deb.getStartExpr(),
						model.newIntVar(0, horizon,  ""),
						fin.getEndExpr(), 
						"");
						
				
				listePont.add(inter);
				
				
			}
			res.add(listePont);
		}
		
		
		
		return res;
		
	}
	
	void ComputeZonesRegroupables(int jobID, Map<List<Integer>, TaskOrdo> allTasks) {

		for (int pont = 0; pont < idRegroupeesPont.size(); pont++) {
			// zones non chevauchables
			for (int rincageID = 0; rincageID < idRegroupeesPont.get(pont).size(); ++rincageID) {

				int idDebZone = idRegroupeesPont.get(pont).get(rincageID)[0];
				int idFinZone = idRegroupeesPont.get(pont).get(rincageID)[1];

				IntVar start = null;
				IntVar end = null;
				;

				for (int i = idDebZone; i <= idFinZone; i++) {
					List<Integer> key = Arrays.asList(jobID, i);
					TaskOrdo taskOrdo = allTasks.get(key);
					// on ajoute tous les mouvement du pont concerné
					
					

					if (i == idDebZone) {
						mvts.get(pont).add(taskOrdo.deb);
						start = taskOrdo.deb;
					}
						
					if (i == idFinZone) {
						//mvts.get(pont).add(taskOrdo.arriveePont);
						end = taskOrdo.fin;
					}
						
				}

				IntVar[] coord = { start, end };

				// ajout du {début,fin} d'un groupe de zone continue pour lequel le pont ne pas
				// aller ailleurs
				coordsRegroupeesPonts.get(pont).add(coord);
				
				IntervalVar inter = model.newIntervalVar(start,
						model.newIntVar(0, horizon, "duree" + jobID + "_" + pont + "_" + rincageID), end,
						"zone_rincage_" + jobID + "_" + pont + "_" + rincageID);

				//zonesRegroupeesPonts.get(pont).add(inter);
				

				IntervalVar interSafe=model.newIntervalVar(inter.getEndExpr(),
						LinearExpr.constant(CST.GAP_ZONE_NOOVERLAP),
						model.newIntVar(0,horizon,"endSafe"+jobID+"_"+pont+"_"+rincageID),
						"zone_rincage_safe_"+jobID+"_"+pont+"_"+rincageID);
				
				zonesRegroupeesPonts.get(pont).add(
						model.newIntervalVar(start,
								model.newIntVar(0,horizon,""),
								interSafe.getEndExpr(),
								"zone_rincage_safe_"+jobID+"_"+pont+"_"+rincageID)
						);


			}
			

		}

	}
	//TODO:
	// ajout deb anodisation aux mvts du pont 1
	// ajout fin anodisation aux mvts du pont 2
	void ComputeZonesLongues(int jobID, Map<List<Integer>, TaskOrdo> allTasks) {

		for (int pont = 0; pont < idRegroupeesPont.size(); pont++) {
			
			// zones chevauchables
			for (int chevauID = 0; chevauID < idLonguePont.get(pont).size(); ++chevauID) {

				int idZone = idLonguePont.get(pont).get(chevauID)[0];

				IntVar start = null;
				IntVar end = null;
				List<Integer> key = Arrays.asList(jobID, idZone);
				TaskOrdo taskOrdo = allTasks.get(key);
				
				// on ajoute tous les mouvement du pont concerné
				mvts.get(pont).add(taskOrdo.deb);
				//mvts.get(pont).add(taskOrdo.arriveePont);

				start = taskOrdo.deb;

				end = taskOrdo.fin;

				/*
				IntervalVar
				aroundStartDeb=model.newIntervalVar(model.newIntVar(0,horizon,""),
				LinearExpr.constant(CST.TEMPS_MVT_PONT_MIN), start, "");
				zonesLonguesPonts.get(pont).add(aroundStartDeb);

				IntervalVar aroundStartFin=model.newIntervalVar(start,
				LinearExpr.constant(CST.TEMPS_MVT_PONT_MIN),
				model.newIntVar(0,horizon,""), "");

				zonesLonguesPonts.get(pont).add(aroundStartFin);

				IntervalVar aroundEndDeb=model.newIntervalVar(model.newIntVar(0,horizon,""),
				LinearExpr.constant(CST.TEMPS_MVT_PONT_MIN), end, "");
				zonesLonguesPonts.get(pont).add(aroundEndDeb);

				IntervalVar aroundEndFin=model.newIntervalVar(end,
				LinearExpr.constant(CST.TEMPS_MVT_PONT_MIN),
				model.newIntVar(0,horizon,""), "");
				zonesLonguesPonts.get(pont).add(aroundEndFin);
				 */

				IntervalVar aroundEndFin=model.newIntervalVar(start,						
						model.newIntVar(0,horizon,""),end, "");
				zonesLonguesPonts.get(pont).add(aroundEndFin);


			}

		}

	}

	// le regroupement se fait
	// par id
	void addZones(List<GammeType> inzones) {

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
			if(task.numzone<CST.ANODISATION_NUMZONE) 
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
			
		}
		for(int p =0;p<CST.NB_PONTS;p++) {
			for (Map.Entry<Integer,ArrayList<Integer>> entry : zoneGroupement.get(p).entrySet()) {
				List<Integer> listeZone=entry.getValue();
				
				int coord[]={listeZone.get(0),listeZone.get(listeZone.size()-1)};
				idRegroupeesPont.get(p).add(coord);
			}
		}
		//int coord[]={debZone,finZone};
		//idRegroupeesPont.get(p-1).add(coord);
		

		tasksJob=lTasksJob;
	}


	void addIntervalForModel(Map<List<Integer>, TaskOrdo> allTasks,Map<Integer, List<IntervalVar>> zoneToIntervals,Map<Integer, 
			List<IntervalVar>> zoneCumulToIntervals,int jobID,HashMap<Integer,ZoneType>  zonesBDD) {

		for (int taskID = 0; taskID < tasksJob.size(); ++taskID) {
			Task task = tasksJob.get(taskID);
			String suffix = "_" + jobID + "_" + taskID;

			ZoneType  zt=zonesBDD.get(task.numzone);
			TaskOrdo taskOrdo = new TaskOrdo(model,horizon,task.duration,zt.derive, suffix);     


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
	void addZonesThanksTime(List<GammeType> inzones) {

		List<Task> lTasksJob = new ArrayList<Task>();

		boolean newZoneZincage[] = { false, false };

		zones = inzones;

		int debZone = -1;
		int finZone = -1;
		for (int i = 0; i < zones.size(); i++) {
			GammeType gt = zones.get(i);
			lTasksJob.add(new Task(gt.time, gt.numzone));

			int p = 0;
			if (gt.numzone > CST.ANODISATION_NUMZONE)
				p = 1;

			boolean zoneCumulOrFinChaine = ((// arret si p1 arrive en ano
					(p == 0 && gt.numzone == CST.ANODISATION_NUMZONE) ||
					// arret si P2 est au bout de ligne
					(p == 1 && i == (zones.size() - 1))
					// arret si zone de cumul
					) || gt.cumul > 1);

			if (!newZoneZincage[p] && (gt.time + gt.derive) < CST.TEMPS_ZONE_OVERLAP_MIN) {
				debZone = i;
				newZoneZincage[p] = true;
				// System.out.println("debZone: "+gt.codezone);
			}
			else if ((gt.time + gt.derive >= CST.TEMPS_ZONE_OVERLAP_MIN && newZoneZincage[p])
					|| zoneCumulOrFinChaine) {
				finZone = i - 1;
				// TODO:
				// CHECK PB SI debzone==finzone
				int coord[] = { debZone, finZone };
				idRegroupeesPont.get(p).add(coord);

				if (!zoneCumulOrFinChaine) {
					// on est sur une zone chevauchable

					int coord2[] = { i };
					idLonguePont.get(p).add(coord2);
				}

				newZoneZincage[p] = false;

			}
			else if (gt.time + gt.derive >= CST.TEMPS_ZONE_OVERLAP_MIN && !newZoneZincage[p]){
				int coord2[] = { i };
				idLonguePont.get(p).add(coord2);
			}


		}
		tasksJob = lTasksJob;

	}

}
