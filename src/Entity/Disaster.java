package Entity;

import java.io.Serializable;
import org.postgis.Point;

/**
 * This class defines the attribute of a disaster. It contains information such as
 * the location of the disaster, its intensity and when it gets triggered with respect
 * to sysetem time.
 */
public class Disaster implements Serializable {

    /**
     * Location of the disaster as a Point object
     */
    private Point latlon;
    /**
     * The intensity of the disaster
     */
    private int intensity;
    /*
     * Time in ticks after which the disaster is triggered
     */
    public int disasterTriggerTimeInTicks;
    /**
     * The time tick at which the disaster occured
     */
    public long tick = 50;
    //TODO remove the initialization

    /**
     * The constructor returns a disaster in the given location of a set intensity.
     * @param latlon the location of the disaster.
     * @param intensity the intensity of the disaster.
     */
    public Disaster(Point latlon, int intensity, int start) {
        this.latlon = latlon;
        this.intensity = intensity;
        this.disasterTriggerTimeInTicks = start;
    }

    /**
     * The constructor returns a disaster in the given location of a set intensity.
     * @param latlon latlon the location of the disaster.
     * @param intensity the intensity of the disaster.
     * @param disasterTriggerTimeInTicks the time when the disaster is triggered in terms of system ticks
     * @param tick the current tick number
     */
    public Disaster(Point latlon, int intensity, int disasterTriggerTimeInTicks, long tick) {
        this.latlon = latlon;
        this.intensity = intensity;
        this.disasterTriggerTimeInTicks = disasterTriggerTimeInTicks;
        this.tick = tick;
    }

    /**
     * Returns the intensity of the disaster
     * @return the intensity of the disaster
     */
    public int getIntensity() {
        return intensity;
    }

    /**
     * Set the intensity of the disaster
     * @param intensity the intensity of the disaster
     */
    public void setIntensity(int intensity) {
        this.intensity = intensity;
    }

    /**
     * Return the location of the disaster as a Point object
     * @return the lcoation of the disaster
     */
    public Point getLatlon() {
        return latlon;
    }

    /**
     * Set the location of the disaster
     * @param latlon the location of the disaster
     */
    public void setLatlon(Point latlon) {
        this.latlon = latlon;
    }

    /**
     * Returns the status of the disaster object
     * @return the status of the disaster object
     */
    @Override
    public String toString() {
        return "Location = " + this.latlon.toString() + "\nIntensity = " + this.intensity;
    }
}
