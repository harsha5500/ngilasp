/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Database.Connections;

/**
 *
 * @author jayanth
 */
public class PoolFactory {

    static boolean useConnectionPooling = true;

    /**
     *
     * @return
     */
    public static dbPooling getPooling() {
        if (useConnectionPooling) {
            return Cp3oPooling.getInstance();
        } else {
            return UnpooledConnectionHandler.getInstance();
        }
    }

//    public static void main(String args[]) {
//        try {
//            Connection connection = getPooling().getConnection();
//            PreparedStatement statement = null;
//            PreparedStatement secondarystatement = null;
//
//            String query = "select * from roads";
//            statement = connection.prepareStatement(query);
//            ResultSet rs = statement.executeQuery();
//            while(rs!=null && rs.next()) {
//                System.out.println(rs.getInt(2));
//            }
//        } catch (SQLException ex) {
//            Logger.getLogger(PoolFactory.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
}
