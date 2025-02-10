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
	
	public final static boolean TEST_FIXED_JOBS=false;  
	
	

	public final static String testBig[] ={
			"000054","000601","000485","000024","000601","000818","000812","000717","000002","000169",
			"000020","000021","000164","000601","000467","000347","000169","000686","000212","000818",
			"000818","000812","000811","000022","000601","000109","000818"};
	
	public final static String big_test[] = {
			"000001","000020","000020","000020","000020","000020","000024","000024","000024","000024",
			"000047","000054","000116","000117","000117","000117","000119","000127","000146","000169",
			"000200","000243","000352","000601","000601","000601","000601","000735","000770","000773",
			"000775","000776","000776","000776","000776","000776","000776","000776","000776","000776",
			"000811","000811","000818","000818","000818","000818"	
	};
	
	public final static String testSmall[] ={"000601","000485","000024"};
		
	
	public final static String gammesTest[] =testSmall;
	//6=30-000369, 
	public final static String TEST_PROD="{1=23-000713, 2=24-000169, 3=D1-000169, 4=N3-000747, 5=N2-000601, 7=44-000697, 8=19-000174, 9=25-000174}";
	
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
	
	// temps autour d'un début de grosse zone
	public final static int TEMPS_MVT_PONT =40;		
	// temps de sécurité entre deux gammes différentes sur un même poste d'ano
	public final static int TEMPS_ANO_ENTRE_P1_P2 = 30;		
	public final static int TEMPS_MINIMAL_AVANT_DEMARRAGE = 240;	
	
	public final static int TEMPS_PONT_MEME_CUVE = 34;	
	public final static int TEMPS_PONT_PAR_CUVE = 3;	
	public final static int TEMPS_PONT_BASE = 42;	
	
	
	
	
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
