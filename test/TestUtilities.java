/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import GlobalData.CTANetwork;
import GlobalData.SharedData;
import Messaging.QueueParameters;
import Utilities.ConfigLoader;
import java.io.IOException;

/**
 *
 * @author bubby
 */
public class TestUtilities {

    public static void testLoadMachineConfigurations(String filename) throws IOException {

        ConfigLoader.loadMachineConfigurations("config/machineConfig");

        for (int i = 0; i < CTANetwork.hosts.size(); i++) {
            String host = CTANetwork.hosts.get(i);
            QueueParameters qp = CTANetwork.hostQueueMap.get(host);
            System.out.println(qp.exchange + " " + qp.password + " " + qp.port + " " + qp.queueName +
                    " " + qp.routingKey + " " + qp.username + " " + qp.virtualHost);
        }

    }

    public static void testTLoadAgentConfigurations(String filename) throws IOException {

        ConfigLoader.loadAgentConfigurations("config/agentConfig");
        System.out.println(SharedData.boundingBox.nw.x + " " + SharedData.boundingBox.nw.y + " " +
                SharedData.boundingBox.se.x + " " + SharedData.boundingBox.se.y);


    }

    public static void testTLoadHospitalConfigurations(String filename) throws IOException {

        ConfigLoader.loadHospitalLocations("config/hospitalConfig");
        for (int i = 0; i < SharedData.hospitals.size(); i++) {
            System.out.println(SharedData.hospitals.get(i));
        }
    }

    public static void testTLoadDisasterConfigurations(String filename) throws IOException {

        ConfigLoader.loadDisasterLocations("config/disasterConfig");
        for (int i = 0; i < SharedData.disasters.size(); i++) {
            System.out.println(SharedData.disasters.get(i));
        }
    }

    public static void main(String[] args) throws IOException {
        TestUtilities.testTLoadDisasterConfigurations("");
    }
}
