/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import Database.Connections.PoolFactory;
import Entity.IdPointPair;
import Entity.TrafficLight;

import GeographicInformation.Cell;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.postgis.Geometry;
import org.postgis.LineString;
import org.wkb4j.engine.WKBParser;
import org.wkb4j.engine.WKBReader;
import org.wkb4j.postgis.PostGISFactory;
import org.postgis.MultiLineString;
import org.postgis.MultiPoint;
import org.postgis.Point;
import java.util.StringTokenizer;
import org.postgis.Polygon;

/**
 * This class will all sql queries. any objects in the system will call methods of this class to query the database
 * @author sagar
 */
public class Sql {

    private static String roadsOneWay = "roads_one_way";
    /**
     * Query for best path between 2 points
     * to-do write the query in the comments here
     */
    private static String bestPathQuery = "";
    /**
     * Query to check if point is in a polygon
     * to-do write query in comment
     */
    private static String pointInPolygonQuery = "";
    /**
     * Query to check distance between two points
     */
    private static String distanceQuery = "";

    /**
     * This method will get the list of all Traffic Hubs
     * @return Ids of hubs
     */
    public static ArrayList<Integer> getTrafficHubs() {
        Connection conn = null;

        String query = "";

        ArrayList<Integer> hubIds = new ArrayList<Integer>();

        try {
            conn = PoolFactory.getPooling().getConnection();
            PreparedStatement statement = null;

            query = "select * from traffichubs";

            System.out.println("####" + query + "####");

            //System.out.print("\n" + query + "\n");

            statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();


            while (rs != null && rs.next()) {

                hubIds.add(rs.getInt("id"));
            }


        } catch (SQLException ex) {
            //System.out.println("####" + query + "####");
            Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
            }

            Utilities.Log.logger.info("The number of hubs: " + hubIds.size() + hubIds);
            return hubIds;
        }

    }

    /**
     *
     * @param table
     * @param cellId
     * @return
     */
    public static Cell getRoadCell(String table, long cellId) {
        Connection conn = null;
        Cell cell = new Cell();

        try {
            conn = PoolFactory.getPooling().getConnection();
            PreparedStatement statement = null;
            String query = "select * from " + table + " where id =" + cellId;

            Utilities.Log.logger.info("\n" + query + "\n");



            statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            while (rs != null && rs.next()) {


                cell.setId(rs.getLong("id"));
                cell.setRoadId(rs.getLong("roadid"));
                cell.setOccupied(rs.getBoolean("occupied"));
                cell.setSpecial(rs.getBoolean("special"));
                cell.setLatLon(getPointFromWKT(rs.getString("cell")));

            }



        } catch (SQLException ex) {
            Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
            }
            return cell;
        }
    }

    /**
     * Returns true if operation successful, returns false if flag was already set to true by some other means or if the query failed for some reason.
     * @param table 
     * @param cellId 
     * @param isOccupied 
     * @return 
     */
    public static boolean setOccupiedFlag(String table, long cellId, boolean isOccupied) {

        if (isOccupied == true) {
            /*
             * The caller is trying to set the flag to true, check if the flag was already true in database
             */
            if (getRoadCell(table, cellId).isOccupied() == true) {
                //Operation failed, do not do any updates. Return false
                return false;
            }
        }

        Connection conn = null;

        try {
            conn = PoolFactory.getPooling().getConnection();
            PreparedStatement statement = null;
            String query = "update " + table + " set occupied = " + isOccupied + " where id =" + cellId;

            Utilities.Log.logger.info("\n" + query + "\n");

            statement = conn.prepareStatement(query);
            statement.execute();


        } catch (SQLException ex) {
            Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
            }

            return true;

        }

    }

    /**
     * This method will generate a left lane a road in roadsTable
     * @param roadsTable The table containing the original road
     * @param lanesTable The table to store the generated lane
     * @param roadId the id of the road
     * @param distance the distance between road and the lane in cartesian units
     */
    public static void generateLeftLane(String roadsTable, String lanesTable, long roadId, double distance) {

        Connection conn = null;
        try {
            conn = PoolFactory.getPooling().getConnection();
            PreparedStatement statement = null;


            WKBReader reader = new WKBReader();

            /* For this demo we will be using the PostGISFactory.*/
            PostGISFactory factory = new PostGISFactory();

            /* Create the WKBParser.*/
            WKBParser parser = new WKBParser(factory);

            //String query = "select * from roads where gid=" + rs.getInt("vertex_id");

            /* You can complement this query with your own code.*/
            reader.readDataWithPartialQuery(
                    conn,
                    "the_geom",
                    "from " + roadsTable + " WHERE gid =" + roadId,
                    parser);
            //reader.readData(conn, query, parser);

            /* In the PostGISFactory, completed Geometries are stored internally
             * and can be returned through this method.*/
            ArrayList geomlist = factory.getGeometries();

            ArrayList<Point> lanePoints = new ArrayList<Point>();

            ArrayList<Point> newPoints = new ArrayList<Point>();
            MultiLineString multiLineString = null;

            if (geomlist.isEmpty()) {
                return;
            }

            for (int i = 0; i < geomlist.size(); i++) {
                multiLineString = (MultiLineString) geomlist.get(i);

                //System.out.println("\nmultilinestring size :" + multiLineString.numLines());
                LineString[] lineString = multiLineString.getLines();



                for (int j = 0; j < lineString.length; j++) {
                    //Line line = lineString[j].getSubGeometry(j);
                    Point points[] = lineString[j].getPoints();

                    for (int k = 0; k < points.length - 1; k++) {
                        newPoints = generateLeftLanePoint(points[k], points[k + 1], distance);
                        lanePoints.add(newPoints.get(0));
                    }
                    lanePoints.add(newPoints.get(1));
                    insertLane(roadId, lanesTable, lanePoints);
                }



            }

            //insertLane(roadId, lanesTable, lanePoints);

        } catch (SQLException ex) {
            System.out.println("error :" + ex.getMessage());

            Utilities.Log.logger.info(ex.getMessage());
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * This method will insent a lane into lanesTable
     * @param roadId The id of the road to which the lane belongs
     * @param lanesTable The table to store the lane
     * @param points The points of the lane
     */
    public static void insertLane(long roadId, String lanesTable, ArrayList<Point> points) {

        //System.out.println("entered insertLane");

        if (points.size() == 0) {
            return;
        }
        int response = 0;
        String query = "";

        Connection conn = null;

        String values = "";

        Point[] pointsArray = new Point[points.size()];

        for (int i = 0; i < points.size(); i++) {
            pointsArray[i] = points.get(i);
        }



        int lineStringLength = points.size() + -1;


        LineString[] lineStringArray = new LineString[lineStringLength];

        for (int i = 0; i < lineStringLength; i++) {
            Point[] linePoints = new Point[2];
            linePoints[0] = pointsArray[i];
            linePoints[1] = pointsArray[i + 1];
            LineString lineString = new LineString(linePoints);
            lineStringArray[i] = lineString;

        }

        MultiLineString multiLineString = new MultiLineString(lineStringArray);



        values += "(" + roadId + ", geomFromText('" + multiLineString + "'))";


        try {

            //System.out.println("entered try insertLane");

            conn = PoolFactory.getPooling().getConnection();
            PreparedStatement statement = null;

            query = "insert into " + lanesTable + "(roadid, lane) values " + values;

            //System.out.println("####" + query + "####");

            //System.out.print("\n" + query + "\n");

            statement = conn.prepareStatement(query);
            response = statement.executeUpdate();

        } catch (SQLException ex) {
            //System.out.println("####" + query + "####");
            Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                conn.close();
                //System.out.println("exiting insertLane");
            } catch (SQLException ex) {
                Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
            }
        }


    }

    /**
     * 
     * @param start
     * @param end
     * @param distance
     * @return
     */
    public static ArrayList<Point> generateLeftLanePoint(Point start, Point end, double distance) {

        //System.out.println("entered generateLeftLanePoint");

        ArrayList<Point> lanePoints = new ArrayList<Point>();


        if (arePointsEqual(start, end) == true) {
            lanePoints.add(start);
            lanePoints.add(end);
            return lanePoints;
        }

        double dx = start.x - end.x;
        double dy = start.y - end.y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        dx /= dist;
        dy /= dist;
        double x3 = start.x + (distance / 2) * dy;
        double y3 = start.y - (distance / 2) * dx;
        double x4 = end.x + (distance / 2) * dy;
        double y4 = end.y - (distance / 2) * dx;

        Point laneStart = new Point(x3, y3);
        Point laneEnd = new Point(x4, y4);


        lanePoints.add(laneStart);
        lanePoints.add(laneEnd);

        //System.out.println("exiting generateLeftLanePoint " + x3 + "," + y3 + "   " + x4 + "," + y4);

        return lanePoints;

    }

    /**
     * 
     * @param a
     * @param b
     * @return
     */
    public static boolean arePointsEqual(Point a, Point b) {

        double tolerance = 0.000000000000000;
        double diffX = Math.abs(a.x - b.x);

        double diffY = Math.abs(a.y - b.y);

        if ((diffX <= tolerance) && (diffY <= tolerance)) {
            return true;
        } else {
            return false;
        }

    }

    /**
     * This method will get the list of traffic lights given the hubid. One or more traffic lights belong to a hub.
     * @param hubId The id of the hub
     * @return
     */
    public static ArrayList getTrafficLights(long hubId) {

        Connection conn = null;

        String query = "";

        ArrayList<TrafficLight> trafficLights = new ArrayList<TrafficLight>();

        try {
            conn = PoolFactory.getPooling().getConnection();
            PreparedStatement statement = null;

            /* Create the WKBReader.*/
            WKBReader reader = new WKBReader();

            /* For this demo we will be using the PostGISFactory.*/
            PostGISFactory factory = new PostGISFactory();

            /* Create the WKBParser.*/
            WKBParser parser = new WKBParser(factory);

            /* You can complement this query with your own code.*/

            //reader.readData(conn, query, parser);

            /* In the PostGISFactory, completed Geometries are stored internally
             * and can be returned through this method.*/


            query = "select * from trafficlights where hubid =" + hubId;

            System.out.println("####" + query + "####");

            //System.out.print("\n" + query + "\n");

            statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();


            while (rs != null && rs.next()) {
                TrafficLight trafficLight = new TrafficLight();
                trafficLight.setRoadid(rs.getInt("roadid"));
                System.out.println("%%%%%%%%%%% ROADID= " + trafficLight.getRoadid() + "%%%%%%%");
                trafficLight.setDuration(rs.getInt("duration"));
                trafficLight.setColor(rs.getInt("color"));
                trafficLight.setCellid(rs.getLong("cellid"));
                trafficLight.setRoadid(rs.getLong("roadid"));
                reader.readDataWithPartialQuery(
                        conn,
                        "cell",
                        "from lanecells WHERE id =" + rs.getInt("cellid"),
                        parser);
                ArrayList geomlist = factory.getGeometries();
                Point point = (Point) geomlist.get(0);

                String cellQuery = "select * from lanecells where id=" + rs.getInt("cellid");
                PreparedStatement cellFetchStatement = conn.prepareStatement(cellQuery);
                ResultSet rsCell = cellFetchStatement.executeQuery();

                boolean occupied = false;

                if (rsCell != null && rsCell.next()) {
                    occupied = rsCell.getBoolean("occupied");
                }

                trafficLight.setLocation(new Cell(point, occupied));
                trafficLights.add(trafficLight);
            }
        } catch (SQLException ex) {
            //System.out.println("####" + query + "####");
            Utilities.Log.logger.info(ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
            }
            Utilities.Log.logger.info("HUB:" + hubId + "Number of traffic Lights: " + trafficLights.size());
            return trafficLights;
        }
    }

    /**
     *
     * @param tableName 
     * @param roadid
     * @param points
     * @return
     */
    public static int updateCells2(String tableName, long roadid, ArrayList<Point> points) {
        int response = 0;
        String query = "";

        Connection conn = null;


        String values = "";

        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);

            values += "(" + roadid + ", geomFromText('" + point + "'), " + (i + 1) + "),";
        }

        values = values.substring(0, values.length() - 1);

        try {


            //Class.forName("org.postgresql.Driver");
            //String url = "jdbc:postgresql://192.168.0.184/disman";


            //conn = DriverManager.getConnection(url, "postgres", "cstep123");
            conn = PoolFactory.getPooling().getConnection();
            PreparedStatement statement = null;

            query = "insert into " + tableName + "(roadid, cell, sequence) values " + values;

            //System.out.println("####" + query + "####");

            //System.out.print("\n" + query + "\n");

            statement = conn.prepareStatement(query);
            response = statement.executeUpdate();

        } catch (SQLException ex) {
            //System.out.println("####" + query + "####");
            Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return response;

    }

    /**
     *
     * @param id the primary key
     * @param multiPoint the geometry to be inserted
     * @return the response of the query from the database
     */
    public static int updateCells(long id, MultiPoint multiPoint) {
        int response = 0;
        String query = "";

        Connection conn = null;

        if (multiPoint.isEmpty()) {
            return 0;
        }
        try {


            //Class.forName("org.postgresql.Driver");
            //String url = "jdbc:postgresql://192.168.0.184/disman";


            //conn = DriverManager.getConnection(url, "postgres", "cstep123");
            conn = PoolFactory.getPooling().getConnection();
            PreparedStatement statement = null;

            query = "update roads2 set cells=GeomFromText('" + multiPoint + "') where gid=" + id;

            //System.out.print("\n" + query + "\n");

            statement = conn.prepareStatement(query);
            response = statement.executeUpdate();

        } catch (SQLException ex) {
            System.out.println("####" + query + "####");
            Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return response;

    }

    /**
     * The method will select a vertext close to the goven point.
     * @param point The point of reference.
     * @return The id of the vertext
     */
    public static long getVertexFromPoint(Point point) {

        long id = -1;

        Connection conn = null;

        try {
            conn = PoolFactory.getPooling().getConnection();
            PreparedStatement statement = null;
            bestPathQuery = "SELECT id FROM vertices_tmp where true = (select ST_Equals(the_geom, GeomFromText('" + point + "')))";

            System.out.print("\n" + bestPathQuery + "\n");

            statement = conn.prepareStatement(bestPathQuery);
            ResultSet rs = statement.executeQuery();



            while (rs != null && rs.next()) {

                id = rs.getInt("id");
                System.out.print("\n rs " + id + "\n");
            }

        } catch (SQLException ex) {
            Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return id;
    }

    /**
     * This method will find the best path and return the cells 
     * @param source
     * @param destination
     * @return
     */
    public static ArrayList<Cell> getBestPathCells(Point source, Point destination) {
        ArrayList<Cell> path = new ArrayList<Cell>();

        Connection conn = null;
        try {
            conn = PoolFactory.getPooling().getConnection();
            PreparedStatement statement = null;


            bestPathQuery = "SELECT * FROM shortest_path('SELECT gid AS id, source::int4,"
                    + "target::int4, length::double precision AS cost, length::double precision "
                    + "AS reverse_cost FROM roads2', " + getVertexFromPoint(source) + "," + getVertexFromPoint(destination) + ", true, true)";

            System.out.print("\n" + bestPathQuery + "\n");

            statement = conn.prepareStatement(bestPathQuery);
            ResultSet rs = statement.executeQuery();

            while (rs != null && rs.next()) {
                //System.out.print("\nrs " + rs.getInt("vertex_id"));
                /* Create the WKBReader.*/
                WKBReader reader = new WKBReader();

                /* For this demo we will be using the PostGISFactory.*/
                PostGISFactory factory = new PostGISFactory();

                /* Create the WKBParser.*/
                WKBParser parser = new WKBParser(factory);

                //String query = "select * from roads where gid=" + rs.getInt("vertex_id");

                /* You can complement this query with your own code.*/
                reader.readDataWithPartialQuery(
                        conn,
                        "cells",
                        "from roads2 WHERE gid =" + rs.getInt("edge_id"),
                        parser);
                //reader.readData(conn, query, parser);

                /* In the PostGISFactory, completed Geometries are stored internally
                 * and can be returned through this method.*/
                ArrayList geomlist = factory.getGeometries();

                for (int i = 0; i < geomlist.size(); i++) {
                    MultiPoint multiPoint = (MultiPoint) geomlist.get(i);

                    for (int j = 0; j < multiPoint.numPoints(); j++) {

                        Point point = multiPoint.getPoint(j);
                        Cell cell = new Cell(point, true);
                        path.add(cell);
                    }
                }
            }


        } catch (SQLException ex) {
            Utilities.Log.logger.info(ex.getMessage());
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return path;

    }

    /**
     *
     * @param source
     * @param destination
     * @return
     */
    public static ArrayList<Cell> getBestPathCells2(Point source, Point destination) {
        ArrayList<Cell> path = new ArrayList<Cell>();

        Connection conn = null;
        try {
            conn = PoolFactory.getPooling().getConnection();
            PreparedStatement statement = null;


            bestPathQuery = "SELECT * FROM shortest_path('SELECT gid AS id, source::int4,"
                    + "target::int4, length::double precision AS cost, length::double precision "
                    + "AS reverse_cost FROM roads2', " + getVertexFromPoint(source) + "," + getVertexFromPoint(destination) + ", true, true)";

            System.out.print("\n" + bestPathQuery + "\n");

            statement = conn.prepareStatement(bestPathQuery);
            ResultSet rs = statement.executeQuery();

            while (rs != null && rs.next()) {
                System.out.print("\nrs " + rs.getInt("vertex_id"));
                /* Create the WKBReader.*/
                WKBReader reader = new WKBReader();

                /* For this demo we will be using the PostGISFactory.*/
                PostGISFactory factory = new PostGISFactory();

                /* Create the WKBParser.*/
                WKBParser parser = new WKBParser(factory);

                //String query = "select * from roads where gid=" + rs.getInt("vertex_id");

                /* You can complement this query with your own code.*/
                reader.readDataWithPartialQuery(
                        conn,
                        "cells",
                        "from roads2 WHERE gid =" + rs.getInt("edge_id"),
                        parser);
                //reader.readData(conn, query, parser);

                /* In the PostGISFactory, completed Geometries are stored internally
                 * and can be returned through this method.*/
                ArrayList geomlist = factory.getGeometries();

                for (int i = 0; i < geomlist.size(); i++) {
                    MultiPoint multiPoint = (MultiPoint) geomlist.get(i);

                    for (int j = 0; j < multiPoint.numPoints(); j++) {

                        Point point = multiPoint.getPoint(j);
                        Cell cell = new Cell(point, true);
                        path.add(cell);
                    }
                }
            }


        } catch (SQLException ex) {
            Utilities.Log.logger.info(ex.getMessage());
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return path;

    }

    /**
     * Returns an arraylist of geographical locations from source to destination
     * @param sourceId
     * @param destinationId 
     * @return
     */
    public static ArrayList<Cell> getBestPath(long sourceId, long destinationId) {
        ArrayList<Cell> path = new ArrayList<Cell>();

        Connection conn = null;
        try {
            conn = PoolFactory.getPooling().getConnection();
            PreparedStatement statement = null;


            bestPathQuery = "SELECT * FROM shortest_path('SELECT gid AS id, source::int4,"
                    + "target::int4, length::double precision AS cost, length::double precision "
                    + "AS reverse_cost FROM roads2', " + sourceId + "," + destinationId + ", true, true)";

            System.out.print("\n" + bestPathQuery + "\n");

            statement = conn.prepareStatement(bestPathQuery, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = statement.executeQuery();

            ArrayList<Long> vertextList = new ArrayList<Long>();

            int size = 0;

            while (rs.next()) {
                size++;
            }

            rs.beforeFirst();
            ;

            for (int i = 0; i < size - 1; i++) {
                rs.next();
                long vertex = rs.getLong("vertex_id");
                vertextList.add(vertex);

            }

            ArrayList<Long> edges = getEdgesFromVertices(vertextList, roadsOneWay);

            for (int i = 0; i < edges.size(); i++) {

                System.out.print("edge " + edges.get(i));

                WKBReader reader = new WKBReader();

                /* For this demo we will be using the PostGISFactory.*/
                PostGISFactory factory = new PostGISFactory();

                /* Create the WKBParser.*/
                WKBParser parser = new WKBParser(factory);

                //String query = "select * from roads where gid=" + rs.getInt("vertex_id");

                /* You can complement this query with your own code.*/
                reader.readDataWithPartialQuery(
                        conn,
                        "cell",
                        "from lanecells WHERE roadid =" + edges.get(i),
                        parser);
                //reader.readData(conn, query, parser);

                /* In the PostGISFactory, completed Geometries are stored internally
                 * and can be returned through this method.*/
                ArrayList geomlist = factory.getGeometries();

                for (int j = 0; j < geomlist.size(); j++) {
                    Point point = (Point) geomlist.get(j);

                    Cell cell = new Cell(point, true);
                    path.add(cell);

                }


            }


        } catch (SQLException ex) {
            Utilities.Log.logger.info(ex.getMessage());
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
            }
            return path;
        }


    }

    /**
     * 
     * @param sourceId
     * @param destinationId
     * @return
     */
    public static ArrayList<Cell> getBestPath2(long sourceId, long destinationId) {
        ArrayList<Cell> path = new ArrayList<Cell>();

        Connection conn = null;
        try {
            conn = PoolFactory.getPooling().getConnection();
            PreparedStatement statement = null;


            bestPathQuery = "SELECT * FROM shortest_path('SELECT gid AS id, source::int4,"
                    + "target::int4, length::double precision AS cost, length::double precision "
                    + "AS reverse_cost FROM roads2', " + sourceId + "," + destinationId + ", true, true)";

            System.out.print("\n" + bestPathQuery + "\n");

            statement = conn.prepareStatement(bestPathQuery);
            ResultSet rs = statement.executeQuery();

//            String cellIdList = "";


            while (rs != null && rs.next()) {

//                cellIdList = cellIdList + " or roadid = " + rs.getInt("edge_id");

                //edgeList += rs.getInt("edge_id") + ",";

                WKBReader reader = new WKBReader();

                /* For this demo we will be using the PostGISFactory.*/
                PostGISFactory factory = new PostGISFactory();

                /* Create the WKBParser.*/
                WKBParser parser = new WKBParser(factory);

                //String query = "select * from roads where gid=" + rs.getInt("vertex_id");

                /* You can complement this query with your own code.*/
                reader.readDataWithPartialQuery(
                        conn,
                        "cell",
                        "from lanecells WHERE roadid =" + rs.getInt("edge_id"),
                        parser);
                //reader.readData(conn, query, parser);

                /* In the PostGISFactory, completed Geometries are stored internally
                 * and can be returned through this method.*/
                ArrayList geomlist = factory.getGeometries();

                for (int i = 0; i < geomlist.size(); i++) {
                    Point point = (Point) geomlist.get(i);

                    Cell cell = new Cell(point, true);
                    path.add(cell);

                }

            }

//            cellIdList = cellIdList.substring(3, cellIdList.length());
//
//            System.out.println("\n\n" + cellIdList +  "\n ");
//
//             WKBReader reader = new WKBReader();
//
//                /* For this demo we will be using the PostGISFactory.*/
//                PostGISFactory factory = new PostGISFactory();
//
//                /* Create the WKBParser.*/
//                WKBParser parser = new WKBParser(factory);
//
//                //String query = "select * from roads where gid=" + rs.getInt("vertex_id");
//
//                /* You can complement this query with your own code.*/
//                reader.readDataWithPartialQuery(
//                        conn,
//                        "cell",
//                        "from cells WHERE " + cellIdList,
//                        parser);
//                //reader.readData(conn, query, parser);
//
//                /* In the PostGISFactory, completed Geometries are stored internally
//                 * and can be returned through this method.*/
//                ArrayList geomlist = factory.getGeometries();
//
//                for (int i = 0; i < geomlist.size(); i++) {
//                    Point point = (Point) geomlist.get(i);
//
//                    Cell cell = new Cell(point, true);
//                    path.add(cell);
//
//                }



            //edgeList = edgeList.substring(0, edgeList.length() - 4);

            //edgeList += ")";

            /* Create the WKBReader.*/



        } catch (SQLException ex) {
            Utilities.Log.logger.info(ex.getMessage());
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return path;

    }

    /**
     * Returns an arraylist of geographical locations from source to destination
     * @param sourceId
     * @param destinationId
     * @return
     */
    public static ArrayList<Cell> getBestPathForPerson2(long sourceId, long destinationId) {
        ArrayList<Cell> path = new ArrayList<Cell>();

        Connection conn = null;
        try {
            conn = PoolFactory.getPooling().getConnection();
            PreparedStatement statement = null;


            bestPathQuery = "SELECT * FROM shortest_path('SELECT gid AS id, source::int4,"
                    + "target::int4, length::double precision AS cost, length::double precision "
                    + "AS reverse_cost FROM roads2', " + sourceId + "," + destinationId + ", true, true)";

            System.out.print("\n" + bestPathQuery + "\n");

            statement = conn.prepareStatement(bestPathQuery);
            ResultSet rs = statement.executeQuery();

//            String cellIdList = "";


            while (rs != null && rs.next()) {

//                cellIdList = cellIdList + " or roadid = " + rs.getInt("edge_id");

                //edgeList += rs.getInt("edge_id") + ",";

                WKBReader reader = new WKBReader();

                /* For this demo we will be using the PostGISFactory.*/
                PostGISFactory factory = new PostGISFactory();

                /* Create the WKBParser.*/
                WKBParser parser = new WKBParser(factory);

                //String query = "select * from roads where gid=" + rs.getInt("vertex_id");

                /* You can complement this query with your own code.*/
                reader.readDataWithPartialQuery(
                        conn,
                        "cell",
                        "from foothpathcells WHERE roadid =" + rs.getInt("edge_id"),
                        parser);
                //reader.readData(conn, query, parser);

                /* In the PostGISFactory, completed Geometries are stored internally
                 * and can be returned through this method.*/
                ArrayList geomlist = factory.getGeometries();

                for (int i = 0; i < geomlist.size(); i++) {
                    Point point = (Point) geomlist.get(i);
                    

                    Cell cell = new Cell(point, true);
                    path.add(cell);

                }

            }

//            cellIdList = cellIdList.substring(3, cellIdList.length());
//
//            System.out.println("\n\n" + cellIdList +  "\n ");
//
//             WKBReader reader = new WKBReader();
//
//                /* For this demo we will be using the PostGISFactory.*/
//                PostGISFactory factory = new PostGISFactory();
//
//                /* Create the WKBParser.*/
//                WKBParser parser = new WKBParser(factory);
//
//                //String query = "select * from roads where gid=" + rs.getInt("vertex_id");
//
//                /* You can complement this query with your own code.*/
//                reader.readDataWithPartialQuery(
//                        conn,
//                        "cell",
//                        "from cells WHERE " + cellIdList,
//                        parser);
//                //reader.readData(conn, query, parser);
//
//                /* In the PostGISFactory, completed Geometries are stored internally
//                 * and can be returned through this method.*/
//                ArrayList geomlist = factory.getGeometries();
//
//                for (int i = 0; i < geomlist.size(); i++) {
//                    Point point = (Point) geomlist.get(i);
//
//                    Cell cell = new Cell(point, true);
//                    path.add(cell);
//
//                }



            //edgeList = edgeList.substring(0, edgeList.length() - 4);

            //edgeList += ")";

            /* Create the WKBReader.*/



        } catch (SQLException ex) {
            Utilities.Log.logger.info(ex.getMessage());
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return path;

    }

    /**
     * This method will get a source and targer for a given road.
     * @param roadId The road id
     * @param tableName Name of the road table
     * @return
     */
    public static ArrayList<Long> getSourceTargetVertices(long roadId, String tableName) {

        ArrayList<Long> vertextList = new ArrayList<Long>();
        double distance = 0;
        Connection conn = null;
        try {
            conn = PoolFactory.getPooling().getConnection();
            PreparedStatement statement = null;

            String query = "select source, target from " + tableName + " where gid = " + roadId;

            System.out.println("\nquery: " + query);
            statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();


            if (rs.next()) {
                Long source = rs.getLong("source");
                Long target = rs.getLong("target");
                vertextList.add(source);
                vertextList.add(target);
            }

        } catch (SQLException ex) {
            Utilities.Log.logger.info(ex.getMessage());
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return vertextList;

    }

    /**
     * This method will get a road given its source and targer ids.
     * @param source The source/start id
     * @param target The target/destination id
     * @param tableName The name of the roads table
     * @return
     */
    public static long getRoadFromSourceTarget(long source, long target, String tableName) {
        long id = -1;

        Connection conn = null;
        try {
            conn = PoolFactory.getPooling().getConnection();
            PreparedStatement statement = null;

            String query = "select gid from " + tableName + " where source = " + source + " and target = " + target;

            //System.out.println("\nquery: " + query);
            statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();


            if (rs.next()) {
                id = rs.getLong("gid");
            }

        } catch (SQLException ex) {
            Utilities.Log.logger.info(ex.getMessage());
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return id;
    }

    /**
     * This method will return best path between nodes given by sourceId and destinationId.
     * @param sourceId Vertext id of source node
     * @param destinationId Vertext id of destination node
     * @param costColumn The column containing cost to traverse the road segment.
     * @param cellTable The table which contains cells
     * @return
     */
    public static ArrayList<Cell> getBestPathForPerson(long sourceId, long destinationId, String costColumn, String cellTable) {
        ArrayList<Cell> path = new ArrayList<Cell>();

        Connection conn = null;
        try {
            conn = PoolFactory.getPooling().getConnection();
            PreparedStatement statement = null;


            bestPathQuery = "SELECT * FROM shortest_path('SELECT gid AS id, source::int4,"
                    + "target::int4, " + costColumn + "::double precision AS cost, " + costColumn + "::double precision "
                    + "AS reverse_cost FROM roads2', " + sourceId + "," + destinationId + ", true, true)";

            Utilities.Log.logger.info("\n" + bestPathQuery + "\n");

            statement = conn.prepareStatement(bestPathQuery, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = statement.executeQuery();

            ArrayList<Long> vertextList = new ArrayList<Long>();

            int size = 0;

            while (rs.next()) {
                size++;
            }

            rs.beforeFirst();

            double cost = 0.0;

            for (int i = 0; i < size - 1; i++) {
                rs.next();
                long vertex = rs.getLong("vertex_id");
                cost += rs.getDouble("cost");
                vertextList.add(vertex);

            }

            System.out.println("cost = " + cost);

            ArrayList<Long> edges = getEdgesFromVertices(vertextList, roadsOneWay);

            for (int i = 0; i < edges.size(); i++) {


                ArrayList<Cell> cellList = getCellsForRoad(cellTable, edges.get(i));

                path.addAll(cellList);

//                //System.out.print("edge " + edges.get(i));
//
//                WKBReader reader = new WKBReader();
//
//                /* For this demo we will be using the PostGISFactory.*/
//                PostGISFactory factory = new PostGISFactory();
//
//                /* Create the WKBParser.*/
//                WKBParser parser = new WKBParser(factory);
//
//                //String query = "select * from roads where gid=" + rs.getInt("vertex_id");
//
//                /* You can complement this query with your own code.*/
//                reader.readDataWithPartialQuery(
//                        conn,
//                        "cell",
//                        "from " + cellTable + " WHERE roadid =" + edges.get(i),
//                        parser);
//                //reader.readData(conn, query, parser);
//
//                /* In the PostGISFactory, completed Geometries are stored internally
//                 * and can be returned through this method.*/
//                ArrayList geomlist = factory.getGeometries();
//
//                for (int j = 0; j < geomlist.size(); j++) {
//                    Point point = (Point) geomlist.get(j);
//
//                    Cell cell = new Cell(point, true);
//                    path.add(cell);
//
//                }


            }



        } catch (SQLException ex) {
            Utilities.Log.logger.info(ex.getMessage());
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
            }
            return path;
        }


    }

    /**
     * 
     * @param point
     * @return
     */
    public static long getNearestPointOnLine(Point point) {

        long source = -1;


        Connection conn = null;
        try {
            conn = PoolFactory.getPooling().getConnection();
            PreparedStatement statement = null;


            String query = "SELECT source, (SELECT ST_distance(GeomFromText('" + point + "'), the_geom)) as dist from roads2 order by dist asc limit 1";


            System.out.print("\n" + query + "\n");

            statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                source = rs.getLong("source");
            }





        } catch (SQLException ex) {
            Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
            }
            return source;
        }

    }

    /**
     * This method will return best path between two points.
     * @param source Source point
     * @param destination Destination point
     * @param costColumn The column containing cost to traverse the road segment.
     * @param cellTable The table which contains cells
     * @return
     */
    public static ArrayList<Cell> getBestPathForPerson(Point source, Point destination, String costColumn, String cellTable) {
        ArrayList<Cell> path = new ArrayList<Cell>();

        Connection conn = null;
        try {
            conn = PoolFactory.getPooling().getConnection();
            PreparedStatement statement = null;

            System.out.println(source.x + " " + source.y + "\n" + destination.x + " " + destination.y);
            long sourceId = getNearestPointOnLine(source);
            long destinationId = getNearestPointOnLine(destination);

            bestPathQuery = "SELECT * FROM shortest_path('SELECT gid AS id, source::int4,"
                    + "target::int4, " + costColumn + "::double precision AS cost, " + costColumn + "::double precision "
                    + "AS reverse_cost FROM roads2', " + sourceId + "," + destinationId + ", true, true)";

            System.out.print("\n" + bestPathQuery + "\n");

            statement = conn.prepareStatement(bestPathQuery, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = statement.executeQuery();

            ArrayList<Long> vertextList = new ArrayList<Long>();

            int size = 0;

            while (rs.next()) {
                size++;
            }

            rs.beforeFirst();

            double cost = 0.0;

            for (int i = 0; i < size - 1; i++) {
                rs.next();
                long vertex = rs.getLong("vertex_id");
                cost += rs.getDouble("cost");
                vertextList.add(vertex);

            }

            System.out.println("cost = " + cost);

            ArrayList<Long> edges = getEdgesFromVertices(vertextList, roadsOneWay);

            for (int i = 0; i < edges.size(); i++) {


                ArrayList<Cell> cellList = getCellsForRoad(cellTable, edges.get(i));

                path.addAll(cellList);

//                //System.out.print("edge " + edges.get(i));
//
//                WKBReader reader = new WKBReader();
//
//                /* For this demo we will be using the PostGISFactory.*/
//                PostGISFactory factory = new PostGISFactory();
//
//                /* Create the WKBParser.*/
//                WKBParser parser = new WKBParser(factory);
//
//                //String query = "select * from roads where gid=" + rs.getInt("vertex_id");
//
//                /* You can complement this query with your own code.*/
//                reader.readDataWithPartialQuery(
//                        conn,
//                        "cell",
//                        "from " + cellTable + " WHERE roadid =" + edges.get(i),
//                        parser);
//                //reader.readData(conn, query, parser);
//
//                /* In the PostGISFactory, completed Geometries are stored internally
//                 * and can be returned through this method.*/
//                ArrayList geomlist = factory.getGeometries();
//
//                for (int j = 0; j < geomlist.size(); j++) {
//                    Point point = (Point) geomlist.get(j);
//
//                    Cell cell = new Cell(point, true);
//                    path.add(cell);
//
//                }


            }



        } catch (SQLException ex) {
            Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);

        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
            }
            return path;
        }


    }

    /**
     * Fethches a roads/edges given one of thier end vertices
     * @param verticeList a lits of vertice ids
     * @param tableName Table of roads/edges
     * @return
     */
    public static ArrayList<Long> getEdgesFromVertices(ArrayList<Long> verticeList, String tableName) {

        ArrayList<Long> edgeList = new ArrayList<Long>();

        Connection conn = null;
        try {
            conn = PoolFactory.getPooling().getConnection();
            PreparedStatement statement = null;

            for (int i = 0; i < verticeList.size(); i++) {

                long source = verticeList.get(i);
                long target = verticeList.get(i + 1);

                long edge = getRoadFromSourceTarget(source, target, tableName);

                edgeList.add(edge);
            }

        } catch (SQLException ex) {
            Utilities.Log.logger.info(ex.getMessage());
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
            }
            return edgeList;
        }


    }

    /**
     * 
     * @param source
     * @param destinationId
     * @param costColumn
     * @param cellTable
     * @return
     */
    public static ArrayList<Cell> getBestPathForPerson(Point source, long destinationId, String costColumn, String cellTable) {
        ArrayList<Cell> path = new ArrayList<Cell>();

        Connection conn = null;
        try {
            conn = PoolFactory.getPooling().getConnection();
            PreparedStatement statement = null;

            long sourceId = getNearestPointOnLine(source);


            bestPathQuery = "SELECT * FROM shortest_path('SELECT gid AS id, source::int4,"
                    + "target::int4, " + costColumn + "::double precision AS cost, " + costColumn + "::double precision "
                    + "AS reverse_cost FROM roads2', " + sourceId + "," + destinationId + ", true, true)";

            System.out.print("\n" + bestPathQuery + "\n");

            statement = conn.prepareStatement(bestPathQuery, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = statement.executeQuery();

            ArrayList<Long> vertextList = new ArrayList<Long>();

            int size = 0;

            while (rs.next()) {
                size++;
            }

            rs.beforeFirst();

            double cost = 0.0;

            for (int i = 0; i < size - 1; i++) {
                rs.next();
                long vertex = rs.getLong("vertex_id");
                cost += rs.getDouble("cost");
                vertextList.add(vertex);

            }

            System.out.println("cost = " + cost);

            ArrayList<Long> edges = getEdgesFromVertices(vertextList, roadsOneWay);

            for (int i = 0; i < edges.size(); i++) {

//                //System.out.print("edge " + edges.get(i));
//
//                WKBReader reader = new WKBReader();
//
//                /* For this demo we will be using the PostGISFactory.*/
//                PostGISFactory factory = new PostGISFactory();
//
//                /* Create the WKBParser.*/
//                WKBParser parser = new WKBParser(factory);
//
//                //String query = "select * from roads where gid=" + rs.getInt("vertex_id");
//
//                /* You can complement this query with your own code.*/
//                reader.readDataWithPartialQuery(
//                        conn,
//                        "cell",
//                        "from " + cellTable + " WHERE roadid =" + edges.get(i),
//                        parser);

                ArrayList<Cell> cellList = getCellsForRoad(cellTable, edges.get(i));

                path.addAll(cellList);
                //reader.readData(conn, query, parser);

                /* In the PostGISFactory, completed Geometries are stored internally
                 * and can be returned through this method.*/
//                ArrayList geomlist = factory.getGeometries();
//
//                for (int j = 0; j < geomlist.size(); j++) {
//                    Point point = (Point) geomlist.get(j);
//
//                    Cell cell = new Cell(point, true);
//                    path.add(cell);
//
//                }


            }



        } catch (SQLException ex) {
            Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);

        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
            }
            return path;
        }


    }

    private static ArrayList<Cell> getCellsForRoad(String cellTable, long roadId) {

        ArrayList<Cell> cellList = new ArrayList<Cell>();

        Connection conn = null;
        try {
            conn = PoolFactory.getPooling().getConnection();
            PreparedStatement statement = null;


            String query = "SELECT id, asewkt(cell) as point, occupied, special, sequence, roadid from lanecells where roadid = " + roadId;


            //System.out.print("\n" + query + "\n");

            statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                Cell cell = new Cell();
                cell.setId(rs.getLong("id"));
                cell.setRoadId(rs.getLong("roadid"));
                cell.setOccupied(rs.getBoolean("occupied"));
                cell.setSpecial(rs.getBoolean("special"));
                cell.setLatLon(getPointFromWKT(rs.getString("point")));
                cellList.add(cell);
            }


        } catch (SQLException ex) {
            Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
            }

            return cellList;

        }

    }

    /**
     * 
     * @param pointWKT
     * @return
     */
    public static Point getPointFromWKT(String pointWKT) {

        String pointStr = pointWKT.substring(6, pointWKT.length() - 1);

        StringTokenizer tokenizer = new StringTokenizer(pointStr, " ");
        double lon = Double.parseDouble(tokenizer.nextToken());
        double lat = Double.parseDouble(tokenizer.nextToken());

        Point point = new Point(lon, lat);
        //System.out.println(point.x + " " + point.y);
        return point;



    }

//    public static ArrayList<Long> getEdgesFromVertices(ArrayList<Long> verticeList, String tableName) {
//
//        ArrayList<Long> edgeList = new ArrayList<Long>();
//
//        Connection conn = null;
//        try {
//            conn = PoolFactory.getPooling().getConnection();
//            PreparedStatement statement = null;
//
//            for (int i = 0; i < verticeList.size(); i++) {
//
//                long source = verticeList.get(i);
//                long target = verticeList.get(i + 1);
//
//                long edge = getRoadFromSourceTarget(source, target, tableName);
//
//                edgeList.add(edge);
//            }
//
//        } catch (SQLException ex) {
//            Utilities.Log.logger.info(ex.getMessage());
//        } finally {
//            try {
//                conn.close();
//            } catch (SQLException ex) {
//                Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            return edgeList;
//        }
//
//
//    }
    /**
     * This method wil inverted the direction of a road and store it in the table.
     * @param sourceTable Table of roads
     * @param geomColumnName The geometry column
     * @param roadId The id of the road
     */
    public static void invertRoads(String sourceTable, String geomColumnName, long roadId) {

        Connection conn = null;
        try {
            conn = PoolFactory.getPooling().getConnection();
            PreparedStatement statement = null;


            WKBReader reader = new WKBReader();

            /* For this demo we will be using the PostGISFactory.*/
            PostGISFactory factory = new PostGISFactory();

            /* Create the WKBParser.*/
            WKBParser parser = new WKBParser(factory);

            //String query = "select * from roads where gid=" + rs.getInt("vertex_id");

            /* You can complement this query with your own code.*/
            reader.readDataWithPartialQuery(
                    conn,
                    geomColumnName,
                    "from " + sourceTable + " WHERE gid =" + roadId,
                    parser);
            //reader.readData(conn, query, parser);


            String query = "select source, target from roads2 where gid =" + roadId;

            System.out.println("\nquery: " + query);
            statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();


            rs.next();
            int source = rs.getInt("source");
            int target = rs.getInt("target");

            /* In the PostGISFactory, completed Geometries are stored internally
             * and can be returned through this method.*/
            ArrayList geomlist = factory.getGeometries();

            for (int i = 0; i < geomlist.size(); i++) {
                MultiLineString multiLineString = (MultiLineString) geomlist.get(i);


                LineString[] lineStrings = new LineString[multiLineString.numLines()];

                for (int j = 0; j < multiLineString.numLines(); j++) {
                    LineString lineString = multiLineString.getLine(j);
                    LineString reverseLineString = lineString.reverse();
                    lineStrings[multiLineString.numLines() - j - 1] = reverseLineString;
                }

                MultiLineString reverseMultiLineString = new MultiLineString(lineStrings);



                String insertQuery = "insert into " + sourceTable + "(" + geomColumnName + ", originalroad, source, target) values(GeomFromText('" + reverseMultiLineString + "')," + roadId + "," + target + "," + source + " )";

                System.out.print("\n" + insertQuery + "\n");

                statement = conn.prepareStatement(insertQuery);
                statement.execute();

            }



            //edgeList = edgeList.substring(0, edgeList.length() - 4);

            //edgeList += ")";

            /* Create the WKBReader.*/



        } catch (SQLException ex) {
            System.out.println(ex.getMessage());

            Utilities.Log.logger.info(ex.getMessage());
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
            }
        }


    }

    /**
     *
     * @param geom1
     * @param geom2
     * @return
     */
    public static double distanceBetween(Geometry geom1, Geometry geom2) {
        double distance = 0;
        Connection conn = null;
        try {
            conn = PoolFactory.getPooling().getConnection();
            PreparedStatement statement = null;

            String query = "select ST_distance(GeomFromText('" + geom1 + "'), GeomFromText('" + geom2 + "'))";

            System.out.println("\nquery: " + query);
            statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();


            while (rs != null && rs.next()) {
                distance = rs.getDouble("st_distance");
            }

        } catch (SQLException ex) {
            Utilities.Log.logger.info(ex.getMessage());
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return distance;
    }

    /**
     *
     * @param args
     */
    @SuppressWarnings("static-access")
    public static void main(String[] args) {



        Sql sql = new Sql();
        Utilities.Log.ConfigureLogger();

        sql.setOccupiedFlag("lanecells", 2, true);

//        ArrayList<Cell> cellList = getBestPathForPerson(1, 40, "cost", "lanecells");
//
//
//        for (int i = 0; i < cellList.size(); i++) {
//            System.out.println(cellList.get(i).getId());
//        }


        //sql.getPointFromWKT("POINT(77.6069183349609 12.9695053100586)");

//
//        Point[] point = new Point[5];
//        point[0] = new Point(75, 15);
//        point[1] = new Point(79, 15);
//        point[2] = new Point(79, 12);
//        point[3] = new Point(75, 12);
//        point[4] = point[0];
//
//        LinearRing[] linearRing = new LinearRing[1];
//        linearRing[0] = new LinearRing(point);
//        Polygon polygon = new Polygon(linearRing);


        //sql.createGeomFromLatLon("diffuse_radiation_india", "latitude", "longitude");


        //       System.out.println(sql.getPointsWithinBoundingBox("police", polygon).size());
//
//        for(int i=0;i<10;i++){
//            System.out.println("hub id :" +sql.generateTrafficLightHub("traffichubs", "trafficlights", "roads2", "vertices_tmp", "lanecells"));
//        }
//          Point source = new Point(77.57932281, 12.91317931);
//          Point destination= new Point( 77.57843270, 12.91317832);
//          sql.getBestPathForPerson(source, destination, Constants.COST_COLUMN_REGULAR_VEHICLE, Constants.TABLE_PEOPLE_CELLS);

//        System.out.println("hub id :" +sql.generateTrafficLightHub("traffichubs", "trafficlights", "roads2", "vertices_tmp", "lanecells"));
//
//        Utilities.Log.ConfigureLogger();
//        long beginID = 1;
//        long endID = 2844;
//
//        long startTime = System.nanoTime();
//        for (long i = beginID; i <= endID; i++) {
//            System.out.println("road id :" + i);
//            //generateLeftLane("roads_one_way", "lanes", i, 2.81E-6);




//            invertRoads("roads_one_way", "the_geom", i);
//
////
//        long beginID = 2;
//        long endID = 2465;
//
//
//        for(long i = beginID; i <= endID; i++) {
//            System.out.println("road id :" + i);
//            //generateLeftLane("roads_one_way", "lanes", i, 1.81E-6);
////            splitIntoCells("foothpaths", "lane", "foothpathcells", i);
//            //invertRoads("roads_one_way", "the_geom", i);
//            splitIntoCells("lanes", "lane", "lanecells", i);
//        }


    }

    /**
     *
     * @return
     */
    public static IdPointPair getIdPointPairOnRoad() {


        IdPointPair idPointPair = new IdPointPair();

        Point firstPoint = null;
        Connection conn = null;

        try {
            conn = PoolFactory.getPooling().getConnection();
            PreparedStatement statement = null;



            /* Create the WKBReader.*/
            WKBReader reader = new WKBReader();

            /* For this demo we will be using the PostGISFactory.*/
            PostGISFactory factory = new PostGISFactory();

            /* Create the WKBParser.*/
            WKBParser parser = new WKBParser(factory);


            long randomRow = Math.round(Math.random() * 800) + 1;

            System.out.println("randomRow : " + randomRow);
            //String query = "select id from vertices_tmp WHERE id =" + randomRow;

            /* You can complement this query with your own code.*/
            reader.readDataWithPartialQuery(
                    conn,
                    "the_geom",
                    "from vertices_tmp WHERE id =" + randomRow,
                    parser);
            //reader.readData(conn, query, parser);

            /* In the PostGISFactory, completed Geometries are stored internally
             * and can be returned through this method.*/
            ArrayList geomlist = factory.getGeometries();



            firstPoint = (Point) geomlist.get(0);

            idPointPair.id = randomRow;

            idPointPair.point = firstPoint;


        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            try {
                conn.close();

            } catch (SQLException ex) {
                Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
            }
        }


        return idPointPair;
    }

    /**
     *
     * @return
     */
    public static Point getPointOnRoad() {


        Point firstPoint = null;
        Connection conn = null;

        try {
            conn = PoolFactory.getPooling().getConnection();
            PreparedStatement statement = null;



            /* Create the WKBReader.*/ PoolFactory.getPooling().getConnection();
            WKBReader reader = new WKBReader();

            /* For this demo we will be using the PostGISFactory.*/
            PostGISFactory factory = new PostGISFactory();

            /* Create the WKBParser.*/
            WKBParser parser = new WKBParser(factory);


            long randomRow = Math.round(Math.random() * 10);

            System.out.println("randomRow : " + randomRow);
            //String query = "select id from vertices_tmp WHERE id =" + randomRow;

            /* You can complement this query with your own code.*/
            reader.readDataWithPartialQuery(
                    conn,
                    "the_geom",
                    "from vertices_tmp WHERE id =" + randomRow,
                    parser);
            //reader.readData(conn, query, parser);

            /* In the PostGISFactory, completed Geometries are stored internally
             * and can be returned through this method.*/
            ArrayList geomlist = factory.getGeometries();



            firstPoint = (Point) geomlist.get(0);


        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            try {
                conn.close();

            } catch (SQLException ex) {
                Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
            }
        }


        return firstPoint;
    }

    /**
     * Will fetch point lying within the bounding box. Suitable for fecthing hospitals, pilice stations within a given area.
     * @param pointTable
     * @param boundingBox
     * @return
     */
    public ArrayList<Point> getPointsWithinBoundingBox(String pointTable, Polygon boundingBox) {

        ArrayList<Point> points = new ArrayList<Point>();

        Connection conn = null;

        try {

            conn = PoolFactory.getPooling().getConnection();

            WKBReader reader = new WKBReader();

            /* For this demo we will be using the PostGISFactory.*/
            PostGISFactory factory = new PostGISFactory();

            /* Create the WKBParser.*/
            WKBParser parser = new WKBParser(factory);

            reader.readDataWithPartialQuery(
                    conn,
                    "the_geom",
                    "from " + pointTable,
                    parser);

            ArrayList geomlist = factory.getGeometries();

            for (int i = 0; i < geomlist.size(); i++) {

                Point point = (Point) geomlist.get(i);
                if (isGeometryWithinGeometry(point, boundingBox)) {
                    points.add(point);
                }

            }




        } catch (SQLException ex) {
            Utilities.Log.logger.info(ex.getMessage());
        } finally {

            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
            }

            return points;
        }




    }

    /**
     *
     * @param point 
     * @param polygon 
     * @return
     */
    public boolean isGeometryWithinGeometry(Point point, Polygon polygon) {

        boolean isWithin = false;
        Connection conn = null;
        try {
            conn = PoolFactory.getPooling().getConnection();
            PreparedStatement statement = null;

            /* Create the WKBReader.*/
            WKBReader reader = new WKBReader();
            /* For this demo we will be using the PostGISFactory.*/
            PostGISFactory factory = new PostGISFactory();
            /* Create the WKBParser.*/
            WKBParser parser = new WKBParser(factory);

//            StringBuffer pointSB = new StringBuffer();
//            StringBuffer polygonSB = new StringBuffer();
//
//            point.innerWKT(pointSB);
//            polygon.outerWKT(polygonSB);
//
//                        System.out.print(pointSB.toString() + " " + polygonSB.toString());

            int numPoints = polygon.numPoints();

            Point point0 = polygon.getPoint(0);
            Point point1 = polygon.getPoint(1);
            Point point2 = polygon.getPoint(2);
            Point point3 = polygon.getPoint(3);
            Point point4 = polygon.getPoint(4);

            String pointStr = "POINT(" + point.getX() + " " + point.getY() + ")";
            String polygonStr = "POLYGON((" + point0.getX() + " " + point0.getY() + "," + point1.getX() + " " + point1.getY() + "," + point2.getX() + " " + point2.getY() + "," + point3.getX() + " " + point3.getY() + "," + point4.getX() + " " + point4.getY() + "))";

            //String polygonStr = "POINT(" + point.getX() +" "+ point.getY() + ")";
            String query = "select ST_within(GeomFromText('" + pointStr + "'), GeomFromText('" + polygonStr + "'))";

            System.out.println("query: " + query);

            statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();



            while (rs != null && rs.next()) {
                isWithin = rs.getBoolean("st_within");
            }

        } catch (SQLException ex) {
            Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return isWithin;

    }

    /**
     * Split roads into cells
     * @param sourceTable 
     * @param id
     * @param cellsTable 
     * @param sourceGeomName  
     */
    public static void splitIntoCells(String sourceTable, String sourceGeomName, String cellsTable, long id) {

        Point firstPoint = null;
        Point lastPoint = null;

        Connection conn = null;
        try {


//            Utilities.Log.logger.info("Inside splitintocells function");
//            System.out.println("Inside splitintocells function");

            conn = PoolFactory.getPooling().getConnection();
            /* Create the WKBReader.*/
            WKBReader reader = new WKBReader();

            /* For this demo we will be using the PostGISFactory.*/
            PostGISFactory factory = new PostGISFactory();

            /* Create the WKBParser.*/
            WKBParser parser = new WKBParser(factory);


            //String query = "select * from roads2 where gid=" + id;

            /* You can complement this query with your own code.*/
            reader.readDataWithPartialQuery(
                    conn,
                    sourceGeomName,
                    " from " + sourceTable + " WHERE id = " + id + "",
                    parser);



            /* In the PostGISFactory, completed Geometries are stored internally
             * and can be returned through this method.*/
            ArrayList geomlist = factory.getGeometries();


            ArrayList<Point> points = new ArrayList<Point>();
            /* Do something with the geometries.*/
            /* Log the result to the console.*/

            MultiLineString geom = (MultiLineString) geomlist.get(0);

            LineString lines[] = geom.getLines();

            for (int i = 0; i < lines.length; ++i) {

                points.removeAll(points);
//                Utilities.Log.logger.info("Number of lines in geometry are " + lines.length);
                Point roadPoints[] = lines[i].getPoints();

                Point p = null, q = null, s = null;
                double x5 = 0;

                for (int j = 0; j < roadPoints.length - 1; j++) {

                    firstPoint = roadPoints[j];
                    lastPoint = roadPoints[j + 1];
//                    Utilities.Log.logger.info("NUmber of points in road is " + roadPoints.length);
//                    Utilities.Log.logger.info("First Point is " + firstPoint.toString());
//                    Utilities.Log.logger.info("Last Point is " + lastPoint.toString());

                    double bearing = calcBearing(firstPoint.getY(), firstPoint.getX(), lastPoint.getY(), lastPoint.getX());

                    boolean firstTime = true;
                    double slope = 0, c = 0, d1 = 0, d2 = 0;

                    x5 = getActualDistance(firstPoint, lastPoint);

                    //Utilities.Log.logger.info("Actual Distance is " + x5);

                    slope = calcSlope(firstPoint, lastPoint);
                    c = lastPoint.getY() - slope * lastPoint.getX();

                    //Utilities.Log.logger.info("Slope is " + slope);
                    //Utilities.Log.logger.info("c is " + c);

                    q = firstPoint;
                    points.add(q);

                    if ((lastPoint.getX() - firstPoint.getX()) != 0) {

                        while ((d1 >= x5 && firstTime)
                                || (q.getX() <= lastPoint.getX() && q.getX() >= firstPoint.getX() && q.getY() <= lastPoint.getY() && q.getY() >= firstPoint.getY() && firstTime) || (q.getX() >= lastPoint.getX() && q.getX() <= firstPoint.getX() && q.getY() >= lastPoint.getY() && q.getY() <= firstPoint.getY() && firstTime) || (q.getX() <= lastPoint.getX() && q.getX() >= firstPoint.getX() && q.getY() >= lastPoint.getY() && q.getY() <= firstPoint.getY() && firstTime) || (q.getX() >= lastPoint.getX() && q.getX() <= firstPoint.getX() && q.getY() <= lastPoint.getY() && q.getY() >= firstPoint.getY() && firstTime)) {

                            s = getNewPoint(slope, c, x5, q, firstPoint, lastPoint);
                            d1 = getCartesianDistance(s, lastPoint);
                            d2 = getCartesianDistance(q, s);

                            if ((q.getX() == s.getX() && q.getY() == s.getY())) {
//                                Utilities.Log.logger.info("Inside second if");
                                s = getNextPoint(q.getX(), q.getY(), bearing, x5);
                            }
                            if (s.getX() == 0 || s.getY() == 0) {
                                firstTime = false;
                                //s = getNextPoint(q.getX(), q.getY(), bearing, 0.000003);
                            } else {
//                                Utilities.Log.logger.info("Next Point is " + s.toString());
                                q = s;
                                points.add(q);
                            }

                        }
                    } else {

                        Utilities.Log.logger.info("Inside else");

                        double y = x5;
                        int m = 1;
                        while (((q.getY() - y) <= firstPoint.getY() && (q.getY() - y) >= lastPoint.getY()) || ((q.getY() + y) >= firstPoint.getY() && (q.getY() + y) <= lastPoint.getY())) {

                            s = new Point();
                            s.setX(q.getX());
                            if (firstPoint.getY() > lastPoint.getY()) {
                                s.setY(q.getY() - (m * x5));
                            } else {
                                s.setY(q.getY() + (m * x5));
                            }
                            y = m * x5;
                            points.add(s);
                            m++;
                        }
                    }
                }
                points.add(lastPoint);
                PreparedStatement statement = null;
                statement = conn.prepareStatement("select roadid from " + sourceTable + " where id = " + id);
                ResultSet rs = statement.executeQuery();

                rs.next();

                long roadid = rs.getLong("roadid");

                Utilities.Log.logger.info("Number of points in road " + id + " is " + points.size());
                updateCells2(cellsTable, roadid, points);
            }
        } catch (Exception e) {
            Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Get cartesian distance based for 1 mt based on slope of line
     * @param p1 first point
     * @param p2 last point
     * @return
     */
    public static double getActualDistance(Point p1, Point p2) {
        double distance = 0;
        double bearing = calcBearing(p1.getY(), p1.getX(), p2.getY(), p2.getX());
        Point B = getNextPoint(p1.getX(), p1.getY(), 90, 0.001);
        Point C = getNextPoint(p1.getX(), p1.getY(), bearing, 0.001);
        if (Math.abs(p1.getX() - p2.getX()) != 0) {
            double shortDistance = pointToLineDistance(p1, B, C);
            double slope = calcSlope(p1, p2);
            double x = shortDistance / slope;
            distance = x / Math.cos(Math.atan(slope));
        } else {
            distance = getCartesianDistance(p1, B);
        }
        return Math.abs(distance);
    }

    /**
     * Get perpendicular distance from a point to a line
     * @param A
     * @param B
     * @param P
     * @return
     */
    public static double pointToLineDistance(Point A, Point B, Point P) {
        double normalLength = Math.sqrt((B.getX() - A.getX()) * (B.getX() - A.getX()) + (B.getY() - A.getY()) * (B.getY() - A.getY()));
        return Math.abs((P.getX() - A.getX()) * (B.getY() - A.getY()) - (P.getY() - A.getY()) * (B.getX() - A.getX())) / normalLength;
    }

    /**
     * calculate slope of a line
     * @param p1
     * @param p2
     * @return
     */
    public static double calcSlope(Point p1, Point p2) {
        double slope = 0;
        if ((p1.getX() - p2.getX()) != 0) {
            slope = (p2.getY() - p1.getY()) / (p2.getX() - p1.getX());
        } else {
        }
        return slope;
    }

    /**
     * 
     * @param vertextId
     * @param roadsTable
     * @param searchStartVertices
     * @return
     */
    public ArrayList<Long> getRoadsWithVertext(long vertextId, String roadsTable, boolean searchStartVertices) {

        Connection conn = null;

        ArrayList<Long> roads = new ArrayList<Long>();

        try {
            conn = PoolFactory.getPooling().getConnection();
            PreparedStatement statement = null;

            String vertexVolumn;

            if (searchStartVertices) {
                vertexVolumn = "source";
            } else {
                vertexVolumn = "target";
            }

            String query = "select gid from " + roadsTable + " where " + vertexVolumn + " = " + vertextId;

            System.out.println("query: " + query);

            statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();


            while (rs.next()) {

                roads.add(rs.getLong("gid"));


            }



        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
            }

            return roads;
        }

    }

    /**
     * 
     * @param roads
     * @param roadTable
     * @param cellsTable
     * @param isVertextSource
     * @return
     */
    public HashMap<Long, Long> getTerminalCellsFromRoads(ArrayList<Long> roads, String roadTable, String cellsTable, Boolean isVertextSource) {

        HashMap<Long, Long> roadsCellMap = new HashMap<Long, Long>();
        Connection conn = null;

        try {
            conn = PoolFactory.getPooling().getConnection();
            PreparedStatement statement = null;

            for (int i = 0; i < roads.size(); i++) {

                long roadId = roads.get(i);
                String query = "select id from " + cellsTable + " where roadid =" + roadId;

                System.out.println("query: " + query);

                statement = conn.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet rs = statement.executeQuery();

                if (isVertextSource) {
                    if (rs.next()) {
                        roadsCellMap.put(roadId, rs.getLong("id"));
                    }
                } else {
                    if (rs.last()) {
                        roadsCellMap.put(roadId, rs.getLong("id"));
                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            try {
                conn.close();

            } catch (SQLException ex) {
                Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
            }

            return roadsCellMap;
        }
    }

    private ArrayList<Integer> getIdsfromTable(String table) {
        Connection conn = null;

        ArrayList<Integer> idList = new ArrayList<Integer>();
        try {
            conn = PoolFactory.getPooling().getConnection();
            PreparedStatement statement = null;
            String query = "select id from " + table;


            System.out.println(query);
            statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();


            while (rs.next()) {

                idList.add(rs.getInt("id"));


            }
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            try {
                conn.close();

            } catch (SQLException ex) {
                Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
            }
            return idList;
        }

    }

    /**
     * 
     * @param table
     * @param lat
     * @param lon
     * @param id
     */
    public void insertGeomValue(String table, double lat, double lon, int id) {

        Connection conn = null;


        try {
            conn = PoolFactory.getPooling().getConnection();
            PreparedStatement statement = null;
            String query = "update " + table + " set geom = GeomFromText('POINT(" + lon + " " + lat + ")') where id = " + id;


            System.out.println(query);
            statement = conn.prepareStatement(query);
            statement.execute();


        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            try {
                conn.close();

            } catch (SQLException ex) {
                Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

    }

    /**
     * 
     * @param table
     * @param latColumn
     * @param lonColumn
     */
    public void createGeomFromLatLon(String table, String latColumn, String lonColumn) {


        Connection conn = null;


        try {
            conn = PoolFactory.getPooling().getConnection();
            PreparedStatement statement = null;
            String query = "alter table " + table + " add column geom geometry";
            System.out.println(query);
            statement = conn.prepareStatement(query);
            statement.execute();

            ArrayList<Integer> idList = getIdsfromTable(table);

            for (int i = 0; i < idList.size(); i++) {
                HashMap<String, Double> latLonMap = getLatLonFromTable(table, latColumn, lonColumn, idList.get(i));
                insertGeomValue(table, latLonMap.get("lat"), latLonMap.get("lon"), idList.get(i));


            }



        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            try {
                conn.close();

            } catch (SQLException ex) {
                Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

    }

    /**
     * 
     * @param table
     * @param latColumn
     * @param lonColumn
     * @param id
     * @return
     */
    public HashMap<String, Double> getLatLonFromTable(String table, String latColumn, String lonColumn, int id) {
        Connection conn = null;

        HashMap<String, Double> latLonMap = new HashMap<String, Double>();
        try {
            conn = PoolFactory.getPooling().getConnection();
            PreparedStatement statement = null;
            String query = "select " + latColumn + ", " + lonColumn + " from " + table + " where id = " + id;


            System.out.println(query);
            statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();


            while (rs.next()) {

                latLonMap.put("lat", rs.getDouble(latColumn));
                latLonMap.put("lon", rs.getDouble(lonColumn));


            }
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            try {
                conn.close();

            } catch (SQLException ex) {
                Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
            }
            return latLonMap;
        }
    }

    /**
     * 
     * @param vertextId
     * @param trafficHubsTable
     * @return
     */
    public int insertIntoTrafficHubs(long vertextId, String trafficHubsTable) {

        Connection conn = null;

        int hubId = -1;

        try {
            conn = PoolFactory.getPooling().getConnection();
            PreparedStatement statement = null;



            String query = "insert into " + trafficHubsTable + "(vertext) values (" + vertextId + ")";

            System.out.println("query: " + query);

            statement = conn.prepareStatement(query);
            statement.execute();

            query = "select id from " + trafficHubsTable + " where vertext = " + vertextId;

            System.out.println("query: " + query);

            statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();



            if (rs.next()) {
                hubId = rs.getInt("id");
            }

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            try {
                conn.close();

            } catch (SQLException ex) {
                Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
            }

            return hubId;

        }


    }

    private void insertIntoTrafficLights(int hubId, long CellId, long roadId, String trafficLightTable) {

        Connection conn = null;
        ;

        try {
            conn = PoolFactory.getPooling().getConnection();
            PreparedStatement statement = null;



            String query = "insert into " + trafficLightTable + "(cellid, roadid, hubid, duration, color) values ("
                    + CellId + ", " + roadId + ", " + hubId + " , 15, 1 )";

            System.out.println("query: " + query);

            statement = conn.prepareStatement(query);
            statement.execute();



        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            try {
                conn.close();


            } catch (SQLException ex) {
                Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    /**
     * 
     * @param roadsCellMap
     * @param vertextId
     * @param trafficHubTable
     * @param trafficLightTable
     * @return
     */
    public int storeTrafficHub(HashMap<Long, Long> roadsCellMap, long vertextId, String trafficHubTable, String trafficLightTable) {

        int hubId = insertIntoTrafficHubs(vertextId, trafficHubTable);

        if (hubId == -1) {
            return hubId;
        }

        Set<Long> roads = roadsCellMap.keySet();

        Iterator<Long> iter = roads.iterator();

        while (iter.hasNext()) {
            long roadId = iter.next();
            long cellId = roadsCellMap.get(roadId);

            insertIntoTrafficLights(hubId, cellId, roadId, trafficLightTable);

        }

        return hubId;

    }

    /**
     * 
     * @param hubTableName
     * @param trafficLightTableName
     * @param roadTableName
     * @param verticesTableName
     * @param cellsTableName
     * @return
     */
    public int generateTrafficLightHub(String hubTableName, String trafficLightTableName, String roadTableName, String verticesTableName, String cellsTableName) {
        int hubId = -1;
        Connection conn = null;

        try {
            conn = PoolFactory.getPooling().getConnection();
            PreparedStatement statement = null;

            long ramdom = Math.round(Math.random() * 100);

            String query = "select id from " + verticesTableName + " where id =" + ramdom;

            System.out.println("query: " + query);

            statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            long vertextId = -1;
            if (rs.next()) {
                vertextId = rs.getLong("id");
            } else {
                Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, "Cound not fetch vertext ID");
                return -1;
            }


            HashMap<Long, Long> roadsCellMap = new HashMap<Long, Long>();
            ArrayList<Long> roads = getRoadsWithVertext(vertextId, roadTableName, true);
            roadsCellMap = getTerminalCellsFromRoads(roads, roadTableName, cellsTableName, true);

            ArrayList<Long> invertedRoads = new ArrayList<Long>();
            for (int i = 0; i < roads.size(); i++) {
                invertedRoads.add(getInvertedRoad(roads.get(i), roadTableName));
            }

            roadsCellMap.putAll(getTerminalCellsFromRoads(roads, roadTableName, cellsTableName, true));

            roads = getRoadsWithVertext(vertextId, roadTableName, false);
            roadsCellMap.putAll(getTerminalCellsFromRoads(roads, roadTableName, cellsTableName, false));

            invertedRoads = new ArrayList<Long>();
            for (int i = 0; i < roads.size(); i++) {
                invertedRoads.add(getInvertedRoad(roads.get(i), roadTableName));
            }

            roadsCellMap.putAll(getTerminalCellsFromRoads(roads, roadTableName, cellsTableName, false));



            hubId = storeTrafficHub(roadsCellMap, vertextId, hubTableName, trafficLightTableName);






        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            try {
                conn.close();

            } catch (SQLException ex) {
                Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return hubId;

    }

    /**
     * 
     * @param roadId
     * @param roadTable
     * @return
     */
    public long getInvertedRoad(long roadId, String roadTable) {

        Connection conn = null;
        long invertedRoadId = -1;

        try {
            conn = PoolFactory.getPooling().getConnection();
            PreparedStatement statement = null;



            String query = "select id from " + roadTable + " where originalroad =" + roadId;

            System.out.println("query: " + query);

            statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                invertedRoadId = rs.getLong("id");
            }

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            try {
                conn.close();

            } catch (SQLException ex) {
                Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
            }

            return invertedRoadId;
        }




    }

    /**
     * get the net point based on the equation of the line
     * @param m
     * @param q
     * @param d
     * @param p
     * @param firstPoint
     * @param lastPoint
     * @return
     */
    public static Point getNewPoint(double m, double q, double d, Point p, Point firstPoint, Point lastPoint) {
        Point s = new Point();

        double root1 = 0, root2 = 0, y = 0, x = 0, discr = 0;

        double a = 1 + (m * m);
        double b = (-(2 * p.getX()) + (2 * m * q) - (2 * m * p.getY()));
        double c = ((p.getX() * p.getX()) + (q * q) + (p.getY() * p.getY()) - (2 * q * p.getY()) - (d * d));

//        Utilities.Log.logger.info("a is " + a);
//        Utilities.Log.logger.info("b is " + b);
//        Utilities.Log.logger.info("c is " + c);

        discr = Math.abs((b * b) - (4 * a * c));

//        Utilities.Log.logger.info("Discremenant is " + discr);

        //Utilities.Log.logger.info("First Point is " + firstPoint.toString());

        if (Double.isNaN(discr)) {
        }

        if (discr > 0) {

//            Utilities.Log.logger.info("First if; discr > 0");

            root1 = (-b + Math.sqrt(discr)) / (2 * a);
            root2 = (-b - Math.sqrt(discr)) / (2 * a);

//            Utilities.Log.logger.info("root 1 is " + root1);
//            Utilities.Log.logger.info("root 2 is " + root2);

        }

        if (discr == 0) {

//            Utilities.Log.logger.info("Second if; discr = 0");

            root1 = (-b) / (2 * a);

            x = root1;
            y = (m * root1) + q;
        }

        if (discr < 0) {

            Utilities.Log.logger.info("Third if; discr < 0");
        }

        if ((root1 >= p.getX() && root1 <= lastPoint.getX()) || (root1 <= p.getX() && root1 >= lastPoint.getX())) {

//            Utilities.Log.logger.info("First Check");
            x = root1;
            y = (m * root1) + q;

        } else if ((root2 >= p.getX() && root2 <= lastPoint.getX()) || (root2 <= p.getX() && root2 >= lastPoint.getX())) {
//            Utilities.Log.logger.info("Second Check");
            x = root2;
            y = (m * root2) + q;
        }

//        Utilities.Log.logger.info("x is " + x);
//        Utilities.Log.logger.info("y is " + y);

        s.setX(x);
        s.setY(y);
        return s;
    }

    /**
     * get cartesian distance between two points
     * @param p1
     * @param p2
     * @return
     */
    public static double getCartesianDistance(Point p1, Point p2) {
        double distance = Math.sqrt(((p2.getX() - p1.getX()) * (p2.getX() - p1.getX())) + ((p2.getY() - p1.getY()) * (p2.getY() - p1.getY())));
        return Math.abs(distance);
    }

    /**
     * Calculate distance between two points
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @return
     */
    public static double getDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // earth's mean radius in km
        double d = Math.acos(Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2))
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(lon2 - lon1))) * R;
        return d;

    }

    /**
     * calculate bearing, in degrees between two points
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @return
     */
    public static double calcBearing(double lat1, double lon1, double lat2, double lon2) {
        double radlat1 = Math.toRadians(lat1);
        double radlat2 = Math.toRadians(lat2);

        double dlon = Math.toRadians(lon2 - lon1);

        double y = Math.sin(dlon) * Math.cos(radlat2);
        double x = Math.cos(radlat1) * Math.sin(radlat2) - Math.sin(radlat1) * Math.cos(radlat2) * Math.cos(dlon);

        double bearing = Math.toDegrees(Math.atan2(y, x));
        return bearing % 360;

    }

    /**
     * get nest point, given source, bearing and distance travelled
     * @param lon
     * @param lat
     * @param bearing
     * @param distance
     * @return
     */
    public static Point getNextPoint(double lon, double lat, double bearing, double distance) {


        double radlon = Math.toRadians(lon);
        double radlat = Math.toRadians(lat);

        double radbrng = Math.toRadians(bearing);
        double radius = 6371.00;


        double latcomp1 = Math.sin(radlat) * Math.cos(distance / radius);
        double latcomp2 = Math.cos(radlat) * Math.sin(distance / radius) * Math.cos(radbrng);
        double full = latcomp1 + latcomp2;


        double lat2 = Math.asin(latcomp1 + latcomp2);
        double lon2 = lon + Math.atan2(Math.sin(radbrng) * Math.sin(distance / radius) * Math.cos(radlat), Math.cos(distance / radius) - Math.sin(radlat) * Math.sin(lat2));

        lat2 = Math.toDegrees(lat2);

        Point p = new Point();
        p.setX(lon2);
        p.setY(lat2);

        if (lon2 == 0 || lat2 == 0) {
            return null;
        } else {
            return p;
        }
    }
}
