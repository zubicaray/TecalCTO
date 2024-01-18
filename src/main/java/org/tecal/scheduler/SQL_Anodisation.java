package org.tecal.scheduler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;



public class SQL_Anodisation {
	private Connection mConnection ;
	private Statement mStatement;
	private String mWHERE_CLAUSE;

	private String mWHERE_NUMZONE;
	private String mListeFiche;
	public  SQL_Anodisation()  {
		String connectionUrl =
                "jdbc:sqlserver://ZUBI-STUDIO\\SQLEXPRESS:1433;"
                + "database=ANODISATION_SECOURS;"
                + "user=sa;"
                + "password=Jeff_nenette;"
                + "encrypt=true;"
                +"integratedSecurity=true;"
                + "trustServerCertificate=true;";
                //+ "loginTimeout=15;";
		
		
		
		mListeFiche="('00079250','00079251','00079252','00079253','00079254',";
		mListeFiche="('00079255','00079256','00079257','00079258','00079259','00079260','00079261','00079262','00079263','00079264','00079265','00079266','00079267')";
		
		//'00079257','00079258','00079259','00079260','00079261',
		mListeFiche="('00079262','00079263','00079264','00079265','00079266','00079267')";
		//mListeFiche="('00079262','00079263','00079264','00079265')";
		//mListeFiche="('00079263','00079264','00079265')";
		mListeFiche="('00079264','00079265')";
		
		// on élimine les postse de chargements /déchargements
		mWHERE_CLAUSE="where DF.numposte not in (1,2,41,42) and DF.DateEntreePoste >=  '20231102' and DF.DateSortiePoste< '20231103'  and "
				+ "DF.numficheproduction in "+mListeFiche+" \r\n";
		
	
		
		mWHERE_NUMZONE=" Z.numzone not in (1,35) ";
		try {
			mConnection = DriverManager.getConnection(connectionUrl);
			mStatement= mConnection.createStatement();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
	
	}
	
class ZoneType {
	
    String codezone;
    int numzone;
    int cumul;
    int idzonebdd;
    int derive;
    ZoneType(String codezone,int numzone,int cumul,int derive,int idzone) {
        this.codezone = codezone;
        this.numzone = numzone;
        this.cumul = cumul;
        this.idzonebdd = idzone;
        this.derive=derive;
      }
    
  }

public HashMap<Integer,ZoneType> getZones() {
		
	ResultSet resultSet = null;        
	HashMap<Integer,ZoneType> res = new HashMap<Integer,ZoneType>();
    // Create and execute a SELECT SQL statement.
    String selectSql = "select Z.CodeZone,Z.numzone, Z.NumDernierPoste-Z.NumPremierPoste+1 as cumul,derive from  \r\n"
    		+ "[Anodisation_secours].[dbo].ZONES Z\r\n"
    		+ "where numzone not in (1,35)\r\n"
    		+ " order by numzone";
    
    try {
		resultSet = mStatement.executeQuery(selectSql);
		int idzone=0;
		// Print results from select statement
        while (resultSet.next()) {
            //System.out.println( resultSet.getString(1));
            ZoneType z=new ZoneType(resultSet.getString(1),resultSet.getInt(2),resultSet.getInt(3),resultSet.getInt(4),idzone);
            res.put(z.numzone,z);
            idzone++;
        }
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

	return res;
}
	
class PosteBDD implements Comparable<PosteBDD> {
	
	int numligne;	
	String nom;
	
	PosteBDD(int numligne, String nom) {
	      this.numligne = numligne;	   
	      this.nom = nom;
	    }
	
	@Override
	public int compareTo(PosteBDD e) {
		return this.numligne-e.numligne;
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
	



public HashMap<String, ArrayList<GammeType> >  getGammesZones() {
		
		ResultSet resultSet = null;    
		
		HashMap<Integer,ZoneType> zones= getZones();
		
		HashMap<String,ArrayList<GammeType> > finalArray  = new HashMap<String, ArrayList<GammeType> >();
        // Create and execute a SELECT SQL statement.
        String selectSql = ""
        		+ "select \r\n"
        		+ "	CONCAT(DC.NumBarre,'-',DC.NumGammeAnodisation),Z.CodeZone,numligne ,Z.numzone, TempsAuPosteSecondes+TempsEgouttageSecondes, \r\n"
        		+ " Z.ID_GROUPEMENT,Z.derive,Z.NumDernierPoste-Z.NumPremierPoste+1 as cumul  "
        		+ "from  \r\n"
        		+ "	[Anodisation_secours].[dbo].[DetailsGammesAnodisation]  DG\r\n"
        		+ "	INNER JOIN [Anodisation_secours].[dbo].ZONES Z\r\n"
        		+ "	on Z.numzone=DG.numzone and  " +mWHERE_NUMZONE+"	INNER JOIN  (\r\n"
        		+ "		select  distinct NumBarre,numficheproduction,NumGammeAnodisation,refGammeAnodisation\r\n"
        		+ "		from [Anodisation_secours].[dbo].[DetailsChargesProduction] \r\n"
        		+ "		where numficheproduction in "+mListeFiche +"\r\n"
        		+ "		--order by NumBarre\r\n"
        		+ "	) AS DC\r\n"
        		+ "	ON DG.numgamme = DC.NumGammeAnodisation\r\n"
        		+ "order by DC.NumBarre,NumGammeAnodisation,numligne "
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
		            resultSet.getInt(6),
		            resultSet.getInt(8));
	            
	          
	            if (!finalArray.containsKey(gt.numgamme)) {
	            	finalArray.put(gt.numgamme, new ArrayList<GammeType>());
	            }
	            
	            finalArray.get(gt.numgamme).add(gt);
	        }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return finalArray;
	}
	
	
	
public HashMap<String, String>  getGammes() {
		
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

	class PosteProd implements Comparable<PosteProd>{
		int[] arrMinutes;
		int numposte;
		int numligneBDD;
		int start;
		int stop;
		String nom;
		
		PosteProd(int numposte,int numligne, String nom,int start, int stop) {
		      this.numposte = numposte;
		      this.numligneBDD = numligne;
		      this.stop = stop;
		      this.start = start;
		      this.nom = nom;
		    }
		

		@Override
		public int compareTo(PosteProd e) {
			return this.start-e.start;
		}
		
	}
	
    // Connect to your database.
    // Replace server name, username, and password with your credentials
    public  HashMap <String, LinkedHashMap<Integer,PosteProd> > getTempsAuPostes() {
        

        ResultSet resultSet = null;        
        
        HashMap <String, LinkedHashMap<Integer,PosteProd> >finalArray = new HashMap <String, LinkedHashMap<Integer,PosteProd>>();

        HashMap<Integer,PosteBDD> postes= getPostes();

        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // tri par DF.Numposte,DG.NumLigne
        // car c l'ordre d'affichage des cases de la gamme dans jfreechart
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