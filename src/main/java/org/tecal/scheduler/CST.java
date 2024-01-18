package org.tecal.scheduler;


public class CST {
	
	// temps min du zone pour qu'on la considere overlapable
	// ie qu'elle autorise d'aller d'autre mvt pendant son traitement
	public final static int TEMPS_ZONE_OVERLAP_MIN=180;	
	// temps incompresible d'un mouvement d epoint
	public final static int TEMPS_MVT_PONT_MIN = 15;
	//temps entre les différentes "zones regroupées"	
	public final static int GAP_ZONE_NOOVERLAP =TEMPS_MVT_PONT_MIN*4;
	public final static int TEMPS_MIN_DERIVE =15;	
	
	
	//--------------------------------------------------------------
	public final static boolean PrintTaskTime =false;
	public final static int ANODISATION_NUMZONE =15;
	public final static int NB_PONTS =2;
	
}
