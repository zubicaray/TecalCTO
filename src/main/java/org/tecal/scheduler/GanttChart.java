package org.tecal.scheduler;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.*;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;
import org.jfree.ui.ApplicationFrame;


import org.tecal.scheduler.SQL_Anodisation.PosteBDD;
import org.tecal.scheduler.SQL_Anodisation.PosteProd;
import org.tecal.scheduler.SQL_Anodisation.ZoneType;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;




public class GanttChart extends ApplicationFrame {

	 
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private SQL_Anodisation mSqlCnx ;
	

	ArrayList<String[]>  labelsModel;
	ArrayList<String[]> labels;

	public GanttChart(SQL_Anodisation sqlCnx,final String title) {

		super(title);
		mSqlCnx=sqlCnx;
		   
	}
	
	
	class ZoneCumul {
	      int cumul;
	     
	      int lastTimeAtPostes[];
	      ZoneCumul(int cumul) {
	        this.cumul = cumul;	 
	        lastTimeAtPostes= new int[cumul];
	       // for(int i=0;i<lastTimeAtPostes.length;i++)  lastTimeAtPostes[i]=0;
	      }
	      public int getPoste(int starttime,int endtime) {
	    	 
	    	  boolean zonePrise=false;
	    	  int idxPoste=0;
	    	  for(int i=0;i<lastTimeAtPostes.length;i++) {
	    		  
	    		  if(lastTimeAtPostes[i]==0 && !zonePrise) {
	    			  lastTimeAtPostes[i]=endtime;
	    			  zonePrise=true;
	    			  
	    			  return i;
	    			  
	    		  }else {
	    			  if(lastTimeAtPostes[i]<=starttime) {
	    				  lastTimeAtPostes[i]=0;
	    				  if( !zonePrise) {
	    					  lastTimeAtPostes[i]=endtime;
	    	    			  zonePrise=true;
	    	    			  
	    	    			  idxPoste=i;
	    				  }
	    				
	    			  }
	    			  
	    		  }
	    		  
	    	  }
	    	  return idxPoste;
	    	  
	      }
	    }
	 /*!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * 
	 * AssignedTask.idtask  = id machine/zone GOOGLE.OR pour toutes les gammes 
	 * task.idzone  = id de la zone propre à la gamme
	 * 
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 */
	
	
	
	public void model_diag(
			Map<Integer, List<AssignedTask>> assignedJobs ,HashMap<Integer,ZoneType> inZonesBDD, 
			HashMap<String, ArrayList<GammeType> > ficheToZones) {
		
		
		 XYIntervalSeriesCollection dataset = new XYIntervalSeriesCollection();
		 int nbZones=inZonesBDD.keySet().size();
		 
		 String[] zonesAllGamme =  new String[nbZones];
		 		 
		 
		 HashMap<Integer,ZoneCumul> zonesCumul=new HashMap<Integer,ZoneCumul>();
		 
		 int cpt=0;
		 // on crée les labels des zones
		 for (Map.Entry<Integer,ZoneType > entry : inZonesBDD.entrySet()) {
			 ZoneType zt = entry.getValue();
			         
			 zonesAllGamme[cpt]=zt.codezone;
			 if(zt.cumul>1) {
				 zonesCumul.put(zt.numzone, new ZoneCumul(zt.cumul));
			 };
			 cpt++;
		 }	 
					 
		 int totalZoneCount =ficheToZones.size();		 		
		
		 //Create series. Start and end times are used as y intervals, and the room is represented by the x value
		 XYIntervalSeries[] series = new XYIntervalSeries[totalZoneCount];

		 int jobID = 0;
		 
		 String[] gammes= new String[ficheToZones.keySet().size()];
		 for (Map.Entry<String, ArrayList<GammeType> > entry : ficheToZones.entrySet()) {
			String lgamme = entry.getKey();			         
			series[jobID] = new XYIntervalSeries(lgamme);
			gammes[jobID] = lgamme;
			dataset.addSeries(series[jobID]);
			jobID++;
		 }
	
		 
		
		 labelsModel = new ArrayList<String[]>(jobID);
		 for(int i=0; i < jobID; i++) {
		//	 labelsModel.add(new ArrayList<String>());
		 }
		 
		 
		 //!!!!!!!!!!!!!!!!!!!!
		 // si pas linkedmap les job id ont dans le désordre chrono
		 // et du coup les zones cumul s'affiche mal
		 Map<Integer, List<AssignedTask>> tabAssignedJobsSorted=new HashMap<Integer, List<AssignedTask>>();
		 
		 Map<Integer, List<AssignedTask>> cumulTask=new HashMap<Integer, List<AssignedTask>>();;
		 //réorga par jobid/list zones
		 assignedJobs.values().forEach( taskArr->{
			
			taskArr.forEach( task->{
				
				if(tabAssignedJobsSorted.containsKey(task.jobID) ==false) {
					tabAssignedJobsSorted.put(task.jobID, new ArrayList<AssignedTask>());
				}
				
				tabAssignedJobsSorted.get(task.jobID).add(task);
				
				// TODO
				// pour corriger le pb de superposition des zones de cumuls
				//isoler les tasks concernées par zone, puis les triées chronologiquement
				// pour pouvoir leur attribuer un id de cumul
				
				//System.out.println("task.idzoneBDD="+task.idzoneBDD );
				if(zonesCumul.containsKey(task.numzone)) {
					 if(!cumulTask.containsKey(task.numzone)) {
						 cumulTask.put(task.numzone, new ArrayList<AssignedTask>());
					 }
					 cumulTask.get(task.numzone).add(task);
				 }
				
			});
			 
		 });
		 
		 // on trie chronologiquement les zones de cumul
		 cumulTask.forEach((numzone, tasks) -> {	 				
			    	
				  Collections.sort(tasks,new Comparator<AssignedTask>() {
				        public int compare(AssignedTask a1, AssignedTask a2) {
				        	int numligne1=a1.start;
				        	int numligne2=a2.start;
				            return numligne1-numligne2;
				        }
				    });
			 }
		 );
		 
		 cumulTask.forEach((numzone, tasks) -> {	 				
			 tasks.forEach( task->{
				ZoneCumul zc=zonesCumul.get(task.numzone);
		    	int end=task.duration+task.start;
		      		
		    	task.IdPosteZoneCumul=zc.getPoste(task.start, end);
				 
				 
			 });
			 
		 }
		 );
		 
		 
		 // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		 //on trie les zones de chaque job par date
		 // pour que jfreechart les ajoute dans l'ordre pour qu'on puisse rtrouver les bons tooltip
		 tabAssignedJobsSorted.forEach((idjob, value) -> {	 
			Collections.sort(value,new Comparator<AssignedTask>() {
			        public int compare(AssignedTask a1, AssignedTask a2) {
			        	int numligne1=a1.start+a1.taskID;
			        	int numligne2=a2.start+a2.taskID;
			        	numligne1=a1.taskID+a1.numzone*100;
			        	numligne2=a2.taskID+a2.numzone*100;
			            return numligne1-numligne2;
			        }
			    });
		 }
		 );
		 
		
		 //pour que tout soit ajouté dans le bon ordre il faut retrier les task		 
		 for (Map.Entry<Integer, List<AssignedTask>> entry : tabAssignedJobsSorted.entrySet()) {
		 
			int idjob=entry.getKey();
			List<AssignedTask> listeTache=entry.getValue();
			
			labelsModel.add(new String[listeTache.size()]);
			int cpt1=0;
		    for(AssignedTask at :listeTache) {	
			 
				String gamme=gammes[idjob];
		    	ArrayList<GammeType> df=ficheToZones.get(gamme);
		    	//on retrouve le idtask de la table ZONES SQLSERVER ( numéroté sans trou à partir de 0 <> numzone donc)
		    	// car quant à lui,le taskid de google, est propre à l'ordres des zones d'une gamme
		    	int posteEncours=df.get(at.taskID).idzonebdd;
	      
			    int[] dr={at.start,at.start+at.duration};		
		
			    //System.out.println("gamme:"+gamme+" "+df.get(at.taskID).codezone+" start:"+at.start+" numligne:"+df.get(at.taskID).numligne); 
			    //System.out.println("at.duration="+at.duration );
			      
			    double incrementY=0.3;
			    
			    
			    if(zonesCumul.containsKey(at.numzone)) {
			    	//System.out.println("gamme:"+gamme+" "+df.get(at.taskID).codezone+" start:"+at.start+" IdPosteZOneCumul:"+IdPosteZOneCumul); 
			    	double offset=incrementY*at.IdPosteZoneCumul;
			    	double incrementSpaceY=0.45;
			    	//Encode the room as x value. The width of the bar is only 0.6 to leave a small gap. The course starts 0.1 h/6 min after the end of the preceding course.
				    series[idjob].add(posteEncours, posteEncours - incrementSpaceY+offset, posteEncours -incrementSpaceY+offset+incrementY, dr[0],dr[0] ,dr[1]);
				 }
			   
			    else {
			    	 series[idjob].add(posteEncours,posteEncours - 0.3,posteEncours +0.3, 
			    			 dr[0],dr[0] ,dr[1] );
			    }
		    
			    labelsModel.get(idjob)[cpt1]="start:"+at.start+", durée:"+at.duration+"\n, fin:"+(at.duration+at.start)
			    		+ " dérive: " +at.derive+ ", " +df.get(at.taskID).codezone;
			    cpt1++;    
			 
			 };
			
		 };
			 
		 
		
		 XYBarRenderer renderer = new XYBarRenderer();
		 renderer.setUseYInterval(true);
		 renderer.setShadowXOffset(0);
         renderer.setShadowYOffset(0);
         renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
         renderer.setBaseItemLabelsVisible(true);
		 
		 XYBarRenderer.setDefaultShadowsVisible(false);
		 
		 renderer.setBaseItemLabelGenerator(new StandardXYItemLabelGenerator());
		 renderer.setBaseItemLabelsVisible(false);
		 renderer.setBarPainter(new StandardXYBarPainter());
		 
		 
		 StandardXYToolTipGenerator ttgen = new StandardXYToolTipGenerator() {

		     
		 private static final long serialVersionUID = 1L;
		
		public String generateToolTip(XYDataset dataset, int series, int item) {    	
			//System.out.println("series:"+series+" "+" item:"+item+" val="+labelsModel[series][item]);    		 
		        return  labelsModel.get(series)[item];
		    }
		};
	     renderer.setSeriesToolTipGenerator(0, ttgen); 
	
	     renderer.setBaseToolTipGenerator(ttgen);	 
		 
		 XYPlot plot = new XYPlot(dataset, new SymbolAxis("zones", zonesAllGamme), new NumberAxis(), renderer);
	     plot.setOrientation(PlotOrientation.HORIZONTAL);
	    
	     JFreeChart chart = new JFreeChart(plot);
	     getContentPane().add(new ChartPanel(chart));
		
	
	}
	
	public void prod_diag() {
		XYIntervalSeriesCollection dataset = new XYIntervalSeriesCollection();
		
		 ArrayList<PosteBDD> posteAllOF = new ArrayList <PosteBDD>();
		 
		 HashMap<Integer,PosteBDD> mapPosteBDD= mSqlCnx.getPostes();
		 
		 mapPosteBDD.values().forEach( v->{ posteAllOF.add(v); });		 
		 
		 
		 //toArray(new PosteBDD[0]);
		// posteCount=posteAllOF.length;
		 
		 Collections.sort(posteAllOF);
		 		 
		ArrayList<String> labelPosteAllOFTmp= new ArrayList<String>();
		for(PosteBDD p:posteAllOF ) {
			labelPosteAllOFTmp.add(p.nom);
		}
		String[] labelPosteAllOF=labelPosteAllOFTmp.toArray(new String[0]);
		 
		 //gamme par fiche production (on peut avoir une  même gamme pour deux fichesProd
		 HashMap<String, String> ficheGamme=  mSqlCnx.getGammes();
		 
		 String[] ficheToZones=ficheGamme.values().toArray(new String[0]);
		 
		 int totalFicheProdCount =ficheToZones.length;
		 
		
		 //Create series. Start and end times are used as y intervals, and the room is represented by the x value
		 XYIntervalSeries[] series = new XYIntervalSeries[totalFicheProdCount];
		 for(int i = 0; i < totalFicheProdCount; i++){
		      series[i] = new XYIntervalSeries(ficheToZones[i]);
		      dataset.addSeries(series[i]);
		 }

		 //temps aux postes par fiches production
		 HashMap<String, LinkedHashMap<Integer,PosteProd> > tempsAuPostes=mSqlCnx.getTempsAuPostes() ;
		 
		
		 
		 String[] fichesProd=ficheGamme.keySet().toArray(new String[0]);
		 
		 int offset=0; 	 
		 
		 HashMap<Integer,PosteProd> firstPostes= new  HashMap<Integer,PosteProd>();
		
		 tempsAuPostes.forEach((key,listePostes)->{			
			 Iterator<Map.Entry<Integer,PosteProd>> iterator = listePostes.entrySet().iterator();

			 Map.Entry<Integer, PosteProd> actualValue = iterator.next();
			 firstPostes.put(actualValue.getValue().start,actualValue.getValue());	   
			 
		 });
		
		 for(PosteProd pp: firstPostes.values()) {
			 if(offset==0) {
				 offset=pp.start;
			 }else {
				 if(offset>pp.start) {
					 offset=pp.start;
				 }
			 }
		 }
		
		 labels = new ArrayList<String[]>(totalFicheProdCount);
		 for(int i=0; i < totalFicheProdCount; i++) {
			 //labels.add(new ArrayList<String>());
			}
		 
		
		 
		for(int currentFiche = 0; currentFiche < totalFicheProdCount; currentFiche++){
			String fiche=fichesProd[currentFiche];
			int cptt=0;
			labels.add(new String[tempsAuPostes.get(fiche).size()]);
			for (Entry<Integer, PosteProd> set :  tempsAuPostes.get(fiche).entrySet()) {
	 
				  PosteProd posteProd=set.getValue();	
				  
				  labels.get(currentFiche)[cptt]="start:"+posteProd.start+", durée:"+(posteProd.stop-posteProd.start)+"end:"+posteProd.stop ;
				  cptt++;
				  
				  //System.out.println( "------------------------------------------------");
				  //System.out.println( "gamme:"+ficheGamme.get(fiche)+ " cpt: "+cptt);		    	  
				  // System.out.println( "durée "+labels[currentFiche][currentLabelPoste]);
				  //System.out.println( "nom= "+posteProd.nom+ "numligne="+set.getKey());

				  //System.out.println( "currentFiche " + fiche+ "idx:"+currentFiche+" ,zone: "+posteProd.numposte+ " "+ "idx:"+posteProd.numligneBDD);
				  //Encode the room as x value. The width of the bar is only 0.6 to leave a small gap. The course starts 0.1 h/6 min after the end of the preceding course.
				  series[currentFiche].add(posteProd.numligneBDD,posteProd.numligneBDD - 0.3, posteProd.numligneBDD +0.3, 
						  posteProd.start-offset, posteProd.start-offset, posteProd.stop-offset );
	
				
				
	            
			}
			
		 }
		 XYBarRenderer renderer = new XYBarRenderer();
		 renderer.setUseYInterval(true);
		 renderer.setShadowXOffset(0);
         renderer.setShadowYOffset(0);
         renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
         renderer.setBaseItemLabelsVisible(true);
		 
		 XYBarRenderer.setDefaultShadowsVisible(false);
		 
		 renderer.setBaseItemLabelGenerator(new StandardXYItemLabelGenerator());
		 renderer.setBaseItemLabelsVisible(false);
		 
		 
		 StandardXYToolTipGenerator ttgen = new StandardXYToolTipGenerator() {

		        /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

				public String generateToolTip(XYDataset dataset, int series, int item) {    		            
		            return  labels.get(series)[item];
		        }
		    };
		    renderer.setSeriesToolTipGenerator(0, ttgen);
		 
	
		    renderer.setBaseToolTipGenerator(ttgen);
		 
	
		 
		 XYPlot plot = new XYPlot(dataset, new SymbolAxis("zones", labelPosteAllOF), new NumberAxis(), renderer);
	     plot.setOrientation(PlotOrientation.HORIZONTAL);
	    
	     JFreeChart chart = new JFreeChart(plot);
	     getContentPane().add(new ChartPanel(chart));
	}
	

}