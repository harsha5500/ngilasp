/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Database.Connections;

/**
 *
 * @author jayanth
 */
public class ConnectionProperties {

    private int minConnections;
    private int maxConnections;
    private int maxIdleTime;

    /**
     * @param minConnections
     * @param maxConnections
     */
    public ConnectionProperties(int minConnections, int maxConnections) {
        this.minConnections = minConnections;
        this.maxConnections = maxConnections;
        this.maxIdleTime = 0;
    }

    /**
     * @param minConnections
     * @param maxConnections
     * @param maxIdleTime
     */
    public ConnectionProperties(int minConnections, int maxConnections,
            int maxIdleTime) {
        this.minConnections = minConnections;
        this.maxConnections = maxConnections;
        this.maxIdleTime = maxIdleTime;
    }

    /**
     * @return Returns the maxConnections.
     */
    public int getMaxConnections() {
        return maxConnections;
    }

    /**
     * @param maxConnections The maxConnections to set.
     */
    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    /**
     * @return Returns the minConnections.
     */
    public int getMinConnections() {
        return minConnections;
    }

    /**
     * @param minConnections The minConnections to set.
     */
    public void setMinConnections(int minConnections) {
        this.minConnections = minConnections;
    }

    /**
     * @return Returns the maxIdleTime.
     */
    public int getMaxIdleTime() {
        return maxIdleTime;
    }

    /**
     * @param maxIdleTime The maxIdleTime to set.
     */
    public void setMaxIdleTime(int maxIdleTime) {
        this.maxIdleTime = maxIdleTime;
    }
}
