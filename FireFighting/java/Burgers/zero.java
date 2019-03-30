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
import afrl.cmasi.AbstractGeometry;
import afrl.cmasi.AirVehicleConfiguration;
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
import afrl.cmasi.searchai.HazardZone;
import afrl.cmasi.searchai.RecoveryPoint;
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
import java.util.Collections;
import java.util.Comparator;
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
        //this is to check if a uav is currently going home
    private boolean returningHome = false;
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

    private static List<Boolean> UAVFireAvailable = new ArrayList<>();
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

    //vehicle type 0 = fixedWing
    //        type 1 = multi
    List<UAV> UAVS = new ArrayList<>();

    Location3D refuelLoc = null;

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
        spiralMaker(53.4750, -1.8351, xUAV1, yUAV1, noThetas);
        spiralMaker(53.4750, -1.6956, xUAV2, yUAV2, noThetas);
        spiralMaker(53.412, -1.8335, xUAV3, yUAV3, noThetas);
        spiralMaker(53.412, -1.6898, xUAV4, yUAV4, noThetas);

        int waypointnum = noThetas;
        for (int i = 1; i < waypointnum; i++) {
            Waypoint waypointDev = new Waypoint();
            //System.out.print("This is waypoint " + i);
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
            if (id == 1) {

                waypointDev.setLatitude(xUAV1[i]);
                waypointDev.setLongitude(yUAV1[i]);
                waypointDev.setAltitude(1000);

            }
            if (id == 2) {

                waypointDev.setLatitude(xUAV2[i]);
                waypointDev.setLongitude(yUAV2[i]);
                waypointDev.setAltitude(1000);
            }
            if (id == 3) {

                waypointDev.setLatitude(xUAV3[i]);
                waypointDev.setLongitude(yUAV3[i]);
                waypointDev.setAltitude(1000);
            }
            if (id == 4) {

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

            //Get location where zone first detected
            Location3D detectedLocation = hazardDetected.getDetectedLocation();
            //System.out.println("LAT: " + detectedLocation.getLatitude());
            // System.out.println("LOG: " + detectedLocation.getLongitude());

            wayPointList.add(detectedLocation.getLatitude());
            wayPointList.add(detectedLocation.getLongitude());
            int detectingEntity = (int) hazardDetected.getDetectingEnitiyID();
            UAV curUAV = null;
            if (detectingEntity != 0) {
                curUAV = UAVS.get(detectingEntity - 1);

            }

            if (curUAV.isFixedWing()) {
                callForFireMapping(out, curUAV, detectedLocation);
            } else if (curUAV.isMulti()) {
                circleFire(out, curUAV, detectedLocation);
            }

//           
        } else if (o instanceof afrl.cmasi.AirVehicleState) {
            AirVehicleState uav = ((AirVehicleState) o);
            //System.out.println("UAV: " + uav.getID());

            boolean batterylow = isBatteryLow(uav);

            //System.out.println("Batter: " + batterylow + " return: " + returningHome);
            //System.out.println("Batter: " + batterylow + " return: " + returningHome);
            if (batterylow && (returningHome == false)) {
                returningHome = true;
                System.out.println("BATTERY LOWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW! GOING HOME FAM");
                missionRecharge(out, uav.getID(), refuelLoc);
            }
            if (scenarioTime > 10) {
                turnAwayFromFire(out, uav);
            }

            //System.out.println("Lat: " + loc.getLatitude());
        } else if (o instanceof afrl.cmasi.SessionStatus) {
            //Example of using an incoming LMCP message
            scenarioTime = ((SessionStatus) o).getScenarioTime();
            //System.out.println(o.toString());
        } else if (o instanceof afrl.cmasi.searchai.RecoveryPoint) {
            RecoveryPoint recovery = (RecoveryPoint) o;
            Circle boundary = (Circle) recovery.getBoundary();

            refuelLoc = boundary.getCenterPoint();
            refuelLoc.setAltitude(700);
            //System.out.println(refuelLoc.toString());
        } else if (o instanceof afrl.cmasi.AirVehicleConfiguration) {
            newDrone((AirVehicleConfiguration) o);
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
    private void newDrone(AirVehicleConfiguration o) {
        AirVehicleConfiguration uavState = (AirVehicleConfiguration) o;
        //System.out.println(uavState.getEntityType());
        if (uavState.getEntityType().equals("FixedWing")) {
            System.out.println("Hi am FixedWing with id: " + uavState.getID());
            UAVS.add(new UAV(0, true, (int) uavState.getID()));
        } else {
            UAVS.add(new UAV(1, true, (int) uavState.getID()));
            System.out.println("Hi am Multi with id: " + uavState.getID());
        }
        sortList(UAVS);
//        for(UAV uav : UAVS){
//            System.out.println(uav.getId());
//        }
    }

    private void sortList(List<UAV> list) {
        Collections.sort(list, new Comparator<UAV>() {
            public int compare(UAV ideaVal1, UAV ideaVal2) {
                // avoiding NullPointerException in case name is null
                Integer idea1 = new Integer(ideaVal1.getId());
                Integer idea2 = new Integer(ideaVal2.getId());
                return idea1.compareTo(idea2);
            }
        });
    }

    private void callForFireMapping(OutputStream out, UAV curUAV, Location3D detectedLocation) throws Exception {

        boolean goInside = false;
        if (curUAV.getLastFire() != null) {
            double latDiff = Math.abs(curUAV.getLastFire().getLatitude() - detectedLocation.getLatitude());
            double lonDiff = Math.abs(curUAV.getLastFire().getLongitude() - detectedLocation.getLongitude());
            if (latDiff > 0.01 || lonDiff > 0.01) {
                goInside = true;
            }
        } else {
            goInside = true;
        }
        if (goInside) {
            for (UAV uav : UAVS) {
                if (uav.isAvailable()) {
                    goToFire(out, uav.getId(), detectedLocation);
                    uav.setAvailable(false);

                }
            }

        }
        curUAV.setLastFire(detectedLocation);
        UAVS.set(curUAV.getId() - 1, curUAV);

    }

    private void circleFire(OutputStream out, UAV curUAV, Location3D detectedLocation) throws Exception {

        curUAV.setAvailable(false);
        System.out.println("Uav " + curUAV.getId() + " found fire!!!");
        System.out.println("PointCount " + curUAV.getCountMissedPoints());
        if (curUAV.isGimbalChanged() == false) {

            //Add point to polygon and send the report
            curUAV.getEstimateHazardZone().getBoundaryPoints().add(detectedLocation);
            sendEstimateReport(out, curUAV.getEstimateHazardZone(), curUAV.getId());

            System.out.println("Rotating Camera");
            sendSensorCommand(out, curUAV.getId());
            curUAV.setGimbalChanged(true);
            curUAV.incrementCountMissedPoints();
        }
        if (curUAV.isNavCommand()) {
            //we found fire change direction CCW
            System.out.println("2Current dir: " + curUAV.getLastAirstate().getHeading() + " askedHeading: " + curUAV.getHeadingDir());

            curUAV.setHeadingDir(curUAV.getLastAirstate().getHeading());

            System.out.println("UAV: " + curUAV.getId() + " turning -12");
            curUAV.setHeadingDir(curUAV.getLastAirstate().getHeading() - 12);
            //increment counter if points == 10 add current point to polygon
            curUAV.incrementCountMissedPoints();
            if (curUAV.getCountMissedPoints() == 10) {
                Location3D hazLoc = curUAV.getEstimateHazardZone().getBoundaryPoints().get(0);
                double distance = Math.sqrt(Math.pow((detectedLocation.getLongitude() - hazLoc.getLongitude()), 2) + Math.pow((detectedLocation.getLatitude() - hazLoc.getLatitude()), 2));
                if (distance < 0.01 && curUAV.getCurrSizePolygon() > 5) {
                    curUAV.setCircleFire(true);
                    curUAV.setCircleCounter(0);
                }

                if (curUAV.getCircleCounter() >= curUAV.getCurrSizePolygon() && curUAV.getCircleCounter() != 0) {
                    System.out.println("New points");
                    curUAV.setCircleFire(false);

                }
                if (curUAV.isCircleFire()) {
                    System.out.println("Counter3: " + curUAV.getCircleCounter() + " size: " + curUAV.getCurrSizePolygon());
                    curUAV.getEstimateHazardZone().getBoundaryPoints().set(curUAV.getCircleCounter(), detectedLocation);
                    curUAV.incrementCircleCounter();
                } else {
                    System.out.println("Add point UAV1");
                    curUAV.getEstimateHazardZone().getBoundaryPoints().add(detectedLocation);
                    curUAV.incrementPolygonSize();
                }

                sendEstimateReport(out, curUAV.getEstimateHazardZone(), curUAV.getId());
                curUAV.setCountMissedPoints(0);
            }
            //send command to change direction
            FlightDirectorAction dir = new FlightDirectorAction(15, SpeedType.Groundspeed, curUAV.getHeadingDir(), curUAV.getLastAirstate().getLocation().getAltitude(), AltitudeType.MSL, 0);
            sendNavigationCommand(out, (int) curUAV.getId(), dir);
            if (curUAV.getHeadingDir() <= -180) {
                curUAV.setHeadingDir(curUAV.getHeadingDir() + 360);
            }
            curUAV.setNavCommand(true);

        }
//                    

        UAVS.set((curUAV.getId() - 1), curUAV);
    }

    private void turnAwayFromFire(OutputStream out, AirVehicleState uav) throws Exception {
        UAV curUAV = UAVS.get(((int) uav.getID() - 1));
        curUAV.setLastAirstate(uav);
        if (curUAV.isGimbalChanged() && curUAV.isNavCommand() == false) {
            System.out.println("Change direction for UAV : " + uav.getID());
            curUAV.setHeadingDir(uav.getHeading() - 90);
            FlightDirectorAction dir = new FlightDirectorAction(uav.getAirspeed(), SpeedType.Groundspeed, curUAV.getHeadingDir(), uav.getLocation().getAltitude(), AltitudeType.MSL, 0);
            sendNavigationCommand(out, (int) uav.getID(), dir);
            curUAV.setNavCommand(true);
        }
        if (curUAV.isNavCommand()) {
            System.out.println("Current dir: " + curUAV.getLastAirstate().getHeading() + " askedHeading: " + curUAV.getHeadingDir());
            System.out.println("Current time: " + scenarioTime + " elapsed: " + curUAV.getElapsedTime());
            if (Math.abs(Math.abs(curUAV.getHeadingDir()) - Math.abs(curUAV.getLastAirstate().getHeading())) < 10) {
                if (scenarioTime - curUAV.getElapsedTime() > 1000) {
                    curUAV.setHeadingDir(curUAV.getLastAirstate().getHeading());

                    System.out.println("UAV: " + uav.getID() + " turning +7");
                    curUAV.setHeadingDir(curUAV.getHeadingDir() + 7);

                    FlightDirectorAction dir = new FlightDirectorAction(15, SpeedType.Groundspeed, curUAV.getHeadingDir(), curUAV.getLastAirstate().getLocation().getAltitude(), AltitudeType.MSL, 0);
                    sendNavigationCommand(out, (int) (uav.getID()), dir);
                    curUAV.setNavCommand(true);
                    if (curUAV.getHeadingDir() > 180) {
                        curUAV.setHeadingDir(curUAV.getHeadingDir() - 360);
                    }
                    curUAV.setElapsedTime(scenarioTime);
                } else {
                    System.out.println("Delay UAV: " + uav.getID());
                }
            }
        }
        UAVS.set(curUAV.getId() - 1, curUAV);

//            GimbalState gimbal = (GimbalState) uav.getPayloadStateList().get(0);
//            CameraState camera = (CameraState) uav.getPayloadStateList().get(1);
//
//            // System.out.println("Gimbal Rot:" + gimbal.getRotation());
//            // System.out.println("Camera Rot:" + camera.getRotation());
    }

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

    public static void spiralMaker(double myLong, double myLat, double x[], double y[], int noThetas) {
        double theta[] = new double[noThetas];

        double b = 0.0018;
        double a = 0.019;

        double inc = 720 / noThetas;
        for (int i = 0; i < noThetas; i++) {
            theta[i] = 0;
        }
        for (int i = 0; i < noThetas; i++) {
            theta[i] = i * inc;
        }

        for (int i = 0; i < noThetas; i++) {

            x[i] = myLong + a * Math.exp(b * theta[i]) * Math.cos(Math.toRadians(theta[i]));
            y[i] = myLat + a * Math.exp(b * theta[i]) * Math.sin(Math.toRadians(theta[i]));
            // print the values
            //System.out.printf("i: %d x[i] = %f, y[i] = %f theta: %f\n",i,x[i],y[i],theta[i]);
        }
    }

    private void missionRecharge(OutputStream out, long id, Location3D refuelLoc) throws Exception {

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

        waypointDev.setLatitude(refuelLoc.getLatitude());
        waypointDev.setLongitude(refuelLoc.getLongitude());
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
    //check if the battery of the uav is low
    public boolean isBatteryLow(AirVehicleState uav) {
        
       double batteryPercentage = uav.getEnergyAvailable();
       
       if(batteryPercentage < 30)
       {
           //System.out.print(batteryPercentage);
           return true;
       }
       return false;
    }
 

    public static void main(String[] args) {
        new zero().start();
    }
}
