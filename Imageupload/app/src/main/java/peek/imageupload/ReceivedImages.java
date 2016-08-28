package peek.imageupload;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import java.io.File;
import java.security.PrivateKey;

public class ReceivedImages extends AppCompatActivity {

    private String[] FilePathStrings;
    private String[] FileNameStrings;
    private File[] listFile;
    GridView grid;
    File file;
    private String app_folder = "peek/received files/";
    GridViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_received_images);

        //Check for SD Card
        if(!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "Error! No SD CARD Found!",Toast.LENGTH_LONG)
                    .show();
        } else {
            //Locate the image folder for this app
            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                + File.separator + app_folder);
            file.mkdirs();
        }

        if (file.isDirectory()){
            listFile = file.listFiles();
            //Create a String array for FilePathStrings
            FilePathStrings = new String[listFile.length];
            //Get the name image file
            FileNameStrings = new String[listFile.length];

            for (int i = 0; i<listFile.length; i++){
                //Get the path of the image file
                FilePathStrings[i] = listFile[i].getAbsolutePath();
                //Get the name of the image file
                FileNameStrings[i] = listFile[i].getName();
            }
        }

        //Locate the GridView in activity_received_images.xml
        grid = (GridView) findViewById(R.id.gridview);
        //Pass Strings to the gridviewadapter Class
        adapter = new GridViewAdapter(this, FilePathStrings, FileNameStrings);
        //Set the gridviewadapter to the GridView
        grid.setAdapter(adapter);

        //Capture gridview item click
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(ReceivedImages.this, ViewImage.class);
                //Pass String arrays path
                i.putExtra("filepath", FilePathStrings);
                // Pass String arrays Name
                i.putExtra("filename", FileNameStrings);
                //Pass click position
                i.putExtra("position", position);
                startActivity(i);
            }
        });
    }
}
