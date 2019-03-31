/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Burgers;

import afrl.cmasi.AirVehicleState;
import afrl.cmasi.Location3D;
import afrl.cmasi.Polygon;
import afrl.cmasi.Waypoint;
import afrl.cmasi.searchai.HazardZone;
import afrl.cmasi.searchai.HazardZoneDetection;
import java.util.ArrayList;

/**
 *
 * @author nikos
 */
public class UAV {
    
    
    private int droneType;
    private boolean available;
    private boolean mission =false;
    private int id;
    
    private double latDiff;
    private double lonDiff;
    private  Location3D lastFire = null;
    
    private int countMissedPoints = 0;
    
    private boolean gimbalChanged = false;
    
    private boolean isgoingHome = false;
    
    private boolean navCommand = false;
    
    private AirVehicleState lastAirstate;
    private float headingDir=0;
    private Polygon estimateHazardZone=new Polygon();
    
    private boolean circleFire = false;
    private int circleCounter = 0;
    
    private int currSizePolygon = 0;
    private long elapsedTime = 0;
    private boolean gotFollower = false;
    Location3D pointBeforeCharge;
    private boolean goingBack = false;
    
    private int droneID;
    
    public UAV(int droneType, boolean available, int id) {
        this.droneType = droneType;
        this.available = available;
        this.id = id;
    }
    
    public boolean patrol = false; 
    private int  follower=-1;
    public long latEnd;
    public long lonEnd;

    public ArrayList<Waypoint> waypointList = new ArrayList<Waypoint>();

    
    public int getDroneType() {
        return droneType;
    }

    public double getLatDiff() {
        return latDiff;
    }

    public void setLatDiff(double latDiff) {
        this.latDiff = latDiff;
    }

    public double getLonDiff() {
        return lonDiff;
    }

    public void setLonDiff(double lonDiff) {
        this.lonDiff = lonDiff;
    }

    public void setDroneType(int droneType) {
        this.droneType = droneType;
    }

    public boolean isAvailable() {
        if(getDroneType() == 1){
             return available;
        }else{
            return false;
        }
       
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public boolean isFixedWing(){
        if(this.getDroneType() == 0){
            return true;
        }else{
            return false;
        }
    }
   
    public boolean isMulti(){
        if(this.getDroneType() == 1){
            return true;
        }else{
            return false;
        }
    }

    public  Location3D getLastFire() {
        return lastFire;
    }

    public  void setLastFire(Location3D lastFire) {
        this.lastFire = lastFire;
    }

    public int getCountMissedPoints() {
        return countMissedPoints;
    }

    public void setCountMissedPoints(int countMissedPoints) {
        this.countMissedPoints = countMissedPoints;
    }

    public boolean isGimbalChanged() {
        return gimbalChanged;
    }

    public void setGimbalChanged(boolean gimbalChanged) {
        this.gimbalChanged = gimbalChanged;
    }
    
    public void incrementCountMissedPoints(){
        this.countMissedPoints++;
    }

    public void incrementCircleCounter(){
        this.circleCounter++;
    }
    public void incrementPolygonSize(){
        this.currSizePolygon++;
    }
    public AirVehicleState getLastAirstate() {
        return lastAirstate;
    }

    public void setLastAirstate(AirVehicleState lastAirstate) {
        this.lastAirstate = lastAirstate;
    }

    public float getHeadingDir() {
        return headingDir;
    }

    public void setHeadingDir(float headingDir) {
        this.headingDir = headingDir;
    }

    public Polygon getEstimateHazardZone() {
        return estimateHazardZone;
    }

    public void setEstimateHazardZone(Polygon estimateHazardZone) {
        this.estimateHazardZone = estimateHazardZone;
    }

    public boolean isCircleFire() {
        return circleFire;
    }

    public void setCircleFire(boolean circleFire) {
        this.circleFire = circleFire;
    }

    public int getCircleCounter() {
        return circleCounter;
    }

    public void setCircleCounter(int circleCounter) {
        this.circleCounter = circleCounter;
    }

    public int getCurrSizePolygon() {
        return currSizePolygon;
    }

    public void setCurrSizePolygon(int currSizePolygon) {
        this.currSizePolygon = currSizePolygon;
    }

    public boolean isNavCommand() {
        return navCommand;
    }

    public void setNavCommand(boolean navCommand) {
        this.navCommand = navCommand;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public boolean isMission() {
        return mission;
    }

    public void setMission(boolean mission) {
        this.mission = mission;
    }

    public boolean isGotFollower() {
        return gotFollower;
    }

    public void setGotFollower(boolean gotFollower) {
        this.gotFollower = gotFollower;
    }
    
    public void setFollower(int i){
        this.follower = i;
    }
    
    public int getFollower(){
        return this.follower ;
    }

    
    
    public long getLatEnd() {
        return latEnd;
    }

    public void setLatEnd(long latEnd) {
        this.latEnd = latEnd;
    }

    public long getLonEnd() {
        return lonEnd;
    }

    public void setLonEnd(long lonEnd) {
        this.lonEnd = lonEnd;
    }
    
    public boolean isReturningHome()
    {
        return isgoingHome;
    }
    
    public void setReturningHome(boolean state)
    {
        isgoingHome = state;
    }

    public boolean isIsgoingHome() {
        return isgoingHome;
    }

    public void setIsgoingHome(boolean isgoingHome) {
        this.isgoingHome = isgoingHome;
    }

    public Location3D getPointBeforeCharge() {
        return pointBeforeCharge;
    }

    public void setPointBeforeCharge(Location3D pointBeforeCharge) {
        this.pointBeforeCharge = pointBeforeCharge;
    }

    public boolean isPatrol() {
        return patrol;
    }

    public void setPatrol(boolean patrol) {
        this.patrol = patrol;
    }

    public ArrayList<Waypoint> getWaypointList() {
        return waypointList;
    }

    public void setWaypointList(ArrayList<Waypoint> waypointList) {
        this.waypointList = waypointList;
    }

    public boolean isGoingBack() {
        return goingBack;
    }

    public void setGoingBack(boolean goingBack) {
        this.goingBack = goingBack;
    }

    public int getDroneID() {
        return droneID;
    }

    public void setDroneID(int droneID) {
        this.droneID = droneID;
    }
    
    
    
}
