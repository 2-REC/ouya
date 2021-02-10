package tv.ouya.sample.cc;

import android.content.Context;
import android.graphics.*;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import tv.ouya.sample.cc.emblem.Shape;

import java.util.HashMap;

public class ShapeAdapter extends BaseAdapter {

    private static int sImageSize;
    private static Paint sPaint;
    private static HashMap<Shape, Bitmap> sShapeBitmaps = new HashMap<Shape, Bitmap>();

    private Context mContext;

    static {
        sPaint = new Paint();
        sPaint.setColor(Color.BLACK);
    }

    public ShapeAdapter(Context context) {
        mContext = context;

        if(sImageSize == 0) {
            sImageSize = context.getResources().getDimensionPixelSize(R.dimen.thumb_size);
        }
    }

    @Override
    public int getCount() {
        return Shape.values().length;
    }

    @Override
    public Shape getItem(int position) {
        return Shape.values()[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHelper vh;
        if(convertView == null) {
            convertView = View.inflate(mContext, R.layout.item_shape, null);
            vh = new ViewHelper();
            vh.image = (ImageView) convertView.findViewById(R.id.image);
            vh.text = (TextView) convertView.findViewById(R.id.text);
            convertView.setTag(vh);
        } else {
            vh = (ViewHelper) convertView.getTag();
        }

        Shape s = getItem(position);

        vh.image.setImageBitmap(getShapeBitmap(s));
        vh.text.setText(s.title);

        return convertView;
    }

    private static Bitmap getShapeBitmap(Shape s) {
        if(sShapeBitmaps.containsKey(s)) {
            return sShapeBitmaps.get(s);
        }

        final Bitmap bmp = Bitmap.createBitmap(sImageSize, sImageSize, Bitmap.Config.ARGB_8888);
        final Canvas c = new Canvas(bmp);
        final Path p = new Path(s.path);
        final Matrix m = new Matrix();

        m.setScale(sImageSize * 0.4f, sImageSize * 0.4f);
        p.transform(m);
        c.translate(sImageSize/2, sImageSize/2);
        c.drawPath(p, sPaint);

        sShapeBitmaps.put(s, bmp);

        return bmp;
    }

    private static class ViewHelper {
        public ImageView image;
        public TextView text;
    }
}
