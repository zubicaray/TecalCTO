package org.tecal.scheduler.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.tecal.scheduler.types.GammeType;
import org.tecal.scheduler.types.PosteBDD;
import org.tecal.scheduler.types.PosteProd;
import org.tecal.scheduler.types.ZoneType;




public class SQL_DATA {
	private Connection mConnection ;
	private Statement mStatement;
	
	private HashMap<Integer,ZoneType>  zones;
	private HashMap<String, ArrayList<GammeType> > gammes;
		
	
	private String mWHERE_CLAUSE;

	private String mWHERE_NUMZONE;
	private String mListeFiche;
	public  SQL_DATA()  {
		String connectionUrl =
                "jdbc:sqlserver://ZUBI-STUDIO\\SQLEXPRESS:1433;"
                + "database=ANODISATION_SECOURS;"
                + "user=sa;"
                + "password=Jeff_nenette;"
                + "encrypt=true;"
                +"integratedSecurity=true;"
                + "trustServerCertificate=true;";
                //+ "loginTimeout=15;";
		
		
		
		mListeFiche="('00079250','00079251','00079252','00079253','00079254')";
		mListeFiche="('00079255','00079256','00079257','00079258','00079259','00079260','00079261','00079262','00079263','00079264','00079265','00079266','00079267')";
		
		//'00079257','00079261',
		mListeFiche="('00079254','00079255','00079256','00079257','00079258','00079259','00079260','00079261','00079262','00079263','00079264','00079265','00079266','00079267')";
		//mListeFiche="('00079258','00079259','00079260','00079261','00079262','00079263','00079264','00079265','00079266','00079267')";
		//mListeFiche="('00079261','00079262','00079263','00079264','00079265','00079266','00079267')";
		
		
		
		// test pour le 26/01/1979
		mListeFiche="('00079261','00079262','00079263','00079264','00079265','00079266')";
		
		
		//mListeFiche="('00079263','00079264','00079265','00079266','00079267')";	
		//mListeFiche="('00079262','00079263','00079264','00079265')";
		//mListeFiche="('00079261','00079262')";				
		
		// on élimine les postse de chargements /déchargements
		mWHERE_CLAUSE="where DF.DateEntreePoste >=  '20231102' and DF.DateSortiePoste< '20231103'  and "
				+ "DF.numficheproduction in "+mListeFiche+" \r\n";
			
		
		mWHERE_NUMZONE=" Z.numzone not in (1998989898) ";
		try {
			mConnection = DriverManager.getConnection(connectionUrl);
			mStatement= mConnection.createStatement();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
		
		setZones();
		setLignesGammes();
	
	}
	

public HashMap<Integer,ZoneType> getZones() {

	return zones;
}
private  void setZones() {
		
	ResultSet resultSet = null;        
	zones = new HashMap<Integer,ZoneType>();
    // Create and execute a SELECT SQL statement.
    String selectSql = "select Z.numzone,Z.CodeZone, Z.NumDernierPoste-Z.NumPremierPoste+1 as cumul,derive from  \r\n"
    		+ "[Anodisation_secours].[dbo].ZONES Z WHERE\r\n"
    		+ mWHERE_NUMZONE
    		+ " order by numzone";
    
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
        		+ "[Anodisation_secours].[dbo].[DetailsGammesProduction]  DG\r\n"
        		+ "INNER JOIN   [Anodisation_secours].[dbo].[DetailsFichesProduction] DF\r\n"
        		+ "on  \r\n"
        		+ "	DG.numficheproduction=DF.numficheproduction and\r\n"
        		+ "	DG.numligne=DF.NumLigne and DG.NumPosteReel=DF.NumPoste\r\n"
        		+ "\r\n"
        		+ "INNER JOIN  [Anodisation_secours].[dbo].POSTES P\r\n"
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
        		+ "	[Anodisation_secours].[dbo].[DetailsGammesAnodisation]  DG\r\n"
        		+ "	INNER JOIN [Anodisation_secours].[dbo].ZONES Z\r\n"
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
    		+ "  FROM [ANODISATION_SECOURS].[dbo].[GammesAnodisation] "
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
        		+ "from  [Anodisation_secours].[dbo].[DetailsChargesProduction] DC"
        		+ " INNER JOIN   [Anodisation_secours].[dbo].[DetailsFichesProduction] DF\r\n"
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
        		+ "[Anodisation_secours].[dbo].[DetailsGammesProduction]  DG\r\n"
        		+ "INNER JOIN   [Anodisation_secours].[dbo].[DetailsFichesProduction] DF\r\n"
        		+ "on  \r\n"
        		+ "	DG.numficheproduction=DF.numficheproduction and\r\n"
        		+ "	DG.numligne=DF.NumLigne and DG.NumPosteReel=DF.NumPoste\r\n"
        		+ "\r\n"
        		+ "INNER JOIN [Anodisation_secours].[dbo].POSTES P\r\n"
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
}