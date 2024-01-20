package org.tecal.scheduler;

import com.google.ortools.Loader;
import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.CpSolverStatus;
import com.google.ortools.sat.CumulativeConstraint;
import com.google.ortools.sat.IntVar;
import com.google.ortools.sat.IntervalVar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import javax.swing.SwingUtilities;  
import org.jfree.ui.RefineryUtilities;
import org.tecal.scheduler.SQL_Anodisation.ZoneType;

class Coord 			        extends ArrayList<IntVar> 	{	private static final long serialVersionUID = 1L;}
class CoordsRincage 			extends ArrayList<IntVar[]> 	{	private static final long serialVersionUID = 1L;}
class ArrayCoordsRincagePonts   extends ArrayList<CoordsRincage>{	private static final long serialVersionUID = 1L;}
class ZonesIntervalVar 			extends ArrayList<IntervalVar> 	{	private static final long serialVersionUID = 1L;}
class ListeZone 				extends ArrayList<IntervalVar> 	{	private static final long serialVersionUID = 1L;}
class ArrayListeZonePonts   	extends ArrayList<ListeZone>	{	private static final long serialVersionUID = 1L;}
class ListeTaskOrdo				extends ArrayList<TaskOrdo> 	{	private static final long serialVersionUID = 1L;}


class AssignedTask {
	//id OF/GAMME propre à G.OR
	int jobID;
	//id zone  propre à G.OR
	int taskID;  
	// numzone de la table ZONE
	int numzone;
	int start;
	int duration;    
	int derive;    
	// offset pour les zones cumul
	int IdPosteZoneCumul;
	// Ctor
	AssignedTask(int jobID, int taskID, int numzone,int start, int duration,int derive) {
		this.jobID = jobID;
		this.taskID = taskID;
		this.start = start;
		this.duration = duration;
		this.numzone=numzone;
		this.derive=derive;
	}
}
class SortTasks implements Comparator<AssignedTask> {
	@Override
	public int compare(AssignedTask a, AssignedTask b) {
		if (a.start != b.start) {
			return a.start - b.start;
		} else {
			return a.duration - b.duration;
		}
	}
}

class Task {

	int numzone;
	int duration;
	Task( int duration,int numzone) {       
		this.duration = duration;
		this.numzone=numzone;
	}
}




/** Minimal Jobshop problem. */
public class TecalOrdo {
	

	public static void main(String[] args) {
		
		
		
		SQL_Anodisation sqlCnx = new SQL_Anodisation();
		if(CST.PRINT_PROD_DIAG)
		SwingUtilities.invokeLater(() -> {  

			 final GanttChart ganttTecal = new GanttChart(sqlCnx,"Gantt Chart prod du 02/11/2023");
			 ganttTecal.prod_diag();
			 ganttTecal.pack();
			 ganttTecal.setSize(new java.awt.Dimension(1500, 870));
		     RefineryUtilities.centerFrameOnScreen(ganttTecal);
		     ganttTecal.setVisible(true);

		});
		Loader.loadNativeLibraries();



		
		HashMap<String, ArrayList<GammeType> > gammeToZones=sqlCnx.getGammesZones();

		HashMap<Integer,ZoneType> zonesBDD=sqlCnx.getZones();
		// lien entre id de la table Zone et les zones de l'ordo
		Integer[] numzoneArr= zonesBDD.keySet().toArray(new Integer[0]);


		//List<List<Task>> allJobs= new ArrayList<List<Task>>();
		// Creates the model.
		CpModel model = new CpModel();

		List<JobType> allJobs= new ArrayList<JobType>();

		int cptJob=0;
		for (Map.Entry<String, ArrayList<GammeType> > entry : gammeToZones.entrySet()) {

			JobType job = new JobType(cptJob, entry.getKey(),model);
			//String lgamme = entry.getKey();
			List<GammeType>  zones = entry.getValue();
			//System.out.println("gamme=" + lgamme + ", nb zone Value length=" + zones.size());
			job.addZones(zones);       
			allJobs.add(job);
		}

		// Computes horizon dynamically as the sum of all durations.
		int horizon = 0;
		for (JobType job : allJobs) {
			for (Task task : job.tasksJob) {
				horizon += task.duration;
			}
		}

		for (JobType job : allJobs) job.horizon=horizon;




		Map<List<Integer>, TaskOrdo> allTasks = new HashMap<>();
		Map<Integer, List<IntervalVar>> zoneToIntervals = new HashMap<>();

		Map<Integer, List<IntervalVar>> zoneCumulToIntervals = new HashMap<>();


		for (int jobID = 0; jobID < allJobs.size(); ++jobID) {
			JobType job = allJobs.get(jobID);
			job.addIntervalForModel(allTasks,zoneToIntervals,zoneCumulToIntervals,jobID,zonesBDD);
			
		}

		for (int jobID = 0; jobID < allJobs.size(); ++jobID) {
			
			allJobs.get(jobID).ComputeZonesRegroupables(jobID, allTasks);  
			allJobs.get(jobID).ComputeZonesLongues(jobID, allTasks);  
			
			//allJobs.get(jobID).printNoOverlapZones();
			
		}

		//--------------------------------------------------------------------------------------------
		// CONSTRAINTES SUR CHAQUE JOB
		//--------------------------------------------------------------------------------------------

		// Create and add disjunctive constraints.		
		for (int numzone : numzoneArr) {



			if(  zoneToIntervals.containsKey(numzone)) {    	 
				List<IntervalVar> list = zoneToIntervals.get(numzone);  
				model.addNoOverlap(list);    	  
			}
			if(  zoneCumulToIntervals.containsKey(numzone)) {    	 
				List<IntervalVar> listCumul = zoneCumulToIntervals.get(numzone);
				ZoneType zt=zonesBDD.get(numzone);
				// zone autorisant le "chevauchement" => zone contenant plus de  1 postes
				IntVar capacity = model.newIntVar(0, zt.cumul, "capacity_of_"+numzone);

				CumulativeConstraint cumul =model.addCumulative(capacity);    		
				long[] zoneUsage  = new long[listCumul.size()];
				Arrays.fill(zoneUsage,1);
				cumul.addDemands(listCumul.toArray(new IntervalVar[0]), zoneUsage);  	  
			}



		}
		
		ArrayListeZonePonts mvtsPonts =new ArrayListeZonePonts();
			
		for(int pont=0;pont<CST.NB_PONTS;pont++) {
			mvtsPonts.add(new ListeZone());
		}
			
		for (int jobID = 0; jobID < allJobs.size(); ++jobID) {
			
			ArrayListeZonePonts mvtsPontsJob=allJobs.get(jobID).mvtsToInterval();
			for(int pont=0;pont<CST.NB_PONTS;pont++) {
				mvtsPonts.get(pont).addAll(mvtsPontsJob.get(pont));
			}
		}
		if(CST.CSTR_MVTS_PONT)
		for(int pont=0;pont<CST.NB_PONTS;pont++) {
			model.addNoOverlap(mvtsPonts.get(pont)); 
		}
		
		

			
		
		
		
		//--------------------------------------------------------------------------------------------
		// CONSTRAINTES SUR CHAQUE POSTE
		//--------------------------------------------------------------------------------------------
		// les zones de rincages ne doivent pas se croiser

		ArrayList<ZonesIntervalVar> listZonesNoOverlapParPont  = new  ArrayList<ZonesIntervalVar>();  
		listZonesNoOverlapParPont.add( new  ZonesIntervalVar()); // add zones pont 1
		listZonesNoOverlapParPont.add( new  ZonesIntervalVar()); // add zones pont 2

		ArrayList<ZonesIntervalVar> listZonesLonguesParPont  = new  ArrayList<ZonesIntervalVar>();  
		listZonesLonguesParPont.add( new  ZonesIntervalVar()); // add zones pont 1
		listZonesLonguesParPont.add( new  ZonesIntervalVar()); // add zones pont 2

		
		//---------------------------------------------------------------------------
		// NOOVERLAP ZONES REGROUPEES -----------------------------------------------
		for (JobType j  :allJobs) { 
			int p=0;    	
			for(ListeZone zonesRegroupeesP :j.zonesRegroupeesPonts) {   
				listZonesNoOverlapParPont.get(p).addAll(zonesRegroupeesP);   
				p++;
			}


		}
		for(ArrayList<IntervalVar> z: listZonesNoOverlapParPont) {
			model.addNoOverlap(z);
		}

		//---------------------------------------------------------------------------
		// NOOVERLAP ZONES LONGUES -- -----------------------------------------------
		for (JobType j  :allJobs) { 
			int p=0;    	
			for(ListeZone zonesLonguesP :j.zonesLonguesPonts) {   
				listZonesLonguesParPont.get(p).addAll(zonesLonguesP);   
				p++;
			}


		}
		//for(ArrayList<IntervalVar> z: listZonesLonguesParPont) {	model.addNoOverlap(z);		}

		//---------------------------------------------------------------------------
		//---------------------------------------------------------------------------
		ListeZone zonesLonguesOther=new ListeZone();

		for(int pont=0;pont<1;pont++) {
			for(int j=0;j<allJobs.size();j++) {
				JobType j1=allJobs.get(j);
				/*
    	 ListeZone zonesLonguesOther=new ListeZone();
    	 for(int k=0;k<allJobs.size();k++) {
    		 if(k==j) continue;

    		 zonesLonguesOther.addAll(allJobs.get(j).zonesLonguesPonts.get(pont));

    	 }
				 */
				zonesLonguesOther.addAll(j1.zonesLonguesPonts.get(pont));
				//model.addNoOverlap(zonesLonguesOther);


			}
		}





		//--------------------------------------------------------------------------------------------
		// PRECEDENCES
		//--------------------------------------------------------------------------------------------

		// Precedences inside a job.
		for (int jobID = 0; jobID < allJobs.size(); ++jobID) {
			List<Task> jobTasks = allJobs.get(jobID).tasksJob;
			for (int taskID = 0; taskID < jobTasks.size() - 1; ++taskID) {
				List<Integer> prevKey = Arrays.asList(jobID, taskID);
				List<Integer> nextKey = Arrays.asList(jobID, taskID + 1);

				model.addGreaterOrEqual(allTasks.get(nextKey).deb, allTasks.get(prevKey).fin);
				
				model.addLessOrEqual(allTasks.get(nextKey).deb, allTasks.get(prevKey).derive);

				model.addGreaterThan(allTasks.get(nextKey).deb, allTasks.get(prevKey).arriveePont);

			}

		}


		//--------------------------------------------------------------------------------------------
		//--------------------------------------------------------------------------------------------


		// Makespan objective.
		IntVar objVar = model.newIntVar(0, horizon, "makespan");
		List<IntVar> ends = new ArrayList<>();
		for (int jobID = 0; jobID < allJobs.size(); ++jobID) {
			List<Task> job = allJobs.get(jobID).tasksJob;
			List<Integer> key = Arrays.asList(jobID, job.size() - 1);
			ends.add(allTasks.get(key).fin);
		}

		model.addMaxEquality(objVar, ends);
		model.minimize(objVar);

		// Creates a solver and solves the model.
		CpSolver solver = new CpSolver();


		Map<Integer, List<AssignedTask>> assignedJobs = new HashMap<>();
		CpSolverStatus status = solver.solve(model);



		if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {

			for (int jobID = 0; jobID < allJobs.size(); ++jobID) {
				if(CST.PrintZonesTime) allJobs.get(jobID).printZoneTimes(solver);
				if(CST.PrintMvtsPont) allJobs.get(jobID).printMvtsPonts(solver);
			}


			System.out.println("Solution:");
			// Create one list of assigned tasks per Zone.

			for (int jobID = 0; jobID < allJobs.size(); ++jobID) {
				List<Task> job = allJobs.get(jobID).tasksJob;
				for (int taskID = 0; taskID < job.size(); ++taskID) {
					Task task = job.get(taskID);
					List<Integer> key = Arrays.asList(jobID, taskID);
					AssignedTask assignedTask = new AssignedTask(
							jobID, taskID, task.numzone,
							(int) solver.value(allTasks.get(key).deb), 
							task.duration,(int) solver.value(allTasks.get(key).intervalReel.getEndExpr()));
					assignedJobs.computeIfAbsent(task.numzone, (Integer k) -> new ArrayList<>());
					assignedJobs.get(task.numzone).add(assignedTask);
				}
			}
			if(CST.PrintTaskTime) {
				// Create per Zone output lines.
				String output = "";
				for (int numzone : numzoneArr) {

					if(assignedJobs.get(numzone) == null) continue;

					// Sort by starting time.
					Collections.sort(assignedJobs.get(numzone), new SortTasks());
					String solLineTasks = "Zone " + numzone + ": ";
					String solLine = "           ";

					for (AssignedTask assignedTask : assignedJobs.get(numzone)) {
						String name = "job_" + assignedTask.jobID + "_task_" + assignedTask.taskID;
						// Add spaces to output to align columns.
						solLineTasks += String.format("%-15s", name);

						String solTmp =
								"[" + assignedTask.start + "," + (assignedTask.start + assignedTask.duration) + "]";
						// Add spaces to output to align columns.
						solLine += String.format("%-15s", solTmp);
					}
					output += solLineTasks + "%n";
					output += solLine + "%n";
				}
				System.out.printf("Optimal Schedule Length: %f%n", solver.objectiveValue());
				System.out.printf(output);
			}



		} else {
			System.out.println("No solution found.");
			return;
		}


		// Statistics.
		System.out.println("\n/////////////////////////////////////////////////////////\n");
		System.out.println("Statistics");
		System.out.printf("  conflicts: %d%n", solver.numConflicts());
		System.out.printf("  branches : %d%n", solver.numBranches());
		System.out.printf("  wall time: %f s%n", solver.wallTime());






		SwingUtilities.invokeLater(() -> {  

			final GanttChart ganttTecalOR = new GanttChart(sqlCnx,"Gantt Chart idéal du 02/11/2023");
			ganttTecalOR.model_diag(assignedJobs,zonesBDD, gammeToZones);
			ganttTecalOR.pack();
			ganttTecalOR.setSize(new java.awt.Dimension(1500, 870));
			RefineryUtilities.centerFrameOnScreen(ganttTecalOR);
			ganttTecalOR.setVisible(true);

		});
		

		 

	}
}
