/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Database.Connections;

/**
 *
 * @author jayanth
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;



/**
 *
 * @author flummoxed
 */
public class UnpooledConnectionHandler implements dbPooling{

    private static UnpooledConnectionHandler handler = null;

	private DbProperties properties = null;

	private UnpooledConnectionHandler() {
		init();
	}

        /**
         *
         * @return
         */
        public synchronized static UnpooledConnectionHandler getInstance() {
		if (handler == null) {
			handler = new UnpooledConnectionHandler();
		}

		return handler;
	}

	private void init() {
		properties = DbResourceUtil.getDbProperties();
		try {
			Class.forName(properties.getDriverClass());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

        /**
         *
         * @return
         * @throws SQLException
         */
        public Connection getConnection() throws SQLException {
		Connection con = DriverManager.getConnection(properties.getDbUrl(),
				properties.getUsername(), properties.getPassword());

		return con;
	}

        /**
         *
         * @param userName
         * @param password
         * @return
         * @throws SQLException
         */
        public Connection getConnection(String userName, String password)
			throws SQLException {
		Connection con = DriverManager.getConnection(properties.getDbUrl(),
				userName, password);

		return con;
	}


}
