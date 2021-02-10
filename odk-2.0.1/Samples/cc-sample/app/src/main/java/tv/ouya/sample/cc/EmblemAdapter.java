package tv.ouya.sample.cc;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import tv.ouya.console.api.content.OuyaMod;
import tv.ouya.console.api.content.OuyaModScreenshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EmblemAdapter extends BaseAdapter {

    private static final String TAG = EmblemAdapter.class.getSimpleName();

    private static int sImageSize;
    private static HashMap<String, Bitmap> sBitmaps = new HashMap<String, Bitmap>();
    private static Paint sCropPaint = new Paint();

    private Context mContext;
    private ArrayList<OuyaMod> mMods = new ArrayList<OuyaMod>();

    public EmblemAdapter(Context context) {
        mContext = context;
        if(sImageSize == 0) {
            sImageSize = context.getResources().getDimensionPixelSize(R.dimen.thumb_size_large);
        }
    }

    public void setMods(List<OuyaMod> mods) {
        mMods.clear();
        mMods.addAll(mods);

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mMods.size();
    }

    @Override
    public OuyaMod getItem(int position) {
        return mMods.get(position);
    }

    public void removeMod(OuyaMod mod) {
        mMods.remove(mod);
        notifyDataSetChanged();
    }

    public void replaceMod(OuyaMod oldMod, OuyaMod newMod) {
        mMods.set(mMods.indexOf(oldMod), newMod);
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHelper helper;
        if(convertView == null) {
            convertView = View.inflate(mContext, R.layout.item_emblem, null);
            helper = new ViewHelper();

            helper.image = (ImageView) convertView.findViewById(R.id.emblem_button);
            helper.icon = (ImageView) convertView.findViewById(R.id.emblem_icon);

            convertView.setTag(helper);
        } else {
            helper = (ViewHelper) convertView.getTag();
        }

        final OuyaMod mod = getItem(position);
        final String uuid = mod.getUUID();
        helper.currentUuid = uuid;

        if(sBitmaps.containsKey(uuid)) {
            helper.image.setImageBitmap(sBitmaps.get(uuid));
        } else {
            mod.getScreenshots().get(0).load(new OuyaModScreenshot.ImageLoadedListener() {
                @Override
                public void onLoaded(OuyaModScreenshot screenshot) {
                    final Bitmap full = screenshot.getImage();
                    final Bitmap b = cropScreenshot(uuid, full);

                    // Recycle the large screenshot to save memory. If needed, the large screenshot will have to be reloaded
                    full.recycle();

                    if(uuid.equals(helper.currentUuid)) {
                        helper.image.setImageBitmap(b);
                    }
                }

                @Override
                public void onFailure(OuyaModScreenshot screenshot) {
                    Log.w(TAG, "Error loading screenshot for mod '" + mod.getTitle() + "'.");
                }
            });
        }

        final int icon = getBestIconForMod(mod);
        if(icon != -1) {
            helper.icon.setImageResource(icon);
        } else {
            helper.icon.setImageDrawable(null);
        }

        return convertView;
    }

    /** Scale and crop our large, 16:9 screenshot to only the size we need */
    private Bitmap cropScreenshot(String uuid, Bitmap screenshot) {
        Bitmap cropped = Bitmap.createBitmap(sImageSize, sImageSize, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(cropped);

        final int screenshotSize = screenshot.getHeight(); // Screenshot is 16:9 so the size of the square is the height

        // Crop out a square from the center of the screenshot, scale it to our desired image size.
        c.drawBitmap(screenshot, new Rect(
                screenshot.getWidth() / 2 - screenshotSize / 2,
                0,
                screenshot.getWidth() / 2 + screenshotSize / 2,
                screenshotSize
        ), new Rect(
                0, 0, sImageSize, sImageSize
        ), sCropPaint);

        sBitmaps.put(uuid, cropped);
        return cropped;
    }

    private static int getBestIconForMod(OuyaMod mod) {
        if(mod.hasUpdate()) {
            return R.drawable.ic_updatable;
        }
        if(mod.isOwnedByCurrentUser() && mod.isPublished()) {
            return R.drawable.ic_published;
        }
        if(!mod.isOwnedByCurrentUser() && mod.isInstalled()) {
            return R.drawable.ic_downloaded;
        }

        return -1;
    }

    private class ViewHelper {
        String currentUuid;
        ImageView image;
        ImageView icon;
    }
}
