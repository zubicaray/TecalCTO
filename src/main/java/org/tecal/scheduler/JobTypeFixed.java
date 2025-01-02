package org.tecal.scheduler;

import org.tecal.scheduler.data.SQL_DATA;

//import org.tecal.scheduler.data.SQL_DATA;

public class JobTypeFixed extends JobType {

	JobTypeFixed(int barre, String inname) {
		super(barre, inname);
		isFixed=true;
	}
	
void makeSafetyBetweenBridges(long time) {
		
		long deb = 0;
		long fin= 0;
	
		//System.out.println("Job "+name);
		for (int taskID = 0; taskID < mTaskOrdoList.size(); ++taskID) {
			
			TaskOrdo taskOrdo = mTaskOrdoList.get(taskID);		
			if(taskOrdo.getFixedEndBDD()<time) {				
				continue;
			}
		
						
			if(taskOrdo.zoneSecu) {
				
				
				deb= taskOrdo.getFixedStartBDD();
				
				if(indexAnod > 0 && taskID-1 == indexAnod) {
					deb=mTaskOrdoList.get(indexAnod).getFixedEndBDD()-CST.TEMPS_ANO_ENTRE_P1_P2;
				}
				
				if(indexAnod > 0 && taskID+1 == indexAnod) {
					fin= mTaskOrdoList.get(indexAnod).getFixedStartBDD();					
				}else {
					fin= taskOrdo.getFixedEndBDD();
				}
				
				int taskID2 = taskID+1;
				while(mTaskOrdoList.get(taskID2).zoneSecu && taskID2 < mTaskOrdoList.size())
				{
					taskID2++;
				}
				
				if(taskID2>taskID+1) {
					if(indexAnod > 0 && taskID2 == indexAnod) {
						fin=mTaskOrdoList.get(indexAnod).getFixedStartBDD()+CST.TEMPS_ANO_ENTRE_P1_P2;
						
					}else {
						fin=mTaskOrdoList.get(taskID2-1).getFixedEndBDD(); 
					}
					
					taskID=taskID2;
				}
				//System.out.println("SAFE BRIDGE "+name+" taskid:"+taskID+" zone:"+	SQL_DATA.getInstance().getZones().get(mTaskOrdoList.get(taskID).mTask.numzone).codezone+" deb:"+deb+", fin="+ (fin));
				mNoOverlapP1P2.add(TecalOrdo.model.newFixedInterval( deb,fin -deb, ""));		
			}
		}
		
	}
	
	void simulateBridgesMoves(long time) {
	
		
		long deb = 0;
		long fin= 0;
		int bridge=0;
		int previousTpsDep=0;
		
		TaskOrdo taskOrdoNext =null;
		//System.out.println("Job "+name);
		for (int taskID = 0; taskID < mTaskOrdoList.size(); ++taskID) {
						
			TaskOrdo taskOrdo = mTaskOrdoList.get(taskID);		
			if(taskOrdo.getFixedFin()<time) {
				//on garde le temps de déplacement pour aller à la prochaine zone "futures"
				previousTpsDep=taskOrdo.tempsDeplacement;
				continue;
			}
			
			if(indexAnod > 0 && taskID >indexAnod) {
				bridge=1;								
			}
			// si pas de zone d'ano
			if(indexAnod < 0 && tasksJob.get(taskID).numzone >=TecalOrdo.mNUMZONE_ANODISATION) {
				bridge=1;								
			}
			
			ListeZone lBridgeMoves=bridgesMoves.get(bridge);
			if(taskID != mTaskOrdoList.size()-1) 
				taskOrdoNext = mTaskOrdoList.get(taskID+1);
			else {
				//la prochaine zone est uniquement de déchargement
								
				if(deb==0) {// cas où le curseur est dans la zone de déchargement
					deb=taskOrdo.getFixedStartBDD()-previousTpsDep;
				}
				fin= taskOrdo.getFixedStartBDD()-deb+CST.TEMPS_MVT_PONT;
				lBridgeMoves.add(TecalOrdo.model.newFixedInterval(deb,fin,""));
				logger.debug("END ZONE, BRIDGE:"+bridge+" ,previousTpsDep="+previousTpsDep+", SIMU="+name+" ,taskid:"+taskID+" zone:"+SQL_DATA.getInstance().getZones().get(mTaskOrdoList.get(taskID).mTask.numzone).codezone+" deb:"+deb+", fin="+(deb+fin));
				
				break;
			}
			
			
			//System.out.println("SIMU "+name+" taskid:"+taskID+" zone:"+SQL_DATA.getInstance().getZones().get(mTaskOrdoList.get(taskID).mTask.numzone).codezone);
			if(taskOrdo.getFixedStartBDD()<time ) {
				// !! ZONE ENCOURS
				deb=taskOrdoNext.getFixedStartBDD()-(taskOrdo.tempsDeplacement+CST.TEMPS_ANO_ENTRE_P1_P2);				
				continue;
			}
			
			
			if(deb==0) {
				//zone futures
				deb=taskOrdoNext.getFixedStartBDD()-(previousTpsDep+CST.TEMPS_ANO_ENTRE_P1_P2);
				continue;
			}	
			
			
			
			boolean isOverlapable=taskOrdo.isOverlapable && (taskOrdo.getEndBDDValue()-time)>CST.TEMPS_ZONE_OVERLAP_MIN;
			if(isOverlapable || taskID ==indexAnod ||  (taskID == mTaskOrdoList.size()-1 ) ) {
				
				if(taskOrdo.getBloquePont()) {
					fin=taskOrdo.getEndBDDValue();
				}
				else {
					int tpsSecu=Math.min(CST.TEMPS_MVT_PONT, taskOrdo.getDuration());
					fin=taskOrdo.getFixedStartBDD()+tpsSecu;
				}
					
								
				lBridgeMoves.add(TecalOrdo.model.newFixedInterval(deb, fin-deb ,""));
				
				logger.debug("BRIDGE:"+bridge+" ,previousTpsDep="+previousTpsDep+", SIMU="+name+" ,taskid:"+taskID+" zone:"+SQL_DATA.getInstance().getZones().get(mTaskOrdoList.get(taskID).mTask.numzone).codezone+" deb:"+deb+", fin="+fin);
					
				
				if(taskID != mTaskOrdoList.size()-1)
					if(taskOrdo.getBloquePont()) {
						deb=taskOrdoNext.getFixedStartBDD()-(taskOrdo.tempsDeplacement);
					}
					else
						deb=taskOrdoNext.getFixedStartBDD()-(taskOrdo.tempsDeplacement+CST.TEMPS_ANO_ENTRE_P1_P2);
				
				
			}			
			
		}
	}
	
	


}
