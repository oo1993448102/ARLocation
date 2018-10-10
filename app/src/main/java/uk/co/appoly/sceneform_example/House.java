package uk.co.appoly.sceneform_example;

/**
 * @Author: EchoZhou
 * @Date: 2018-09-28 14:25
 * @Description:
 */
public class House {

    private String name ;
    private double lat;
    private double lng;

    public House(String name, double lat, double lng) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}
