package peek.imageupload;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewImage extends Activity {

    //Declare Variables
    TextView text;
    ImageView imageview;

    @Override
    public void onCreate(Bundle savedInstanceState){
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        //Get the view from viewimage.xml
        setContentView(R.layout.view_image);

        //retrieve data from receivedimages activity
        Intent i = getIntent();

        //get position
        int position = i.getExtras().getInt("position");

        //Get String arrays path
        String[] filepath = i.getStringArrayExtra("filepath");

        //Get String arrays name
        String[] filename = i.getStringArrayExtra("filename");

        //Locate the TextView in viewimage.xml
        text = (TextView) findViewById(R.id.imagetext);

        //Load the text into the Text fiel followed by position
        text.setText(filename[position]);

        //locate the imageview in view_image
        imageview = (ImageView) findViewById(R.id.full_image_view);

        //get display size
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenwidth = size.x;
        int screenheight = size.y;

        //Decode the file path with bitmap factory
        // Get Image Size
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filepath[position], bitmapOptions);
        double imageWidth = bitmapOptions.outWidth;
        double imageHeight = bitmapOptions.outHeight;
        //calculate aspect ratio so image doesnt scale weird
        double d_aspectratio = imageHeight/imageWidth;
        double d_height = screenwidth * d_aspectratio;
        int height = (int) d_height;
        bitmapOptions.inJustDecodeBounds = false;
        //decode the image map with the new bounds
        Bitmap bmp = BitmapFactory.decodeFile(filepath[position]);
        Bitmap resized = Bitmap.createScaledBitmap(bmp, screenwidth, height, true);

        //Set the decoded bitmap to imageview
        imageview.setImageBitmap(resized);
        //bmp.recycle();
    }
}
