package org.tecal.scheduler.data;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
import org.tecal.scheduler.types.GammeType;
import org.tecal.scheduler.types.PosteBDD;
import org.tecal.scheduler.types.PosteProd;
import org.tecal.scheduler.types.ZoneType;


class TempsDeplacement 		extends HashMap<List<Integer>,Integer[]>	{	private static final long serialVersionUID = 1L;}

public class SQL_DATA {
	private Connection mConnection ;
	private Statement mStatement;
	
	private HashMap<Integer,ZoneType>  zones;
	private HashMap<String, ArrayList<GammeType> > gammes;		
	static TempsDeplacement  mTempsDeplacement;		
	
	private String mWHERE_CLAUSE;

	private String mListeFiche;
	public  SQL_DATA()  {
		
		String connectionUrl = null;
		File fileToParse = new File("tecalCPO.ini");
		
		try {
			Ini ini = new Ini(fileToParse);
			String user=ini.get("BDD", "user");
			String database=ini.get("BDD", "database");
			String password=ini.get("BDD", "password");
			String server=ini.get("BDD", "server");
			
			
			connectionUrl =
	                "jdbc:sqlserver://"+server+";"
	                + "database="+database+";"
	                + "user="+user+";"
	                + "password="+password+";"
	                + "encrypt=true;"
	                +"integratedSecurity=true;"
	                + "trustServerCertificate=true;";
	    
			
		} catch (InvalidFileFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		mListeFiche="('00079250','00079251','00079252','00079253','00079254')";
		mListeFiche="('00079255','00079256','00079257','00079258','00079259','00079260','00079261','00079262','00079263','00079264','00079265','00079266','00079267')";
		
		//'00079257','00079261',
		mListeFiche="('00079254','00079255','00079256','00079257','00079258','00079259','00079260','00079261','00079262','00079263','00079264','00079265','00079266','00079267')";
		//mListeFiche="('00079258','00079259','00079260','00079261','00079262','00079263','00079264','00079265','00079266','00079267')";
		//mListeFiche="('00079261','00079262','00079263','00079264','00079265','00079266','00079267')";
		
		
		// test pour le 26/01/2024
		mListeFiche="('00079261','00079262','00079263','00079264','00079265','00079266')";
		
		
		//mListeFiche="('00079263','00079264','00079265','00079266','00079267')";	
		//mListeFiche="('00079262','00079263','00079264','00079265')";
		//mListeFiche="('00079261','00079262')";				
		
		// on élimine les postse de chargements /déchargements
		mWHERE_CLAUSE="where DF.numficheproduction in "+mListeFiche+" ";			
		
		
		
		try {
			mConnection = DriverManager.getConnection(connectionUrl);
			mStatement= mConnection.createStatement();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
		
		setZones();
		setLignesGammes();
		setTempsDeplacements();
	
	}
	

public HashMap<Integer,ZoneType> getZones() {
	return zones;
}
private  void setZones() {
		
	ResultSet resultSet = null;        
	zones = new HashMap<Integer,ZoneType>();
    // Create and execute a SELECT SQL statement.
    String selectSql = "select Z.numzone,Z.CodeZone, Z.NumDernierPoste-Z.NumPremierPoste+1 as cumul,derive from  \r\n"
    		+ "ZONES Z order by numzone";
    
    try {
		resultSet = mStatement.executeQuery(selectSql);
		int idzone=0;
		// Print results from select statement
        while (resultSet.next()) {
            //System.out.println( resultSet.getString(1));
            ZoneType z=new ZoneType(resultSet.getInt(1),resultSet.getString(2),resultSet.getInt(3),resultSet.getInt(4),idzone);
            zones.put(z.numzone,z);
            idzone++;
        }
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

	
}
	


public HashMap<Integer,PosteBDD> getPostes() {
		
		ResultSet resultSet = null;        
		HashMap<Integer,PosteBDD> res = new HashMap<Integer,PosteBDD>();
		int cpt=0;
        // Create and execute a SELECT SQL statement.
        String selectSql = "select distinct P.Nomposte +' - ' + P.LibellePoste,P.numposte from  \r\n"
        		+ "[DetailsGammesProduction]  DG\r\n"
        		+ "INNER JOIN   [DetailsFichesProduction] DF\r\n"
        		+ "on  \r\n"
        		+ "	DG.numficheproduction=DF.numficheproduction and\r\n"
        		+ "	DG.numligne=DF.NumLigne and DG.NumPosteReel=DF.NumPoste\r\n"
        		+ "\r\n"
        		+ "INNER JOIN  POSTES P\r\n"
        		+ "on P.Numposte=DF.Numposte\r\n"
        		+ mWHERE_CLAUSE
        		+ "order by P.numposte, P.Nomposte +' - ' + P.LibellePoste";
        
        try {
			resultSet = mStatement.executeQuery(selectSql);
			// Print results from select statement
	        while (resultSet.next()) {
	            //System.out.println( resultSet.getString(1));
	            res.put(resultSet.getInt(2),new PosteBDD(cpt,resultSet.getString(1)));
	            cpt++;
	        }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        
        
		return res;
	}
public HashMap<String, ArrayList<GammeType> >  getLignesGammesAll() {
	
	return gammes;
}

private void  setLignesGammes() {
		
		ResultSet resultSet = null;    
		
		
		
		gammes  = new HashMap<String, ArrayList<GammeType> >();
        // Create and execute a SELECT SQL statement.
        String selectSql = ""
        		+ "select \r\n"
        		+ "	DG.NumGamme,Z.CodeZone,numligne ,Z.numzone, "
        		+ " TempsAuPosteSecondes+TempsEgouttageSecondes, \r\n"
        		+ " Z.ID_GROUPEMENT,Z.derive,Z.NumDernierPoste-Z.NumPremierPoste+1 as cumul  "
        		+ "from  \r\n"
        		+ "	[DetailsGammesAnodisation]  DG\r\n"
        		+ "	INNER JOIN ZONES Z\r\n"
        		+ "	on Z.numzone=DG.numzone "
        		+ "order by NumGamme,numligne "
        		;
        try {
			resultSet = mStatement.executeQuery(selectSql);
			// Print results from select statement
	        while (resultSet.next()) {
	            //System.out.println("numzone:"+resultSet.getInt(4)+"   "+resultSet.getString(1) + " " + resultSet.getString(2));
	            
	            int numzone=resultSet.getInt(4);
	            
	            GammeType gt=new GammeType(resultSet.getString(1),	          
		            resultSet.getString(2),	  
		            resultSet.getInt(3),	            
		            numzone,
		            resultSet.getInt(5),
		            zones.get(numzone).idzonebdd,
		            resultSet.getInt(7),
		            resultSet.getInt(6));
	            
	          
	            if (!gammes.containsKey(gt.numgamme)) {
	            	gammes.put(gt.numgamme, new ArrayList<GammeType>());
	            }
	            
	            gammes.get(gt.numgamme).add(gt);
	        }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
public ResultSet getEnteteGammes() {
	ResultSet resultSet = null;    
	
	
	
	gammes  = new HashMap<String, ArrayList<GammeType> >();
    // Create and execute a SELECT SQL statement.
    String selectSql = ""
    		+ "SELECT [NumGamme] as numero"
    		+ ",[NomGamme] as designation "    	
    		+ "  FROM [GammesAnodisation] "
    		+ "order by NumGamme "
    		;
    try {
		resultSet = mStatement.executeQuery(selectSql);
		// Print results from select statement
    
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

	return resultSet;
}
	
	
	
public HashMap<String, String>  getFicheGamme() {
		
		ResultSet resultSet = null;        
		HashMap<String, String> res = new HashMap<>();
        // Create and execute a SELECT SQL statement.
        String selectSql = "select distinct DF.numficheproduction,NumGammeAnodisation "
        		+ "from  [DetailsChargesProduction] DC"
        		+ " INNER JOIN   [DetailsFichesProduction] DF\r\n"
        		+ "on  \r\n"
        		+ "	DC.numficheproduction=DF.numficheproduction  "
        		
        		+ mWHERE_CLAUSE;
        try {
			resultSet = mStatement.executeQuery(selectSql);
			// Print results from select statement
	        while (resultSet.next()) {
	            //System.out.println(resultSet.getString(1) + " " + resultSet.getString(2));
	            res.put(resultSet.getString(1), resultSet.getString(2));
	        }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return res;
	}


public void setTempsDeplacements() {
	
	 ResultSet resultSet = null;        
     
	 mTempsDeplacement = new TempsDeplacement();
	 
	 
     String selectSql = "SELECT [depart]"
     		+ "      ,[arrivee]"
     		+ "      ,[lent]"
     		+ "      ,[normal]"
     		+ "      ,[rapide]"
     		+ "  FROM [TempsDeplacements]";
     
     try {
			resultSet = mStatement.executeQuery(selectSql);
			// Print results from select statement
	        while (resultSet.next()) {
	        
	            int depart=resultSet.getInt(1);
	            int arrivee=resultSet.getInt(2);
	            int lent=resultSet.getInt(3);
	            int normal=resultSet.getInt(4);
	            int rapide=resultSet.getInt(5);
	            
	            List<Integer> key=Arrays.asList(depart, arrivee);
	            Integer values[]= {lent,normal,rapide};
	            mTempsDeplacement.put(key,values);
	            
	        }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	
}
    // Connect to your database.
    // Replace server name, username, and password with your credentials
    public  HashMap <String, LinkedHashMap<Integer,PosteProd> > getTempsAuPostes() {
        

        ResultSet resultSet = null;        
        
        HashMap <String, LinkedHashMap<Integer,PosteProd> >finalArray = new HashMap <String, LinkedHashMap<Integer,PosteProd>>();

        HashMap<Integer,PosteBDD> postes= getPostes();

        /**
         * tri par DF.Numposte,DG.NumLigne
         * car c l'ordre d'affichage des cases de la gamme dans jfreechart
         */
        
        String selectSql = "select DG.numficheproduction,P.Nomposte +' - ' + P.LibellePoste,  \r\n"
        		+ "DATEDIFF(SECOND, DATEADD(DAY, DATEDIFF(DAY, 0, DF.DateEntreePoste), 0),DF.DateEntreePoste),\r\n"
        		+ "DATEDIFF(SECOND, DATEADD(DAY, DATEDIFF(DAY, 0, DF.DateSortiePoste), 0),DF.DateSortiePoste)	,DF.NumLigne, DF.Numposte  from  \r\n"
        		+ "[DetailsGammesProduction]  DG\r\n"
        		+ "INNER JOIN   [DetailsFichesProduction] DF\r\n"
        		+ "on  \r\n"
        		+ "	DG.numficheproduction=DF.numficheproduction and\r\n"
        		+ "	DG.numligne=DF.NumLigne and DG.NumPosteReel=DF.NumPoste\r\n"
        		+ "\r\n"
        		+ "INNER JOIN POSTES P\r\n"
        		+ "on P.Numposte=DF.Numposte\r\n"
        		+ mWHERE_CLAUSE
        		+ "order by DG.numficheproduction, DF.Numposte,DG.NumLigne";
        
        try {
			resultSet = mStatement.executeQuery(selectSql);
			// Print results from select statement
	        while (resultSet.next()) {
	            //System.out.println(resultSet.getString(1) + " " + resultSet.getString(2));
	            //System.out.println(resultSet.getString(3) + " " + resultSet.getString(4));
	            String fiche=resultSet.getString(1);
	            String Nomposte=resultSet.getString(2);
	            int numligne=resultSet.getInt(5);
	            int numposte=resultSet.getInt(6);
	            
	            if (!finalArray.containsKey(fiche)) {
	            	finalArray.put(fiche, new  LinkedHashMap<Integer,PosteProd> ());
	            }
	            int[] arrMinutes=   {resultSet.getInt(3),resultSet.getInt(4)};
	            
	            int numligneBDD=postes.get(numposte).numligne;
	            // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	            //numligne: numéro de ligne de prod ( permet de différencier deux C06 différents pour une même gamme
	            //numligneBDD id sans trou de la game Postes
	            finalArray.get(fiche).put( numligne,new PosteProd(numposte,numligneBDD,Nomposte, arrMinutes[0],arrMinutes[1]));
	            
	        }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

      return finalArray;
       
    }


    static TempsDeplacement getmTempsDeplacement() {
		return mTempsDeplacement;
	}
    public static int getTempsDeplacement(int dep,int arr,int vitesse) {    	
		return mTempsDeplacement.get(Arrays.asList(dep, arr))[vitesse];
	}


	static void setmTempsDeplacement(TempsDeplacement mTempsDeplacement) {
		SQL_DATA.mTempsDeplacement = mTempsDeplacement;
	}
}