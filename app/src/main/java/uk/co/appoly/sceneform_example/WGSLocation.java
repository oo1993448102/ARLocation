package uk.co.appoly.sceneform_example;

import android.location.Location;

import uk.co.appoly.arcorelocation.utils.TransUtil;

/**
 * @Author: EchoZhou
 * @Date: 2018-09-27 18:08
 * @Description:
 */
public class WGSLocation extends Location {
    public WGSLocation(String provider) {
        super(provider);
    }

    public WGSLocation(Location l) {
        super(l);
    }

    @Override
    public double getLatitude() {
        return TransUtil.wgs84togcj02(super.getLongitude(),super.getLatitude())[1];
    }
}
