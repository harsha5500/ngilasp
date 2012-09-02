/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Database.Connections;

/**
 *
 * @author jayanth
 */

import com.mchange.v2.c3p0.ComboPooledDataSource;
import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @author flummoxed
 */
public class Cp3oPooling implements dbPooling {

    private static ComboPooledDataSource cpds = null;

    private Cp3oPooling() {
        init();
    }
    private static Cp3oPooling cp3oPooling = null;

    /**
     *
     * @return
     */
    public synchronized static Cp3oPooling getInstance() {
        if (cp3oPooling == null) {
            cp3oPooling = new Cp3oPooling();
            System.out.println("should hav come only once...");
        }

        return cp3oPooling;
    }

    private void init() {
        DbProperties properties = DbResourceUtil.getDbProperties();
        cpds = new ComboPooledDataSource();
        try {
            cpds.setDriverClass(properties.getDriverClass());
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }
        // loads the jdbc driver
        cpds.setJdbcUrl(properties.getDbUrl());
        cpds.setUser(properties.getUsername());
        cpds.setPassword(properties.getPassword());


        if (properties.getConnectionProperties() != null) {
            // the settings below are optional -- c3p0 can work with defaults
            cpds.setMinPoolSize(properties.getConnectionProperties().getMinConnections());
            cpds.setAcquireIncrement(10);
            cpds.setMaxPoolSize(properties.getConnectionProperties().getMaxConnections());
            cpds.setMaxIdleTime(properties.getConnectionProperties().getMaxIdleTime());
        } else {
            cpds.setMinPoolSize(DEFAULT_MIN_PROPERTIES);
            cpds.setAcquireIncrement(10);
            cpds.setMaxPoolSize(DEFAULT_MIN_PROPERTIES);
            cpds.setMaxIdleTime(DEFAULT_MAX_IDLE_TIME);

        }

        // The DataSource cpds is now a fully configured and usable pooled
        // DataSource ...
    }

    /**
     *
     * @return
     * @throws SQLException
     */
    public Connection getConnection() throws SQLException {
        return cpds.getConnection();
    }

    public Connection getConnection(String userName, String password)
            throws SQLException {
        return cpds.getConnection(userName, password);
    }
}
