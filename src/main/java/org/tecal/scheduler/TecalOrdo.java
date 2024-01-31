package org.tecal.scheduler;

import com.google.ortools.Loader;
import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.CpSolverStatus;
import com.google.ortools.sat.CumulativeConstraint;
//import com.google.ortools.sat.DecisionStrategyProto;
import com.google.ortools.sat.IntVar;
import com.google.ortools.sat.IntervalVar;
import com.google.ortools.sat.LinearExpr;
import com.opencsv.exceptions.CsvException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.ui.RefineryUtilities;
import org.tecal.scheduler.data.CSV_DATA;
import org.tecal.scheduler.data.SQL_DATA;
import org.tecal.scheduler.types.GammeType;
import org.tecal.scheduler.types.ZoneType;
import org.tecal.ui.CPO;


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
	
	
	// map de toutes les gammes
	HashMap<String, ArrayList<GammeType> > mGammes;
	// map des barres associé à leut gamme
	private HashMap<String, ArrayList<GammeType> > mBarres;
	private SQL_DATA sqlCnx ;
	public SQL_DATA getSqlCnx() {
		return sqlCnx;
	}

	public void setSqlCnx(SQL_DATA sqlCnx) {
		this.sqlCnx = sqlCnx;
	}

	private CSV_DATA csv ;

	private HashMap<Integer,ZoneType> zonesBDD;
	// lien entre id de la table Zone et les zones de l'ordo
	private Integer[] numzoneArr;

	// Creates the model.
	private	List<JobType> allJobs= new ArrayList<JobType>();
	private	Map<List<Integer>, TaskOrdo> allTasks = new HashMap<>();
	private	Map<Integer, List<IntervalVar>> zoneToIntervals = new HashMap<>();
	private	Map<Integer, List<IntervalVar>> zoneCumulToIntervals = new HashMap<>();
	
	private CpModel model;
	
	static int horizon = 0;
	
	private StringBuilder outputMsg;
	private int mSource;
	
	public TecalOrdo(int source) {
		
		mBarres=new  HashMap<String, ArrayList<GammeType> >();		
		
		Loader.loadNativeLibraries();
		model = new CpModel();
		outputMsg=new StringBuilder();
		
		setDataSource(source);
		
	}
	
	public void  setDataSource(int source) {

		setSource(source);
		
		if(CST.SQLSERVER ==source) {
			sqlCnx = new SQL_DATA();
			mGammes=sqlCnx.getLignesGammesAll();
			zonesBDD=sqlCnx.getZones();
		}else {
			
			try {
				csv = new CSV_DATA();
			} catch (IOException | CsvException | URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			 
			mGammes=csv.getLignesGammesAll();				 
			zonesBDD=csv.getZones();
			
		}
		
		
		 
		 numzoneArr= zonesBDD.keySet().toArray(new Integer[0]);
		
	}
	public void setBarresTest() {
	 int i=0;
	 for(String gamme: CST.gammesTest ) {
		 i++;
		 mBarres.put(i+"-"+gamme, mGammes.get(gamme));
		 
	 }
	};
	
	public void setBarres(LinkedHashMap<Integer,String> inBarres) {
		
		mBarres.clear();
		for (Map.Entry<Integer,String > entry : inBarres.entrySet()) {
			
			int numbarre=entry.getKey();
			String gamme=entry.getValue();
			 mBarres.put(numbarre+"-"+gamme, mGammes.get(gamme));
		
		}
		
	}
	
	public void  run(boolean modeFast,int contrainteLEvel,CPO ganttFrame) {

		
		prepareZones(model,modeFast,contrainteLEvel);
		
		
		

		//--------------------------------------------------------------------------------------------
		// CONSTRAINTES SUR CHAQUE JOB
		//--------------------------------------------------------------------------------------------
		jobConstraints(model);	
		//--------------------------------------------------------------------------------------------
		// PRECEDENCES
		//--------------------------------------------------------------------------------------------
		jobsPrecedence(model);
		//--------------------------------------------------------------------------------------------
		// CONSTRAINTES SUR CHAQUE POSTE
		//--------------------------------------------------------------------------------------------
		machineConstraints(model);
		//--------------------------------------------------------------------------------------------
		//--------------------------------------------------------------------------------------------


		// Makespan objective.
		IntVar objVar = model.newIntVar(0, horizon, "makespan");
		List<IntVar> ends = new ArrayList<>();
		List<IntVar> starts = new ArrayList<>();
		for (int jobID = 0; jobID < allJobs.size(); ++jobID) {
			List<Task> job = allJobs.get(jobID).tasksJob;
			List<Integer> key = Arrays.asList(jobID, job.size() - 1);
			ends.add(allTasks.get(key).fin);
			starts.add(allTasks.get(key).deb);
		}

		
		
		model.addMaxEquality(objVar, ends);
		model.minimize(objVar);
		/*
		model.addDecisionStrategy(starts.toArray(new IntVar[0]), 
				DecisionStrategyProto.VariableSelectionStrategy.CHOOSE_FIRST,
				DecisionStrategyProto.DomainReductionStrategy.SELECT_MIN_VALUE);
		 */
		// Creates a solver and solves the model.
		CpSolver solver = new CpSolver();
		//solver.getParameters().setNumWorkers(1);
		if(modeFast) {
			solver.getParameters().setStopAfterFirstSolution(true);	
		}
		
		//solver.getParameters().setStopAfterRootPropagation(true);
		
		


		Map<Integer, List<AssignedTask>> assignedJobs = new HashMap<>();
		CpSolverStatus status = solver.solve(model);

		

		if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {

			for (int jobID = 0; jobID < allJobs.size(); ++jobID) {
				if(CST.PrintZonesTime) allJobs.get(jobID).printZoneTimes(solver);
				
			}

			outputMsg.append("-----------------------------------------------------------------.");
			outputMsg.append(System.getProperty("line.separator"));
			outputMsg.append("Solution:");
			outputMsg.append(System.getProperty("line.separator"));
			// Create one list of assigned tasks per Zone.

			for (int jobID = 0; jobID < allJobs.size(); ++jobID) {
				List<Task> job = allJobs.get(jobID).tasksJob;
				for (int taskID = 0; taskID < job.size(); ++taskID) {
					Task task = job.get(taskID);
					List<Integer> key = Arrays.asList(jobID, taskID);
					
					int debut=(int) solver.value(allTasks.get(key).startBDD);
					int fin=(int) solver.value(allTasks.get(key).endBDD);
					int derive=(int) solver.value(allTasks.get(key).finDerive.getStartExpr());
					
					if( task.numzone == CST.CHARGEMENT_NUMZONE) {
						fin=derive;
					}
					
					AssignedTask assignedTask = new AssignedTask(
							jobID, taskID, task.numzone,
							debut, 
							fin-debut,derive);
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
				outputMsg.append(String.format("Optimal Schedule Length: %f%n", solver.objectiveValue()));
				outputMsg.append(System.getProperty("line.separator"));
				outputMsg.append(output);
				outputMsg.append(System.getProperty("line.separator"));
			}



		} else {
			outputMsg.append("No solution found.");
			outputMsg.append(System.getProperty("line.separator"));
			return;
		}


		// Statistics.
		//outputMsg.append("\n/////////////////////////////////////////////////////////\n");
		outputMsg.append("Statistiques:\n");
		outputMsg.append(String.format("  conflits: %d%n", solver.numConflicts()));
		outputMsg.append(String.format("  branches : %d%n", solver.numBranches()));
		outputMsg.append(String.format("  temps %f s%n", solver.wallTime()));






		

		GanttChart ganttTecalOR = new GanttChart(sqlCnx,"Gantt Chart idéal du 02/11/2023");
		JFreeChart chart=ganttTecalOR.model_diag(assignedJobs,zonesBDD, mBarres);
		
		ChartPanel cp = new ChartPanel(chart);
		
		ganttFrame.addGantt(cp);
		
		ganttFrame.pack();
		ganttFrame.setSize(new java.awt.Dimension(1500, 870));
		RefineryUtilities.centerFrameOnScreen(ganttFrame);
		ganttFrame.setVisible(true);

		
		
	}
	
	public String  print() {
		return outputMsg.toString();
	}
	public static void main(String[] args) throws IOException, CsvException, URISyntaxException {
		
		TecalOrdo tecalOrdo=new TecalOrdo(CST.SQLSERVER);		
			
		tecalOrdo.setBarresTest();
		
		CPO frame=new CPO(null);
		
		tecalOrdo.run(CST.MODE_FAST,CST.PORTION_HORIZON,frame);		
		if(CST.PRINT_PROD_DIAG )
			SwingUtilities.invokeLater(() -> {  

				 final GanttChart ganttTecal = new GanttChart(tecalOrdo.getSqlCnx(),"Gantt Chart prod du 02/11/2023");
				 ganttTecal.prod_diag();
				 ganttTecal.pack();
				 ganttTecal.setSize(new java.awt.Dimension(1500, 870));
			     RefineryUtilities.centerFrameOnScreen(ganttTecal);
			     ganttTecal.setVisible(true);

			});

		System.out.printf(tecalOrdo.print());	

	}
	
	private  void prepareZones(CpModel model,boolean modeFast,int contrainteLEvel) {
		
		int cptJob=0;
		allJobs.clear();
		allTasks.clear();
		zoneToIntervals.clear();
		zoneCumulToIntervals.clear();
		for (Map.Entry<String, ArrayList<GammeType> > entry : mBarres.entrySet()) {

			JobType job = new JobType(cptJob, entry.getKey(),model);
			//String lgamme = entry.getKey();
			List<GammeType>  zones = entry.getValue();
			//System.out.println("gamme=" + lgamme + ", nb zone Value length=" + zones.size());
			job.addZones(zones);       
			allJobs.add(job);
		}

		// Computes horizon dynamically as the sum of all durations.
		horizon=0;
		for (JobType job : allJobs) {
			for (Task task : job.tasksJob) {
				horizon += task.duration;
			}
		}
		
	
		
		if(modeFast) {
			int max_time_job=0;
			for (JobType job : allJobs) {
				int t=0;
				for (Task task : job.tasksJob) {
					t += task.duration;
				}
				if(t>max_time_job) max_time_job=t;
			}
			
			horizon=max_time_job+(max_time_job/contrainteLEvel)*allJobs.size();
			
			
		}
		System.out.println("HORIZON=" + horizon);

		for (JobType job : allJobs) job.horizon=horizon;
		

		
		for (int jobID = 0; jobID < allJobs.size(); ++jobID) {
			JobType job = allJobs.get(jobID);
			job.addIntervalForModel(allTasks,zoneToIntervals,zoneCumulToIntervals,jobID,zonesBDD);
			
		}

		for (int jobID = 0; jobID < allJobs.size(); ++jobID) {

			allJobs.get(jobID).ComputeZonesNoOverlap(jobID, allTasks);  
			//allJobs.get(jobID).printNoOverlapZones();
			
		}

	
	}
	/**
	 * certaines zones peuvent avoir plusieurs barres en même temps
	 * @param model
	 */
	private  void jobConstraints(CpModel model) {
		// Create and add disjunctive constraints.		
		for (int numzone : numzoneArr) {

			if(  zoneToIntervals.containsKey(numzone)) {    	 
				List<IntervalVar> intervalParZone = zoneToIntervals.get(numzone);  
				model.addNoOverlap(intervalParZone);    	  
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
	}
	/**
	 * 
	 * au sein d'un même job, le début de la zone suivante
	 * se situe à 2*CST.TEMPS_MVT_PONT_MIN_JOB de la fin de l'autre au minimum
	 * et plus à la dérive précédente + 2*CST.TEMPS_MVT_PONT_MIN_JOB
	 * @param model
	 */
	private  void jobsPrecedence (CpModel model) {

		// Precedences inside a job.
		for (int jobID = 0; jobID < allJobs.size(); ++jobID) {
			List<Task> jobTasks = allJobs.get(jobID).tasksJob;
			for (int taskID = 0; taskID < jobTasks.size() - 1; ++taskID) {
				List<Integer> prevKey = Arrays.asList(jobID, taskID);
				List<Integer> nextKey = Arrays.asList(jobID, taskID + 1);

				model.addLessOrEqual(allTasks.get(nextKey).deb, allTasks.get(prevKey).fin);
				
				model.addGreaterOrEqual(allTasks.get(nextKey).deb, allTasks.get(prevKey).finDerive.getEndExpr());

				//model.addGreaterThan(allTasks.get(nextKey).deb, allTasks.get(prevKey).arriveePont);

			}

		}

	}
	/**
	 * zone regroupée: ensemble de zone dont la durée est inférieure à TEMPS_ZONE_OVERLAP_MIN=180
	 * voici l'algo principale
	 * 1/les zones regroupées ne doivent pas se chevaucher entre job, la marge de sécurité à gauhe et à droite est CST.GAP_ZONE_NOOVERLAP = 90 
	 * 2/ chaque début de zones longues ne doit pas chevauché le début d'une zone longue d'un autre job
	 * 3/ une zone regroupée du job ne doit pas chevauché le début d'une zone longue d'un autre job
	 * 
	 * partucilarités:
	 * 1/la zone juste après le chargement est élargie sur sa gauche de 30 secondes
	 * pour tenir compte du temps à venir chercher la chage au chargement
	 * car le début de la zone de chargement n'est pas à considérer pour les mvts du pont
	 * 
	 * TODO: check si cette zone est une zone groupée
	 * 
	 * comme la zone d'ano est commune au deux pont
	 * on élargie la fin de la dernière zone groupéé pour qu'elle corresponde à l'arrivée du pont 1 en ano
	 * 
	 * 
	 * TODO: check quoi si cette zone est une zone longue ??
	 * @param model
	 */
	private  void machineConstraints(CpModel model) {
		// les zones de rincages ne doivent pas se croiser

				ArrayList<ZonesIntervalVar> listZonesNoOverlapParPont  = new  ArrayList<ZonesIntervalVar>();  
				listZonesNoOverlapParPont.add( new  ZonesIntervalVar()); // add zones pont 1
				listZonesNoOverlapParPont.add( new  ZonesIntervalVar()); // add zones pont 2

				
				//---------------------------------------------------------------------------
				// NOOVERLAP ZONES REGROUPEES -----------------------------------------------
				//---------------------------------------------------------------------------
				
				if(CST.CSTR_NOOVERLAP_ZONES_GROUPEES)
				{
					
					//---------------------------------------------------------------------------
					// le débuts des zones lognues des autres jobs 
					// toutes les zones regroupées ne doivent pas se croiser
					//---------------------------------------------------------------------------
					for (JobType j  :allJobs) { 
						int p=0;    	
						for(ListeZone zonesRegroupeesP :j.tasksNoOverlapPont) {   
							listZonesNoOverlapParPont.get(p).addAll(zonesRegroupeesP);   				 
							p++;
						}
						p=0;

					}
					for(ArrayList<IntervalVar> listZonesNoOverlap: listZonesNoOverlapParPont) {
						model.addNoOverlap(listZonesNoOverlap);
					}
					//---------------------------------------------------------------------------
					//le débuts des zones lognues des autres jobs 
					// ne doivent pas croiser les zones regroupées du job en cours
					//---------------------------------------------------------------------------
					for(int pont=0;pont<CST.NB_PONTS;pont++) {
						for(int j=0;j<allJobs.size();j++) {
							JobType j1=allJobs.get(j);
							
					    	 ListeZone zonesGroupeesWithAllOthers=new ListeZone();
					    	 ListeZone zonesLonguesWithAllOthers=new ListeZone();
					    
					    	 zonesGroupeesWithAllOthers.addAll(j1.tasksNoOverlapPont.get(pont));   
					    	 zonesLonguesWithAllOthers.addAll(j1.debutLonguesZonesPont.get(pont));   
					    	 
					    	 for(int k=0;k<allJobs.size();k++) {
					    		 if(k==j) continue;			
					    		 zonesGroupeesWithAllOthers.addAll(allJobs.get(k).debutLonguesZonesPont.get(pont));	
					    		 zonesLonguesWithAllOthers.addAll(allJobs.get(k).debutLonguesZonesPont.get(pont));
					    	 }
							 
					    	 model.addNoOverlap(zonesGroupeesWithAllOthers);
					    	 model.addNoOverlap(zonesLonguesWithAllOthers);
							


						}
					}
					
				}					
	}
	
	
static IntervalVar getMvt(CpModel model,IntVar mvtPont,int horizon,int left){
		
		IntervalVar before = model.newIntervalVar(
				model.newIntVar(0, horizon, ""),
				LinearExpr.constant(CST.TEMPS_MVT_PONT*2)
				, mvtPont,
				"");
		
		IntervalVar after = model.newIntervalVar(mvtPont,
				LinearExpr.constant(CST.TEMPS_MVT_PONT), 
				model.newIntVar(0, horizon, ""),
				"");
		
		return model.newIntervalVar(before.getStartExpr(),model.newIntVar(0, horizon,  ""),after.getEndExpr(),"");
	
	}

	static IntervalVar getMvt(CpModel model,IntVar mvtPont,int horizon){
		
		IntervalVar before = model.newIntervalVar(
				model.newIntVar(0, horizon, ""),
				LinearExpr.constant(CST.TEMPS_MVT_PONT)
				, mvtPont,
				"");
		
		IntervalVar after = model.newIntervalVar(mvtPont,
				LinearExpr.constant(CST.TEMPS_MVT_PONT), 
				model.newIntVar(0, horizon, ""),
				"");
		
		return model.newIntervalVar(before.getStartExpr(),model.newIntVar(0, horizon,  ""),after.getEndExpr(),"");
	
	}
	
	static IntervalVar getMvt(CpModel model,IntVar mvtPontStart,IntVar mvtPontEnd,int horizon){
		
		IntervalVar before = model.newIntervalVar(
				model.newIntVar(0, horizon, ""),
				LinearExpr.constant(CST.TEMPS_MVT_PONT)
				, mvtPontStart,
				"");
		
		IntervalVar after = model.newIntervalVar(mvtPontEnd,
				LinearExpr.constant(CST.TEMPS_MVT_PONT), 
				model.newIntVar(0, horizon, ""),
				"");
		
		return model.newIntervalVar(before.getStartExpr(),model.newIntVar(0, horizon,  ""),after.getEndExpr(),"");
	
	}

	public int getSource() {
		return mSource;
	}

	public void setSource(int mSource) {
		this.mSource = mSource;
	}
}
