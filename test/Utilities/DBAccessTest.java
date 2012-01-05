/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Utilities;

//import Database.DBAccess;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author flummoxed
 */
public class DBAccessTest {

    public DBAccessTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of ExecQuery method, of class DBAccess.
     */
    @Test
    public void testExecQuery() {
        try {
            System.out.println("ExecQuery");
            DBAccess instance = new DBAccess("192.168.0.23:5432", "disman", "postgres", "cstep123");
            instance.Query = "select id from roads where id<100";
            ResultSet expResult = null;
            ResultSet result = instance.ExecQuery();
            assertEquals(1, result.findColumn("id"));
            // TODO review the generated test code and remove the default call to fail.
            //fail("The test case is a prototype.");
            instance.CloseConnection();
        } catch (SQLException ex) {
            Logger.getLogger(DBAccessTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("SQL Exception");
        }
    }

    /**
     * Test of ExecUpdate method, of class DBAccess.
     */
    @Test
    public void testExecUpdate() {
        System.out.println("ExecUpdate");
        DBAccess instance = new DBAccess("192.168.0.23:5432", "disman", "postgres", "cstep123");
        Integer expResult = null;
        Integer result = instance.ExecUpdate();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
        instance.CloseConnection();
    }

    /**
     * Test of CloseConnection method, of class DBAccess.
     */
    @Test
    public void testCloseConnection() {
        System.out.println("CloseConnection");
        DBAccess instance = new DBAccess("192.168.0.23:5432", "disman", "postgres", "cstep123");
        instance.CloseConnection();
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

}