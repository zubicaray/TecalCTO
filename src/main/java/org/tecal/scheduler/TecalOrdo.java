package org.tecal.scheduler;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jfree.ui.RefineryUtilities;
import org.tecal.scheduler.data.CSV_DATA;
import org.tecal.scheduler.data.SQL_DATA;
import org.tecal.scheduler.types.AssignedTask;
import org.tecal.scheduler.types.Barre;
import org.tecal.scheduler.types.ElementGamme;
import org.tecal.scheduler.types.ZoneType;
import org.tecal.ui.GanttChart;

import com.google.ortools.Loader;
import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.CpSolverStatus;
import com.google.ortools.sat.CumulativeConstraint;
import com.google.ortools.sat.IntVar;
import com.google.ortools.sat.IntervalVar;
import com.google.ortools.sat.LinearExpr;
import com.opencsv.exceptions.CsvException;

class TempsDeplacement extends HashMap<Integer[], Integer[]> {
	private static final long serialVersionUID = 1L;
}

class Coord extends ArrayList<IntVar> {
	private static final long serialVersionUID = 1L;
}

class CoordsRincage extends ArrayList<IntVar[]> {
	private static final long serialVersionUID = 1L;
}

class ArrayCoordsRincagePonts extends ArrayList<CoordsRincage> {
	private static final long serialVersionUID = 1L;
}

class ZonesIntervalVar extends ArrayList<IntervalVar> {
	private static final long serialVersionUID = 1L;
}

class ListeZone extends ArrayList<IntervalVar> {
	private static final long serialVersionUID = 1L;
}

class ArrayListeZonePonts extends ArrayList<ListeZone> {
	private static final long serialVersionUID = 1L;
}

class ListeTaskOrdo extends ArrayList<TaskOrdo> {
	private static final long serialVersionUID = 1L;
}

class SortTasks implements Comparator<AssignedTask> {
	@Override
	public int compare(AssignedTask a, AssignedTask b) {
		if (a.start != b.start) {
			return (int) (a.start - b.start);
		} else {
			return a.duration - b.duration;
		}
	}
}



class Task {
	int numzone;
	int duration;
	int egouttage;
	int derive;
	boolean BloquePont;


	Task(int duration, int numzone, int egouttage,int derive,boolean BloquePont) {
		this.duration = duration;
		this.numzone = numzone;
		this.egouttage = egouttage;
		this.derive = derive;	
		this.BloquePont=BloquePont;
		
	}
}



public class TecalOrdo {
	
	private long mCurrentTime;
	private long mIncrementedTime=0;
	private TecalOrdoParams mParams;
	private int mTEMPS_MAX_SOLVEUR;
	private static final Logger logger = LogManager.getLogger(TecalOrdo.class);
	// map de toutes les gammes
	
	// geston des barres
	private HashMap<Integer, List<ElementGamme>>			mBarreFutures;	

	private LinkedHashMap<Integer, List<ElementGamme>>		mBarresAll;
	private LinkedHashMap<Integer, Barre>					mBarresSettings;
	private LinkedHashMap<Integer, String> 					mBarreLabels;	
	private HashSet<Integer> 								mBarresEnCours;
	private LinkedHashSet<Integer> 							mBarresPrioritaires;
	
	
	public HashSet<Integer> getBarresEnCours() { return mBarresEnCours;	}

	public void adBarreEnCours(int barre) {
		
		this.mBarresEnCours.add(barre);
		
	}
	
	public void  addFixedJobsEnCours(int barreId) {
		adBarreEnCours(barreId);
		mJobsEnCours.put(barreId, mJobsFuturs.get(barreId).makeFixedJob(mAssignedTasksByBarreId.get(barreId)));
	}

	//OBLIGATOIRE SINON : NO SOLUTION !
	public void  updateFixedJobsEnCours() {
		
		for(int barreId:mPassedTasksByBarreId.keySet()) {
			JobTypeFixed j=mJobsEnCours.get(barreId).makeFixedJob(mPassedTasksByBarreId.get(barreId));
			mJobsEnCours.remove(barreId);
			mJobsEnCours.put(barreId,j);
		}
		
	}
	private boolean hasSolution;

	private CSV_DATA csv;

	private HashMap<Integer, ZoneType> zonesBDD;
	// lien entre id de la table Zone et les zones de l'ordo
	private Integer[] numzoneArr;

	// Creates the model.
	private LinkedHashMap<Integer, JobType> mJobsEnCours 	= new LinkedHashMap<>();
	private LinkedHashMap<Integer, JobType> mJobsFuturs 	= new LinkedHashMap<>();
	private LinkedHashMap<Integer, JobType> mAllJobs 		= new LinkedHashMap<>();

	public LinkedHashMap<Integer, JobType> getJobsFutur() {
		return mJobsFuturs;
	}

	public LinkedHashMap<Integer, JobType> getAllJobs() {
		return mAllJobs;
	}

	private Map<List<Integer>, TaskOrdo> 	allTasks 			= new HashMap<>();
	private Map<Integer, List<IntervalVar>> zoneToIntervals 	= new HashMap<>();
	private Map<Integer, List<IntervalVar>> multiZoneIntervals 	= new HashMap<>();


	private Map<Integer, List<AssignedTask>> 			mAssignedTasksByNumzone;
	private LinkedHashMap<Integer, List<AssignedTask>> 	mAssignedTasksByBarreId;
	private LinkedHashMap<Integer, List<AssignedTask>> 	mPassedTasksByBarreId;
	// AssignedTask des génération précédentes et qu sont en cours de production

	private ArrayList<JobType> arrayAllJobs;

	
	private boolean mOngoingWork=false;

	public static CpModel model;
	public static CpSolver solver;

	static public int horizon = 0;
	static public boolean extra_overlap = true;
	private StringBuilder mOutPutMsg;

	public StringBuilder getOutputMsg() {
		return mOutPutMsg;
	}

	private int mSource;


	public TecalOrdo(int source) {
		
		mCurrentTime=0;
		mParams=TecalOrdoParams.getInstance();
		mTEMPS_MAX_SOLVEUR=mParams.getTEMPS_MAX_SOLVEUR();

		mBarreFutures = new HashMap<>();
		mBarresAll = new LinkedHashMap<>();
		mBarreLabels = new LinkedHashMap<>();
	
		mBarresEnCours	=new HashSet<>();
		mBarresPrioritaires=new LinkedHashSet<>();

		mAssignedTasksByNumzone = new HashMap<>();
		mAssignedTasksByBarreId = new LinkedHashMap<>();
		mPassedTasksByBarreId = new LinkedHashMap<>();

		arrayAllJobs=new ArrayList<>();

		Loader.loadNativeLibraries();		

		mOutPutMsg = new StringBuilder();
		model = new CpModel();
		
		

		setDataSource(source);

	}
	
	public int getTEMPS_MAX_SOLVEUR() {
		return mTEMPS_MAX_SOLVEUR;
	}

	public void setTEMPS_MAX_SOLVEUR(int mTEMPS_MAX_SOLVEUR) {
		this.mTEMPS_MAX_SOLVEUR = mTEMPS_MAX_SOLVEUR;
	}

	public void removeBarreFinie(int idbarre) {
		
		mJobsEnCours.remove(idbarre);
		mAllJobs.remove(idbarre);
		mBarresEnCours.remove(idbarre);
		mBarreLabels.remove(idbarre);
		mBarresAll.remove(idbarre);
		mPassedTasksByBarreId.remove(idbarre);
	}


	public void setDataSource(int source) {

		setSource(source);

		if (CST.SQLSERVER == source) {
			
			zonesBDD = SQL_DATA.getInstance().getZones();
		} else {

			try {
				csv = new CSV_DATA();
			} catch (IOException | CsvException | URISyntaxException e) {
				
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, e, "Alerte exception !", JOptionPane.ERROR_MESSAGE);
			}
			
			zonesBDD = csv.getZones();

		}

		numzoneArr = zonesBDD.keySet().toArray(new Integer[0]);

	}

	public LinkedHashMap<Integer, Barre> setBarresTestWithName() {

		HashMap<String, String> testMap = CST.transformStringToMap(CST.TEST_PROD);
		
		//testMap = CST.transformStringToMap("{1=1-000001, 2=2-000002, 3=3-000003, 4=4-000004, 5=5-000152, 6=6-000152, 7=7-000152, 8=8-000152, 10=10-000152}");
		LinkedHashMap<Integer, Barre> res= new LinkedHashMap<>();
		int i = 0;
		for (Map.Entry<String, String> e : testMap.entrySet()) {
			i++;
			Barre b=new Barre(i,e.getKey(),e.getValue(),CST.VITESSE_NORMALE,CST.VITESSE_NORMALE,false);
			mBarreFutures.put(i, b.getGammeArray());
			mBarresAll.put(i, b.getGammeArray());
			mBarreLabels.put(i, e.getKey()+" - "+e.getValue());
			res.put(i,b);
		}

		return res;
	}
	
	public LinkedHashMap<Integer, Barre> setBarresTest() {

		LinkedHashMap<Integer, Barre> res= new LinkedHashMap<>();
		int i = 0;
		for ( String gamme : CST.gammesTest) {
			i++;
			Barre b=new Barre(i,i+"",gamme,CST.VITESSE_NORMALE,CST.VITESSE_NORMALE,false);
			mBarreFutures.put(i, b.getGammeArray());
			mBarresAll.put(i, b.getGammeArray());
			mBarreLabels.put(i, i+" - "+gamme);
			res.put(i,b);
		}

		return res;
	}

	public Map<Integer, List<AssignedTask>> getAssignedJobs() {
		return mAssignedTasksByNumzone;
	}

	public HashMap<Integer, List<ElementGamme>> getBarresFutures() {
		return mBarreFutures;
	}

	public void setBarres(final LinkedHashMap<Integer, Barre> inBarresSettingsFutures) {

		// mBarreLabels=inBarres;
		printBarres();
		mBarreFutures.clear();
		mBarresSettings=inBarresSettingsFutures;
		mBarresPrioritaires.clear();
		for (Map.Entry<Integer, Barre> entry : inBarresSettingsFutures.entrySet()) {

			int numbarre = entry.getKey();
			Barre barre = entry.getValue();
			
			if(barre.isPrioritaire()) mBarresPrioritaires.add(barre.getIdbarre());

			mBarreFutures.put(numbarre, barre.getGammeArray());
			
			
			if(!mBarreLabels.containsKey(numbarre)) {
				mBarreLabels.put(numbarre, barre.getBarreNom()+"-"+barre.getGamme());
				mBarresAll.put(numbarre,barre.getGammeArray());
			}
			else {
				mBarreLabels.put(numbarre, barre.getBarreNom()+"-"+barre.getGamme());
			}
		

		}
		
		// on enlève les barres précédentes qui ne sont pas en cours et plus à faire (enlevées dans l'IHM)
		ArrayList<Integer> barresToRemove=new ArrayList<Integer>();
		for(Integer idbarre : mBarresAll.keySet()) {			
			if(!mBarresEnCours.contains(idbarre) && ! mBarreFutures.containsKey(idbarre)) {
				barresToRemove.add(idbarre);				
			}
		}
		for( int barre:barresToRemove) {
			mBarreLabels.remove(barre);
			mBarresAll.remove(barre);
		}
		
		printBarres();

	}

	private void printBarres() {
		if(CST.PRINT_BARRES) {
			logger.info("mBarreFutures="+mBarreFutures.keySet());
			logger.info("mBarreLabels="+mBarreLabels);
			logger.info("mBarresEnCours="+mBarresEnCours);
			logger.info("mBarresAll="+mBarresAll.keySet());
		}
		
	}

	public LinkedHashMap<Integer, Barre> runTest() {

		//mBarresSettings = setBarresTestWithName();
		mBarresSettings = setBarresTest();	
		run();
		return mBarresSettings;
	}
	
	public void incremente() {
		mCurrentTime+=1;		
	}
	public void incremente(int v) {
		mCurrentTime+=v;	
		mIncrementedTime=+v;
		
	}
	public long getCurrentTime() {
		return mCurrentTime;		
	}
	
	
	public void execute(LinkedHashMap<Integer, Barre> inBarresFutures) {

		//System.out.println("mCurrentTime:"+mCurrentTime);
		setBarres(inBarresFutures);
		run();
		
		
		
	}

	public void run() {
		
		printInfos();
		
		model = new CpModel();
		mOngoingWork=true;
		prepareZones();
		// --------------------------------------------------------------------------------------------
		// CONSTRAINTES SUR CHAQUE JOB
		// --------------------------------------------------------------------------------------------
		jobConstraints();
		// --------------------------------------------------------------------------------------------
		// PRECEDENCES
		// --------------------------------------------------------------------------------------------
		jobsPrecedence();	
		// --------------------------------------------------------------------------------------------
		// CONSTRAINTES SUR CHAQUE POSTE
		// --------------------------------------------------------------------------------------------		
		bridgesConstraints();
		brigesSecurity();		
		// --------------------------------------------------------------------------------------------
		// --------------------------------------------------------------------------------------------

		// Makespan objective.
		IntVar objVar = model.newIntVar(0, horizon, "makespan");
		List<IntVar> endsFutures = new ArrayList<>();
		List<IntVar> startsFutures = new ArrayList<>();
		
		
		priorisationBarres(startsFutures, endsFutures); 
		
		// les nouvelles barres doivent commencer après celles déjà présentes
		if(mCurrentTime>0)
			for(IntVar start:startsFutures ) {
				model.addLessThan(LinearExpr.constant(mCurrentTime+CST.TEMPS_MINIMAL_AVANT_DEMARRAGE),start);
			}
		
		if(endsFutures.size()>0) {
			model.addMaxEquality(objVar, endsFutures);
			model.minimize(objVar);
		}

		// Creates a solver and solves the model.
		solver = new CpSolver();

		solver.getParameters().setMaxTimeInSeconds(mTEMPS_MAX_SOLVEUR);
		solver.getParameters().setLogSearchProgress(false);
		solver.getParameters().setNumSearchWorkers(Runtime.getRuntime().availableProcessors());

		mAssignedTasksByNumzone.clear();
		mAssignedTasksByBarreId.clear();

		CpSolverStatus status = solver.solve(model);
		mOngoingWork=false;
		printInfos();
		hasSolution = false;
		displayResults(status);
	}

	private void displayResults(CpSolverStatus status) {
		mOutPutMsg.append("-----------------------------------------------------------------");
		mOutPutMsg.append(System.getProperty("line.separator"));
		if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {

			hasSolution = true;
						
			if(status == CpSolverStatus.OPTIMAL) {
				mOutPutMsg.append("Solution OPTIMALE !");
				logger.info("Solution OPTIMALE !");
			}
			else {
				mOutPutMsg.append("Solution:");
			}

			mOutPutMsg.append(System.getProperty("line.separator"));
			int tpsAno=0;
			createAssignedTasks();
			for (AssignedTask assignedTask : mAssignedTasksByNumzone.get(CST.ANODISATION_NUMZONE)) {				
				tpsAno+=assignedTask.duration;				
			}
			mOutPutMsg.append("Heures en anodisation: "+secondsToHours(tpsAno));
			
			mOutPutMsg.append(System.getProperty("line.separator"));
			
			 // Calcul de la somme des durations par clé
	        Map<Integer, Integer> sumByZone = new HashMap<>();
	        int SumSum=0;
	        for (Map.Entry<Integer, List<AssignedTask>> entry : mAssignedTasksByNumzone.entrySet()) {
	        	if(entry.getKey() != CST.CHARGEMENT_NUMZONE && entry.getKey() != CST.DECHARGEMENT_NUMZONE ) {
	        		int sum = entry.getValue().stream().mapToInt(AssignedTask::getDuration).sum();
	 	            sumByZone.put(entry.getKey(), sum);
	 	            SumSum+=sum;
	        	}
	           
	        }
	        // Tri par valeurs décroissantes
	        LinkedHashMap<Integer, Integer> sortedByValueDesc = sumByZone.entrySet()
	                .stream()
	                .sorted(Map.Entry.<Integer, Integer>comparingByValue(Comparator.reverseOrder()))
	                .collect(Collectors.toMap(
	                        Map.Entry::getKey,
	                        Map.Entry::getValue,
	                        (e1, e2) -> e1,
	                        LinkedHashMap::new
	                ));

	        // Affichage du résultat trié
	        sortedByValueDesc.forEach(
	        		(zone, sum) -> 
	        		mOutPutMsg.append("Zone " + zonesBDD.get(zone).codezone + " : Somme des durations = " + secondsToHours(sum)+"\n")
	        );

	        
	        
			mOutPutMsg.append(System.getProperty("line.separator"));
			mOutPutMsg.append("Temps total:"+SumSum);
			mOutPutMsg.append(System.getProperty("line.separator"));

			if (CST.PrintTaskTime) {
				// Create per Zone output lines.
				String output = "";
				for (int numzone : numzoneArr) {

					if (mAssignedTasksByNumzone.get(numzone) == null) {
						continue;
					}

					// Sort by starting time.
					Collections.sort(mAssignedTasksByNumzone.get(numzone), new SortTasks());
					String solLineTasks = "Zone " + numzone + ": ";
					String solLine = "           ";

						
					for (AssignedTask assignedTask : mAssignedTasksByNumzone.get(numzone)) {
						String name = "job_" + assignedTask.barreID + "_task_" + assignedTask.taskID;
						// Add spaces to output to align columns.
						solLineTasks += String.format("%-15s", name);

						String solTmp = "[" + assignedTask.start + "," + (assignedTask.start + assignedTask.duration)
								+ "]";
						// Add spaces to output to align columns.
						solLine += String.format("%-15s", solTmp);
					}
					output += solLineTasks + "%n";
					output += solLine + "%n";
				}
				mOutPutMsg.append(String.format("Optimal Schedule Length: %f%n", solver.objectiveValue()));
				mOutPutMsg.append(System.getProperty("line.separator"));
				mOutPutMsg.append(output);
				mOutPutMsg.append(System.getProperty("line.separator"));
			}

		} else {
			mOutPutMsg.append("Pas de solution trouvée.");
			logger.info("Pas de solution trouvée.");
			mOutPutMsg.append(System.getProperty("line.separator"));
	
		
			//
			arrayAllJobs.clear();
			arrayAllJobs.addAll(mJobsEnCours.values());
			createAssignedTasks();
			
			return;
		}

		// Statistics.
		// mOutPutMsg.append("\n/////////////////////////////////////////////////////////\n");
		mOutPutMsg.append("Statistiques:\n");
		mOutPutMsg.append(String.format("  conflits: %d%n", solver.numConflicts()));
		mOutPutMsg.append(String.format("  branches : %d%n", solver.numBranches()));
		mOutPutMsg.append(String.format("  temps %f s%n", solver.wallTime()));
	}
	public static String secondsToHours(int seconds ) {
     
        // Convertit les secondes en durée
        Duration duration = Duration.ofSeconds(seconds);

        // Extrait heures, minutes, secondes
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        long secs = duration.getSeconds() % 60;

        // Formate en HH:mm:ss
        String timeFormat = String.format("%02d:%02d:%02d", hours, minutes, secs);
        return timeFormat;
    }

	private void printInfos() {
		if(CST.PRINT_JOBS) {
			logger.info("mJobsEnCours="+mJobsEnCours.keySet());
			logger.info("mJobsFutures="+mJobsFuturs.keySet());
		}
		logger.info("mCurrentTime="+mCurrentTime);
		
	}
	public LocalDateTime getTime() {
		if(mIncrementedTime !=0) {
			LocalDateTime ldt=LocalDateTime.now();
			return ldt.plusSeconds(mIncrementedTime);
		}
		return LocalDateTime.now();
	}

	private void priorisationBarres(List<IntVar> startsFutures ,
			List<IntVar> endsFutures ) {
		
		
		HashMap<Integer,IntVar> endByBarreIdNonPrio	= new HashMap<> ();
		HashMap<Integer,IntVar> endByBarreIdPrio	= new HashMap<> ();

		for (JobType job: mJobsFuturs.values()) {
			
			TaskOrdo taskFirst=job.mTaskOrdoList.get(0);
			TaskOrdo taskLast=job.mTaskOrdoList.get(job.mTaskOrdoList.size()-1);
			endsFutures.add(taskFirst.getFin());
			startsFutures.add(taskFirst.getStart());
			
			if(mBarresPrioritaires.contains(job.mBarreId))
			{
				endByBarreIdPrio.put(job.mBarreId,taskFirst.getFin());
			}
			else {
				endByBarreIdNonPrio.put(job.mBarreId,taskFirst.getFin());
			}
			
			if(mBarreFutures.containsKey(job.mBarreId))
			{
				Barre b=job.getBarre();
				//LA BARRE A UNE HEURE LIMITE !!!!!!!!!!!!
				LocalDateTime ldtMaxHourBarre=b.getHeureLimite();
				if(ldtMaxHourBarre != null) {
					LocalDateTime ldt=getTime();
					Long seconds= ldtMaxHourBarre.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond()
							-ldt.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond()+mCurrentTime;
					model.addLessThan(taskLast.getStart(),seconds);
				}
				
			}
			
			
			
		}
		
		for( IntVar prio:endByBarreIdPrio.values()) {
		
			// la fin de la barre prio est inférieure à celles des non prio
			for( IntVar v :endByBarreIdNonPrio.values()) {
				model.addLessThan(prio,v);
			}			
		
		}
		Iterator<Integer> it = mBarresPrioritaires.iterator(); 
		 
	    //s'il y a plusieurs barres prioritaires il faut respecter l'ordre du LinkedHashSet 
        while (it.hasNext()) {   
            // Print HashSet values 
           int barre=it.next(); 
           if(it.hasNext()) {
        	   int next=it.next(); 
               model.addLessThan(endByBarreIdPrio.get(barre),endByBarreIdPrio.get(next));               
           }
          
          // System.out.print(b+" -> "+next); 
        }
        
        
        
	}

	private void createAssignedTasks() {
		for (JobType job : arrayAllJobs) {
			List<Task> tasks = job.tasksJob;
			int barre = job.getBarreID();
			for (int taskID = 0; taskID < tasks.size(); ++taskID) {
				Task task = tasks.get(taskID);
				List<Integer> key = Arrays.asList(barre, taskID);

				long debut = allTasks.get(key).getStartValue();
				long finBDD =  allTasks.get(key).getEndBDDValue();
				

				long derive;
				// on ne sait pas à quel moment entre le min et le max de dérive
				// le solveur a choisit => on doit regarder quand commence la tache d'apres
				// pour calculer la dérive			
				
				if(task.numzone == CST.DECHARGEMENT_NUMZONE || task.numzone == CST.CHARGEMENT_NUMZONE || taskID +1== tasks.size()) {
					derive = finBDD;
				}else {
					List<Integer> keySuivante = Arrays.asList(barre, taskID + 1);
					//todo bug
					derive = allTasks.get(keySuivante).getStartValue()
							- allTasks.get(key).tempsDeplacement - allTasks.get(key).mTask.egouttage;
				}
				
				
				//System.out.println("------------"); 
				//System.out.println("debut -> "+debut); 
				//System.out.println("finBDD -> "+finBDD); 
				//System.out.println("egouttage -> "+allTasks.get(key).egouttage);
				//System.out.println("tempsDeplacement-> "+allTasks.get(key).tempsDeplacement);
				//System.out.println("derive -> "+derive);
				//System.out.println("debut next:-> "+(allTasks.get(key).tempsDeplacement+finBDD+allTasks.get(key).egouttage));


				AssignedTask assignedTask = new AssignedTask(barre, taskID, task.numzone, debut, 
						(int) (finBDD - debut),(int) derive);
				mAssignedTasksByNumzone.computeIfAbsent(task.numzone, (Integer k) -> new ArrayList<>());
				mAssignedTasksByNumzone.get(task.numzone).add(assignedTask);

				if(!mPassedTasksByBarreId.containsKey(barre)){
					mAssignedTasksByBarreId.computeIfAbsent(barre, (Integer k) -> new ArrayList<>());
					mAssignedTasksByBarreId.get(barre).add(assignedTask);
				}
			

			}
		}
	}

	public String print() {
		return mOutPutMsg.toString();
	}

	public static void main(String[] args) throws IOException, CsvException, URISyntaxException {

		TecalOrdo tecalOrdo = new TecalOrdo(CST.SQLSERVER);

		tecalOrdo.runTest();
		if (CST.PRINT_PROD_DIAG) {
			SwingUtilities.invokeLater(() -> {

				final GanttChart ganttTecal = new GanttChart("Gantt Chart prod du 02/11/2023");
				//ganttTecal.prod_diag(CST.mListeOf26janvier, CST.getDate("20240126"));
				ganttTecal.pack();
				ganttTecal.setSize(new java.awt.Dimension(1500, 870));
				RefineryUtilities.centerFrameOnScreen(ganttTecal);
				ganttTecal.setVisible(true);

			});
		}

		System.out.printf(tecalOrdo.print());

	}

	private void prepareZones() {

		mJobsFuturs.clear();
		allTasks.clear();
		zoneToIntervals.clear();
		multiZoneIntervals.clear();
		
		
		for (Map.Entry<Integer, List<ElementGamme>> entry : mBarreFutures.entrySet()) {

			int numBarre = entry.getKey();
			String name = mBarreLabels.get(numBarre);

			JobType job = new JobType(numBarre,mBarresSettings.get(numBarre), name);

			
			List<ElementGamme> zones = entry.getValue();
			if(zones==null) {
				mOutPutMsg.append("Pas de zones pour la barre :"+name+", on l'élimine !! \n");
				continue;
			}
			// on calcul les indexes des zones a regrouper par pont
			job.buildTaskList(zones);
			mJobsFuturs.put(numBarre, job);
		}

		
		//mJobsEnCours.clear();
		
		Collection<JobType> c=mJobsEnCours.values();
		Collection<JobType> d=mJobsFuturs.values();

		updateFixedJobsEnCours();
		
		arrayAllJobs.clear();
		arrayAllJobs.addAll(c);
		arrayAllJobs.addAll(d);

		mAllJobs.clear();		
		mAllJobs.putAll(mJobsEnCours);
		mAllJobs.putAll(mJobsFuturs);



		// Computes horizon dynamically as the sum of all durations.
		computeHorizon();		

	
		
		for (JobType job : arrayAllJobs) {
			// on créé les zones avec leut temps de déplacement, égouttage, etc ...
			job.addIntervalForModel(allTasks, zoneToIntervals, multiZoneIntervals);
			// on créé les zones corespondant a mouvement des ponts
			job.simulateBridgesMoves(mCurrentTime);
			// regroupement des zones qui pourraient être trop proches de zones d'autre jobs
			// sur pont adverses
			job.makeSafetyBetweenBridges(mCurrentTime);
			
		}

	}

	private void computeHorizon() {
		horizon = 0;
		
		for (JobType job : arrayAllJobs) {			
			for (Task task : job.tasksJob) {	
				horizon += task.duration+mParams.getTEMPS_MVT_PONT()+task.derive;
			}
		}
		if(arrayAllJobs.size()>10) 	horizon/=2; 	
		/*
		if(arrayAllJobs.size()<3) 	
		  horizon*=2;
		else  horizon/=2; 	
		*/
		horizon+=mCurrentTime+CST.TEMPS_MINIMAL_AVANT_DEMARRAGE+100;
		logger.info("*********************************************************************");
		//horizon=Math.min(horizon,CST.TEMPS_MAX_JOURNEE);
		logger.info("horizon"+horizon);

	}

	/**
	 * certaines zones peuvent avoir plusieurs barres en même temps
	 *
	 * @param model
	 */
	private void jobConstraints() {

		HashMap<Integer, CumulativeConstraint> cumulConstr = new HashMap<>();

		// Create and add disjunctive constraints.
		for (int numzone : numzoneArr) {

			if (zoneToIntervals.containsKey(numzone)) {
				List<IntervalVar> intervalParZone = zoneToIntervals.get(numzone);
				model.addNoOverlap(intervalParZone);
			}
			if (multiZoneIntervals.containsKey(numzone) ) {
				List<IntervalVar> listCumul = multiZoneIntervals.get(numzone);
				ZoneType zt = zonesBDD.get(numzone);
				// zone autorisant le "chevauchement" => zone contenant plus de 1 postes
				int capacity ;
				
				if(numzone== mParams.getNUMZONE_ANODISATION() && mParams.getCAPACITE_ANODISATION() >0) {
					capacity=mParams.getCAPACITE_ANODISATION();
				}
				else 
					capacity = zt.cumul;
					

				CumulativeConstraint cumul = model.addCumulative(capacity);

				long[] zoneUsage = new long[listCumul.size()];
				Arrays.fill(zoneUsage, 1);
				cumul.addDemands(listCumul.toArray(new IntervalVar[0]), zoneUsage);
				cumulConstr.put(numzone, cumul);

			}

		}
		

	}


	private void jobsPrecedence() {

		// Precedences inside a job.



		for (JobType job: mJobsFuturs.values()) {
			List<Task> jobTasks = job.tasksJob;

			for (int taskID = 0; taskID < jobTasks.size() -1; ++taskID) {

				//if(taskID==2) continue;
				//if(taskID==1) continue;
				
				TaskOrdo prev=job.mTaskOrdoList.get(taskID);
				TaskOrdo next=job.mTaskOrdoList.get(taskID+1);


				
				model.addLessOrEqual(next.getStart(), prev.getFin());        
				model.addGreaterOrEqual(next.getStart(), prev.getDeriveNulle());

				

				
				
			}


		}

	}

	private void brigesSecurity() {

		if (CST.CSTR_BRIDGES_SECURITY) {
			ListeZone zonesAutourAnodisation = new ListeZone();
			for (JobType job : mAllJobs.values()) {				
				zonesAutourAnodisation.addAll(job.mNoOverlapP1P2);
			}
			model.addNoOverlap(zonesAutourAnodisation);
		}

	}

	private void bridgesConstraints() {

		// ---------------------------------------------------------------------------
		// CONTRAINTES SUR PONTS -----------------------------------------------
		// ---------------------------------------------------------------------------

		if (CST.CSTR_NOOVERLAP_BRIDGES) {
			ArrayList<ZonesIntervalVar> listZonesNoOverlapParPont = new ArrayList<>();
			listZonesNoOverlapParPont.add(new ZonesIntervalVar()); // add zones pont 1
			listZonesNoOverlapParPont.add(new ZonesIntervalVar()); // add zones pont 2
			for (JobType j : mAllJobs.values()) {
				int p = 0;
				for (ListeZone bridgeMoveP : j.bridgesMoves) {
					listZonesNoOverlapParPont.get(p).addAll(bridgeMoveP);
					p++;
				}
				p = 0;
			}

			for (ArrayList<IntervalVar> listZonesNoOverlap : listZonesNoOverlapParPont) {
				model.addNoOverlap(listZonesNoOverlap);
			}
		}
	}

	static IntVar getBackward( IntVar mvtPont, int decay) {

		IntVar decayed = model.newIntVar(0, horizon, "");
		model.newIntervalVar(decayed, LinearExpr.constant(decay), mvtPont, "");

		return decayed;

	}

	static IntVar getForeward(IntVar mvtPont, int decay) {

		IntVar decayed = model.newIntVar(0, horizon, "");
		model.newIntervalVar(mvtPont, LinearExpr.constant(decay), decayed, "");

		return decayed;

	}


	static IntervalVar getNoOverlapZone(IntVar mvtPont, int left, int right) {

		IntervalVar before = model.newIntervalVar(model.newIntVar(0, horizon, ""), LinearExpr.constant(left), mvtPont,
				"");

		IntervalVar after = model.newIntervalVar(mvtPont, LinearExpr.constant(right), model.newIntVar(0, horizon, ""),
				"");

		return model.newIntervalVar(before.getStartExpr(), model.newIntVar(0, horizon, ""), after.getEndExpr(), "");

	}

	static IntervalVar getNoOverlapZone(LinearExpr mvtPont) {

		IntervalVar before = model.newIntervalVar(model.newIntVar(0, horizon, ""),
				LinearExpr.constant(TecalOrdoParams.getInstance().getTEMPS_MVT_PONT()), mvtPont, "");

		IntervalVar after = model.newIntervalVar(mvtPont, LinearExpr.constant(TecalOrdoParams.getInstance().getTEMPS_MVT_PONT()),
				model.newIntVar(0, horizon, ""), "");

		return model.newIntervalVar(before.getStartExpr(), model.newIntVar(0, horizon, ""), after.getEndExpr(), "");

	}

	static IntervalVar getNoOverlapZone(LinearExpr mvtPont, int left, int right) {

		IntervalVar before = model.newIntervalVar(model.newIntVar(0, horizon, ""), LinearExpr.constant(left), mvtPont,
				"");

		IntervalVar after = model.newIntervalVar(mvtPont, LinearExpr.constant(right), model.newIntVar(0, horizon, ""),
				"");

		return model.newIntervalVar(before.getStartExpr(), model.newIntVar(0, horizon, ""), after.getEndExpr(), "");

	}

	static IntervalVar getNoOverlapZone(IntVar mvtPont) {

		IntervalVar before = model.newIntervalVar(model.newIntVar(0, horizon, ""),
				LinearExpr.constant(TecalOrdoParams.getInstance().getTEMPS_MVT_PONT()), mvtPont, "");

		IntervalVar after = model.newIntervalVar(mvtPont, LinearExpr.constant(TecalOrdoParams.getInstance().getTEMPS_MVT_PONT()),
				model.newIntVar(0, horizon, ""), "");

		return model.newIntervalVar(before.getStartExpr(), model.newIntVar(0, horizon, ""), after.getEndExpr(), "");

	}

	static IntervalVar getNoOverlapZone( IntVar mvtPontStart, IntVar mvtPontEnd) {

		IntervalVar before = model.newIntervalVar(model.newIntVar(0, horizon, ""),
				LinearExpr.constant(TecalOrdoParams.getInstance().getTEMPS_MVT_PONT()), mvtPontStart, "");

		IntervalVar after = model.newIntervalVar(mvtPontEnd, LinearExpr.constant(TecalOrdoParams.getInstance().getTEMPS_MVT_PONT()),
				model.newIntVar(0, horizon, ""), "");

		return model.newIntervalVar(before.getStartExpr(), model.newIntVar(0, horizon, ""), after.getEndExpr(), "");

	}

	static IntervalVar getNoOverlapZone(IntervalVar mvt) {

		IntervalVar before = model.newIntervalVar(model.newIntVar(0, horizon, ""),
				LinearExpr.constant(TecalOrdoParams.getInstance().getTEMPS_MVT_PONT()), mvt.getStartExpr(), "");

		IntervalVar after = model.newIntervalVar(mvt.getEndExpr(), LinearExpr.constant(TecalOrdoParams.getInstance().getTEMPS_MVT_PONT()),
				model.newIntVar(0, horizon, ""), "");

		return model.newIntervalVar(before.getStartExpr(), model.newIntVar(0, horizon, ""), after.getEndExpr(), "");

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
	public void setHasSolution(boolean b) {
		 hasSolution=b;
	}

	public LinkedHashMap<Integer, String> getBarreLabels() {
		return mBarreLabels;
	}

	public void setBarreLabels(LinkedHashMap<Integer, String> inBarreGammes) {

		// mBarreLabels=inBarres;
		// mBarreFutures.clear();
		for (Map.Entry<Integer, String> entry : inBarreGammes.entrySet()) {

			int numbarre = entry.getKey();
			String gamme = entry.getValue();
			if (!mBarreLabels.containsKey(numbarre)) {
				mBarreLabels.put(numbarre, gamme);
			}

		}

	}

	public LinkedHashMap<Integer, List<ElementGamme>> getBarreZonesAll() {
		return mBarresAll;
	}

	public void removeAssignedTaskByBarreId(int barreid) {
	
		
		mPassedTasksByBarreId.put(barreid,mAssignedTasksByBarreId.get(barreid));
		mAssignedTasksByBarreId.remove(barreid);
	}
	public void removeAllByBarreId(int barreid) {
		mPassedTasksByBarreId.remove(barreid);
		mAssignedTasksByBarreId.remove(barreid);
		removeBarreFinie(barreid);
		
	}

	public LinkedHashMap<Integer, List<AssignedTask>> getPassedTasksByBarreId() {
		return mPassedTasksByBarreId;
	}

	public boolean isWorking() {
		
		return mOngoingWork;
	}

	public LinkedHashMap<Integer, List<AssignedTask>> getAssignedTasksByBarreId() {		
		return mAssignedTasksByBarreId;
	}
	public  ArrayList<JobType> getArrayJobs() {
		return arrayAllJobs;
	}
	
	
}
