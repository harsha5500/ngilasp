/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Database.Connections;

/**
 *
 * @author jayanth
 */
public class DbProperties {

    private String username;
    private String password;
    private String dbUrl;
    private String driverClass;
    private ConnectionProperties connectionProperties;

    /**
     * @param username
     * @param password
     * @param dbUrl
     * @param driverClass
     */
    public DbProperties(String username, String password, String dbUrl,
            String driverClass) {
        this.username = username;
        this.password = password;
        this.dbUrl = dbUrl;
        this.driverClass = driverClass;
    }

    /**
     * @param username
     * @param password
     * @param dbUrl
     * @param driverClass
     * @param connectionProperties
     */
    public DbProperties(String username, String password, String dbUrl,
            String driverClass, ConnectionProperties connectionProperties) {
        this.username = username;
        this.password = password;
        this.dbUrl = dbUrl;
        this.driverClass = driverClass;
        this.connectionProperties = connectionProperties;
    }

    /**
     * @return Returns the dbUrl.
     */
    public String getDbUrl() {
        return dbUrl;
    }

    /**
     * @param dbUrl
     *            The dbUrl to set.
     */
    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    /**
     * @return Returns the driverClass.
     */
    public String getDriverClass() {
        return driverClass;
    }

    /**
     * @param driverClass
     *            The driverClass to set.
     */
    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }

    /**
     * @return Returns the password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password
     *            The password to set.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return Returns the username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username
     *            The username to set.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return Returns the connectionProperties.
     */
    public ConnectionProperties getConnectionProperties() {
        return connectionProperties;
    }

    /**
     * @param connectionProperties
     *            The connectionProperties to set.
     */
    public void setConnectionProperties(
            ConnectionProperties connectionProperties) {
        this.connectionProperties = connectionProperties;
    }
}
