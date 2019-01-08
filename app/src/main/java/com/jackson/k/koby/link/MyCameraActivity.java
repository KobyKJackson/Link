package com.jackson.k.koby.link;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MyCameraActivity extends Activity {
    private static final int CAMERA_REQUEST = 1888;
    ImageView mimageView;
    TextView textView, textView2;
    private LocationManager locationManager;
    private LocationListener listener;
    private Location location;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_camera);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        mimageView = (ImageView) this.findViewById(R.id.imageView1);
        button = (Button) this.findViewById(R.id.button1);
        textView = this.findViewById(R.id.editText2);
        textView2 = this.findViewById(R.id.textView2);

        configure_button();

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location1) {
                location = location1;
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        };
    }

    public void takeImageFromCamera(View view) {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            Bitmap mphoto = (Bitmap) data.getExtras().get("data");
            mimageView.setImageBitmap(mphoto);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            textView.setText(sdf.format(new Date()));
            textView2.append("n " + location.getLongitude() + " " + location.getLatitude());

        }
    }


    void configure_button() {
        // this code won'textView execute IF permissions are not allowed, because in the line above there is return statement.
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //noinspection MissingPermission
                try{
                    locationManager.requestLocationUpdates("gps", 5000, 0, listener);
                }
                catch (SecurityException e)
                {

                }
                takeImageFromCamera(view);
            }
        });
    }
}