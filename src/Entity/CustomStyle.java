package Entity;

/**
 * This class defines the style elements that are needed for a placemark in a
 * kml file. These parameters are adjusted to get different colors, images and size
 * of a give place mark.
 */
public class CustomStyle {
    /**
     * The URL to the image of the icon representing this placemark
     */
    public String url;
    /**
     * The scale of the image of the icon representing the placemark
     */
    public String scale;
    /**
     * The color of the icon representing the placemark
     */
    public String color;

    /**
     * Constructor to create a custom style to represent a placemark
     * @param clr the color of the icon
     * @param scl the scale of the icon
     * @param link the URL or URI pointing to the location of the icon
     */
    public CustomStyle(String clr, String scl, String link) {
        url = link;
        scale = scl;
        color = clr;
    }

    /**
     * Constructor to create a custom style to represent a placemark
     * @param scl the scale of the icon
     * @param link the URL or URI pointing to the location of the icon
     */
    public CustomStyle(String scl, String link) {
        url = link;
        scale = scl;

    }
    /**
     * Create a empty syle object
     */
    public CustomStyle() {
    }
}
