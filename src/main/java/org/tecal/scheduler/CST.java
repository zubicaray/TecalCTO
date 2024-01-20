package org.tecal.scheduler;


public class CST {
	
	//------------------------------------------------------------
	// -----------------  CONSTRAINTES ----------------------------------
	public final static boolean CSTR_MVTS_PONT =false;
	
	//------------------------------------------------------------
	// -----------------  TEMPS ----------------------------------
	// temps min du zone pour qu'on la considere overlapable
	// ie qu'elle autorise d'aller d'autre mvt pendant son traitement
	public final static int TEMPS_ZONE_OVERLAP_MIN=180;	
	// temps incompresible d'un mouvement d epoint
	public final static int TEMPS_MVT_PONT_MIN = 15;
	//temps entre les différentes "zones regroupées"	
	public final static int GAP_ZONE_NOOVERLAP =TEMPS_MVT_PONT_MIN*1;
	//public final static int TEMPS_MIN_DERIVE =10;	
	
	public final static int TEMPS_MVT_PONT =60;	
	
	
	//--------------------------------------------------------------
	//------------ AFFICHAGE -----------------------------------
	public final static boolean PrintTaskTime =false;
	public final static boolean PRINT_PROD_DIAG =true;
	public final static boolean PrintZonesTime=false;
	public final static boolean PrintMvtsPont=false;
	
	
	//------------------------------------------------------------
	public final static int ANODISATION_NUMZONE =15;
	public final static int COLMATAGE_NUMZONE =32;
	public final static int NB_PONTS =2;
	
}
