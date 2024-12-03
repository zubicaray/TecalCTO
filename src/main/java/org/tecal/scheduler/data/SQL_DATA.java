package org.tecal.scheduler.data;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.StringJoiner;

import javax.swing.JOptionPane;

import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tecal.scheduler.types.GammeType;
import org.tecal.scheduler.types.PosteBDD;
import org.tecal.scheduler.types.PosteProd;
import org.tecal.scheduler.types.ZoneType;

class TempsDeplacement 		extends HashMap<List<Integer>,Integer[]>	{	private static final long serialVersionUID = 1L;}

public class SQL_DATA {
	private static final Logger logger = LogManager.getLogger(SQL_DATA.class);
	private Connection mConnection ;
	public Connection getmConnection() {
		return mConnection;
	}
	public void setmConnection(Connection mConnection) {
		this.mConnection = mConnection;
	}
	private Statement mStatement;

	private  HashMap<Integer,ZoneType>  mZones;

	public HashMap<Integer, ZoneType> getZones() {
		return mZones;
	}

	private  ArrayList<Integer>   mZonesSecu;
	private  HashMap<Integer,Integer>  mRelatedZones;
	public HashMap<Integer, Integer> getRelatedZones() {
		return mRelatedZones;
	}
	
	private HashMap<String, ArrayList<GammeType> > gammes;
	private HashSet<String> mMissingTimeMovesGammes;
	static TempsDeplacement  mTempsDeplacement;
	private   final SimpleDateFormat FMT =
	            new SimpleDateFormat( "yyyyMMdd" );

	public static String quote(String s) {
	    return new StringBuilder()
	        .append('\'')
	        .append(s)
	        .append('\'')
	        .toString();
	}
	public static String toClause(String[] arrayS) {

		StringJoiner joiner = new StringJoiner(",");


		for(String s : arrayS) {
			joiner.add(quote(s));
		}
		return joiner.toString();
	}
	private static final SQL_DATA instance = new SQL_DATA();

	public static SQL_DATA getInstance() {

		return instance;

	}

	private  SQL_DATA()  {

		String connectionUrl = null;
		File fileToParse = new File("TecalCPO.ini");

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
	                //+"integratedSecurity=true;"
	                + "trustServerCertificate=true;";


		} catch (InvalidFileFormatException e) {
			JOptionPane.showMessageDialog(null, e, "Alerte exception ! \r\n"+ connectionUrl,JOptionPane.ERROR_MESSAGE);
			logger.fatal(e);
			e.printStackTrace();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, e, "Alerte exception !", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			logger.fatal(e);
		}






		try {
			mConnection = DriverManager.getConnection(connectionUrl);
			mStatement= mConnection.createStatement();
			
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, e, "Alerte exception !", JOptionPane.ERROR_MESSAGE);
			
			logger.fatal(e);
		}


		setZones();
		setZonesSecu();
		setRelatedZones();
		setLignesGammes();
		setTempsDeplacements();
		setMissingTimeMovesGammes();

	}




/*
 * permet de relier par exemple la zone C32 qui fait partie d ela multizone C31->C32
 */
private void setRelatedZones() {

	mRelatedZones = new HashMap<>();
	for(ZoneType  z1: mZones.values()) {
		for(ZoneType  z2: mZones.values()) {
			if(z1.numzone == z2.numzone) {
				continue;
			}

			if( z2.idPosteFin>z2.idPosteDeb &&  // z2 est une multi zone
				z2.idPosteDeb <= z1.idPosteDeb && z2.idPosteFin >= z1.idPosteFin // z1 est comprise dedans
			) {
				mRelatedZones.put(z1.numzone,z2.numzone);
			}

		}

	}
}


public  void setZonesSecu() {

	ResultSet resultSet = null;
	if(mZonesSecu ==null)
		mZonesSecu= new ArrayList<>();
	else mZonesSecu.clear();

    // Create and execute a SELECT SQL statement.
    String selectSql = "select Z.numzone "
    		+ " from Zones Z  where SecuritePonts=1 "
    		+ "order by numzone";

    try {
		resultSet = mStatement.executeQuery(selectSql);

		// Print results from select statement
        while (resultSet.next()) {

            mZonesSecu.add(resultSet.getInt(1));

        }
	} catch (SQLException e) {
		JOptionPane.showMessageDialog(null, e, "Alerte exception !", JOptionPane.ERROR_MESSAGE);
		logger.error(e);
	}


}


public  void setZones() {

	ResultSet resultSet = null;
	if(mZones == null)
		mZones = new HashMap<>();
	else mZones.clear();
	
    // Create and execute a SELECT SQL statement.
    String selectSql = "select Z.numzone,Z.CodeZone, NbrPostes as cumul"
    		+ ",derive ,"
    		+ "NumPremierPoste,NumDernierPoste"
    		+ " from   "
    		+ "Zones Z order by numzone";

    try {
		resultSet = mStatement.executeQuery(selectSql);
		int idzone=0;
		// Print results from select statement
        while (resultSet.next()) {
            //System.out.println( resultSet.getString(1));
            ZoneType z=new ZoneType(resultSet.getInt(1),resultSet.getString(2),resultSet.getInt(3),
            		resultSet.getInt(4),idzone,resultSet.getInt(5),resultSet.getInt(6));
            mZones.put(z.numzone,z);
            idzone++;
        }
	} catch (SQLException e) {
		JOptionPane.showMessageDialog(null, e, "Alerte exception !", JOptionPane.ERROR_MESSAGE);
		logger.error(e);
	}


}

public Statement getStatement() throws SQLException {

	return mConnection.createStatement();

}

public PreparedStatement getPreparedStatement(String query) throws SQLException {

	return mConnection.prepareStatement(query);

}

//TODO
//appeler deux fois pour le diag de prod
public HashMap<Integer,PosteBDD> getPostes(String[] listeOF) {

		ResultSet resultSet = null;
		HashMap<Integer,PosteBDD> res = new HashMap<>();
		int cpt=0;
        // Create and execute a SELECT SQL statement.
        String selectSql = "select distinct P.Nomposte +' - ' + P.LibellePoste,P.numposte from   "
        		+ "[DetailsGammesProduction]  DG "
        		+ " RIGHT OUTER  JOIN [DetailsFichesProduction] DF "
        		+ "on   "
        		+ "	DG.numficheproduction=DF.numficheproduction  COLLATE FRENCH_CI_AS  and "
        		+ "	DG.numligne=DF.NumLigne and DG.NumPosteReel=DF.NumPoste "
        		+ " "
        		+ "INNER JOIN  POSTES P "
        		+ "on P.Numposte=DF.Numposte "
        		+ " where DF.numficheproduction in  ("+toClause(listeOF)+")   "
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
			JOptionPane.showMessageDialog(null, e, "Alerte exception !", JOptionPane.ERROR_MESSAGE);
			logger.error(e);
		}



		return res;
	}
public HashMap<String, ArrayList<GammeType> >  getLignesGammesAll() {

	return gammes;
}

private void  setLignesGammes() {

		ResultSet resultSet = null;



		gammes  = new HashMap< >();
        // Create and execute a SELECT SQL statement.
        String selectSql = ""
        		+ "select  "
        		+ "	DG.NumGamme,Z.CodeZone,numligne ,Z.numzone, "
        		+ " TempsAuPosteSecondes,  "
        		+ " Z.derive,Z.NumDernierPoste-Z.NumPremierPoste+1 as cumul ,TempsEgouttageSecondes "
        		+ "from   "
        		+ "	[DetailsGammesAnodisation]  DG "
        		+ "	INNER JOIN Zones Z "
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
		            mZones.get(numzone).idzonebdd,
		            resultSet.getInt(6),

		            resultSet.getInt(8));


	            if (!gammes.containsKey(gt.numgamme)) {
	            	gammes.put(gt.numgamme, new ArrayList<>());
	            }

	            gammes.get(gt.numgamme).add(gt);
	        }
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, e, "Alerte exception !", JOptionPane.ERROR_MESSAGE);
			logger.error(e);
		}


	}
public  ResultSet getBDDZones() {
	
	ResultSet rs=null;
	String query = "SELECT * FROM Zones";

    try {
    	Statement stmt = mConnection.createStatement();
    
        rs = stmt.executeQuery(query);

        

    } catch (SQLException e) {
       
        JOptionPane.showMessageDialog(null, e, "Erreur lors du chargement des données : " + e.getMessage(), JOptionPane.ERROR_MESSAGE);
        logger.error(e);
    }
	
	return rs;
}

public  ResultSet getStatsAnodisation(String[] listeOF) {
	ResultSet resultSet = null;
	String paramsOF=toClause(listeOF);
	
	String requeteSQL = String.format("""
            DECLARE @DateDebut DATETIME;
            DECLARE @DateFin DATETIME;

            SELECT 
                @DateDebut = MIN(DateEntreePoste),
                @DateFin = MAX(DateSortiePoste)
            FROM DetailsFichesProduction
            WHERE NumPoste IN (18, 19, 20) 
              AND NumFicheProduction IN (%s);

            WITH CTE_Durees AS (
                SELECT
                    NumPoste,
                    DATEDIFF(SECOND, 
                        DateEntreePoste,DateSortiePoste) AS DureeOccupation
                FROM DetailsFichesProduction
                WHERE NumPoste IN (18, 19, 20)
                  AND DateSortiePoste > @DateDebut
                  AND DateEntreePoste < @DateFin
                  AND NumFicheProduction IN (%s)
            )
            SELECT
                P.NomPoste,
                SUM(DureeOccupation) AS 'duree totale',
                --DATEDIFF(SECOND, @DateDebut, @DateFin) AS DureeTotalePeriode,
                CAST(SUM(DureeOccupation) * 100.0 / NULLIF(DATEDIFF(SECOND, @DateDebut, @DateFin), 0) AS DECIMAL(10, 2)) AS 'taux occupation'
            FROM CTE_Durees,Postes P
            WHERE P.NumPoste=CTE_Durees.NumPoste
            GROUP BY P.NomPoste
            ORDER BY P.NomPoste
            """,paramsOF,paramsOF);
	try {
		resultSet = mStatement.executeQuery(requeteSQL);
		
	} catch (SQLException e) {
		JOptionPane.showMessageDialog(null, e, "Alerte exception !", JOptionPane.ERROR_MESSAGE);
		logger.error(e);
		
	}
	
	return resultSet;
}


public  ResultSet getTauxAnodisationJours( Date dateDebut , Date dateFin ) {
	ResultSet resultSet = null;
	
	java.util.Date dt = dateFin;
	Calendar c = Calendar.getInstance();
	c.setTime(dt);
	c.add(Calendar.DATE, 1);
	dt = c.getTime();
	
	String query = """
          
            
		SELECT
		    J as Jour,DureeOccupation,DureeOccupation*100/DureeMaxPossible as TauxOccupationPourcentage
		FROM (
		    SELECT
		        CAST(DateEntreePoste AS DATE ) as J,
		        DATEDIFF(SECOND, Min(DateEntreePoste) ,max(DateSortiePoste))*3 AS DureeMaxPossible,
		        SUM(DATEDIFF(SECOND, DateEntreePoste ,DateSortiePoste         )) AS DureeOccupation
		
		    FROM DetailsFichesProduction
		    WHERE NumPoste IN (18, 19, 20) -- Postes concernés
		        AND DateEntreePoste >= ? -- Exclure les enregistrements terminés avant la période
		        AND DateSortiePoste < ?   -- Exclure les enregistrements commençant après la période
		    GROUP BY 
		        CAST(DateEntreePoste AS DATE)
		    HAVING 
		        COUNT(*) > 0 -- Elimine les jours sans occupation
		) T
		    ORDER BY Jour;
            """;
	try {
		
		 PreparedStatement statement = mConnection.prepareStatement(query);

		    // Définir les paramètres (assurez-vous que `dateDebut` et `dateFin` sont bien définis)
		    statement.setDate(1, new java.sql.Date(dateDebut.getTime())); // 1er paramètre
		    statement.setDate(2, new java.sql.Date(dt.getTime()));   // 2e paramètre

		    // Exécuter la requête
		     resultSet = statement.executeQuery();
		
	} catch (SQLException e) {
		JOptionPane.showMessageDialog(null, e, "Alerte exception !", JOptionPane.ERROR_MESSAGE);
		logger.error(e);
	}
	
	return resultSet;
}



public  ResultSet getEnteteGammes() {
	ResultSet resultSet = null;



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
		JOptionPane.showMessageDialog(null, e, "Alerte exception !", JOptionPane.ERROR_MESSAGE);
		logger.error(e);
	}

	return resultSet;
}
public void  setMissingTimeMovesGammes() {
	ResultSet resultSet = null;
	mMissingTimeMovesGammes = new HashSet<>();



    // Create and execute a SELECT SQL statement.
    String selectSql = "SELECT DISTINCT NumGamme FROM\r\n"
    		+ "(\r\n"
    		+ "SELECT\r\n"
    		+ "	D1.NumGamme,\r\n"
    		+ "    D1.NumLigne,D1.NumZone as N1,D2.NumZone as N2,T.normal\r\n"
    		+ "    \r\n"
    		+ "  FROM \r\n"
    		+ "	[DetailsGammesAnodisation] D1\r\n"
    		+ "	INNER JOIN  [DetailsGammesAnodisation] D2\r\n"
    		+ "	ON D1.NumGamme=D2.NumGamme and D1.NumLigne=D2.NumLigne-1\r\n"
    		+ "	INNER JOIN TempsDeplacements T\r\n"
    		+ "	ON T.depart=D1.NumZone and T.arrivee=D2.NumZone and T.normal=0\r\n"
    		+ " -- order by D1.NumGamme,D1.NumLigne\r\n"
    		+ "  ) TEMP "
    		+ "order by NumGamme "
    		;
    try {

		resultSet = mStatement.executeQuery(selectSql);
		 while (resultSet.next()) {
	            //System.out.println(resultSet.getString(1) + " " + resultSet.getString(2));
			 mMissingTimeMovesGammes.add(resultSet.getString(1));
	        }

	} catch (SQLException e) {
		JOptionPane.showMessageDialog(null, e, "Alerte exception !", JOptionPane.ERROR_MESSAGE);
		logger.error(e);
	}

}


public boolean gammeChangedAfterOF(String of,String gamme) {
	boolean res= false;
	ResultSet resultSet;
	String req="select 1 \r\n"
			+ "from \r\n"
			+ "	DetailsFichesProduction P\r\n"
			+ "	INNER JOIN DetailsGammesProduction PA\r\n"
			+ "		on  PA.numligne=P.numligne and P.NumFicheProduction=PA.NumFicheProduction\r\n"
			+ "	INNER JOIN  DetailsGammesAnodisation  G\r\n"
			+ "		on PA.numligne=G.numligne and G.numzone != PA.numzone\r\n"			
			+ "where  P.NumFicheProduction='"+of+"' and G.NumGamme='"+gamme+"' ;";
	


	    try {
	    	resultSet = mStatement.executeQuery(req);
			// Print results from select statement
	    	if(resultSet.isBeforeFirst()) return true;

		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, e, "Alerte exception !", JOptionPane.ERROR_MESSAGE);
			logger.error(e);
			res=false;
		}

	    return res;

}

public boolean gammeCalibrageExists(String gamme) {
	boolean res= false;
	ResultSet resultSet;
	String req="select 1 \r\n"
			+ "from \r\n"
			+ "	CalibrageTempsGammes \r\n"
			+ "	Where NumGamme='"+gamme+"' ;";
	


	    try {
	    	resultSet = mStatement.executeQuery(req);
			// Print results from select statement
	    	if(resultSet.isBeforeFirst()) return true;

		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, e, "Alerte exception !", JOptionPane.ERROR_MESSAGE);
			logger.error(e);
			res=false;
		}

	    return res;

}

public boolean updateCalibrageGamme(String gamme,String of,java.util.Date d) {
	boolean res= false;
	String date=toSQLServerFormat(d); 
	String req="update \r\n"
			+ "	CalibrageTempsGammes \r\n"
			+" set NumFicheProduction='"+of+"' , date='"+date+"'"
			+ "	Where NumGamme='"+gamme+"' ;";
	


	    try {
	    	res = mStatement.execute(req);
			// Print results from select statement
	    	

		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, e, "Alerte exception !", JOptionPane.ERROR_MESSAGE);
			logger.error(e);
			res=false;
		}

	    return res;

}

public boolean insertCalibrageGamme(String gamme,String of,java.util.Date d) {
	boolean res= false;
	
	String date=toSQLServerFormat(d); 
	
	String req="insert into  \r\n"
			+ "	CalibrageTempsGammes (NumGamme,NumFicheProduction,date) values \r\n"
			+"( '"+gamme+"','"+of+"','"+date+"' )";
	


	    try {
	    	res = mStatement.execute(req);
			// Print results from select statement
	    	

		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, e, "Alerte exception !", JOptionPane.ERROR_MESSAGE);
			logger.error(e);
			res=false;
		}

	    return res;

}





public boolean updateTpsMvts(String of,boolean updateNoNull) {
	boolean res= false;

	String req="Update  [TempsDeplacements] \r\n"
			+ "SET normal=T.tps\r\n"
			+ "FROM \r\n"
			+ "	[dbo].[TempsDeplacements] TPS\r\n"
			+ "	INNER JOIN (\r\n"
			+ "\r\n"
			+ "		select \r\n"
			+ "			Z1.NumZone N1 ,--Z1.LibelleZone M1,\r\n"
			+ "			Z2.NumZone N2,--Z2.LibelleZone M2,\r\n"
			+ "			DATEDIFF(SECOND, F1.DateSortiePoste, F2.DateEntreePoste)-\r\n"
			+ "				DP1.TempsEgouttageSecondes	as tps\r\n"
			+ "		from \r\n"
			+ "			DetailsFichesProduction  F1\r\n"
			+ "			INNER JOIN DetailsFichesProduction  F2\r\n"
			+ "			ON F1.NumFicheProduction =F2.NumFicheProduction\r\n"
			+ "				AND F1.NumLigne=F2.NumLigne-1			\r\n"
			+ "			INNER JOIN DetailsGammesProduction DP1\r\n"
			+ "				ON F1.NumFicheProduction =DP1.NumFicheProduction and F1.NumLigne =DP1.NumLigne\r\n"
			+ "			INNER JOIN DetailsGammesProduction DP2\r\n"
			+ "				ON F2.NumFicheProduction =DP2.NumFicheProduction and F2.NumLigne =DP2.NumLigne			\r\n"
			+ "			INNER JOIN Zones Z1\r\n"
			+ "				on Z1.NumZone=DP1.NumZone\r\n"
			+ "			INNER JOIN Zones Z2\r\n"
			+ "				on Z2.NumZone=DP2.NumZone\r\n"
			+ "			\r\n"
			+ "	\r\n"
			+ "		where F1.NumFicheProduction='"+of+"' \r\n"
			+ "	)  T\r\n"
			+ "	ON TPS.depart=T.N1 and TPS.arrivee=T.N2 ";
	if(!updateNoNull) {
		req+="where normal=0";
	}


	    try {
			res = mStatement.execute(req);
			// Print results from select statement

		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, e, "Alerte exception !", JOptionPane.ERROR_MESSAGE);
			logger.error(e);
			res=false;
		}

	    return res;

}

public boolean eraseTpsMvts() {
	boolean res= true;

	String req="Update  [TempsDeplacements]  set normal=0; ";
	

	    try {
			mStatement.execute(req);
			// Print results from select statement

		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, e, "Alerte exception !", JOptionPane.ERROR_MESSAGE);
			logger.error(e);
			res=false;
		}

	    return res;

}

public boolean resetAllTpsMvts() {
	boolean res= true;

	String req="Update  [TempsDeplacements] \r\n"
			+ "SET normal=T.tps\r\n"
			+ "FROM \r\n"
			+ "	[dbo].[TempsDeplacements] TPS\r\n"
			+ "	INNER JOIN (\r\n"
			+ "\r\n"
			+ "		select \r\n"
			+ "			Z1.NumZone N1 ,\r\n"
			+ "			Z2.NumZone N2,\r\n"
			+ "			AVG(\r\n"
			+ "				DATEDIFF(SECOND, F1.DateSortiePoste, F2.DateEntreePoste)-\r\n"
			+ "				DP1.TempsEgouttageSecondes\r\n"
			+ "			)	as tps\r\n"
			+ "		from \r\n"
			+ "			DetailsFichesProduction  F1\r\n"
			+ "			INNER JOIN DetailsFichesProduction  F2\r\n"
			+ "			ON F1.NumFicheProduction COLLATE FRENCH_CI_AS =F2.NumFicheProduction  COLLATE FRENCH_CI_AS  \r\n"
			+ "				AND F1.NumLigne=F2.NumLigne-1			and 	F1.DateSortiePoste< F2.DateEntreePoste\r\n"
			+ "			INNER JOIN DetailsGammesProduction DP1\r\n"
			+ "				ON F1.NumFicheProduction COLLATE FRENCH_CI_AS  =DP1.NumFicheProduction  COLLATE FRENCH_CI_AS and F1.NumLigne =DP1.NumLigne\r\n"
			+ "			INNER JOIN DetailsGammesProduction DP2\r\n"
			+ "				ON F2.NumFicheProduction COLLATE FRENCH_CI_AS =DP2.NumFicheProduction COLLATE FRENCH_CI_AS and F2.NumLigne =DP2.NumLigne			\r\n"
			+ "			INNER JOIN Zones Z1\r\n"
			+ "				on Z1.NumZone=DP1.NumZone\r\n"
			+ "			INNER JOIN Zones Z2\r\n"
			+ "				on Z2.NumZone=DP2.NumZone\r\n"
			+ "			\r\n"
			+ "	\r\n"
			+ "		where F1.NumFicheProduction COLLATE FRENCH_CI_AS in (select NumFicheProduction COLLATE FRENCH_CI_AS from CalibrageTempsGammes )   \r\n"
			+ "		group by Z1.NumZone  ,Z2.NumZone "
			+ "		\r\n"
			+ "	)  T\r\n"
			+ "	ON TPS.depart=T.N1 and TPS.arrivee=T.N2 ";
	

	    try {
			mStatement.execute(req);
			// Print results from select statement

		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, e, "Alerte exception !", JOptionPane.ERROR_MESSAGE);
			logger.error(e);
			res=false;
		}

	    return res;

}




public ResultSet getTpsMvts() {
	ResultSet resultSet = null;



    // Create and execute a SELECT SQL statement.
    String selectSql = "SELECT [depart],Z1.CodeZone\r\n"
    		+ "      ,[arrivee],Z2.CodeZone\r\n"
    		+ "      ,[lent]\r\n"
    		+ "      ,[normal]\r\n"
    		+ "      ,[rapide]\r\n"
    		+ "  FROM [TempsDeplacements] T,\r\n"
    		+ "  [Zones] Z1,\r\n"
    		+ "  [Zones] Z2\r\n"
    		+ "  WHERE\r\n"
    		+ "  Z1.NumZone=T.depart and Z2.numzone=T.arrivee"


    		;
    try {
		resultSet = mStatement.executeQuery(selectSql);
		// Print results from select statement

	} catch (SQLException e) {
		JOptionPane.showMessageDialog(null, e, "Alerte exception !", JOptionPane.ERROR_MESSAGE);
		logger.error(e);
	}

	return resultSet;
}


public ResultSet getVisuProd(java.util.Date inDate) {
	ResultSet resultSet = null;

	java.util.Date dt = inDate;
	Calendar c = Calendar.getInstance();
	c.setTime(dt);
	c.add(Calendar.DATE, 1);
	dt = c.getTime();

	String deb=toSQLServerFormat(inDate);
	String fin=toSQLServerFormat(dt);

    // Create and execute a SELECT SQL statement.
    String selectSql = "select distinct  DG.numficheproduction as [N° OF], 	DC.NumGammeANodisation as [gamme ],DC.NumBarre as  [barre] \r\n"
    		+ "from   	[DetailsGammesProduction]  DG 	\r\n"
    		+ "LEFT OUTER JOIN   [DetailsFichesProduction] DF 	on   		\r\n"
    		+ "DG.numficheproduction=DF.numficheproduction COLLATE FRENCH_CI_AS and 		\r\n"
    		+ "DG.numligne=DF.NumLigne  and DF.NumLigne=1 	\r\n"
    		+ "INNER JOIN ( select distinct numficheproduction,NumGammeANodisation,NumBarre from [DetailsChargesProduction] where numligne=1\r\n"
    		+ ") DC 	\r\n"
    		+ "on   		DC.numficheproduction=DF.numficheproduction  COLLATE FRENCH_CI_AS \r\n"
    		+ "WHERE		DF.DateEntreePoste >=  '"+deb+"'  and DF.DateEntreePoste < '"+fin+"'      "
    		+ "order by DG.numficheproduction ,DC.NumBarre"


    		;
    try {
		resultSet = mStatement.executeQuery(selectSql);
		// Print results from select statement

	} catch (SQLException e) {
		JOptionPane.showMessageDialog(null, e, "Alerte exception !", JOptionPane.ERROR_MESSAGE);
		logger.error(e);
	}

	return resultSet;
}

private String toSQLServerFormat(java.util.Date d) {

        java.sql.Date       date = new java.sql.Date( d.getTime());
        return  FMT.format( date ) ;


}


public HashMap<String, String>  getFicheGamme(String[] listeOF) {

		ResultSet resultSet = null;
		HashMap<String, String> res = new HashMap<>();
        // Create and execute a SELECT SQL statement.
        String selectSql = "select distinct DF.numficheproduction,NumGammeAnodisation ,DateEntreePoste "
        		+ "from  [DetailsChargesProduction] DC"
        		+ " INNER JOIN   [DetailsFichesProduction] DF "
        		+ "on   "
        		+ "	DC.numficheproduction=DF.numficheproduction   COLLATE FRENCH_CI_AS "

        		+" and DF.numficheproduction in ("+toClause(listeOF)+")  order by DateEntreePoste " ;
        try {
			resultSet = mStatement.executeQuery(selectSql);
			// Print results from select statement
	        while (resultSet.next()) {
	            //System.out.println(resultSet.getString(1) + " " + resultSet.getString(2));
	            res.put(resultSet.getString(1), resultSet.getString(2));
	        }
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, e, "Alerte exception !", JOptionPane.ERROR_MESSAGE);
			logger.error(e);
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
			JOptionPane.showMessageDialog(null, e, "Alerte exception !", JOptionPane.ERROR_MESSAGE);
			logger.error(e);
		}



}
    // Connect to your database.
    // Replace server name, username, and password with your credentials
    public  HashMap <String, LinkedHashMap<Integer,PosteProd> > getTempsAuPostes(String[] listeOF,Date ds) {


        ResultSet resultSet = null;

        HashMap <String, LinkedHashMap<Integer,PosteProd> >finalArray = new HashMap <>();

        HashMap<Integer,PosteBDD> postes= getPostes(listeOF);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        String format = formatter.format(ds);

        /**
         * tri par DF.Numposte,DG.NumLigne
         * car c l'ordre d'affichage des cases de la gamme dans jfreechart
         */

        String selectSql = "select DF.numficheproduction,P.Nomposte +' - ' + P.LibellePoste,   "
        		+ "DATEDIFF(SECOND, '"+format+"',DF.DateEntreePoste), "
        		+ "DATEDIFF(SECOND, '"+format+"',DF.DateSortiePoste)	,DF.NumLigne, DF.Numposte  from   "
        		+ "[DetailsGammesProduction]  DG "
        		+ "RIGHT OUTER JOIN   [DetailsFichesProduction] DF "
        		+ "on   "
        		+ "	DG.numficheproduction=DF.numficheproduction  COLLATE FRENCH_CI_AS  and "
        		+ "	DG.numligne=DF.NumLigne and DG.NumPosteReel=DF.NumPoste "
        		+ " "
        		+ "INNER JOIN POSTES P "
        		+ "on P.Numposte=DF.Numposte "
        		+ " and DF.numficheproduction in ("+toClause(listeOF)+")  "
        		+ "  order by DF.numficheproduction, DF.Numposte,DG.NumLigne";

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
	            
	            //System.out.println( "fiche= "+fiche+" ,numposte= " + numposte);

	            if (!finalArray.containsKey(fiche)) {
	            	finalArray.put(fiche, new  LinkedHashMap<> ());
	            }
	            int[] arrMinutes=   {resultSet.getInt(3),resultSet.getInt(4)};

	            int numligneBDD=postes.get(numposte).numligne;
	            // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	            //numligne: numéro de ligne de prod ( permet de différencier deux C06 différents pour une même gamme
	            //numligneBDD id sans trou de la game Postes
	            finalArray.get(fiche).put( numligne,new PosteProd(numposte,numligneBDD,Nomposte, arrMinutes[0],arrMinutes[1]));

	        }
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, e, "Alerte exception !", JOptionPane.ERROR_MESSAGE);
			logger.error(e);
		}

      return finalArray;

    }

    public  void insertLogCPO(LocalDateTime d,int idbarre, String label, int cptZone,int numZone, LocalTime entree, LocalTime sortie) {
        
    	
    	logger.info("logs de la barre "+label);
        // Requête SQL pour insérer une ligne
        String sql = "INSERT INTO LOGS_CPO (date_log, idbarre, label, idZone, NumZone, entree, sortie) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (            PreparedStatement preparedStatement = mConnection.prepareStatement(sql)) {

            // Définir les paramètres de la requête
            preparedStatement.setTimestamp(1,Timestamp.valueOf(d)); // Date actuelle comme clé primaire
            preparedStatement.setInt(2, idbarre);                                // Champ idbarre
            preparedStatement.setString(3, label);                              // Champ label
            preparedStatement.setInt(4, cptZone);
            preparedStatement.setInt(5, numZone);                               // Champ NumZone
            preparedStatement.setTime(6, Time.valueOf(entree));                 // Heure d'entrée
            preparedStatement.setTime(7, Time.valueOf(sortie));                 // Heure de sortie

            // Exécuter la requête
            int rowsInserted = preparedStatement.executeUpdate();
            if (rowsInserted > 0) {
               // System.out.println("Une nouvelle ligne a été insérée avec succès !");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            logger.error("Erreur lors de l'insertion des données : " + e.getMessage());
        }
    }

    static TempsDeplacement getmTempsDeplacement() {
		return mTempsDeplacement;
	}
    public  int getTempsDeplacement(int dep,int arr,int vitesse) {
		return mTempsDeplacement.get(Arrays.asList(dep, arr))[vitesse];
	}


	 void setmTempsDeplacement(TempsDeplacement TempsDeplacement) {
		mTempsDeplacement = TempsDeplacement;
	}
	public  HashSet<String> getMissingTimeMovesGammes() {
		return mMissingTimeMovesGammes;
	}
	public ArrayList<Integer> getZonesSecu() {
		
		return mZonesSecu;
	}
}