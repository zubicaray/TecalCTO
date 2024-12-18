package org.tecal.scheduler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import javax.swing.JOptionPane;

public class CST {
	
	
	public final static SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd", Locale.FRENCH);

	public static Date getDate(String s)  {
		try {
			return formatter.parse("s");
		} catch (ParseException e) {
			
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e, "Alerte exception !", JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}
	
	public final static String[] mListeOf26janvier={"00079260","00079261","00079262","00079263","00079264","00079265","00079266"};		
	public final static String gammesTest_PB_pont2[] ={"000022","000022","000467","000210","000246"};


	public final static String test26janvier[] ={"000020","000021","000164","000601","000467","000347","000169"};

	// pb chevauchage colmatage
	public final static String test2[] ={"000601","000347","000601"};

	public final static String testBig[] ={"000054","000601","000485","000024","000601","000818","000812","000717","000002","000169",
			"000020","000021","000164","000601","000467","000347","000169",
			"000686","000212","000818","000818","000812","000811","000022","000601","000109","000818"};
	public final static String testPbNoSolu[] ={"000794","000794","000794"};
	
	
	public final static String gammesTest[] ={"000601", "000097", "000097", "000020", "000485", 
			"000097", "000105", "000601", "000778", "000024", "000097", "000811", "000097", "000152",
			"000152", "000152", "000152", "000152", "000152"};
	
	public static HashMap<String, String> transformStringToMap(String input) {
        HashMap<String, String> map = new HashMap<>();
        
        // Retirer les accolades
        input = input.substring(1, input.length() - 1);
        
        // Séparer chaque paire (index=clé-valeur)
        String[] pairs = input.split(", ");
        
        for (String pair : pairs) {
            // Isoler la partie clé-valeur
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                String[] parts = keyValue[1].split("-");
                if (parts.length == 2) {
                    map.put(parts[0], parts[1]);
                }
            }
        }
        return map;
    }
	
	
	
	//------------------------------------------------------------
	// -----------------  VITESSE --------------------------------
	//------------------------------------------------------------
	public final static int VITESSE =1;
	public final static int CPT_GANTT_OFFSET =100;
	
	
	//------------------------------------------------------------
	// -----------------  CONSTRAINTES ---------------------------
	//------------------------------------------------------------

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
	public final static int TEMPS_ANO_ENTRE_P1_P2 = 30;		
	public final static int TEMPS_MINIMAL_AVANT_DEMARRAGE = 240;	
	
	
	
	
	//--------------------------------------------------------------
	//------------ AFFICHAGE ---------------------------------------
	//--------------------------------------------------------------
	public final static boolean PRINT_PROD_DIAG 	=false;
	public final static boolean PrintTaskTime 		=false;
	public final static boolean PrintZonesTime		=false;
	public final static boolean PrintMvtsPont		=false;
	public final static boolean PrintGroupementZones=false;
	public final static boolean PRINT_BARRES		=true;
	public final static boolean PRINT_JOBS			=true;
	
	
	//------------------------------------------------------------
	public final static int TEMPS_MAX_SOLVEUR		=40;
	public final static int TEMPS_MAX_JOURNEE		=86400;
	public final static int CAPACITE_ANODISATION 	=3;
	public final static int ANODISATION_NUMZONE 	=15;
	public final static int COLMATAGE_NUMZONE 		=32;
	public final static int DECHARGEMENT_NUMZONE 	=35;
	public final static int CHARGEMENT_NUMZONE 		=1;
	public final static int NB_PONTS 				=2;
	//--------------------------------------------------------------
	//------------ VITESSES PONT ---------------------------------------
	//--------------------------------------------------------------
	public final static int VITESSE_LENTE			=0;
	public final static int VITESSE_NORMALE			=1;
	public final static int VITESSE_RAPIDE			=2;
	public final static String[] VITESSES			={"lente","normale","rapide"};
	public final static int VITESSE_LENTE_DESCENTE	=17;
	public final static int VITESSE_RAPIDE_DESCENTE	=3;
	public final static int VITESSE_LENTE_MONTEE	=17;
	public final static int VITESSE_RAPIDE_MONTEE	=6;
	
	
	
}
