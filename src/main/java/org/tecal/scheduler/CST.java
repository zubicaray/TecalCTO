package org.tecal.scheduler;


public class CST {
	
	//------------------------------------------------------------
	// -----------------  CONSTRAINTES ---------------------------
	//------------------------------------------------------------
	public final static boolean CSTR_NOOVERLAP_MVTS_PONT =false;
	public final static boolean CSTR_NOOVERLAP_ZONES_GROUPEES = true ;
	
	//------------------------------------------------------------
	// -----------------  TEMPS ----------------------------------
	//------------------------------------------------------------
	// temps min du zone pour qu'on la considere overlapable
	// ie qu'elle autorise d'aller d'autre mvt pendant son traitement
	public final static int TEMPS_ZONE_OVERLAP_MIN=180;	
	// temps incompresible d'un mouvement d epoint
	public final static int TEMPS_MVT_PONT_MIN = 20;
	//temps entre les différentes "zones regroupées"	
	public final static int GAP_ZONE_NOOVERLAP =TEMPS_MVT_PONT_MIN*2;
	//
	public final static int TEMPS_MVT_PONT =60;	
	
	public final static int TEMPS_DECHARGEMENT =120;	
	
	
	
	//--------------------------------------------------------------
	//------------ AFFICHAGE ---------------------------------------
	//--------------------------------------------------------------
	public final static boolean PrintTaskTime =false;
	public final static boolean PRINT_PROD_DIAG =true;
	public final static boolean PrintZonesTime=false;
	public final static boolean PrintMvtsPont=false;
	public final static boolean PrintGroupementZones=true;
	
	
	//------------------------------------------------------------
	public final static int ANODISATION_NUMZONE =15;
	public final static int COLMATAGE_NUMZONE =32;
	public final static int DECHARGEMENT_NUMZONE =35;
	public final static int NB_PONTS =2;
	
}
