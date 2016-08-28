package peek.imageupload;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    //private Button button;
    private String encoded_string, image_name, android_key;
    //private Bitmap bitmap;
    private File file;
    private Uri file_uri;
    //boolean isImageFitToScreen;
    private double latitude;
    private double longitude;

    //response from http post
    private String serverresponse;


    // Allows quickly changing the app name and will rename the image storage folder
    private String app_folder = "peek";

    /*initializes current pic number. Number alternates from 1 to 2
     and back to 1. This is because the async decoding task was taking too
     long and the imageview preview would update with the old photo before
     the new one was done decoding. Maybe there is some way to buffer the
     new image into memory but I dont know how
     */
    int currentpicnum = 0;

    //sets the image quality during storage to reduce transmit bandwidth
    // allowable from 0 to 100 (percent)
    int image_quality = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button takepicbutton = (Button) findViewById(R.id.takepic);
        Button sendpicbutton = (Button) findViewById(R.id.sendpic);

        /* Code for adjusting the size of the image view with a click. Might be useful later
        final ImageView cameraimage = (ImageView) findViewById(R.id.cameraimage);
        cameraimage.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v) {
                if(isImageFitToScreen) {
                    isImageFitToScreen = false;
                    cameraimage.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    cameraimage.setAdjustViewBounds(true);
                } else {
                    isImageFitToScreen = true;
                    cameraimage.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                    cameraimage.setScaleType(ImageView.ScaleType.FIT_XY);
                }
            }
        });
        */

        assert sendpicbutton != null;
        sendpicbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                makeRequest();
            }
        });
        assert takepicbutton != null;
        takepicbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                //take the picture

                /*Can increment the number in order to get multiple pictures.
                Currently used to alternate between 1 and 2 to allow 2 images to be stored
                 */
                currentpicnum = currentpicnum + 1;
                if (currentpicnum == 3) {
                    currentpicnum = 1;
                }

                //Start default camera app
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                getFileUri();
                i.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                i.putExtra(MediaStore.EXTRA_OUTPUT,file_uri);
                startActivityForResult(i, 10);
            }
        });
        createuserGUID();
        requestwritepermission();
        requestlocationpermissions();

    }

    private void createuserGUID(){
        SharedPreferences prefs = this.getSharedPreferences("userGUID", 0);
        String not_set = "NOTSET";
        String android_key;
        android_key = prefs.getString("userGUID", not_set);
        if (android_key.equals(not_set)) {
            Log.d("Tag", "Creating user id for 1st run");
            android_key = UUID.randomUUID().toString();
            prefs.edit().putString("userGUID", android_key).commit();
        }

    }

    final int REQUEST_CODE_ASK_WRITE_PERMISSIONS = 123;
    // Request code for write permissions.
    private void requestwritepermission(){

        int hasWritePermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(hasWritePermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_ASK_WRITE_PERMISSIONS);
            return;
        }
        //if request has previously been granted
        //Toast.makeText(MainActivity.this, "Write permission approved", Toast.LENGTH_LONG)
        //        .show();

        //Run Encode Image Method if write permissions granted
        //new Encode_image().execute();

        //set the image view to the new picture
        //ImageView cameraimagepreview = (ImageView) findViewById(R.id.cameraimage);
        //assert cameraimagepreview != null;
        //cameraimagepreview.setImageURI(file_uri);

    }

    final private int REQUEST_CODE_ASK_LOCATION_PERMISSIONS = 456;
    private void requestlocationpermissions() {

        int hasLocationPermission = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
        if (hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_CODE_ASK_LOCATION_PERMISSIONS);
            return;
        }
    }

    //Need to handle nag code in the case they pic dont request again. Write is required for app function.
    //**************** to be implemented later.
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_ASK_WRITE_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //pass
                    //Toast.makeText(MainActivity.this, "Write permission approved", Toast.LENGTH_LONG)
                    //        .show();

                    //new Encode_image().execute();

                } else {
                    //failure
                    Toast.makeText(MainActivity.this, "Permission denied. Required by App", Toast.LENGTH_LONG)
                            .show();
                }
                break;
            case REQUEST_CODE_ASK_LOCATION_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //pass
                    //getlocation();
                } else {
                    Toast.makeText(MainActivity.this, "Location Permission denied. Required by App", Toast.LENGTH_LONG)
                            .show();
                }
            default:

        }
    }



    // Sets the file path for naming the file
    private void getFileUri() {

        image_name = "testing" + currentpicnum + ".jpg";
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                + File.separator + app_folder + File.separator + image_name

        );
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                + File.separator + app_folder
        );

        file_uri = Uri.fromFile(file);
        //check if the file exists and delete it.
        File f = new File(file_uri.getPath());
        //check if the folder exists. If it doesnt then create it
            /*These statements below are not going to work on a new install
            because the write permission has not been requested yet. Need
            to rearrange to request permissions first.
            */
        if (!directory.exists() && !directory.isDirectory()) {
            //create empty directory
            if (directory.mkdirs()) {
                Log.i("CreateDir", "App dir created");
            } else {
                Log.w("CreateDir", "Unable to create app dir!");
            }
        } else {
            Log.i("CreateDir", "App dir already exists");
        }

        if(!f.exists()) {
            //delete the file
            f.delete();}
    }


    //After the camera returns with the new picture then we request write permissions.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 10 && resultCode == RESULT_OK){

            int hasWritePermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if(hasWritePermission != PackageManager.PERMISSION_GRANTED) {
                requestwritepermission();
            } else {
                new Encode_image().execute();
                ImageView cameraimagepreview = (ImageView) findViewById(R.id.cameraimage);
                assert cameraimagepreview != null;
                cameraimagepreview.setImageURI(file_uri);
            }


        }
    }


    //---------------------------------------------------------------------------------------------


    //Writes the image to the SD card. Done in async task so the UI isnt blocked, but messes with
    //imageview previewer so image 2 images are required.
    private class Encode_image extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            //original code for streaming the bitmap into memory
            Bitmap bitmap;
            bitmap = BitmapFactory.decodeFile(file_uri.getPath());
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, image_quality, stream);
            bitmap.recycle();
            byte[] array = stream.toByteArray();
            encoded_string = Base64.encodeToString(array, 0);

            return null;
        }
    }

    //-------------------------------------------------------------------------------------------------
    //not used but this is an alternate way to write to the sd card.
    /*
    private class StoreImagetoSDCard extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            //file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            //        + File.separator + image_name
            //);
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fileOutputStream);
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (FileNotFoundException e){
                e.printStackTrace();
            }catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

    }
    */
    //-------------------------------------------------------------------------

    /* This posts the file and sql data to the database.
    Need to implement passing the user info along with the file info.
     */

    private void getuserGUID() {

        SharedPreferences prefs = this.getSharedPreferences("userGUID", 0);
        String not_set = "NOTSET";
        //String android_key;
        android_key = prefs.getString("userGUID", not_set);


        //userID = android_key;

    }

    private void getlocation() {
        //Get the location manager
        int hasLocationPermission = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
        if (hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
            requestlocationpermissions();
        } else {

            LocationManager locationManager = (LocationManager)
                    getSystemService(LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            String bestProvider = locationManager.getBestProvider(criteria, false);
            Location location = locationManager.getLastKnownLocation(bestProvider);
            try {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                latitude = (double) Math.round(latitude * 10) / 10;
                latitude = (double) Math.round(longitude * 10) / 10;
            } catch (NullPointerException e) {
                latitude = -2.0;
                longitude = -2.0;
            }
        }
    }


    private void makeRequest() {
        getlocation();
        if ((currentpicnum > 0) && latitude != -1.0) {
            //getlocation();
            //Toast.makeText(this, "Right before post", Toast.LENGTH_SHORT).show();
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            StringRequest request = new StringRequest(Request.Method.POST, "http://10.10.1.49/uploadimage.php",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            serverresponse = response;
                            handleserverresponse();

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }) {
                //@Override

                protected Map<String, String> getParams() throws AuthFailureError {
                    getuserGUID();
                    String s_latitude = String.valueOf(latitude);
                    String s_longitude = String.valueOf(longitude);
                    HashMap<String, String> map = new HashMap<>();
                    map.put("encoded_string", encoded_string);
                    map.put("image_name", image_name);
                    map.put("user_GUID", android_key);
                    map.put("latitude", s_latitude);
                    map.put("longitude", s_longitude);

                    return map;
                }
            };
            requestQueue.add(request);
        } else if (currentpicnum == 0){
            Toast.makeText(this, "Take a picture first", Toast.LENGTH_SHORT).show();
        } else if (latitude == -1.0) {
            Toast.makeText(this, "Location not acquired. Enable location permission and turn on location services",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void handleserverresponse() {
        if (serverresponse.equals("FALSE")){
            Toast.makeText(this, "Picture Sent", Toast.LENGTH_SHORT).show();
            currentpicnum = 0;
        } else {
            Toast.makeText(this, "Server communications failed. Try again or report to developer",
                    Toast.LENGTH_LONG).show();
        }
    }
}


