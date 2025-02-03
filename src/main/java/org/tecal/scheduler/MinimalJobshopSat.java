package org.tecal.scheduler;

import static java.lang.Math.max;

import com.google.ortools.Loader;
import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.CpSolverStatus;
import com.google.ortools.sat.CumulativeConstraint;
import com.google.ortools.sat.IntVar;
import com.google.ortools.sat.IntervalVar;
import com.google.ortools.sat.LinearExpr;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/** Minimal Jobshop problem. */
public class MinimalJobshopSat {
  public static void main(String[] args) {
    Loader.loadNativeLibraries();
    class Task {
      int machine;
      int duration;
      Task(int machine, int duration,int derive) {
        this.machine = machine;
        this.duration = duration;
      }
    }

    final List<List<Task>> allJobs =Arrays.asList(
    	     Arrays.asList(new Task(1, 100,0),new Task(7, 240, 0),new Task(11, 30, 0),new Task(11, 30, 0),new Task(12, 120, 150),
    	    		 new Task(13, 10, 0),new Task(14, 10, 0),new Task(15, 4500, 0),new Task(16, 10, 0),
    	        new Task(17, 10, 0),new Task(19, 10, 0),new Task(19, 20, 0),new Task(20, 10, 0),
    	        new Task(20, 30, 0),new Task(24, 300, 120),new Task(25, 10, 0),new Task(29, 10, 0),
    	        new Task(29, 30, 0),new Task(32, 600, 800),new Task(37, 1200, 800),new Task(35, 100, 0)),  
    	     Arrays.asList(new Task(1, 100, 0),new Task(2, 300, 200),new Task(3, 180, 0),new Task(4, 300, 0),new Task(5, 10, 0),
    	        new Task(6, 10, 0),new Task(12, 135, 150),new Task(13, 10, 0),new Task(14, 10, 0),new Task(15, 4320, 0),
    	        new Task(16, 10, 0),new Task(17, 10, 0),new Task(19, 10, 0),new Task(20, 10, 0),new Task(27, 941, 180),
    	        new Task(28, 20, 0),new Task(25, 10, 0),new Task(26, 480, 120),new Task(25, 10, 0),new Task(29, 10, 0),
    	        new Task(31, 900, 800),new Task(37, 900, 800),new Task(35, 100, 0)),  
    	     Arrays.asList(new Task(1, 100, 0),
    	        new Task(2, 920, 200),new Task(4, 480, 0),new Task(5, 30, 0),new Task(5, 30, 0),new Task(6, 30, 0),
    	        new Task(6, 30, 0),new Task(12, 180, 150),new Task(13, 10, 0),new Task(14, 10, 0),new Task(15, 3360, 0),
    	        new Task(16, 10, 0),new Task(16, 10, 0),new Task(17, 10, 0),
    	        new Task(17, 10, 0),new Task(19, 10, 0),new Task(20, 10, 0),new Task(24, 600, 120),
    	        new Task(25, 10, 0),new Task(25, 10, 0),new Task(29, 10, 0),new Task(29, 10, 0),
    	        new Task(32, 1200, 800),new Task(35, 100, 0))
    	    );
    	    

    int numMachines = 1;
    for (List<Task> job : allJobs) {
      for (Task task : job) {
        numMachines = max(numMachines, 1 + task.machine);
      }
    }
    final int[] allMachines = IntStream.range(0, numMachines).toArray();

    // Computes horizon dynamically as the sum of all durations.
    int horizon = 0;
    for (List<Task> job : allJobs) {
      for (Task task : job) {
        horizon += task.duration;
      }
    }

    // Creates the model.
    CpModel model = new CpModel();

    class TaskType {
      IntVar start;
      IntVar end;
      IntervalVar interval;
    }
    Map<List<Integer>, TaskType> allTasks = new HashMap<>();
    Map<Integer, List<IntervalVar>> machineToIntervals = new HashMap<>();

    for (int jobID = 0; jobID < allJobs.size(); ++jobID) {
      List<Task> job = allJobs.get(jobID);
      for (int taskID = 0; taskID < job.size(); ++taskID) {
        Task task = job.get(taskID);
        String suffix = "_" + jobID + "_" + taskID;

        TaskType taskType = new TaskType();
        taskType.start = model.newIntVar(0, horizon, "start" + suffix);
        taskType.end = model.newIntVar(0, horizon, "end" + suffix);
        taskType.interval = model.newIntervalVar(
            taskType.start, LinearExpr.constant(task.duration), taskType.end, "interval" + suffix);

        List<Integer> key = Arrays.asList(jobID, taskID);
        allTasks.put(key, taskType);
        machineToIntervals.computeIfAbsent(task.machine, (Integer k) -> new ArrayList<>());
        machineToIntervals.get(task.machine).add(taskType.interval);
      }
    }

    // Create and add disjunctive constraints.
    for (int machine : allMachines) {
      List<IntervalVar> list = machineToIntervals.get(machine);
      if(list == null) continue;
      
      CumulativeConstraint cumul33 = model.addCumulative(2);
      
      if(machine==15) {
    	  CumulativeConstraint cumul = model.addCumulative(3);

    	  long[] zoneUsage = new long[list.size()];
    	  Arrays.fill(zoneUsage, 1);
    	  cumul.addDemands(list.toArray(new IntervalVar[0]), zoneUsage);
      }
      else if(machine==33) {
    	  long[] zoneUsage = new long[list.size()];
    	  Arrays.fill(zoneUsage, 1);
    	  cumul33.addDemands(list.toArray(new IntervalVar[0]), zoneUsage);
      }
      else {
    	  if(machine==32 || machine==31) {
    		  long[] zoneUsage = new long[list.size()];    		
    		  cumul33.addDemands(list.toArray(new IntervalVar[0]), zoneUsage);
    	  }
    	  model.addNoOverlap(list);
      }
    }

    // Precedences inside a job.
    for (int jobID = 0; jobID < allJobs.size(); ++jobID) {
      List<Task> job = allJobs.get(jobID);
      for (int taskID = 0; taskID < job.size() - 1; ++taskID) {
        List<Integer> prevKey = Arrays.asList(jobID, taskID);
        List<Integer> nextKey = Arrays.asList(jobID, taskID + 1);
        model.addGreaterOrEqual(allTasks.get(nextKey).start, allTasks.get(prevKey).end);
      }
    }

    // Makespan objective.
    IntVar objVar = model.newIntVar(0, horizon, "makespan");
    List<IntVar> ends = new ArrayList<>();
    for (int jobID = 0; jobID < allJobs.size(); ++jobID) {
      List<Task> job = allJobs.get(jobID);
      List<Integer> key = Arrays.asList(jobID, job.size() - 1);
      ends.add(allTasks.get(key).end);
    }
    model.addMaxEquality(objVar, ends);
    model.minimize(objVar);

    // Creates a solver and solves the model.
    CpSolver solver = new CpSolver();
    CpSolverStatus status = solver.solve(model);

    if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
      class AssignedTask {
        int jobID;
        int taskID;
        int start;
        int duration;
        // Ctor
        AssignedTask(int jobID, int taskID, int start, int duration) {
          this.jobID = jobID;
          this.taskID = taskID;
          this.start = start;
          this.duration = duration;
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
      System.out.println("Solution:");
      // Create one list of assigned tasks per machine.
      Map<Integer, List<AssignedTask>> assignedJobs = new HashMap<>();
      for (int jobID = 0; jobID < allJobs.size(); ++jobID) {
        List<Task> job = allJobs.get(jobID);
        for (int taskID = 0; taskID < job.size(); ++taskID) {
          Task task = job.get(taskID);
          List<Integer> key = Arrays.asList(jobID, taskID);
          AssignedTask assignedTask = new AssignedTask(
              jobID, taskID, (int) solver.value(allTasks.get(key).start), task.duration);
          assignedJobs.computeIfAbsent(task.machine, (Integer k) -> new ArrayList<>());
          assignedJobs.get(task.machine).add(assignedTask);
        }
      }

      // Create per machine output lines.
      String output = "";
      for (int machine : allMachines) {
        // Sort by starting time.
    	  if(!assignedJobs.containsKey(machine) ) continue;
        Collections.sort(assignedJobs.get(machine), new SortTasks());
        String solLineTasks = "Machine " + machine + ": ";
        String solLine = "           ";

        for (AssignedTask assignedTask : assignedJobs.get(machine)) {
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
    } else {
      System.out.println("No solution found.");
    }

    // Statistics.
    System.out.println("Statistics");
    System.out.printf("  conflicts: %d%n", solver.numConflicts());
    System.out.printf("  branches : %d%n", solver.numBranches());
    System.out.printf("  wall time: %f s%n", solver.wallTime());
  }

  private MinimalJobshopSat() {}
}