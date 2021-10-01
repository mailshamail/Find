package ru.mailshamail.find;

import android.content.Intent;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;

public class addMarkers {

    private MapView map;
    private String name;
    private double shirota, dolgota;
    private String time;


    public addMarkers(MapView map, String name, double shirota, double dolgota, String time) {
        this.map = map;
        this.name = name;
        this.shirota = shirota;
        this.dolgota = dolgota;
        this.time = time;
    }

    public Marker addMarker()
    {

        Marker marker = new Marker(map);
        GeoPoint point = new GeoPoint(shirota, dolgota);
        marker.setPosition(point);

        if (name != null) {
            if(time != null) {
                StringBuffer sb = new StringBuffer(time);
                sb.insert(2, ":");
                System.out.println(sb);
                marker.setTitle(name + "/" + sb);

            }
        }
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(marker);

        System.out.println("Маркер добавлен");

        return marker;
    }
}
