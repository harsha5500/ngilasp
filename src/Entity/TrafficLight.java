package Entity;

import GeographicInformation.Cell;
import GlobalData.Constants;
import java.io.Serializable;

/**
 * This class defines a TrafficLight. This entity indicates the traffic
 * color status to influence the movement of vehicle agents. This entity is controlled
 * by the TrafficLightHub agent.
 * @see TrafficLightHub
 */
public class TrafficLight implements Serializable {

    /**
     * The status of color. RED or GREEN, as defined in Constants class
     * @see Constants
     */
    int color;
    /**
     * The location of the traffic color agent on the map. The agent is associated
     * to a cell on a road on which it influences the traffic flow
     */
    Cell location;
    /**
     * The duration for which the traffic color displays the current Signal
     */
    int duration;
    /**
     * The status of the time out to change the signal
     */
    int currentDurationStep;
    /**
     * The road which the TrafficLight controls the traffic
     */
    long roadid;
    /**
     * The id of the cell where the TrafficLight is situated of a given road
     */
    long cellid;

    /**
     * Constructor for TrafficLight. The parameters are stored in the database
     * @param light 
     * @param location the cell location of the traffic color
     * @param duration the duration of the signal being displayed before its change
     * @param currentStep the current tick count before the signal is changed
     * @param road the road that the traffic color controls
     * @see Database.Sql
     */
    public TrafficLight(int light, Cell location, int duration, int currentStep, long road) {
        this.color = light;
        this.location = location;
        this.duration = duration;
        this.currentDurationStep = currentStep;
        this.roadid = road;
    }

    /**
     * Constructor for TrafficLight. The parameters are stored in the database
     * @param light the current signal being displayed, RED or GREEN
     * @param location the cell location of the traffic color
     * @param duration the duration of the signal being displayed before its change
     * @param currentStep the current tick count before the signal is changed
     * @param road the road that the traffic color controls
     * @param cellid the cell in which the traffic light is situated in
     * @see Database.Sql
     */
    public TrafficLight(int light, Cell location, int duration, int currentStep, long road, long cellid) {
        this.color = light;
        this.location = location;
        this.duration = duration;
        this.currentDurationStep = currentStep;
        this.roadid = road;
        this.cellid = cellid;
    }

    /**
     * Create a empty TrafficLight object
     */
    public TrafficLight() {
    }

    /**
     * Checks the current signal status and flips it to its opposite.
     * Currently RED is fliped to GREEN and vice versa.
     */
    public void flipSignal() {
        if (this.color == Constants.SIGNAL_RED) {
            this.color = Constants.SIGNAL_GREEN;
            //this.style.color = Constants.SIGNAL_COLOR_GREEN;
        } else {
            this.color = Constants.SIGNAL_RED;
            //this.style.color = Constants.SIGNAL_COLOR_RED;
        }
    }

    /**
     * Increaments the currentDurationStep by one to indicate the arrival of a
     * TICK message.
     */
    public void incrementStep() {
        this.currentDurationStep++;
    }

    /**
     * Checks if the traffic color has completed displaying the current signal
     * for its specified duration.
     * @return true if the signal has been displayed for the specified duration.
     */
    public boolean isDurationComplete() {
        if (this.currentDurationStep == this.duration) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Return the color of the current signal displayed by the TrafficSignal
     * @return the color of the current signal displayed
     */
    public int getColor() {
        return color;
    }

    /**
     * Set the color of the current signal displayed by the TrafficSignal
     * @param color the color of the current signal displayed
     */
    public void setColor(int color) {
        this.color = color;
    }

    /**
     * Return the color that the agent is showing.
     * @return the color that the signal is displaying.
     */
    public int getLight() {
        return color;
    }

    /**
     * Return the location of the traffic light as a Cell.
     * @return lcoation of the traffic light
     * @see Cell
     */
    public Cell getLocation() {
        return location;
    }

    /**
     *  Set the Location of the traffic light.
     * @param location location of the traffic light
     * @see Cell
     */
    public void setLocation(Cell location) {
        this.location = location;
    }

    /**
     * Return the ID of the road that this TrafficLight handles. The ID
     * identifies the road in the GIS database.
     * @return the ID of the road
     */
    public long getRoadid() {
        return roadid;
    }

    /**
     * Returns the cell id for the TrafficLight
     * @return the cell if of the TrafficLight
     */
    public long getCellid() {
        return cellid;
    }

    /**
     * Set the cell id for the TrafficLight
     * @param cellid the cell id for the TrafficLight
     */
    public void setCellid(long cellid) {
        this.cellid = cellid;
    }

    /**
     * Return the ID of the road that this TrafficLight handles. The ID
     * identifies the road in the GIS database.
     * @param roadid the ID of the road
     */
    public void setRoadid(long roadid) {
        this.roadid = roadid;
    }

    /**
     * Get the duration for which the traffic color displays the current signal
     * @return Duration of current signal display
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Set the duration for which the traffic color displays the current signal
     * @param duration the duration for which the traffic color displays the current signal
     */
    public void setDuration(int duration) {
        this.duration = duration;
    }

    /**
     * Returns the current Step in the counter to change the signal.
     * @return current Step in the counter to change the signal.
     */
    public int getCurrentStep() {
        return currentDurationStep;
    }

    /**
     * Set the current Step in the counter to change the signal.
     * @param currentStep 
     */
    public void setCurrentStep(int currentStep) {
        this.currentDurationStep = currentStep;
    }

    /**
     * String representation of the TrafficLight
     * @return string representation of the TrafficLight
     */
    @Override
    public String toString() {
        return "State: " + getLight()
                + " Location: " + getLocation().toString()
                + " TimeOutDuration: " + getDuration()
                + " CurrentStep: " + getCurrentStep()
                + " RelatedRoad: " + getRoadid();
    }

    /**
     * Return true if the traffic color is situated in the same location and
     * manages the same road else returns false.
     * @param obj TrafficLight object
     * @return true if the objects are equal
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final TrafficLight other = (TrafficLight) obj;

        if ((this.getLocation().equals(other.getLocation())) && (this.getRoadid() == other.getRoadid())) {
            return true;
        }

        return false;
    }

    /**
     * Return hash code of the TrafficLight object
     * @return hash code of the TrafficLight object
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
