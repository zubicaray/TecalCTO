package org.tecal.scheduler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CST {
	
	
	public final static SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd", Locale.FRENCH);

	public static Date getDate(String s)  {
		try {
			return formatter.parse("s");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public final static String[] mListeOf26janvier={"00079260","00079261","00079262","00079263","00079264","00079265","00079266"};
	
	;
	
	
			
	
	
	//public final static String gammesTest[] ={"000007","000005","000009","000010"};
	public final static String gammesTestBig[] ={"000022","000210","000021","000164","000601","000467","000347","000169","000347"};
	public final static String gammesTestBig2[] ={"000021","000164","000601","000467","000347","000169","000021","000164","000601","000601","000467","000467","000347","000169","000021","000164"};
	public final static String gammesTest_PB_pont2[] ={"000022","000022","000467","000210","000246"};
	//public final static String gammesTest[] ={"000210","000210","000210","000210","000210"};
	//public final static String gammesTest[] ={"000022","000246"};
	public final static String gammesTest1[] ={"000022","000554","000552","000553","000552","000553"};
	public final static String test26janvierBig[] ={"000020","000021","000164","000601","000467","000347","000169","000020","000021","000164","000601","000467","000347","000169"};
	public final static String test26janvier[] ={"000020","000021","000164","000601","000467","000347","000169"};
	public final static String test8mars[] ={"000094","000169","000174","000776","000671"};
	public final static String test8mars2[] ={"000094","000003"};
	// C10 ne sort pas car il attend C05
	public final static String testPrioriteC05[] ={"000601","000164"};
	// pb chevauchage colmatage
	public final static String test2[] ={"000601","000347","000347","000347"};
	public final static String test1mars[] ={"000686","000212","000818","000818","000812","000811","000022","000601","000109","000818"};
	public final static String gammesTest[] =test26janvier;
	
	
	

	//------------------------------------------------------------
	// -----------------  CONSTRAINTES ---------------------------
	//------------------------------------------------------------
	public final static boolean MODE_ECO = false;
	public final static int 	PORTION_HORIZON = 4 ;
	//ancienne méthode
	public final static boolean CSTR_ECART_ZONES_CUMULS=false;
	public final static boolean CSTR_NOOVERLAP_ZONES_GROUPEES=false ;
	//nouvelle méthode
	public final static boolean CSTR_NOOVERLAP_BRIDGES = true;
	public final static boolean	CSTR_BRIDGES_SECURITY = true;

	//------------------------------------------------------------
	// -----------------  DATA ---------------------------
	//------------------------------------------------------------
	public final static int SQLSERVER =1;
	public final static int CSV =2;
	
	//------------------------------------------------------------
	//------------------  TEMPS ----------------------------------
	//------------------------------------------------------------
	// pour affichage dans Gantt, peu importe le temps
	public final static int TEMPS_DECHARGEMENT =60;	
	public final static int TEMPS_CHARGEMENT =100;	
	// temps min du zone pour qu'on la considere overlapable
	// ie qu'elle autorise d'aller d'autre mvt pendant son traitement
	public final static int TEMPS_ZONE_OVERLAP_MIN=180;	
	// temps incompresible d'un mouvement d epoint
	public final static int TEMPS_MVT_PONT_MIN_JOB = 23;
	//temps entre les différentes "zones regroupées"	
	public final static int GAP_ZONE_NOOVERLAP =90;//TEMPS_MVT_PONT_MIN_JOB*5; //90
	// temps autour d'un début de grosse zone
	public final static int TEMPS_MVT_PONT =40;		
	// temps de sécurité entre deux gammes différentes sur un même poste d'ano
	public final static int TEMPS_ANO_ENTRE_P1_P2 = 60;	
	
	
	//--------------------------------------------------------------
	//------------ AFFICHAGE ---------------------------------------
	//--------------------------------------------------------------
	public final static boolean PRINT_PROD_DIAG 	=false;
	public final static boolean PrintTaskTime 		=false;
	public final static boolean PrintZonesTime		=false;
	public final static boolean PrintMvtsPont		=false;
	public final static boolean PrintGroupementZones=false;
	
	
	//------------------------------------------------------------
	public final static int NUMZONE_DEBUT_PONT_2 	=15;
	public final static int ANODISATION_NUMZONE 	=15;
	public final static int COLMATAGE_NUMZONE 		=32;
	public final static int DECHARGEMENT_NUMZONE 	=35;
	public final static int CHARGEMENT_NUMZONE 		=1;
	public final static int NB_PONTS 				=2;
	
}
