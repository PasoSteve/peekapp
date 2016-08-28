package peek.imageupload;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GridViewAdapter extends BaseAdapter{

    //Declare Variables
    private Activity activity;
    private String[] filepath;
    private String[] filename;
    //thumbnailwidth allows for quickly adjusting the grid width for testing optimal size
    private int thumbnailwidth = 300;

    private static LayoutInflater inflater = null;

    public GridViewAdapter(Activity a, String[] fpath, String[] fname){
        activity = a;
        filepath = fpath;
        filename = fname;
        inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    public int getCount(){
        return filepath.length;
    }

    public Object getItem(int postion) {
        return postion;
    }

    public long getItemId(int position){
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent){
        View vi = convertView;
        if (convertView == null)
            vi = inflater.inflate(R.layout.gridview_item, null);
        //Locate the TextView in received images
        TextView text = (TextView) vi.findViewById(R.id.text);
        //Locate the imageview in received images
        ImageView image = (ImageView) vi.findViewById(R.id.image);

        //set file name to the Textview followed by position
        text.setText(filename[position]);

        //Decode the filepath with bitmapfactory


        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filepath[position], bitmapOptions);
        double imageWidth = bitmapOptions.outWidth;
        double imageHeight = bitmapOptions.outHeight;
        //calculate aspect ratio so image doesnt scale weird
        double d_aspectratio = imageHeight/imageWidth;
        double d_height = thumbnailwidth * d_aspectratio;
        int height = (int) d_height;
        bitmapOptions.inJustDecodeBounds = false;
        //decode the image map with the new bounds
        Bitmap bmp = BitmapFactory.decodeFile(filepath[position]);
        Bitmap resized = Bitmap.createScaledBitmap(bmp, thumbnailwidth, height, true);




        //Bitmap bmp = BitmapFactory.decodeFile(filepath[position]);
        //Bitmap resized = Bitmap.createScaledBitmap(bmp, 400, 400, true);

        //set the decoded bitmapp to the imageview
        image.setImageBitmap(resized);
        return vi;
    }
}
