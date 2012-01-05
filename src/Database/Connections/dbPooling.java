/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Database.Connections;

import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @author jayanth
 */
public interface dbPooling {

    /**
     *
     */
    public static int DEFAULT_MIN_PROPERTIES = 5 ;

    /**
     *
     */
    public static int DEFAULT_MAX_PROPERTIES = 15 ;

    /**
     *
     */
    public static int DEFAULT_MAX_IDLE_TIME = 30 ;//30 SECS

    /**
     *
     * @return
     * @throws SQLException
     */
    public Connection getConnection() throws SQLException;

        /**
         *
         * @param userName
         * @param password
         * @return
         * @throws SQLException
         */
        public Connection getConnection(String userName, String password)
			throws SQLException;

}
