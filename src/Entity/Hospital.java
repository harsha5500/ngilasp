package Entity;

import Agents.Person;
import java.util.ArrayList;
import org.postgis.Point;

/**
 * This is the hospital entity that is present on the map. This class extends the general
 * facility present on the map. This class is a place holder for a real hospital and hence contains
 * attributes as capacity and the list of people presently in the hospital. When a person reaches
 * the hospital, the capacity is checked aganist the listOfPatients to check whether he can enter
 * the hospital.
 * @see Facility
 */
public class Hospital extends Facility {

    /**
     * This is the total number of patients the hospital can accomodate
     */
    private int capacity;
    /**
     * The list of all the patinets in the hospital.
     */
    private ArrayList<Person> listOfPatients;
    /**
     * The id of the cell in which the hospital is located
     */
    private long cellId;

    /**
     * Creates a hospital Facility.
     * @param latlon The location of the hospital.
     * @param name The name of the hospital.
     * @param address The address of the hospital.
     * @param capacity The capacity of the hospital.
     * @param listOfPatients The list of patients in the hospital.
     */
    public Hospital(Point latlon, String name, String address, int capacity, ArrayList<Person> listOfPatients) {
        this.capacity = capacity;
        this.listOfPatients = listOfPatients;
        this.latlon = latlon;
        this.name = name;
        this.address = address;
    }

    /**
     * Creates a hospital Facility.
     * @param latlon The location of the hospital.
     * @param name The name of the hospital.
     * @param capacity The capacity of the hospital.
     * @param cellId the cell id of the location of the hospital
     */
    public Hospital(Point latlon, String name, int capacity, long cellId) {
        this.capacity = capacity;
        this.latlon = latlon;
        this.name = name;
        this.cellId = cellId;
    }

    /**
     * Creates a hospital Facility.
     * @param latlon location The location of the hospital.
     * @param name name The name of the hospital.
     * @param capacity The capacity of the hospital.
     * @param listOfPatients The list of patients in the hospital.
     */
    public Hospital(Point latlon, String name, int capacity, ArrayList<Person> listOfPatients) {
        this.capacity = capacity;
        this.listOfPatients = listOfPatients;
        this.latlon = latlon;
        this.name = name;
    }

    /**
     *Creates a hospital Facility.
     * @param latlon The location of the hospital.
     * @param name The name of the hospital.
     * @param capacity The capacity of the hospital.
     */
    public Hospital(Point latlon, String name, int capacity) {
        this.latlon = latlon;
        this.name = name;
        this.capacity = capacity;
        this.listOfPatients = new ArrayList<Person>();
    }

    public Hospital(double lat, double lon, String name, int capacity) {
        this.latlon = new Point(lat, lon);
        this.name = name;
        this.capacity = capacity;
        this.listOfPatients = new ArrayList<Person>();
    }

    /**
     * Returns the cell id of the hospital
     * @return the cell id of the hospital
     */
    public long getCellId() {
        return cellId;
    }

    /**
     * Set the cell id for the hospital
     * @param cellId the cell id for the hospital
     */
    public void setCellId(long cellId) {
        this.cellId = cellId;
    }

    /**
     * Returns the capacity of the hospital.
     * @return return the capacity of the hospital.
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Set the capacity of the hospital.
     * @param capacity the capacity of the hospital.
     */
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    /**
     *The list of Person objects that have reached the hospital.
     * @return the list of person agents
     */
    public ArrayList<Person> getListOfPatients() {
        return listOfPatients;
    }

    /**
     *Set the list of person agents that are in the hospital.
     * @param listOfPatients the list of person agents in the hospital.
     */
    public void setListOfPatients(ArrayList<Person> listOfPatients) {
        this.listOfPatients = listOfPatients;
    }

    /**
     * Returns the string represnetation of the hospital.
     * @return string representation of the hospital.
     */
    @Override
    public String toString() {
        String buildString = "Hospital Name = " + this.name + "\nAddress = " + this.address + "\nCapacity = " + this.capacity + "\nLocation = " + this.latlon.toString() + "\nNumber of Patients = " + this.listOfPatients.size();
        return buildString;
    }
}
