package org.tecal.scheduler;



public class JobTypeFixed extends JobType {

	JobTypeFixed(int barre, String inname) {
		super(barre, inname);
		isFixed=true;
	}
	
void makeSafetyBetweenBridges() {
		
		long deb = 0;
		long fin= 0;
	
		//System.out.println("Job "+name);
		for (int taskID = 0; taskID < mTaskOrdoList.size(); ++taskID) {
						
			if(mTaskOrdoList.get(taskID).zoneSecu) {
				deb= mTaskOrdoList.get(taskID).getFixedStartBDD();
				
				if(indexAnod > 0 && taskID-1 == indexAnod) {
					deb=mTaskOrdoList.get(indexAnod).getFixedEndBDD()-CST.TEMPS_ANO_ENTRE_P1_P2;
				}
				
				if(indexAnod > 0 && taskID+1 == indexAnod) {
					fin= mTaskOrdoList.get(indexAnod).getFixedStartBDD();
					
				}else {
					fin= mTaskOrdoList.get(taskID).getFixedEndBDD();
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
				mNoOverlapP1P2.add(TecalOrdo.model.newFixedInterval( deb,fin -deb, ""));
				
				
				
			}
			
						
		}
		
	}
	
	void simulateBridgesMoves() {
	
		
		long deb = 0;
		long fin= 0;
		int bridge=0;
		
		TaskOrdo taskOrdoNext =null;
		//System.out.println("Job "+name);
		for (int taskID = 0; taskID < mTaskOrdoList.size(); ++taskID) {
			
						
			
			TaskOrdo taskOrdo = mTaskOrdoList.get(taskID);		
			
			if(taskID != mTaskOrdoList.size()-1) 
				taskOrdoNext = mTaskOrdoList.get(taskID+1);
		
			if(taskID >indexAnod) {
				bridge=1;								
			}
			ListeZone lBridgeMoves=bridgesMoves.get(bridge);
			
			if(taskID==0) {
				deb=taskOrdoNext.getFixedStartBDD()-(taskOrdo.tempsDeplacement+CST.TEMPS_ANO_ENTRE_P1_P2);
				continue;
			}
			
			
			if(taskOrdo.isOverlapable || taskID ==indexAnod ||  taskID == mTaskOrdoList.size()-1 ) {
				
				if(taskOrdo.getBloquePont()) {
					fin=taskOrdo.getEndBDDValue();
				}
				else
				fin=taskOrdo.getFixedStartBDD()+CST.TEMPS_MVT_PONT;
				
				lBridgeMoves.add(TecalOrdo.model.newFixedInterval(deb, fin-deb ,""));
				
				if(taskID != mTaskOrdoList.size()-1)
					deb=taskOrdoNext.getFixedStartBDD()-(taskOrdo.tempsDeplacement+CST.TEMPS_ANO_ENTRE_P1_P2);
				
				
			}			
			
		}
	}
	
	


}
