package org.tecal.scheduler.data;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
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
import org.tecal.scheduler.types.GammeType;
import org.tecal.scheduler.types.PosteBDD;
import org.tecal.scheduler.types.PosteProd;
import org.tecal.scheduler.types.ZoneType;

class TempsDeplacement 		extends HashMap<List<Integer>,Integer[]>	{	private static final long serialVersionUID = 1L;}

public class SQL_DATA {
	private Connection mConnection ;
	private Statement mStatement;

	private  HashMap<Integer,ZoneType>  zones;

	public HashMap<Integer, ZoneType> getZones() {
		return zones;
	}
	public void setZones(HashMap<Integer, ZoneType> zones) {
		this.zones = zones;
	}
	public  ArrayList<Integer>   zonesSecu;
	public  HashMap<Integer,Integer>  relatedZones;
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
	                +"integratedSecurity=true;"
	                + "trustServerCertificate=true;";


		} catch (InvalidFileFormatException e) {
			JOptionPane.showMessageDialog(null, e, "Alerte exception !", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, e, "Alerte exception !", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e, "Alerte exception !", JOptionPane.ERROR_MESSAGE);
		}






		try {
			mConnection = DriverManager.getConnection(connectionUrl);
			mStatement= mConnection.createStatement();
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, e, "Alerte exception !", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
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

	relatedZones = new HashMap<>();
	for(ZoneType  z1: zones.values()) {
		for(ZoneType  z2: zones.values()) {
			if(z1.numzone == z2.numzone) {
				continue;
			}

			if( z2.idPosteFin>z2.idPosteDeb &&  // z2 est une multi zone
				z2.idPosteDeb <= z1.idPosteDeb && z2.idPosteFin >= z1.idPosteFin // z1 est comprise dedans
			) {
				relatedZones.put(z1.numzone,z2.numzone);
			}

		}

	}
}


private  void setZonesSecu() {

	ResultSet resultSet = null;
	zonesSecu = new ArrayList<>();

    // Create and execute a SELECT SQL statement.
    String selectSql = "select Z.numzone "
    		+ " from  ZONES Z  where SecuritePonts=1 "
    		+ "order by numzone";

    try {
		resultSet = mStatement.executeQuery(selectSql);

		// Print results from select statement
        while (resultSet.next()) {

            zonesSecu.add(resultSet.getInt(1));

        }
	} catch (SQLException e) {
		JOptionPane.showMessageDialog(null, e, "Alerte exception !", JOptionPane.ERROR_MESSAGE);
		e.printStackTrace();
	}


}


private  void setZones() {

	ResultSet resultSet = null;
	zones = new HashMap<>();
    // Create and execute a SELECT SQL statement.
    String selectSql = "select Z.numzone,Z.CodeZone, Z.NumDernierPoste-Z.NumPremierPoste+1 as cumul,derive ,"
    		+ "NumPremierPoste,NumDernierPoste"
    		+ " from   "
    		+ "ZONES Z order by numzone";

    try {
		resultSet = mStatement.executeQuery(selectSql);
		int idzone=0;
		// Print results from select statement
        while (resultSet.next()) {
            //System.out.println( resultSet.getString(1));
            ZoneType z=new ZoneType(resultSet.getInt(1),resultSet.getString(2),resultSet.getInt(3),
            		resultSet.getInt(4),idzone,resultSet.getInt(5),resultSet.getInt(6));
            zones.put(z.numzone,z);
            idzone++;
        }
	} catch (SQLException e) {
		JOptionPane.showMessageDialog(null, e, "Alerte exception !", JOptionPane.ERROR_MESSAGE);
		e.printStackTrace();
	}


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
			e.printStackTrace();
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
        		+ "	INNER JOIN ZONES Z "
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
		            resultSet.getInt(6),

		            resultSet.getInt(8));


	            if (!gammes.containsKey(gt.numgamme)) {
	            	gammes.put(gt.numgamme, new ArrayList<>());
	            }

	            gammes.get(gt.numgamme).add(gt);
	        }
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, e, "Alerte exception !", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}


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
		e.printStackTrace();
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
		e.printStackTrace();
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
			e.printStackTrace();
			res=false;
		}

	    return res;

}
public boolean updateTpsMvts(String of,boolean updateNoNull) {
	boolean res= false;

	String req="Update  [TempsDeplacements] \r\n"
			//+ "SET normal=dbo.InlineMax(0,T.tps) \r\n"
			+ "SET normal=T.tps \r\n"
			+ "FROM \r\n"
			+ "	[TempsDeplacements] TPS\r\n"
			+ "	INNER JOIN (\r\n"
			+ "\r\n"
			+ "		select DP1.NumZone N1,P1.NomPoste M1, DP2.NumZone N2,P2.NomPoste M2,\r\n"
			+ "			DATEDIFF(SECOND, F1.DateSortiePoste,F2.DateEntreePoste)-DP1.TempsEgouttageSecondes	as tps\r\n"
			+ "		from \r\n"
			+ "			DetailsFichesProduction  F1\r\n"
			+ "			INNER JOIN DetailsFichesProduction  F2\r\n"
			+ "			ON F1.NumFicheProduction =F2.NumFicheProduction  COLLATE FRENCH_CI_AS \r\n"
			+ "				AND F1.NumLigne=F2.NumLigne-1 and F1.DateEntreePoste<F2.DateEntreePoste\r\n"
			+ "			INNER JOIN POSTES P1\r\n"
			+ "				on P1.Numposte=F1.Numposte\r\n"
			+ "			INNER JOIN POSTES P2\r\n"
			+ "				on P2.Numposte=F2.Numposte\r\n"
			+ "			INNER JOIN DetailsGammesProduction DP1\r\n"
			+ "			ON F1.NumFicheProduction =DP1.NumFicheProduction  COLLATE FRENCH_CI_AS and \r\n"
			+ "			 F1.NumLigne=DP1.NumLigne\r\n"
			+ "			INNER JOIN DetailsGammesProduction DP2\r\n"
			+ "			ON F2.NumFicheProduction =DP2.NumFicheProduction  COLLATE FRENCH_CI_AS and \r\n"
			+ "			 F2.NumLigne=DP2.NumLigne"
			+ "	\r\n"
			+ "		where F1.NumFicheProduction = '"+of+"'   COLLATE FRENCH_CI_AS  \r\n"
			+ "	--order by F1.NumLigne\r\n"
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
			e.printStackTrace();
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
		e.printStackTrace();
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
		e.printStackTrace();
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
        String selectSql = "select distinct DF.numficheproduction,NumGammeAnodisation "
        		+ "from  [DetailsChargesProduction] DC"
        		+ " INNER JOIN   [DetailsFichesProduction] DF "
        		+ "on   "
        		+ "	DC.numficheproduction=DF.numficheproduction   COLLATE FRENCH_CI_AS "

        		+" and DF.numficheproduction in ("+toClause(listeOF)+")  " ;
        try {
			resultSet = mStatement.executeQuery(selectSql);
			// Print results from select statement
	        while (resultSet.next()) {
	            //System.out.println(resultSet.getString(1) + " " + resultSet.getString(2));
	            res.put(resultSet.getString(1), resultSet.getString(2));
	        }
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, e, "Alerte exception !", JOptionPane.ERROR_MESSAGE);
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
			JOptionPane.showMessageDialog(null, e, "Alerte exception !", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
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
        		+ "DATEDIFF(SECOND, DATEADD(DAY, DATEDIFF(DAY, 0, '"+format+"'), 0),DF.DateEntreePoste), "
        		+ "DATEDIFF(SECOND, DATEADD(DAY, DATEDIFF(DAY, 0, '"+format+"'), 0),DF.DateSortiePoste)	,DF.NumLigne, DF.Numposte  from   "
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
	            
	            System.out.println( "fiche= "+fiche+" ,numposte= " + numposte);

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
			e.printStackTrace();
		}

      return finalArray;

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
}