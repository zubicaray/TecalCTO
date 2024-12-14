package org.tecal.scheduler.data;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.tecal.scheduler.types.ElementGamme;
import org.tecal.scheduler.types.ZoneType;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

public class CSV_DATA {
	
	List<String[]> mCSV_gammes;
	List<String[]> mCSV_zones;
	
	HashMap<Integer,ZoneType> zones;
	HashMap<String,ArrayList<ElementGamme> > gammes;
	
	public  CSV_DATA() throws IOException, CsvException, URISyntaxException  {
		
		URL res = getClass().getClassLoader().getResource("gammes.csv");
		File file = Paths.get(res.toURI()).toFile();
		String absolutePath = file.getAbsolutePath();
		CSVParser csvParser = new CSVParserBuilder().withSeparator(';').build(); 
	
		try(CSVReader reader = new CSVReaderBuilder(
		          new FileReader(absolutePath))
		          .withCSVParser(csvParser)   // custom CSV parser
		      //.withSkipLines(1)           // skip the first line, header info
		          .build()){
			mCSV_gammes = reader.readAll();
		     
		}
		
		res = getClass().getClassLoader().getResource("zones.csv");
		file = Paths.get(res.toURI()).toFile();
		absolutePath = file.getAbsolutePath();
		
		try(CSVReader reader = new CSVReaderBuilder(
		          new FileReader(absolutePath, StandardCharsets.UTF_8))
		          .withCSVParser(csvParser)   // custom CSV parser
		      //.withSkipLines(1)           // skip the first line, header info
		          .build()){
			  mCSV_zones = reader.readAll();
		     
		}
		
	
		
		zones = new HashMap<Integer,ZoneType>();
		gammes= new HashMap<String, ArrayList<ElementGamme> >();
		
		setZones();
		setGammesZones();
		
		
	}

	private void setZones() {
	  
		
	    	
		int idzone=0;
		// Print results from select statement
	    for(int i=0;i<mCSV_zones.size();i++){
	    	String[] line= mCSV_zones.get(i);
	    	//for(String s : line)     System.out.println("|"+s+"|");
	    	
	    	int numzone= Integer.parseInt(line[0].replaceAll(" ",""));
	        ZoneType z=new ZoneType(numzone,line[1].trim(),
	        		Integer.parseInt(line[2].replaceAll(" ","")),
	        		Integer.parseInt(line[3].replaceAll(" ","")),
	        		//TODO pas mis dans le CSV
	        		Integer.parseInt(line[4].replaceAll(" ","")),
	        		Integer.parseInt(line[5].replaceAll(" ","")),
	        		idzone);
	        zones.put(z.numzone,z);
	        idzone++;
	    }
	          
	
	}
	
	public HashMap<Integer,ZoneType> getZones() {

		return zones;
	}
	public HashMap<String, ArrayList<ElementGamme> >  getLignesGammesAll() {
		
		return gammes;
	}
	
	//TODO:ajouter egouttage dans les csv
	private  void  setGammesZones() {
		
	
		
	        
		 for(int i=0;i<mCSV_gammes.size();i++){
	        //System.out.println("numzone:"+resultSet.getInt(4)+"   "+resultSet.getString(1) + " " + resultSet.getString(2));
			String[] line= mCSV_gammes.get(i);
            int numzone=Integer.valueOf(line[2]);
            ZoneType zt=zones.get(numzone);
            
            //TODO:check
            ElementGamme gt=new ElementGamme(line[0].replaceAll(" ",""),	          
            		zt.codezone,	  
            		Integer.parseInt(line[1].replaceAll(" ","")),	            
            		numzone,
            		Integer.parseInt(line[3].replaceAll(" ","")), 
            		zt.idzonebdd,
            		Integer.parseInt(line[4].replaceAll(" ","")), 
            		Integer.parseInt(line[5].replaceAll(" ",""))
		            );
            
          
            if (!gammes.containsKey(gt.numgamme)) {
            	gammes.put(gt.numgamme, new ArrayList<ElementGamme>());
            }
            
            gammes.get(gt.numgamme).add(gt);
        }
		

	}
	
	
		

}
