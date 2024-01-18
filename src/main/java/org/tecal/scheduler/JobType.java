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


	// task qui permettent au pont se s'en aller ailleurs
	List<TaskOrdo> tasksAllowP1;
	// dates d'entrée et de sortie du pont 2 sur toutes les zones après ANODISATION
	List<TaskOrdo> tasksAllowP2;
	List<List<TaskOrdo>> tasksAllowMove;

	// dates d'entrée et de sortie du pont 1 sur toutes les zones avant anodisation
	List<IntVar> mvtP1;
	// dates d'entrée et de sortie du pont 2 sur toutes les zones après ANODISATION
	List<IntVar> mvtP2;

	List<List<IntVar>> mvts;

	List<GammeType> zones;

	List<int[]> idLongueP1;
	// groupement de zones non chevauchable sur le pont 2
	List<int[]> idLongueP2;
	List<List<int[]>> idLonguePont;

	ListeZone zonesLonguesP1;
	ListeZone zonesLonguesP2;
	ArrayListeZonePonts zonesLonguesPonts;

	// groupement de zones non chevauchable sur le pont 1:
	// liste qui contient , par exemple, des tableaux du type [date arrivée C3, date
	// fin C4]
	// ou encore [ entrée en C17, départ de C21]
	List<int[]> idRegroupeesP1;
	// groupement de zones non chevauchable sur le pont 2
	List<int[]> idRegroupeesP2;
	List<List<int[]>> idRegroupeesPont;

	CoordsRincage coordsRegroupeesP1;
	CoordsRincage coordsRegroupeesP2;
	ArrayCoordsRincagePonts coordsRegroupeesPonts;

	ListeZone zonesRegroupeesP1;
	ListeZone zonesRegroupeesP2;
	ArrayListeZonePonts zonesRegroupeesPonts;

	public void printNoOverlapZones() {
		int pont = 0;
		System.out.println("-----------------------------------------------------------");
		System.out.println("JOB: " + name);

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

	public void printZoneTimes(CpSolver solver) {

		System.out.println("\n-----------------------------------------------------------");
		System.out.println("JOB: " + name);
		ListeZone allBigZones = new ListeZone();

		for (int p = 0; p < 1; p++) {

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
			System.out.println("pont" + (p + 1));
			System.out.println("-----------------------------------------------------------");

			int cptZoneNoOverlap = 0;
			for (IntervalVar z : allBigZones) {

				System.out.println(" ******* zone: " + cptZoneNoOverlap + "************");
				System.out.println("deb: " + solver.value(z.getStartExpr()));
				System.out.println("fin: " + solver.value(z.getEndExpr()));

				cptZoneNoOverlap++;
			}

			// break;
		}

	}

	@Override
	public String toString() {

		return "";

	}

	JobType(int jobid, String inname, CpModel inModel) {

		model = inModel;
		jobId = jobid;
		name = inname;

		mvtP1 = new ArrayList<IntVar>();
		mvtP2 = new ArrayList<IntVar>();

		idRegroupeesP1 = new ArrayList<int[]>();
		idRegroupeesP2 = new ArrayList<int[]>();

		idRegroupeesPont = new ArrayList<List<int[]>>();
		idRegroupeesPont.add(idRegroupeesP1);
		idRegroupeesPont.add(idRegroupeesP2);

		idLonguePont = new ArrayList<List<int[]>>();
		idLongueP1 = new ArrayList<int[]>();
		idLongueP2 = new ArrayList<int[]>();
		idLonguePont.add(idLongueP1);
		idLonguePont.add(idLongueP2);

		mvts = new ArrayList<List<IntVar>>();
		mvts.add(mvtP1);
		mvts.add(mvtP2);

		coordsRegroupeesP1 = new CoordsRincage();
		coordsRegroupeesP2 = new CoordsRincage();
		coordsRegroupeesPonts = new ArrayCoordsRincagePonts();
		coordsRegroupeesPonts.add(coordsRegroupeesP1);
		coordsRegroupeesPonts.add(coordsRegroupeesP2);

		zonesRegroupeesP1 = new ListeZone();
		zonesRegroupeesP2 = new ListeZone();
		zonesRegroupeesPonts = new ArrayListeZonePonts();
		zonesRegroupeesPonts.add(zonesRegroupeesP1);
		zonesRegroupeesPonts.add(zonesRegroupeesP2);

		zonesLonguesP1 = new ListeZone();
		zonesLonguesP2 = new ListeZone();
		zonesLonguesPonts = new ArrayListeZonePonts();
		zonesLonguesPonts.add(zonesLonguesP1);
		zonesLonguesPonts.add(zonesLonguesP2);

	}

	void ComputeZonesNoOverlap(int jobID, Map<List<Integer>, TaskOrdo> allTasks) {

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
					mvts.get(pont).add(taskOrdo.deb);
					mvts.get(pont).add(taskOrdo.fin);

					if (i == idDebZone)
						start = taskOrdo.deb;
					if (i == idFinZone)
						end = taskOrdo.fin;
				}

				IntVar[] coord = { start, end };

				// ajout du {début,fin} d'un groupe de zone continue pour lequel le pont ne pas
				// aller ailleurs
				coordsRegroupeesPonts.get(pont).add(coord);

				IntervalVar inter = model.newIntervalVar(start,
						model.newIntVar(0, horizon, "duree" + jobID + "_" + pont + "_" + rincageID), end,
						"zone_rincage_" + jobID + "_" + pont + "_" + rincageID);

				zonesRegroupeesPonts.get(pont).add(inter);


				IntervalVar interSafe=model.newIntervalVar(inter.getEndExpr(),
						LinearExpr.constant(Constantes.GAP_ZONE_NOOVERLAP),
						model.newIntVar(0,horizon,"endSafe"+jobID+"_"+pont+"_"+rincageID),
						"zone_rincage_safe_"+jobID+"_"+pont+"_"+rincageID);
				zonesRegroupeesPonts.get(pont).add(interSafe);


			}
			// zones chevauchables
			for (int chevauID = 0; chevauID < idLonguePont.get(pont).size(); ++chevauID) {

				int idZone = idLonguePont.get(pont).get(chevauID)[0];

				IntVar start = null;
				IntVar end = null;
				List<Integer> key = Arrays.asList(jobID, idZone);
				TaskOrdo taskOrdo = allTasks.get(key);

				start = taskOrdo.deb;

				end = taskOrdo.fin;

				/*
				IntervalVar
				aroundStartDeb=model.newIntervalVar(model.newIntVar(0,horizon,""),
				LinearExpr.constant(Constantes.TEMPS_MVT_PONT_MIN), start, "");
				zonesLonguesPonts.get(pont).add(aroundStartDeb);

				IntervalVar aroundStartFin=model.newIntervalVar(start,
				LinearExpr.constant(Constantes.TEMPS_MVT_PONT_MIN),
				model.newIntVar(0,horizon,""), "");

				zonesLonguesPonts.get(pont).add(aroundStartFin);

				IntervalVar aroundEndDeb=model.newIntervalVar(model.newIntVar(0,horizon,""),
				LinearExpr.constant(Constantes.TEMPS_MVT_PONT_MIN), end, "");
				zonesLonguesPonts.get(pont).add(aroundEndDeb);

				IntervalVar aroundEndFin=model.newIntervalVar(end,
				LinearExpr.constant(Constantes.TEMPS_MVT_PONT_MIN),
				model.newIntVar(0,horizon,""), "");
				zonesLonguesPonts.get(pont).add(aroundEndFin);
				 */

				IntervalVar aroundEndFin=model.newIntervalVar(start,						
						model.newIntVar(0,horizon,""),end, "");
				zonesLonguesPonts.get(pont).add(aroundEndFin);


			}

		}

	}


	void addZones(List<GammeType> inzones) {

		zones = inzones;
		List<Task> lTasksJob =new ArrayList<Task>();

		boolean newZoneZincage[]= {false,false} ;



		int debZone=-1;
		int finZone=-1;
		for (int i = 0; i < zones.size(); i++) {
			// les idtask partent de zéro et ce suivent sans trou
			lTasksJob.add(new Task(zones.get(i).time,zones.get(i).numzone));

			for(int p=1;p <= idRegroupeesPont.size();p++) {
				if(zones.get(i).nonChevauchementPont==p && !newZoneZincage[p-1]) {		
					debZone=i;
					newZoneZincage[p-1]=true;
				}
				if(zones.get(i).nonChevauchementPont==0 && newZoneZincage[p-1]) {
					// a cooriger !! fin zone n'est pas i !!!
					finZone=i-1;
					int coord[]={debZone,finZone};
					idRegroupeesPont.get(p-1).add(coord);
					newZoneZincage[p-1]=false;

				}
			}


		}
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
			if (gt.numzone > Constantes.ANODISATION_NUMZONE)
				p = 1;

			boolean zoneCumulOrFinChaine = ((// arret si p1 arrive en ano
					(p == 0 && gt.numzone == Constantes.ANODISATION_NUMZONE) ||
					// arret si P2 est au bout de ligne
					(p == 1 && i == (zones.size() - 1))
					// arret si zone de cumul
					) || gt.cumul > 1);

			if (!newZoneZincage[p] && (gt.time + gt.derive) < Constantes.TEMPS_ZONE_OVERLAP_MIN) {
				debZone = i;
				newZoneZincage[p] = true;
				// System.out.println("debZone: "+gt.codezone);
			}
			else if ((gt.time + gt.derive >= Constantes.TEMPS_ZONE_OVERLAP_MIN && newZoneZincage[p])
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
			else if (gt.time + gt.derive >= Constantes.TEMPS_ZONE_OVERLAP_MIN && !newZoneZincage[p]){
				int coord2[] = { i };
				idLonguePont.get(p).add(coord2);
			}


		}
		tasksJob = lTasksJob;

	}

}
