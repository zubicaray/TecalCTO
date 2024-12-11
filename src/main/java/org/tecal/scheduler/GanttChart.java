package org.tecal.scheduler;

import java.awt.Color;
import java.awt.Font;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JFrame;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;
import org.tecal.scheduler.data.SQL_DATA;
import org.tecal.scheduler.types.AssignedTask;
import org.tecal.scheduler.types.GammeType;
import org.tecal.scheduler.types.PosteBDD;
import org.tecal.scheduler.types.PosteProd;
import org.tecal.scheduler.types.ZoneType;




public class GanttChart extends JFrame {


	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private  LocalDateTime mStartTime;

	public LocalDateTime getStartTime() {
		return mStartTime;
	}
	@SuppressWarnings("unused")
	private String[] mZonesAllGamme;
	private XYIntervalSeriesCollection mDataset;
	private HashMap<Integer,ZoneType> mZonesBDD;
	private Map<Integer, List<AssignedTask>> mTabAssignedJobsSorted;
	HashMap<Integer,Integer> barreToIndex;
	HashMap<Integer,Integer> indexToBarreIndex;

	private long mLowerBound;

	private XYPlot mPlot;
	private ChartPanel  mChartPanel;


	public ChartPanel getChartPanel() {
		return mChartPanel;
	}
	private  HashMap<Integer,String[]>  labelsModel;
	//ArrayList<AssignedTask[]>  tasksTab;
	ArrayList<String[]> labels;
	private ValueMarker timeBar;

	public ValueMarker getTimeBar() {
		return timeBar;
	}

	public GanttChart(final String title) {

		super(title);


		 mZonesBDD=SQL_DATA.getInstance().getZones();
		 int nbZones=mZonesBDD.keySet().size();
		 mZonesAllGamme =  new String[nbZones];
		 mDataset = new XYIntervalSeriesCollection();
		 barreToIndex=new HashMap<>();
		 indexToBarreIndex=new HashMap<>();


		 mChartPanel=  new ChartPanel(null);

		 timeBar = new ValueMarker(1500);  // position is the value on the axis
	     timeBar.setPaint(Color.red);
		 timeBar.setValue(CST.CPT_GANTT_OFFSET);

	}


	class ZoneCumul {
	      int cumul;

	      int lastTimeAtPostes[];
	      ZoneCumul(int cumul) {
	        this.cumul = cumul;
	        lastTimeAtPostes= new int[cumul];
	       // for(int i=0;i<lastTimeAtPostes.length;i++)  lastTimeAtPostes[i]=0;
	      }
	      public int getPosteIdx(int starttime,int endtime,int derive) {

	    	  boolean zonePrise=false;
	    	  int idxPoste=0;
	    	  for(int i=lastTimeAtPostes.length-1;i>=0;i--) {

	    		  if(lastTimeAtPostes[i]==0 && !zonePrise) {
	    			  lastTimeAtPostes[i]=derive;
	    			  zonePrise=true;

	    			  return i;

	    		  }else {
	    			  if(lastTimeAtPostes[i]<=starttime) {
	    				  lastTimeAtPostes[i]=0;
	    				  if( !zonePrise) {
	    					  lastTimeAtPostes[i]=derive;
	    	    			  zonePrise=true;

	    	    			  idxPoste=i;
	    				  }

	    			  }

	    		  }

	    	  }
	    	  return idxPoste;

	      }
	    }




	public String toMinutes(int t) {
		return String.format("%02d:%02d",  t / 60, (t % 60));
	}
	public String tmpsAvantSortie(int fin) {

		if(timeBar != null && mStartTime !=null) {
			int seconds =(int) (fin -timeBar.getValue());
			String minutes="";

			String hour="";
			String res;

			if(seconds >0) {
				minutes=toMinutes(seconds);
				hour= mStartTime.plusSeconds(fin-CST.CPT_GANTT_OFFSET).toString().substring(0,8);
				res=" sortie dans "+ minutes+" minutes à "+hour;
			}
			else {
				minutes=toMinutes(-1*seconds);
				hour= mStartTime.plusSeconds(fin-CST.CPT_GANTT_OFFSET).toString().substring(0,8);
				res=" sortie il y a  "+ minutes+" minutes à "+hour;
			}

			return res;


		}

		return "";
	}

	public void  backward(int v) {
		timeBar.setValue(timeBar.getValue()-v);
	}
	public void  foreward(int v) {
		timeBar.setValue(timeBar.getValue()+v);
	}

	public void model_diag(TecalOrdo  inTecalOrdo){

		mLowerBound=0;

		LinkedHashMap<Integer, ArrayList<GammeType> > 	barreZones	= inTecalOrdo.getBarreZonesAll();
		LinkedHashMap<Integer,String> 					barreLabels	= inTecalOrdo.getBarreLabels();
		Map<Integer, List<AssignedTask>> mAssignedTasksByNumzone	= inTecalOrdo.getAssignedJobs();

		indexToBarreIndex.clear();
		barreToIndex.clear();

		// on doit mettre les barres sous forme d'index comme demandée
		// par l'objet XYIntervalSeries qui est un tableau
		int cptBarre=0;
		for (Map.Entry<Integer, ArrayList<GammeType> > entry : barreZones.entrySet()) {
			barreToIndex.put(entry.getKey(),cptBarre);
			indexToBarreIndex.put(cptBarre,entry.getKey());
			cptBarre++;
		}


		mDataset.removeAllSeries();


		 HashMap<Integer,ZoneCumul> zonesCumul=new HashMap<>();


		 // on crée les labels des zones
		 int cpt=0;
		 for (Map.Entry<Integer,ZoneType > entry : mZonesBDD.entrySet()) {
			 ZoneType zt = entry.getValue();

			 mZonesAllGamme[cpt]=zt.codezone;
			 if(zt.cumul>1) {
				 zonesCumul.put(zt.numzone, new ZoneCumul(zt.cumul));
			 }
			 cpt++;
		 }

		 int totalZoneCount =barreZones.size();

		 //Create series. Start and end times are used as y intervals, and the room is represented by the x value
		 XYIntervalSeries[]  series = new XYIntervalSeries[totalZoneCount];


		 for (Map.Entry<Integer, ArrayList<GammeType> > entry : barreZones.entrySet()) {
			//String lgamme =entry.getKey()+"-"+ ficheToGamme.get(entry.getKey());
			int barre=entry.getKey();
			String lgamme=barreLabels.get(barre);

			int index=barreToIndex.get(barre);
			series[index] = new XYIntervalSeries(lgamme);
			mDataset.addSeries(series[index]);

		 }



		 labelsModel = new HashMap<>();


		 //!!!!!!!!!!!!!!!!!!!!
		 // si pas linkedmap les job id sont dans le désordre chrono
		 // et du coup les zones cumul s'affiche mal
		 mTabAssignedJobsSorted=new HashMap<>();

		 computeTasksByBarreID(mAssignedTasksByNumzone, zonesCumul);



		 /**
		  * on trie les zones de chaque job par date
		  * pour que jfreechart les ajoute dans l'ordre pour qu'on puisse trouver les bons tooltip
		  */

		 mTabAssignedJobsSorted.values().forEach( value -> {
			Collections.sort(value,new Comparator<AssignedTask>() {
			        @Override
					public int compare(AssignedTask a1, AssignedTask a2) {
			        	int numligne1=(int)a1.start+a1.taskID;
			        	int numligne2=(int)a2.start+a2.taskID;
			        	numligne1=a1.taskID+a1.numzone*100;
			        	numligne2=a2.taskID+a2.numzone*100;
			            return numligne1-numligne2;
			        }
			    });
		 }
		 );

		 //for(int idjob=0;idjob<mJobsFuturs.size();idjob++) {
		 for (Entry<Integer, List<AssignedTask>> lset :  mTabAssignedJobsSorted.entrySet()) {
			List<AssignedTask> listeTache=lset.getValue();
			Integer barre=lset.getKey();
			int index=barreToIndex.get(barre);
			labelsModel.put(index,new String[listeTache.size()]);
			//tasksTab.add(new AssignedTask[listeTache.size()]);
			int cpt1=0;
		    for(AssignedTask at :listeTache) {


		    	ArrayList<GammeType> df=barreZones.get(barre);
		    	//on retrouve le idtask de la table ZONES SQLSERVER ( numéroté sans trou à partir de 0 <> numzone donc)
		    	// car quant à lui,le taskid de google, est propre à l'ordres des zones d'une gamme
		    	int posteEncours=df.get(at.taskID).idzonebdd;

			    long[] dr={at.start,at.start+at.duration,at.start+at.duration+at.derive};


			    if(at.start < mLowerBound || mLowerBound==0) {
			    	mLowerBound=at.start;
			    }
			    //System.out.println("gamme:"+gamme+" "+df.get(at.taskID).codezone+" start:"+at.start+" numligne:"+df.get(at.taskID).numligne);
			    //System.out.println("at.duration="+at.duration );

			    double incrementY=0.3;


			    if(zonesCumul.containsKey(at.numzone)) {
			    	//System.out.println("gamme:"+gamme+" "+df.get(at.taskID).codezone+" start:"+at.start+" IdPosteZOneCumul:"+IdPosteZOneCumul);
			    	double offset=incrementY*at.IdPosteZoneCumul;
			    	double incrementSpaceY=0.45;
			    	//Encode the room as x value. The width of the bar is only 0.6 to leave a small gap. The course starts 0.1 h/6 min after the end of the preceding course.
				    series[index].add(posteEncours, posteEncours - incrementSpaceY+offset, posteEncours -incrementSpaceY+offset+incrementY, dr[0],dr[0] ,dr[1]);
				 }

			    else {
			    	 series[index].add(posteEncours,posteEncours - 0.3,posteEncours +0.3,
			    			 dr[0],dr[0] ,dr[1] );


			    }

			    labelsModel.get(index)[cpt1]="barre "+barreLabels.get(barre)+" en "+df.get(at.taskID).codezone+"<br>start:"+at.start+", durée:"+toMinutes(at.duration)+", fin:"+(at.derive)
			    		+ "<br>dérive: " +(at.derive-dr[1])+", égouttage:"+df.get(at.taskID).egouttage ;



			    cpt1++;



			 }
		 }


		 buildPlot();

	}

	private void computeTasksByBarreID(Map<Integer, List<AssignedTask>> mAssignedTasksByNumzone,
			HashMap<Integer, ZoneCumul> zonesCumul) {
		Map<Integer, List<AssignedTask>> cumulTask=new HashMap<>();
		 //réorga par jobid/list zones
		 mAssignedTasksByNumzone.values().forEach( taskArr->{

			taskArr.forEach( task->{

				if(!mTabAssignedJobsSorted.containsKey(task.barreID)) {
					mTabAssignedJobsSorted.put(task.barreID, new ArrayList<>());
				}

				mTabAssignedJobsSorted.get(task.barreID).add(task);


				//System.out.println("task.idzoneBDD="+task.idzoneBDD );
				if(zonesCumul.containsKey(task.numzone)) {
					 if(!cumulTask.containsKey(task.numzone)) {
						 cumulTask.put(task.numzone, new ArrayList<>());
					 }
					 cumulTask.get(task.numzone).add(task);
				 }

			});

		 });

		 // on trie chronologiquement les zones de cumul
		 cumulTask.forEach((numzone, tasks) -> {

				  Collections.sort(tasks,new Comparator<AssignedTask>() {
				        @Override
						public int compare(AssignedTask a1, AssignedTask a2) {
				        	int numligne1=(int)a1.start;
				        	int numligne2=(int)a2.start;
				            return numligne1-numligne2;
				        }
				    });
			 }
		 );

		 cumulTask.forEach((numzone, tasks) -> {
			 tasks.forEach( task->{
				ZoneCumul zc=zonesCumul.get(task.numzone);
		    	int end=task.duration+(int)task.start;

		    	task.IdPosteZoneCumul=zc.getPosteIdx((int)task.start, end,task.derive);


			 });

		 }
		 );
	}
	public void setStartTime() {
		mStartTime= LocalDateTime.now();
	}

	private void buildPlot() {
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

		 @Override
		 public String generateToolTip(XYDataset dataset, int series, int item) {
			 	int barre=indexToBarreIndex.get(series);
			 	//System.out.println("series:"+series+" "+" item:"+item+" val="+labelsModel[series][item]);
			 	
			 	return "<html>" +
			 	labelsModel.get(series)[item]+ "<br>" +
			 	tmpsAvantSortie(mTabAssignedJobsSorted.get(barre).get(item).derive) + "<br>" +
	               "</html>";
		    }
		 };

	

	     renderer.setSeriesToolTipGenerator(0, ttgen);
	     renderer.setBaseToolTipGenerator(ttgen);


		 mPlot = new XYPlot(mDataset, new SymbolAxis("zones", mZonesAllGamme), new NumberAxis(), renderer);

		 mPlot.setOrientation(PlotOrientation.HORIZONTAL);

	     mPlot.addRangeMarker(timeBar);

	     JFreeChart j= new JFreeChart(mPlot);

	     ((NumberAxis)mPlot.getRangeAxis()).setAutoRangeIncludesZero(false);

	     mPlot.getRangeAxis().setTickLabelPaint(Color.WHITE);
	     mPlot.getDomainAxis().setTickLabelPaint(Color.WHITE);
	     mPlot.getDomainAxis().setLabelPaint(Color.WHITE);


	     ToolTipManager.sharedInstance().setInitialDelay(0);
	     UIManager.put("ToolTip.font", new Font("SansSerif", Font.BOLD, 20)); // Exemple de police

	     mChartPanel.setChart(j);

	}


	public void prod_diag(String[] listeOF,java.util.Date date) {
		XYIntervalSeriesCollection dataset = new XYIntervalSeriesCollection();

		 ArrayList<PosteBDD> posteAllOF = new ArrayList <>();

		 HashMap<Integer,PosteBDD> mapPosteBDD= SQL_DATA.getInstance().getPostes(listeOF);

		 mapPosteBDD.values().forEach( v->{ posteAllOF.add(v); });


		 //toArray(new PosteBDD[0]);
		// posteCount=posteAllOF.length;

		 Collections.sort(posteAllOF);

		ArrayList<String> labelPosteAllOFTmp= new ArrayList<>();
		for(PosteBDD p:posteAllOF ) {
			labelPosteAllOFTmp.add(p.nom);
		}
		String[] labelPosteAllOF=labelPosteAllOFTmp.toArray(new String[0]);

		 //gamme par fiche production (on peut avoir une  même gamme pour deux fichesProd
		 HashMap<String, String> ficheGamme=  SQL_DATA.getInstance().getFicheGamme(listeOF);



		 //temps aux postes par fiches production
		 HashMap<String, LinkedHashMap<Integer,PosteProd> > tempsAuPostes=SQL_DATA.getInstance().getTempsAuPostes(listeOF,date) ;



		 String[] fichesProd=ficheGamme.keySet().toArray(new String[0]);

		 int offset=0;

		 HashMap<Integer,PosteProd> firstPostes= new  HashMap<>();

		 tempsAuPostes.forEach((key,listePostes)->{
			 Iterator<Map.Entry<Integer,PosteProd>> iterator = listePostes.entrySet().iterator();

			 Map.Entry<Integer, PosteProd> actualValue = iterator.next();
			 firstPostes.put(actualValue.getValue().start,actualValue.getValue());

		 });



		 String[] ficheZones=tempsAuPostes.keySet().toArray(new String[0]);//ficheGamme.values().toArray(new String[0]);

		 int totalNbBarres =ficheZones.length;


		 //Create series. Start and end times are used as y intervals, and the room is represented by the x value
		 XYIntervalSeries[] series = new XYIntervalSeries[totalNbBarres];
		 for(int i = 0; i < totalNbBarres; i++){
		      series[i] = new XYIntervalSeries(ficheZones[i]);
		      dataset.addSeries(series[i]);
		 }

		 for(PosteProd pp: firstPostes.values()) {
			 if(offset==0) {
				 offset=pp.start;
			 }else {
				 if(offset>pp.start) {
					 offset=pp.start;
				 }
			 }
		 }

		labels = new ArrayList<>(totalNbBarres);


		for(int currentFiche = 0; currentFiche < totalNbBarres; currentFiche++){
			String fiche=fichesProd[currentFiche];
			int cptt=0;

			if(!tempsAuPostes.containsKey(fiche)) {
				System.out.println( "Alerte exception ! numfiche="+fiche);
				//continue;
			}
			labels.add(new String[tempsAuPostes.get(fiche).size()]);
			for (Entry<Integer, PosteProd> set :  tempsAuPostes.get(fiche).entrySet()) {

				  PosteProd posteProd=set.getValue();

				  labels.get(currentFiche)[cptt]="<html>"+fiche+
						  "<br>start:"+(posteProd.start-offset)
						  +"<br>durée:"+(posteProd.stop-posteProd.start)
						  +"<br>end:"+(posteProd.stop-offset) +"</html>";
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

				@Override
				public String generateToolTip(XYDataset dataset, int series, int item) {
		            return  labels.get(series)[item];
		        }
		    };
		    renderer.setSeriesToolTipGenerator(0, ttgen);


		    renderer.setBaseToolTipGenerator(ttgen);



		 XYPlot plot = new XYPlot(dataset, new SymbolAxis("zones", labelPosteAllOF), new NumberAxis(), renderer);
	     plot.setOrientation(PlotOrientation.HORIZONTAL);
	     plot.getRangeAxis().setTickLabelPaint(Color.WHITE);
	     plot.getDomainAxis().setTickLabelPaint(Color.WHITE);
	     plot.getDomainAxis().setLabelPaint(Color.WHITE);

	     ToolTipManager.sharedInstance().setInitialDelay(0);
	     UIManager.put("ToolTip.font", new Font("SansSerif", Font.BOLD, 20)); // Exemple de police

	     JFreeChart chart = new JFreeChart(plot);
	     ChartPanel cp=new ChartPanel(chart);
		 cp.setDismissDelay(Integer.MAX_VALUE); // Garder les tooltips affichés indéfiniment
		 cp.setInitialDelay(0); // Affichage immédiat
		 cp.setReshowDelay(0); // Pas de délai entre deux affichages
		 
	     getContentPane().add(cp);
	}


}