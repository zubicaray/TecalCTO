
package org.tecal.S7Connector;
import java.io.IOException;

import com.github.s7connector.api.DaveArea;
import com.github.s7connector.api.S7Connector;
import com.github.s7connector.api.factory.S7ConnectorFactory;



public class S7Example {
    public static void main(String[] args) throws IOException {
        // Adresse IP de l'automate S7-300
        String plcIpAddress = "192.168.0.1";

    	//Create connection
        S7Connector connector = 
                S7ConnectorFactory
                .buildTCPConnector()
                .withHost(plcIpAddress)                
                .withRack(0) //optional
                .withSlot(2) //optional
                .build();
                    
    	//Read from DB100 10 bytes
    	byte[] bs = connector.read(DaveArea.DB, 100, 10, 0);

    	//Set some bytes
    	bs[0] = 0x00;
    		
    	//Write to DB100 10 bytes
    	connector.write(DaveArea.DB, 101, 0, bs);

    	//Close connection
    	connector.close();
    }
}
