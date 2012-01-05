/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Database.Connections;

/**
 *
 * @author jayanth
 */
import java.util.ResourceBundle;


/**
 *
 * @author flummoxed
 */
public abstract class DbResourceUtil {

    /**
     *
     * @return
     */
    public static DbProperties getDbProperties() {

		ResourceBundle bundle = ResourceBundle.getBundle("Database.Connections.db");

		String username = bundle.getString("username");
		String password = bundle.getString("password");
		String dbUrl = bundle.getString("db_url");
		String driverClass = bundle.getString("driver_class");
		String minConnections = bundle.getString("min_connections");
		String maxConnections = bundle.getString("max_connections");
		String maxIdleTime = bundle.getString("max_idle_time");

		if (minConnections == null || maxConnections == null) {
			return new DbProperties(username, password, dbUrl, driverClass);
		}
		return new DbProperties(username, password, dbUrl, driverClass,
				new ConnectionProperties(Integer.parseInt(minConnections),
						Integer.parseInt(maxConnections),
						Integer.parseInt(maxIdleTime)));

	}


}
