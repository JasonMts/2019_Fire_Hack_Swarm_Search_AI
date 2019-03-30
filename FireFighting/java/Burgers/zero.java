package Burgers;

// ===============================================================================
// Authors: Jacob Allex-Buckner
// Organization: University of Dayton Research Institute Applied Sensing Division
//
// Copyright (c) 2018 Government of the United State of America, as represented by
// the Secretary of the Air Force.  No copyright is claimed in the United States under
// Title 17, U.S. Code.  All Other Rights Reserved.
// ===============================================================================
// This file was auto-created by LmcpGen. Modifications will be overwritten.
import afrl.cmasi.AirVehicleState;
import afrl.cmasi.searchai.HazardZoneDetection;
import afrl.cmasi.searchai.HazardZoneEstimateReport;
import afrl.cmasi.AltitudeType;
import afrl.cmasi.CameraState;
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
import afrl.cmasi.FlightDirectorAction;
import afrl.cmasi.GimbalAngleAction;
import afrl.cmasi.GimbalState;
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
import java.util.Random;

/**
 * Connects to the simulation and sends a fake mission command to every UAV that
 * is requested in the plan request.
 */
public class zero extends Thread {

    /**
     * simulation TCP port to connect to
     */
    private static int port = 5555;
    /**
     * address of the server
     */
    private static String host = "localhost";
    /**
     * Array of booleans indicating if loiter command has been sent to each UAV
     */
    boolean[] uavsLoiter = new boolean[4];
    Polygon estimatedHazardZoneUAV1 = new Polygon();
    Polygon estimatedHazardZoneUAV2 = new Polygon();

    public int wayPointNumber = 1;
    public int missionCount = 1;

    public int numOfPoints = 0;
    List<Double> wayPointList = new ArrayList<>();
    List<Integer> loiterCommand = new ArrayList<>();
    List<Integer> gimbalCommand = new ArrayList<>();
    List<Integer> navCommand = new ArrayList<>();
    int wayPointListCount = 0;

    int LoiterCommandID = 1;

    double lastLat_1 = 0;
    double lastLon_1 = 0;
    double lastLat_2 = 0;
    double lastLon_2 = 0;

    float headingDirUAV1 = 0;
    float headingDirUAV2 = 0;
    float headingDirUAV3 = 0;
    float headingDirUAV4 = 0;
    private static AirVehicleState latestAirStateUAV1;
    private static AirVehicleState latestAirStateUAV2;
    private static AirVehicleState latestAirStateUAV3;
    private static AirVehicleState latestAirStateUAV4;

    private static HazardZoneDetection latestHazard;

    private static long scenarioTime;
    private static long elapsedTImeUAV3 = 0;
    private static long elapsedTImeUAV2 = 0;
    private static int countMissedPoints2 = 0;
private static int countMissedPoints3 = 0;
    private static boolean UAV2_available = true;
    private static boolean UAV3_available = true;

    private static boolean circleFire2 = false;
    private static boolean circleFire3 = false;
    private int circleCounter2 = 0;
    private int circleCounter3 = 0;
    private int currSizePolygon2 = 0;
    
    private int currSizePolygon3 = 0;
    private static Location3D lastFire = null;
    
    
    int noThetas = 100;
    double xUAV1[] = new double[noThetas];
    double yUAV1[] = new double[noThetas];
    double xUAV2[] = new double[noThetas];
    double yUAV2[] = new double[noThetas];
    double xUAV3[] = new double[noThetas];
    double yUAV3[] = new double[noThetas];
    double xUAV4[] = new double[noThetas];
    double yUAV4[] = new double[noThetas];

    public zero() {
    }

    @Override
    public void run() {
        try {

            // connect to the server
            Socket socket = connect(host, port);
            boolean missionCommand = false;
            loiterCommand.add(0);
            loiterCommand.add(0);
            loiterCommand.add(0);
            loiterCommand.add(0);
            gimbalCommand.add(0);
            gimbalCommand.add(0);
            gimbalCommand.add(0);
            gimbalCommand.add(0);
            navCommand.add(0);
            navCommand.add(0);
            navCommand.add(0);
            navCommand.add(0);

            while (true) {
                //Continually read the LMCP messages that AMASE is sending out
                readMessages(socket.getInputStream(), socket.getOutputStream());
                if (missionCommand == false) {

                    //FlightDirectorAction dir = new FlightDirectorAction(20, SpeedType.Groundspeed, 20, 700, AltitudeType.MSL, 0);
                    //sendNavigationCommand(socket.getOutputStream(), 2, dir);
                    sendMissionCommand(socket.getOutputStream(), 1);
                    sendMissionCommand(socket.getOutputStream(), 2);
                    sendMissionCommand(socket.getOutputStream(), 3);
                    sendMissionCommand(socket.getOutputStream(), 4);
                    missionCommand = true;

                    //sendMissionCommand(out,2,detectedLocation);
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(zero.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

//    public void sendMissionCommand(OutputStream out, int id) throws Exception {
//        //Setting up the mission to send to the UAV
//        MissionCommand o = new MissionCommand();
//        o.setFirstWaypoint(1);
//        //Setting the UAV to recieve the mission
//        o.setVehicleID(id);
//        o.setStatus(CommandStatusType.Pending);
//        //Setting a unique mission command ID
//        o.setCommandID(missionCount);
//        missionCount++;
//        //Creating the list of waypoints to be sent with the mission command
//        ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
//        //Creating the first waypoint
//        //Note: all the following attributes must be set to avoid issues
//        Waypoint waypoint1 = new Waypoint();
//        //Setting 3D coordinates
//        waypoint1.setLatitude(wayPointList.get(wayPointListCount));
//        waypoint1.setLongitude(wayPointList.get(wayPointListCount + 1));
//        waypoint1.setAltitude(100);
//        waypoint1.setAltitudeType(AltitudeType.MSL);
//        //Setting unique ID for the waypoint
//        waypoint1.setNumber(1);
//
//        //Setting speed to reach the waypoint
//        waypoint1.setSpeed(20);
//        waypoint1.setSpeedType(SpeedType.Airspeed);
//
//        //Setting the climb rate to reach new altitude (if applicable)
//        waypoint1.setClimbRate(0);
//        waypoint1.setTurnType(TurnType.TurnShort);
//        //Setting backup waypoints if new waypoint can't be reached
//        waypoint1.setContingencyWaypointA(0);
//        waypoint1.setContingencyWaypointB(0);
//        /* 
//		  waypoint1.setNextWaypoint(2);
//		  
//		  
//         //Setting up the second waypoint to be sent in the mission command
//         Waypoint waypoint2 = new Waypoint();
//         waypoint2.setLatitude((Double)wayPointList.get(2));
//         waypoint2.setLongitude((Double)wayPointList.get(3));
//         waypoint2.setAltitude(100);
//         waypoint2.setAltitudeType(AltitudeType.MSL);
//         waypoint2.setNumber(2);
//         waypoint2.setNextWaypoint(1);
//         waypoint2.setSpeed(30);
//         waypoint2.setSpeedType(SpeedType.Airspeed);
//         waypoint2.setClimbRate(0);
//         waypoint2.setTurnType(TurnType.TurnShort);
//         waypoint2.setContingencyWaypointA(0);
//         waypoint2.setContingencyWaypointB(0);  */
//
//        //Adding the waypoints to the waypoint list
//        waypoints.add(waypoint1);
//        // waypoints.add(waypoint2);
//
//        //Setting the waypoint list in the mission command
//        o.getWaypointList().addAll(waypoints);
//
//        //Sending the Mission Command message to AMASE to be interpreted
//        out.write(avtas.lmcp.LMCPFactory.packMessage(o, true));
//        wayPointNumber++;
//        wayPointListCount += 2;
//    }
    public void sendMissionCommand(OutputStream out, int id) throws Exception {
        //Setting up the mission to send to the UAV

        //We will now have 4 missions
        ArrayList<MissionCommand> missions = new ArrayList<MissionCommand>();

        //one for each drone
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
spiralMaker(53.4750, -1.8351, xUAV1 , yUAV1,noThetas);
        spiralMaker(53.4750, -1.6956, xUAV2 , yUAV2,noThetas);
        spiralMaker(53.412, -1.8335, xUAV3 , yUAV3,noThetas);
        spiralMaker(53.412, -1.6898, xUAV4 , yUAV4,noThetas);

        int waypointnum = noThetas;
        for(int i = 1; i < waypointnum; i++)
        {
            Waypoint waypointDev = new Waypoint();
            System.out.print("This is waypoint " + i);
            //double randomLongitude = 0;
            //double randomLatitude = 0;
         
           /* if(i > 1)
            {
                int decide = getRandomIntegerBetweenRange(1,4);
                if(decide == 1)
                {
                    randomLongitude = new Random().nextDouble() * 0.04;
                    randomLatitude = new Random().nextDouble() * 0.04;
                }
                else if (decide == 2)
                {
                    randomLongitude = new Random().nextDouble() * -0.04;
                    randomLatitude = new Random().nextDouble() * 0.04;
                }
                else if (decide == 3)
                {
                    randomLongitude = new Random().nextDouble() * 0.04;
                    randomLatitude = new Random().nextDouble() * -0.04;
                }
                else
                {
                    randomLongitude = new Random().nextDouble() * -0.04;
                    randomLatitude = new Random().nextDouble() * -0.04;
                } 
            }*/
            if(id == 1)
            {
                
                waypointDev.setLatitude(xUAV1[i]);
                waypointDev.setLongitude(yUAV1[i]);
                waypointDev.setAltitude(1000);
                
            }
            if(id == 2)
            {
                
                waypointDev.setLatitude(xUAV2[i]);
                waypointDev.setLongitude(yUAV2[i]);
                waypointDev.setAltitude(1000);
            }
            if(id == 3)
            {
                
                waypointDev.setLatitude(xUAV3[i]);
                waypointDev.setLongitude(yUAV3[i]);
                waypointDev.setAltitude(1000);
            }
            if(id == 4)
            {
                
                waypointDev.setLatitude(xUAV4[i]);
                waypointDev.setLongitude(yUAV4[i]);
                waypointDev.setAltitude(1000);
            }
            if (waypointDev.getLatitude() > 53.5340) {
                waypointDev.setLatitude(53.3772);
                waypointDev.setLongitude(-1.762);

            }
            waypointDev.setAltitude(700);
            waypointDev.setAltitudeType(AltitudeType.MSL);
            //Setting unique ID for the waypoint
            waypointDev.setNumber(i);

            //Setting speed to reach the waypoint
            waypointDev.setSpeed(100);
            waypointDev.setSpeedType(SpeedType.Airspeed);

            //Setting the climb rate to reach new altitude (if applicable)
            waypointDev.setClimbRate(100);
            waypointDev.setTurnType(TurnType.TurnShort);
            //Setting backup waypoints if new waypoint can't be reached
            waypointDev.setContingencyWaypointA(0);
            waypointDev.setContingencyWaypointB(0);

            if (i != waypointnum - 1) {
                waypointDev.setNextWaypoint(i + 1);
            }

            waypoints.add(waypointDev);

        }

        //Setting the waypoint list in the mission command
        o.getWaypointList().addAll(waypoints);

        //Sending the Mission Command message to AMASE to be interpreted
        out.write(avtas.lmcp.LMCPFactory.packMessage(o, true));
        wayPointNumber++;
        wayPointListCount += 2;
    }

    public void goToFire(OutputStream out, int id, Location3D loc) throws Exception {
        //Setting up the mission to send to the UAV

        //We will now have 4 missions
        ArrayList<MissionCommand> missions = new ArrayList<MissionCommand>();

        //one for each drone
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

        Waypoint waypointDev = new Waypoint();

        waypointDev.setLatitude(loc.getLatitude());
        waypointDev.setLongitude(loc.getLongitude());
        waypointDev.setAltitude(700);

        waypointDev.setAltitudeType(AltitudeType.MSL);
        //Setting unique ID for the waypoint
        waypointDev.setNumber(1);

        //Setting speed to reach the waypoint
        waypointDev.setSpeed(100);
        waypointDev.setSpeedType(SpeedType.Airspeed);

        //Setting the climb rate to reach new altitude (if applicable)
        waypointDev.setClimbRate(100);
        waypointDev.setTurnType(TurnType.TurnShort);
        //Setting backup waypoints if new waypoint can't be reached
        waypointDev.setContingencyWaypointA(0);
        waypointDev.setContingencyWaypointB(0);

        // waypointDev.setNextWaypoint(i + 1);
        waypoints.add(waypointDev);

        //Setting the waypoint list in the mission command
        o.getWaypointList().addAll(waypoints);

        //Sending the Mission Command message to AMASE to be interpreted
        out.write(avtas.lmcp.LMCPFactory.packMessage(o, true));
        wayPointNumber++;
        wayPointListCount += 2;
    }

    public void sendSensorCommand(OutputStream out, int uav) throws Exception {
        //Setting up the mission to send to the UAV
        VehicleActionCommand o = new VehicleActionCommand();
        o.setVehicleID(uav);
        o.setStatus(CommandStatusType.Pending);
        o.setCommandID(1);

        //Setting up the vehical action command list
        ArrayList<VehicleAction> vehicleActionList = new ArrayList<VehicleAction>();

        //Setting up the gimbal stare vehicle action
//         GimbalStareAction gimbalStareAction = new GimbalStareAction();
//         gimbalStareAction.setPayloadID(1);
//         gimbalStareAction.setDuration(1000000);
//         
        GimbalAngleAction state = new GimbalAngleAction();
        state.setPayloadID(1);
        state.setRotation(0);
        state.setAzimuth(45);
        state.setElevation(-45);

        //Creating a 3D location object for the stare point
        // Location3D location = new Location3D(1.52, -132.51, 0, afrl.cmasi.AltitudeType.MSL);
        //gimbalStareAction.setStarepoint(location);
        //Adding the gimbal stare action to the vehicle action list
        vehicleActionList.add(state);

        o.getVehicleActionList().addAll(vehicleActionList);

        //Sending the Vehicle Action Command message to AMASE to be interpreted
        out.write(avtas.lmcp.LMCPFactory.packMessage(o, true));
    }

    public void sendNavigationCommand(OutputStream out, int uav, FlightDirectorAction flight) throws Exception {
        //Setting up the mission to send to the UAV
        VehicleActionCommand o = new VehicleActionCommand();
        o.setVehicleID(uav);
        o.setStatus(CommandStatusType.Pending);
        o.setCommandID(1);

        //Setting up the vehical action command list
        ArrayList<VehicleAction> vehicleActionList = new ArrayList<VehicleAction>();

        //Setting up the gimbal stare vehicle action
//         GimbalStareAction gimbalStareAction = new GimbalStareAction();
//         gimbalStareAction.setPayloadID(1);
//         gimbalStareAction.setDuration(1000000);
//         
        //Creating a 3D location object for the stare point
        // Location3D location = new Location3D(1.52, -132.51, 0, afrl.cmasi.AltitudeType.MSL);
        //gimbalStareAction.setStarepoint(location);
        //Adding the gimbal stare action to the vehicle action list
        vehicleActionList.add(flight);

        o.getVehicleActionList().addAll(vehicleActionList);

        //Sending the Vehicle Action Command message to AMASE to be interpreted
        out.write(avtas.lmcp.LMCPFactory.packMessage(o, true));
    }

    /**
     * Sends loiter command to the AMASE Server
     *
     * @param out
     * @throws Exception
     */
    public void sendLoiterCommand(OutputStream out, long vehicleId, Location3D location) throws Exception {
        System.out.println("UAV Loitter: " + vehicleId);
        //Setting up the mission to send to the UAV
        VehicleActionCommand o = new VehicleActionCommand();
        o.setVehicleID(vehicleId);
        o.setStatus(CommandStatusType.Pending);
        o.setCommandID(LoiterCommandID);

        //Setting up the loiter action
        LoiterAction loiterAction = new LoiterAction();
        loiterAction.setLoiterType(LoiterType.Circular);
        loiterAction.setRadius(100);
        loiterAction.setAxis(0);
        loiterAction.setLength(0);
        loiterAction.setDirection(LoiterDirection.Clockwise);
        loiterAction.setDuration(100000);
        loiterAction.setAirspeed(20);

        //Creating a 3D location object for the stare point
        loiterAction.setLocation(location);

        //Adding the loiter action to the vehicle action list
        o.getVehicleActionList().add(loiterAction);

        //Sending the Vehicle Action Command message to AMASE to be interpreted
        out.write(avtas.lmcp.LMCPFactory.packMessage(o, true));
        LoiterCommandID++;
    }

    /**
     * Sends loiter command to the AMASE Server
     *
     * @param out
     * @throws Exception
     */
    public void sendEstimateReport(OutputStream out, Polygon estimatedShape, int id) throws Exception {
        //Setting up the mission to send to the UAV
        HazardZoneEstimateReport o = new HazardZoneEstimateReport();
        o.setEstimatedZoneShape(estimatedShape);
        o.setUniqueTrackingID(id);
        o.setEstimatedGrowthRate(0);
        o.setPerceivedZoneType(afrl.cmasi.searchai.HazardType.Fire);
        o.setEstimatedZoneDirection(0);
        o.setEstimatedZoneSpeed(0);
        //System.out.println(estimatedShape.toString());
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
            latestHazard = hazardDetected;
            //Get location where zone first detected
            Location3D detectedLocation = hazardDetected.getDetectedLocation();
            System.out.println("LAT: " + detectedLocation.getLatitude());
            System.out.println("LOG: " + detectedLocation.getLongitude());

            wayPointList.add(detectedLocation.getLatitude());
            wayPointList.add(detectedLocation.getLongitude());
            int detectingEntity = (int) hazardDetected.getDetectingEnitiyID();

            if (detectingEntity == 3) {
                UAV3_available = false;
                System.out.println("Uav " + detectingEntity + " found fire!!!");
                System.out.println("PointCount " + countMissedPoints3 );
                if (gimbalCommand.get(detectingEntity - 1) == 0) {

                    //Add point to polygon and send the report
                    estimatedHazardZoneUAV1.getBoundaryPoints().add(detectedLocation);
                    sendEstimateReport(out, estimatedHazardZoneUAV1, 1);

                    System.out.println("Rotating Camera");
                    sendSensorCommand(out, detectingEntity);
                    gimbalCommand.set((detectingEntity - 1), 1);
                    countMissedPoints3++;
                }
                if (navCommand.get(detectingEntity - 1) == 1) {
                    //we found fire change direction CCW
                    System.out.println("2Current dir: " + latestAirStateUAV3.getHeading() + " askedHeading: " + headingDirUAV3);

                    headingDirUAV3 = latestAirStateUAV3.getHeading();

                    System.out.println("UAV: " + detectingEntity + " turning -12");
                    headingDirUAV3 -= 12;
                    //increment counter if points == 10 add current point to polygon
                    countMissedPoints3++;
                    if (countMissedPoints3 == 10) {
                        Location3D hazLoc = estimatedHazardZoneUAV1.getBoundaryPoints().get(0);
                         double distance = Math.sqrt(Math.pow((detectedLocation.getLongitude() - hazLoc.getLongitude()),2) + Math.pow((detectedLocation.getLatitude() - hazLoc.getLatitude()),2));
                        if (distance < 0.01 && currSizePolygon3 > 5 ) {    
                            circleFire3 = true;
                            circleCounter3 = 0;
                        }
                      
                        if(circleCounter3 >= currSizePolygon3 && circleCounter3 != 0){
                            System.out.println("New points");
                            circleFire3 = false;
                            
                        }
                        if (circleFire3 ) {
                            System.out.println("Counter3: " + circleCounter3 + " size: " + currSizePolygon3 );
                            estimatedHazardZoneUAV1.getBoundaryPoints().set(circleCounter3, detectedLocation);
                            circleCounter3++;
                        } else {
                            System.out.println("Add point UAV1");
                            estimatedHazardZoneUAV1.getBoundaryPoints().add(detectedLocation);
                            currSizePolygon3++;
                        }
                       
                        sendEstimateReport(out, estimatedHazardZoneUAV1, 1);
                        countMissedPoints3 = 0;
                    }
                    //send command to change direction
                    FlightDirectorAction dir = new FlightDirectorAction(15, SpeedType.Groundspeed, headingDirUAV3, latestAirStateUAV3.getLocation().getAltitude(), AltitudeType.MSL, 0);
                    sendNavigationCommand(out, (int) detectingEntity, dir);
                    if (headingDirUAV3 <= -180) {
                        headingDirUAV3 += 360;
                    }

                    navCommand.set((detectingEntity - 1), 1);
                }
//                    
            } else if (detectingEntity == 2) {
                UAV2_available = false;
                System.out.println("Uav " + detectingEntity + " found fire!!!");
                System.out.println("PointCount " + countMissedPoints2 );
                if (gimbalCommand.get(detectingEntity - 1) == 0) {

                    //Add point to polygon and send the report
                    estimatedHazardZoneUAV2.getBoundaryPoints().add(detectedLocation);
                    sendEstimateReport(out, estimatedHazardZoneUAV2, 2);

                    System.out.println("Rotating Camera");
                    sendSensorCommand(out, detectingEntity);
                    gimbalCommand.set((detectingEntity - 1), 1);
                    countMissedPoints2++;
                }
                if (navCommand.get(detectingEntity - 1) == 1) {
                    //we found fire change direction CCW
                    System.out.println("2Current dir: " + latestAirStateUAV2.getHeading() + " askedHeading: " + headingDirUAV2);

                    headingDirUAV2 = latestAirStateUAV2.getHeading();

                    System.out.println("UAV: " + detectingEntity + " turning -12");
                    headingDirUAV2 -= 12;
                    //increment counter if points == 10 add current point to polygon
                    countMissedPoints2++;
                    if (countMissedPoints2 == 10) {
                        System.out.println("Add point UAV2");
                        Location3D hazLoc = estimatedHazardZoneUAV2.getBoundaryPoints().get(0);
                        double distance = Math.sqrt(Math.pow((detectedLocation.getLongitude() - hazLoc.getLongitude()),2) + Math.pow((detectedLocation.getLatitude() - hazLoc.getLatitude()),2));
                        if (distance < 0.01 && currSizePolygon2 > 5) {
                            circleFire2 = true;
                            circleCounter2 = 0;
                        }
                        if(circleCounter2 >= currSizePolygon2 && circleCounter2 != 0){
                            System.out.println("New points");
                            circleFire2 = false;
                            
                            
                        }
                        if (circleFire2 ) {
                            System.out.println("Counter2: " + circleCounter2 + " size: " + currSizePolygon2);
                            estimatedHazardZoneUAV2.getBoundaryPoints().set(circleCounter2, detectedLocation);
                            circleCounter2++;
                        } else {
                            estimatedHazardZoneUAV2.getBoundaryPoints().add(detectedLocation);
                            currSizePolygon2++;
                        }

                        sendEstimateReport(out, estimatedHazardZoneUAV2, 2);
                        countMissedPoints2 = 0;
                    }
                    //send command to change direction
                    FlightDirectorAction dir = new FlightDirectorAction(15, SpeedType.Groundspeed, headingDirUAV2, latestAirStateUAV2.getLocation().getAltitude(), AltitudeType.MSL, 0);
                    sendNavigationCommand(out, (int) detectingEntity, dir);
                    if (headingDirUAV2 <= -180) {
                        headingDirUAV2 += 360;
                    }

                    navCommand.set((detectingEntity - 1), 1);
                }

            } else if (detectingEntity == 1) {
                boolean goInside = false;
                if (lastFire != null) {
                    double latDiff = Math.abs(lastFire.getLatitude() - detectedLocation.getLatitude());
                    double lonDiff = Math.abs(lastFire.getLongitude() - detectedLocation.getLongitude());
                    if (latDiff > 0.01 || lonDiff > 0.01) {
                        goInside = true;
                    }
                } else {
                    goInside = true;
                }
                if (goInside) {
                    if (UAV2_available == true) {
                        goToFire(out, 2, detectedLocation);
                        UAV2_available = false;
                    } else if (UAV3_available == true) {
                        goToFire(out, 3, detectedLocation);
                        UAV3_available = false;
                    } else {
                        System.out.println("No available drones");
                    }
                }
                lastFire = detectedLocation;
            } else if (detectingEntity == 4) {
                boolean goInside = false;
                if (lastFire != null) {
                    double latDiff = Math.abs(lastFire.getLatitude() - detectedLocation.getLatitude());
                    double lonDiff = Math.abs(lastFire.getLongitude() - detectedLocation.getLongitude());
                    if (latDiff > 0.01 || lonDiff > 0.01) {
                        goInside = true;
                    }
                } else {
                    goInside = true;
                }
                if (goInside) {
                    if (UAV3_available == true) {
                        goToFire(out, 3, detectedLocation);
                        UAV3_available = false;
                    } else if (UAV2_available == true) {
                        goToFire(out, 2, detectedLocation);
                        UAV2_available = false;
                    } else {
                        System.out.println("No available drones");
                    }
                }
                lastFire = detectedLocation;
            }

//           
        } else if (o instanceof afrl.cmasi.AirVehicleState) {
            AirVehicleState uav = ((AirVehicleState) o);
            //System.out.println("UAV: " + uav.getID());

            Location3D loc = uav.getLocation();
            //System.out.println("Lat: " + loc.getLatitude());

            if (uav.getID() == 2) {
                latestAirStateUAV2 = uav;
                if (gimbalCommand.get((int) uav.getID() - 1) == 1 && navCommand.get((int) uav.getID() - 1) == 0) {
                    System.out.println("Change direction for UAV : " + uav.getID());
                    headingDirUAV2 = uav.getHeading() - 90;
                    FlightDirectorAction dir = new FlightDirectorAction(uav.getAirspeed(), SpeedType.Groundspeed, headingDirUAV2, uav.getLocation().getAltitude(), AltitudeType.MSL, 0);
                    sendNavigationCommand(out, (int) uav.getID(), dir);
                    navCommand.set((int) uav.getID() - 1, 1);
                }
                if (navCommand.get((int) (uav.getID() - 1)) == 1) {
                    System.out.println("Current dir: " + latestAirStateUAV2.getHeading() + " askedHeading: " + headingDirUAV2);
                    System.out.println("Current time: " + scenarioTime + " elapsed: " + elapsedTImeUAV2);
                    if (Math.abs(Math.abs(headingDirUAV2) - Math.abs(latestAirStateUAV2.getHeading())) < 10) {
                        if (scenarioTime - elapsedTImeUAV2 > 1000) {
                            headingDirUAV2 = latestAirStateUAV2.getHeading();

                            if (headingDirUAV2 > 0) {
                                System.out.println("UAV: " + uav.getID() + " turning -5");
                                headingDirUAV2 += 7;
                            } else {
                                System.out.println("UAV: " + uav.getID() + " turning +5");
                                headingDirUAV2 += 7;
                            }

                            FlightDirectorAction dir = new FlightDirectorAction(15, SpeedType.Groundspeed, headingDirUAV2, latestAirStateUAV2.getLocation().getAltitude(), AltitudeType.MSL, 0);
                            sendNavigationCommand(out, (int) (uav.getID()), dir);
                            navCommand.set(((int) (uav.getID() - 1)), 1);
                            if (headingDirUAV2 > 180) {
                                headingDirUAV2 -= 360;
                            }
                            elapsedTImeUAV2 = scenarioTime;
                        } else {
                            System.out.println("Delay UAV: " + uav.getID());
                        }
                    }
                }

                GimbalState gimbal = (GimbalState) uav.getPayloadStateList().get(0);
                CameraState camera = (CameraState) uav.getPayloadStateList().get(1);

                // System.out.println("Gimbal Rot:" + gimbal.getRotation());
                // System.out.println("Camera Rot:" + camera.getRotation());
            } else if (uav.getID() == 3) {
                latestAirStateUAV3 = uav;
                if (gimbalCommand.get((int) uav.getID() - 1) == 1 && navCommand.get((int) uav.getID() - 1) == 0) {
                    System.out.println("Change direction for UAV : " + uav.getID());
                    headingDirUAV3 = uav.getHeading() - 90;
                    FlightDirectorAction dir = new FlightDirectorAction(uav.getAirspeed(), SpeedType.Groundspeed, headingDirUAV3, uav.getLocation().getAltitude(), AltitudeType.MSL, 0);
                    sendNavigationCommand(out, (int) uav.getID(), dir);
                    navCommand.set((int) uav.getID() - 1, 1);
                }
                if (navCommand.get((int) (uav.getID() - 1)) == 1) {
                    System.out.println("Current dir: " + latestAirStateUAV3.getHeading() + " askedHeading: " + headingDirUAV3);
                    System.out.println("Current time: " + scenarioTime + " elapsed: " + elapsedTImeUAV3);
                    if (Math.abs(Math.abs(headingDirUAV3) - Math.abs(latestAirStateUAV3.getHeading())) < 10) {
                        if (scenarioTime - elapsedTImeUAV3 > 1000) {
                            headingDirUAV3 = latestAirStateUAV3.getHeading();

                            if (headingDirUAV3 > 0) {
                                System.out.println("UAV: " + uav.getID() + " turning -5");
                                headingDirUAV3 += 7;
                            } else {
                                System.out.println("UAV: " + uav.getID() + " turning +5");
                                headingDirUAV3 += 7;
                            }

                            FlightDirectorAction dir = new FlightDirectorAction(15, SpeedType.Groundspeed, headingDirUAV3, latestAirStateUAV3.getLocation().getAltitude(), AltitudeType.MSL, 0);
                            sendNavigationCommand(out, (int) (uav.getID()), dir);
                            navCommand.set(((int) (uav.getID() - 1)), 1);
                            if (headingDirUAV3 > 180) {
                                headingDirUAV3 -= 360;
                            }
                            elapsedTImeUAV3 = scenarioTime;
                        } else {
                            System.out.println("Delay UAV: " + uav.getID());
                        }
                    }
                }

            }

        } else if (o instanceof afrl.cmasi.SessionStatus) {
            //Example of using an incoming LMCP message
            scenarioTime = ((SessionStatus) o).getScenarioTime();
            //System.out.println(o.toString());
        }
    }

    /**
     * tries to connect to the server. If there is a problem (such as the server
     * not running yet) it pauses, then tries again. If the server quits and
     * restarts, this method is called by the thread in order to re-establish
     * communication.
     *
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
                Logger.getLogger(zero.class
                        .getName()).log(Level.SEVERE, null, ex1);
            }
            return connect(host, port);
        }
        System.out.println("Connected to " + host + ":" + port);
        return socket;
    }

    public static int getRandomIntegerBetweenRange(int min, int max) {
        int x = (int) (Math.random() * ((max - min) + 1)) + min;
        return x;
    }
    public static void spiralMaker(double myLong, double myLat,double x[] ,double y[], int noThetas) {
            double theta[] = new double[noThetas];
            
            double b = 0.0018;
            double a = 0.019;

            double inc =  720/noThetas;
            for (int i = 0; i< noThetas; i++){
                theta[i] = 0;
            }
            for (int i = 0; i< noThetas; i++){
                theta[i] = i*inc;
            }
                        
            for (int i = 0; i< noThetas; i++) {

                    x[i]  = myLong + a*Math.exp(b*theta[i])*Math.cos(Math.toRadians(theta[i]));
                    y[i]  = myLat + a*Math.exp(b*theta[i])*Math.sin(Math.toRadians(theta[i]));
                    // print the values
                    System.out.printf("i: %d x[i] = %f, y[i] = %f theta: %f\n",i,x[i],y[i],theta[i]);
            }
    }	

    public static void main(String[] args) {
        new zero().start();
    }
}
