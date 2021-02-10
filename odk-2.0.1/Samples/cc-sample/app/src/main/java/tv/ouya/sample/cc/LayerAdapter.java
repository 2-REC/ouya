package tv.ouya.sample.cc;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import tv.ouya.sample.cc.emblem.Emblem;
import tv.ouya.sample.cc.emblem.Layer;

public class LayerAdapter extends BaseAdapter {

    private static int sImageSize = -1;

    private Context mContext;
    private Emblem mEmblem;

    public LayerAdapter(Context context, Emblem emblem) {
        mContext = context;
        mEmblem = emblem;
    }

    @Override
    public int getCount() {
        return mEmblem.getLayers().length;
    }

    @Override
    public Layer getItem(int position) {
        return mEmblem.getLayer(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHelper vh;
        if(convertView == null) {
            convertView = View.inflate(mContext, R.layout.item_layer, null);

            vh = new ViewHelper();
            vh.image = (ImageView) convertView.findViewById(R.id.image);
            vh.text = (TextView) convertView.findViewById(R.id.text);

            convertView.setTag(vh);
        } else {
            vh = (ViewHelper) convertView.getTag();
        }

        Layer l = getItem(position);

        vh.image.setImageBitmap(createLayerBitmap(mContext, l));
        vh.text.setText(mContext.getString(R.string.layer_position, position+1, getCount()));

        return convertView;
    }

    private Bitmap createLayerBitmap(Context context, Layer layer) {
        if(sImageSize == -1) {
            sImageSize = context.getResources().getDimensionPixelSize(R.dimen.thumb_size);
        }

        Bitmap b = Bitmap.createBitmap(sImageSize, sImageSize, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);

        c.drawColor(Color.WHITE);
        c.translate(sImageSize/2, sImageSize/2);

        if(layer != null) {
            layer.draw(c, sImageSize/2);
        }

        return b;
    }

    private static class ViewHelper {
        public ImageView image;
        public TextView text;
    }
}
