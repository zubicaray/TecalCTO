package org.tecal.scheduler;

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
import java.util.Map.Entry;

import javax.swing.SwingUtilities;

import org.jfree.ui.RefineryUtilities;
import org.tecal.scheduler.data.CSV_DATA;
import org.tecal.scheduler.data.SQL_DATA;
import org.tecal.scheduler.types.AssignedTask;
import org.tecal.scheduler.types.GammeType;
import org.tecal.scheduler.types.ZoneType;

import com.google.ortools.Loader;
import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.CpSolverStatus;
import com.google.ortools.sat.CumulativeConstraint;
import com.google.ortools.sat.IntVar;
import com.google.ortools.sat.IntervalVar;
import com.google.ortools.sat.LinearExpr;
import com.opencsv.exceptions.CsvException;



 class TempsDeplacement 		extends HashMap<Integer[],Integer[]>	{	private static final long serialVersionUID = 1L;}
 class Coord 			        extends ArrayList<IntVar> 		{	private static final long serialVersionUID = 1L;}
 class CoordsRincage 			extends ArrayList<IntVar[]> 	{	private static final long serialVersionUID = 1L;}
 class ArrayCoordsRincagePonts  extends ArrayList<CoordsRincage>{	private static final long serialVersionUID = 1L;}
 class ZonesIntervalVar 		extends ArrayList<IntervalVar> 	{	private static final long serialVersionUID = 1L;}
 class ListeZone 				extends ArrayList<IntervalVar> 	{	private static final long serialVersionUID = 1L;}
 class ArrayListeZonePonts   	extends ArrayList<ListeZone>	{	private static final long serialVersionUID = 1L;}
 class ListeTaskOrdo			extends ArrayList<TaskOrdo> 	{	private static final long serialVersionUID = 1L;}

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
	int egouttage;
	Task( int duration,int numzone,int egouttage) {
		this.duration = duration;
		this.numzone=numzone;
		this.egouttage=egouttage;
	}
}

public class TecalOrdo {


	// map de toutes les gammes
	private HashMap<String, ArrayList<GammeType> > mGammes;
	private HashMap<Integer, ArrayList<GammeType> > mBarreZones;
	private LinkedHashMap<Integer,String> mBarreGammes;
	private SQL_DATA sqlCnx ;
	@SuppressWarnings("unused")
	private  SQL_DATA getSqlCnx() {return sqlCnx;}
	
	private boolean hasSolution;


	private CSV_DATA csv ;

	private HashMap<Integer,ZoneType> zonesBDD;
	// lien entre id de la table Zone et les zones de l'ordo
	private Integer[] numzoneArr;

	// Creates the model.
	private	List<JobType> allJobs= new ArrayList<>();
	public List<JobType> getAllJobs() {
		return allJobs;
	}


	private	Map<List<Integer>, TaskOrdo> 	allTasks 			= new HashMap<>();
	private	Map<Integer, List<IntervalVar>> zoneToIntervals 	= new HashMap<>();
	private	Map<Integer, List<IntervalVar>> multiZoneIntervals 	= new HashMap<>();
	private	Map<Integer, List<IntervalVar>> cumulDemands		= new HashMap<>();


	private Map<Integer, List<AssignedTask>> assignedTasksByNumzone;
	private Map<Integer, List<AssignedTask>> assignedTasksByJobId;
	
	
	public Map<Integer, List<AssignedTask>> getAssignedTasksByJobId() {
		return assignedTasksByJobId;
	}


	private CpModel model;

	static int horizon = 0;

	private StringBuilder outputMsg;
	public StringBuilder getOutputMsg() {
		return outputMsg;
	}


	private int mSource;

	@SuppressWarnings("unused")
	private int mTEMPS_ZONE_OVERLAP_MIN=0;
	// temps incompresible d'un mouvement d epoint
	@SuppressWarnings("unused")
	private int mTEMPS_MVT_PONT_MIN_JOB=0;
	@SuppressWarnings("unused")
	private	 int mGAP_ZONE_NOOVERLAP=0 ;
	// temps autour d'un début de grosse zone
	@SuppressWarnings("unused")
	private int mTEMPS_MVT_PONT=0 ;
	// temps de sécurité entre deux gammes différentes sur un même poste d'ano
	@SuppressWarnings("unused")
	private int mTEMPS_ANO_ENTRE_P1_P2=0;
	@SuppressWarnings("unused")
	private int mTEMPS_MAX_SOLVEUR=0;

	public TecalOrdo(int source) {

		mBarreZones=new  HashMap< >();
		sqlCnx=SQL_DATA.getInstance();

		Loader.loadNativeLibraries();
		//model = new CpModel();

		outputMsg=new StringBuilder();

		setDataSource(source);

	}




	public void  setParams(int[] inParams) {

		mTEMPS_ZONE_OVERLAP_MIN=inParams[0];
		// temps incompresible d'un mouvement d epoint
		mTEMPS_MVT_PONT_MIN_JOB =inParams[1];
		//temps entre les différentes "zones regroupées"
		mGAP_ZONE_NOOVERLAP =inParams[2];
		// temps autour d'un début de grosse zone
		mTEMPS_MVT_PONT =inParams[3];
		// temps de sécurité entre deux gammes différentes sur un même poste d'ano
		mTEMPS_ANO_ENTRE_P1_P2=inParams[4];

		mTEMPS_MAX_SOLVEUR=inParams[4];
	}

	public void  setDataSource(int source) {

		setSource(source);

		if(CST.SQLSERVER ==source) {

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
	public LinkedHashMap<Integer,String>  setBarresTest() {
		mBarreGammes=new LinkedHashMap<Integer,String>();
		
		int i=0;
		for(String gamme: CST.gammesTest ) {
			i++;
			mBarreZones.put(i, mGammes.get(gamme));
			mBarreGammes.put(i, gamme);
		}
		
		return mBarreGammes;
	}

	public Map<Integer, List<AssignedTask>> getAssignedJobs() {
		return assignedTasksByNumzone;
	}

	public HashMap<Integer, ArrayList<GammeType>> getBarres() {
		return mBarreZones;
	}

	public void setBarres(LinkedHashMap<Integer,String> inBarres) {

		mBarreGammes=inBarres;
		mBarreZones.clear();
		for (Map.Entry<Integer,String > entry : inBarres.entrySet()) {

			int numbarre=entry.getKey();
			String gamme=entry.getValue();
			mBarreZones.put(numbarre, mGammes.get(gamme));

		}

	}


	public LinkedHashMap<Integer,String>  runTest() {

		
		LinkedHashMap<Integer,String> res=setBarresTest();
		int[] params= {
				CST.TEMPS_ZONE_OVERLAP_MIN,
				CST.TEMPS_MVT_PONT_MIN_JOB,
				CST.GAP_ZONE_NOOVERLAP,
				CST.TEMPS_MVT_PONT,CST.TEMPS_ANO_ENTRE_P1_P2,5//CST.TEMPS_MAX_SOLVEUR
		};

		setParams(params);
	

		execute();
		return res;
	}

	public void  run(LinkedHashMap<Integer,String> inBarres) {
	
		setBarres(inBarres);		
		execute();
	}

	private void execute() {
		model=new CpModel();
		prepareZones();
		//--------------------------------------------------------------------------------------------
		// CONSTRAINTES SUR CHAQUE JOB
		//--------------------------------------------------------------------------------------------
		jobConstraints();
		//--------------------------------------------------------------------------------------------
		// PRECEDENCES
		//--------------------------------------------------------------------------------------------
		jobsPrecedence();
		//--------------------------------------------------------------------------------------------
		// CONSTRAINTES SUR CHAQUE POSTE
		//--------------------------------------------------------------------------------------------
		machineConstraints();
		bridgesConstraints();
		brigesSecurity();
		//--------------------------------------------------------------------------------------------
		//--------------------------------------------------------------------------------------------
		// sur les postes d'oxy, faire en sorte que le pont 2 ne puisse pas croiser le pont et récproquement
		zoneCumulConstraints();


		// Makespan objective.
		IntVar objVar = model.newIntVar(0, horizon, "makespan");
		List<IntVar> ends = new ArrayList<>();
		List<IntVar> starts = new ArrayList<>();
		for (int jobCpt = 0; jobCpt < allJobs.size(); ++jobCpt) {
			List<Task> job = allJobs.get(jobCpt).tasksJob;
			List<Integer> key = Arrays.asList(jobCpt, job.size() - 1);
			ends.add(allTasks.get(key).fin);
			starts.add(allTasks.get(key).startBDD);
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
		//if(modeFast) {
		//	solver.getParameters().setStopAfterFirstSolution(true);
		//}

		// PARAM MIRACLE
		solver.getParameters().setMaxTimeInSeconds(mTEMPS_MAX_SOLVEUR);


		assignedTasksByNumzone = new HashMap<>();
		assignedTasksByJobId = new HashMap<>();
		CpSolverStatus status = solver.solve(model);

		hasSolution=false;

		if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
			
			hasSolution=true;

			for (JobType job : allJobs) {
				if(CST.PrintZonesTime) {
					job.printZoneTimes(solver);
				}
			}

			outputMsg.append("-----------------------------------------------------------------.");
			outputMsg.append(System.getProperty("line.separator"));
			outputMsg.append("Solution:");
			outputMsg.append(System.getProperty("line.separator"));
			// Create one list of assigned tasks per Zone.

			createAssignedTasks(solver);


			if(CST.PrintTaskTime) {
				// Create per Zone output lines.
				String output = "";
				for (int numzone : numzoneArr) {

					if(assignedTasksByNumzone.get(numzone) == null) {
						continue;
					}

					// Sort by starting time.
					Collections.sort(assignedTasksByNumzone.get(numzone), new SortTasks());
					String solLineTasks = "Zone " + numzone + ": ";
					String solLine = "           ";

					for (AssignedTask assignedTask : assignedTasksByNumzone.get(numzone)) {
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
	}

	private void createAssignedTasks(CpSolver solver) {
		for (int jobID = 0; jobID < allJobs.size(); ++jobID) {
			List<Task> job = allJobs.get(jobID).tasksJob;
			for (int taskID = 0; taskID < job.size(); ++taskID) {
				Task task = job.get(taskID);
				List<Integer> key = Arrays.asList(jobID, taskID);

				int debut=(int) solver.value(allTasks.get(key).startBDD);
				int fin=(int) solver.value(allTasks.get(key).endBDD);

				int derive;
				// on ne sait pas à quel moment entre le min et le max de dérive
				// le solveur a choisit => on doit regarder quand commence la tache d'apres
				// pour calculer la dérive
				if(task.numzone != CST.DECHARGEMENT_NUMZONE){
					List<Integer> keySuivante = Arrays.asList(jobID, taskID+1);
					derive=(int) solver.value(allTasks.get(keySuivante).startBDD)-allTasks.get(key).tempsDeplacement-allTasks.get(key).egouttage;
				} else {
					derive=(int) solver.value(allTasks.get(key).deriveMax);
				}


				if( task.numzone == CST.CHARGEMENT_NUMZONE) {
					fin=derive;
				}

				AssignedTask assignedTask = new AssignedTask(
						jobID, taskID, task.numzone,
						debut,
						fin-debut,derive);
				assignedTasksByNumzone.computeIfAbsent(task.numzone, (Integer k) -> new ArrayList<>());
				assignedTasksByNumzone.get(task.numzone).add(assignedTask);
				
				assignedTasksByJobId.computeIfAbsent(jobID, (Integer k) -> new ArrayList<>());
				assignedTasksByJobId.get(jobID).add(assignedTask);
				
			}
		}
	}

	public String  print() {
		return outputMsg.toString();
	}
	public static void main(String[] args) throws IOException, CsvException, URISyntaxException {

		TecalOrdo tecalOrdo=new TecalOrdo(CST.SQLSERVER);



		tecalOrdo.runTest();
		if(CST.PRINT_PROD_DIAG ) {
			SwingUtilities.invokeLater(() -> {

				 final GanttChart ganttTecal = new GanttChart("Gantt Chart prod du 02/11/2023");
				 ganttTecal.prod_diag(CST.mListeOf26janvier,CST.getDate("20240126"));
				 ganttTecal.pack();
				 ganttTecal.setSize(new java.awt.Dimension(1500, 870));
			     RefineryUtilities.centerFrameOnScreen(ganttTecal);
			     ganttTecal.setVisible(true);

			});
		}

		System.out.printf(tecalOrdo.print());

	}

	private  void prepareZones() {

		int cptJob=0;
		allJobs.clear();
		allTasks.clear();
		zoneToIntervals.clear();
		multiZoneIntervals.clear();
		cumulDemands.clear();
		for (Map.Entry<Integer, ArrayList<GammeType> > entry : mBarreZones.entrySet()) {

			int numBarre=entry.getKey();
			String name=numBarre+"-"+mBarreGammes.get(numBarre);
			
			JobType job = new JobType(cptJob, name,model);
			cptJob++;
			//String lgamme = entry.getKey();
			List<GammeType>  zones = entry.getValue();
			// on calcul les indexes des zones a regrouper par pont
			job.computeCoords(zones);
			allJobs.add(job);
		}

		// Computes horizon dynamically as the sum of all durations.
		horizon=0;
		for (JobType job : allJobs) {
			for (Task task : job.tasksJob) {
				horizon += task.duration;
			}
		}

		System.out.println("HORIZON=" + horizon);

		for (JobType job : allJobs) {
			job.horizon=horizon;
		}



		for (int jobID = 0; jobID < allJobs.size(); ++jobID) {
			JobType job = allJobs.get(jobID);

			//on créé les zones avec leut temps de déplacement, égouttage, etc ...
			job.addIntervalForModel(allTasks,zoneToIntervals,multiZoneIntervals,cumulDemands,jobID,zonesBDD);
			//on créé les zones corespondant a mouvement des ponts
			job.simulateBridgesMoves();
			// on identifie et  regroupe les zones trop courte pour autoriser un mvt du pont
			job.ComputeZonesNoOverlap(jobID, allTasks);
			// regroupement des zones qui pourraient être trop proches de zones d'autre jobs sur pont adverses
			job.makeSafetyBetweenBridges();

		}

		for (int jobID = 0; jobID < allJobs.size(); ++jobID) {

			//allJobs.get(jobID).ComputeZonesNoOverlap(jobID, allTasks);
			//allJobs.get(jobID).printNoOverlapZones();

		}


	}
	/**
	 * certaines zones peuvent avoir plusieurs barres en même temps
	 * @param model
	 */
	private  void jobConstraints() {

		HashMap<Integer,CumulativeConstraint> cumulConstr= new HashMap<>();

		// Create and add disjunctive constraints.
		for (int numzone : numzoneArr) {

			if(  zoneToIntervals.containsKey(numzone)) {
				List<IntervalVar> intervalParZone = zoneToIntervals.get(numzone);
				model.addNoOverlap(intervalParZone);
			}
			if(  multiZoneIntervals.containsKey(numzone)) {
				List<IntervalVar> listCumul = multiZoneIntervals.get(numzone);
				ZoneType zt=zonesBDD.get(numzone);
				// zone autorisant le "chevauchement" => zone contenant plus de  1 postes
				IntVar capacity = model.newIntVar(0, zt.cumul, "capacity_of_"+numzone);

				CumulativeConstraint cumul =model.addCumulative(capacity);

				long[] zoneUsage  = new long[listCumul.size()];
				Arrays.fill(zoneUsage,1);
				cumul.addDemands(listCumul.toArray(new IntervalVar[0]), zoneUsage);
				cumulConstr.put(numzone, cumul);

			}

		}
		// parse the intervals of single task machine that belongs to the "cumulative" machines (multi tasks machine)
        for (Entry<Integer, List<IntervalVar>> entry : cumulDemands.entrySet()) {
            int idCumulZone = entry.getKey();
            //get the constraint on the current "cumulative" machine
            CumulativeConstraint cumul=cumulConstr.get(idCumulZone);
            List<IntervalVar> inters = entry.getValue();
            for(IntervalVar iv:inters) {
            	cumul.addDemand(iv, 1);
            }


        }




	}
	// on impose du temps entre la fin du zone et le début d'une autre
	private  void zoneCumulConstraints() {

		if(CST.CSTR_ECART_ZONES_CUMULS)
		{

			for(List<IntervalVar> intervalParZone :multiZoneIntervals.values()) {
				List<IntervalVar> nooverlapAno=new  ArrayList<> ();
				for( int i=0;i<intervalParZone.size();i++) {

					IntervalVar interval=intervalParZone.get(i);
					LinearExpr debInter=interval.getStartExpr();
					LinearExpr finInter=interval.getEndExpr();

					// TODO
					// !!!
					// comprendre pourquoi ca bloque ici quand les params changent
					IntervalVar deb=getNoOverlapZone(model,debInter,30,30);
					IntervalVar fin=getNoOverlapZone(model,finInter,30,30);



					nooverlapAno.add(deb);
					nooverlapAno.add(fin);

				}
				model.addNoOverlap(nooverlapAno);
			}
		}

	}


	private  void jobsPrecedence () {

		// Precedences inside a job.
		for (int jobID = 0; jobID < allJobs.size(); ++jobID) {
			List<Task> jobTasks = allJobs.get(jobID).tasksJob;

			for (int taskID = 0; taskID < jobTasks.size() - 1; ++taskID) {
				List<Integer> prevKey = Arrays.asList(jobID, taskID);
				List<Integer> nextKey = Arrays.asList(jobID, taskID + 1);

				// last OK
				//le debut de la zone suivante doit etre compris
				//entre le début et la fin de la dérive
				model.addLessOrEqual(allTasks.get(nextKey).startBDD, allTasks.get(prevKey).fin);
				model.addGreaterOrEqual(allTasks.get(nextKey).startBDD, allTasks.get(prevKey).deriveNulle);


			}

		}

	}

	private  void machineConstraints() {



		//---------------------------------------------------------------------------
		// NOOVERLAP ZONES REGROUPEES -----------------------------------------------
		//---------------------------------------------------------------------------

		if(CST.CSTR_NOOVERLAP_ZONES_GROUPEES)
		{
			// les zones de rincages ne doivent pas se croiser

			ArrayList<ZonesIntervalVar> listZonesNoOverlapParPont  = new  ArrayList<>();
			listZonesNoOverlapParPont.add( new  ZonesIntervalVar()); // add zones pont 1
			listZonesNoOverlapParPont.add( new  ZonesIntervalVar()); // add zones pont 2
			//---------------------------------------------------------------------------
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
			//le débuts des zones longues des autres jobs
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
			    		 if(k==j) {
							continue;
						}
			    		 zonesGroupeesWithAllOthers.addAll(allJobs.get(k).debutLonguesZonesPont.get(pont));
			    		 zonesLonguesWithAllOthers.addAll(allJobs.get(k).debutLonguesZonesPont.get(pont));
			    	 }

			    	 model.addNoOverlap(zonesGroupeesWithAllOthers);
			    	 model.addNoOverlap(zonesLonguesWithAllOthers);

				}
			}

		}
	}
	private void brigesSecurity () {

		if(CST.CSTR_BRIDGES_SECURITY) {
			ListeZone zonesAutourAnodisation=new ListeZone();
			for (JobType job : allJobs) {
				/*
		    	 IntervalVar z1=getNoOverlapZone(model,job.mTaskOrdoList.get(job.indexLastZoneP1).intervalBDD);
		    	 zonesAutourAnodisation.add(z1);
		    	 IntervalVar z2=getNoOverlapZone(model,job.mTaskOrdoList.get(job.indexFirstZoneP2).intervalBDD);
		    	 zonesAutourAnodisation.add(z2);
		    	 */
		    	 zonesAutourAnodisation.addAll(job.mNoOverlapP1P2);


			}
			model.addNoOverlap(zonesAutourAnodisation);
		}


	}

	private  void bridgesConstraints() {


		//---------------------------------------------------------------------------
		// CONTRAINTES SUR PONTS -----------------------------------------------
		//---------------------------------------------------------------------------

		if(CST.CSTR_NOOVERLAP_BRIDGES)
		{
			ArrayList<ZonesIntervalVar> listZonesNoOverlapParPont  = new  ArrayList<>();
			listZonesNoOverlapParPont.add( new  ZonesIntervalVar()); // add zones pont 1
			listZonesNoOverlapParPont.add( new  ZonesIntervalVar()); // add zones pont 2
			for (JobType j  :allJobs) {
				int p=0;
				for(ListeZone bridgeMoveP :j.bridgesMoves) {
					listZonesNoOverlapParPont.get(p).addAll(bridgeMoveP);
					p++;
				}
				p=0;
			}

			for(ArrayList<IntervalVar> listZonesNoOverlap: listZonesNoOverlapParPont) {
				model.addNoOverlap(listZonesNoOverlap);
			}
		}
	}


	static IntVar getBackward(CpModel model,IntVar mvtPont,int decay){


		IntVar decayed=model.newIntVar(0, horizon, "");
		model.newIntervalVar(
				decayed,
				LinearExpr.constant(decay)
				, mvtPont,
				"");

		return decayed;

	}
	static IntVar getForeward(CpModel model,IntVar mvtPont,int decay){


		IntVar decayed=model.newIntVar(0, horizon, "");
		 model.newIntervalVar(
				 mvtPont,
				LinearExpr.constant(decay)
				, decayed,
				"");

		return decayed;

	}
	static IntervalVar getNoOverlapZone(CpModel model,IntVar mvtPont,int left,int right){

		IntervalVar before = model.newIntervalVar(
				model.newIntVar(0, horizon, ""),
				LinearExpr.constant(left)
				, mvtPont,
				"");

		IntervalVar after = model.newIntervalVar(mvtPont,
				LinearExpr.constant(right),
				model.newIntVar(0, horizon, ""),
				"");

		return model.newIntervalVar(before.getStartExpr(),model.newIntVar(0, horizon,  ""),after.getEndExpr(),"");

	}

	static IntervalVar getNoOverlapZone(CpModel model,LinearExpr mvtPont){

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
	static IntervalVar getNoOverlapZone(CpModel model,LinearExpr mvtPont,int left,int right){

		IntervalVar before = model.newIntervalVar(
				model.newIntVar(0, horizon, ""),
				LinearExpr.constant(left)
				, mvtPont,
				"");

		IntervalVar after = model.newIntervalVar(mvtPont,
				LinearExpr.constant(right),
				model.newIntVar(0, horizon, ""),
				"");

		return model.newIntervalVar(before.getStartExpr(),model.newIntVar(0, horizon,  ""),after.getEndExpr(),"");

	}
	static IntervalVar getNoOverlapZone(CpModel model,IntVar mvtPont){

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

	static IntervalVar getNoOverlapZone(CpModel model,IntVar mvtPontStart,IntVar mvtPontEnd){

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

static IntervalVar getNoOverlapZone(CpModel model,IntervalVar mvt){

		IntervalVar before = model.newIntervalVar(
				model.newIntVar(0, horizon, ""),
				LinearExpr.constant(CST.TEMPS_MVT_PONT)
				, mvt.getStartExpr(),
				"");

		IntervalVar after = model.newIntervalVar( mvt.getEndExpr(),
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




	public boolean hasSolution() {
		return hasSolution;
	}






	public LinkedHashMap<Integer,String> getBarreGammes() {
		return mBarreGammes;
	}




	public void setBarreGammes(LinkedHashMap<Integer,String> mBarreGammes) {
		this.mBarreGammes = mBarreGammes;
	}
}

