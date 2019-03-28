// ===============================================================================
// Authors: Jacob Allex-Buckner
// Organization: University of Dayton Research Institute Applied Sensing Division
//
// Copyright (c) 2018 Government of the United State of America, as represented by
// the Secretary of the Air Force.  No copyright is claimed in the United States under
// Title 17, U.S. Code.  All Other Rights Reserved.
// ===============================================================================

// This file was auto-created by LmcpGen. Modifications will be overwritten.

import afrl.cmasi.searchai.HazardZoneDetection;
import afrl.cmasi.searchai.HazardZoneEstimateReport;
import afrl.cmasi.AltitudeType;
import afrl.cmasi.CommandStatusType;
import afrl.cmasi.GimbalStareAction;
import afrl.cmasi.Location3D;
import afrl.cmasi.LoiterAction;
import afrl.cmasi.LoiterDirection;
import afrl.cmasi.LoiterType;
import afrl.cmasi.MissionCommand;
import afrl.cmasi.SessionStatus;
import afrl.cmasi.SpeedType;
import afrl.cmasi.TurnType;
import afrl.cmasi.VehicleAction;
import afrl.cmasi.VehicleActionCommand;
import afrl.cmasi.Waypoint;
import afrl.cmasi.Polygon;
import afrl.cmasi.Circle;
import avtas.lmcp.LMCPFactory;
import avtas.lmcp.LMCPObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.List;

/**
 * Connects to the simulation and sends a fake mission command to every UAV that is requested in the plan request.
 */
public class zero extends Thread {

    /** simulation TCP port to connect to */
    private static int port = 5555;
    /** address of the server */
    private static String host = "localhost";
    /**Array of booleans indicating if loiter command has been sent to each UAV */
    boolean[] uavsLoiter = new boolean[4];
    Polygon estimatedHazardZone = new Polygon();
	
	public int wayPointNumber = 1;
	public int missionCount = 1;
	List wayPointList = new ArrayList<Double>();
	int wayPointListCount = 0;

    public zero() {
    }

    @Override
    public void run() {
        try {
            // connect to the server
            Socket socket = connect(host, port);
			boolean missionCommand = false;
			
            while(true) {
                //Continually read the LMCP messages that AMASE is sending out
                readMessages(socket.getInputStream(), socket.getOutputStream());
				if(missionCommand == false){
				    if(wayPointList.size() == 6){
						//sendMissionCommand(socket.getOutputStream(),1);
						sendMissionCommand(socket.getOutputStream(),2);
						sendMissionCommand(socket.getOutputStream(),1);
						
						missionCommand = true;
					}
				  
					//sendMissionCommand(out,2,detectedLocation);
				}
            }

        } catch (Exception ex) {
            Logger.getLogger(zero.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
  public void sendMissionCommand(OutputStream out, int id) throws Exception {
        //Setting up the mission to send to the UAV
         MissionCommand o = new MissionCommand();
         o.setFirstWaypoint(1);
         //Setting the UAV to recieve the mission
         o.setVehicleID(id);
         o.setStatus(CommandStatusType.Pending);
         //Setting a unique mission command ID
         o.setCommandID(missionCount);
		 missionCount++;
         //Creating the list of waypoints to be sent with the mission command
         ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
         //Creating the first waypoint
         //Note: all the following attributes must be set to avoid issues
         Waypoint waypoint1 = new Waypoint();
         //Setting 3D coordinates
         waypoint1.setLatitude((Double)wayPointList.get(wayPointListCount));
         waypoint1.setLongitude((Double)wayPointList.get(wayPointListCount + 1));
         waypoint1.setAltitude(100);
         waypoint1.setAltitudeType(AltitudeType.MSL);
         //Setting unique ID for the waypoint
         waypoint1.setNumber(1);
        
         //Setting speed to reach the waypoint
         waypoint1.setSpeed(30);
         waypoint1.setSpeedType(SpeedType.Airspeed);
         //Setting the climb rate to reach new altitude (if applicable)
         waypoint1.setClimbRate(0);
         waypoint1.setTurnType(TurnType.TurnShort);
         //Setting backup waypoints if new waypoint can't be reached
         waypoint1.setContingencyWaypointA(0);
         waypoint1.setContingencyWaypointB(0);
         /* 
		  waypoint1.setNextWaypoint(2);
		  
		  
         //Setting up the second waypoint to be sent in the mission command
         Waypoint waypoint2 = new Waypoint();
         waypoint2.setLatitude((Double)wayPointList.get(2));
         waypoint2.setLongitude((Double)wayPointList.get(3));
         waypoint2.setAltitude(100);
         waypoint2.setAltitudeType(AltitudeType.MSL);
         waypoint2.setNumber(2);
         waypoint2.setNextWaypoint(1);
         waypoint2.setSpeed(30);
         waypoint2.setSpeedType(SpeedType.Airspeed);
         waypoint2.setClimbRate(0);
         waypoint2.setTurnType(TurnType.TurnShort);
         waypoint2.setContingencyWaypointA(0);
         waypoint2.setContingencyWaypointB(0);  */
         
         //Adding the waypoints to the waypoint list
         waypoints.add(waypoint1);
        // waypoints.add(waypoint2);
         
         //Setting the waypoint list in the mission command
         o.getWaypointList().addAll(waypoints);
         
         //Sending the Mission Command message to AMASE to be interpreted
         out.write(avtas.lmcp.LMCPFactory.packMessage(o, true));
		 wayPointNumber++;
		 wayPointListCount +=2;
    }
    
    /**
     * Sends loiter command to the AMASE Server
     * @param out
     * @throws Exception 
     */
    public void sendLoiterCommand (OutputStream out, long vehicleId , Location3D location) throws Exception {
        //Setting up the mission to send to the UAV
         VehicleActionCommand o = new VehicleActionCommand();
         o.setVehicleID(vehicleId);
         o.setStatus(CommandStatusType.Pending);
         o.setCommandID(1);
         
         //Setting up the loiter action
         LoiterAction loiterAction = new LoiterAction();
         loiterAction.setLoiterType(LoiterType.Circular);
         loiterAction.setRadius(250);
         loiterAction.setAxis(0);
         loiterAction.setLength(0);
         loiterAction.setDirection(LoiterDirection.Clockwise);
         loiterAction.setDuration(100000);
         loiterAction.setAirspeed(15);
         
         //Creating a 3D location object for the stare point
         loiterAction.setLocation(location);
         
         //Adding the loiter action to the vehicle action list
         o.getVehicleActionList().add(loiterAction);
         
         //Sending the Vehicle Action Command message to AMASE to be interpreted
         out.write(avtas.lmcp.LMCPFactory.packMessage(o, true));
    }

    /**
     * Sends loiter command to the AMASE Server
     * @param out
     * @throws Exception
     */
    public void sendEstimateReport(OutputStream out, Polygon estimatedShape) throws Exception {
        //Setting up the mission to send to the UAV
        HazardZoneEstimateReport o = new HazardZoneEstimateReport();
        o.setEstimatedZoneShape(estimatedShape);
        o.setUniqueTrackingID(1);
        o.setEstimatedGrowthRate(0);
        o.setPerceivedZoneType(afrl.cmasi.searchai.HazardType.Fire);
        o.setEstimatedZoneDirection(0);
        o.setEstimatedZoneSpeed(0);


        //Sending the Vehicle Action Command message to AMASE to be interpreted
        out.write(avtas.lmcp.LMCPFactory.packMessage(o, true));
    }

    /**
    * Reads in messages being sent out by the AMASE Server
    */
    public void readMessages(InputStream in, OutputStream out) throws Exception {
        //Use each of the if statements to use the incoming message
        LMCPObject o = LMCPFactory.getObject(in);
        //Check if the message is a HazardZoneDetection
        if (o instanceof afrl.cmasi.searchai.HazardZoneDetection) {
			
            HazardZoneDetection hazardDetected = ((HazardZoneDetection) o);
            //Get location where zone first detected
            Location3D detectedLocation = hazardDetected.getDetectedLocation();
			System.out.println("LAT: " + detectedLocation.getLatitude());
			System.out.println("LOG: " + detectedLocation.getLongitude());
			
			wayPointList.add(detectedLocation.getLatitude());
			wayPointList.add(detectedLocation.getLongitude());
			
            //Get entity that detected the zone
            int detectingEntity = (int) hazardDetected.getDetectingEnitiyID();
			
            //Check if hint
            if (detectingEntity == 0) {
                //Do nothing for now, hints will be added later
                return;
            }

            //Check if the UAV has already been sent the loiter command and proceed if it hasn't
            if (uavsLoiter[detectingEntity - 1] == false) {
                //Send the loiter command
                sendLoiterCommand(out, detectingEntity, detectedLocation);

                //Note: Polygon points must be in clockwise or counter-clockwise order to get a shape without intersections
                estimatedHazardZone.getBoundaryPoints().add(detectedLocation);

                //Send out the estimation report to draw the polygon
                sendEstimateReport(out, estimatedHazardZone);

                uavsLoiter[detectingEntity - 1] = true;
                System.out.println("UAV" + detectingEntity + " detected hazard at " + detectedLocation.getLatitude() +
                        "," + detectedLocation.getLongitude() + ". Sending loiter command.");
            }
        }else if(o instanceof afrl.cmasi.AirVehicleState){
			AirVehicleState uav = ((AirVehicleState) o);
			Location3D loc = uav.getLocation();
		}
    }


    /** tries to connect to the server.  If there is a problem (such as the server not running yet) it
     *  pauses, then tries again.  If the server quits and restarts, this method is called by the thread
     *  in order to re-establish communication.
     * @param host
     * @param port
     * @return
     */
    public Socket connect(String host, int port) {
        Socket socket = null;
        try {
            socket = new Socket(host, port);
        } catch (UnknownHostException ex) {
            System.err.println("Host Unknown. Quitting");
            System.exit(0);
        } catch (IOException ex) {
            System.err.println("Could not Connect to " + host + ":" + port + ".  Trying again...");
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex1) {
                Logger.getLogger(zero.class.getName()).log(Level.SEVERE, null, ex1);
            }
            return connect(host, port);
        }
        System.out.println("Connected to " + host + ":" + port);
        return socket;
    }

    public static void main(String[] args) {
        new zero().start();
    }
}
