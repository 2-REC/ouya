package tv.ouya.sample.cc.emblem;

import android.graphics.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Emblem {
    public static final String FILE_NAME = "emblem.json";
    private static final int MAX_LAYERS = 32;

    private final Layer[] mLayers = new Layer[MAX_LAYERS];


    public Emblem() {
    }

    public Emblem(JSONObject json) throws JSONException{
        JSONArray layers = json.getJSONArray("layers");
        for(int i = 0; i < layers.length(); i++) {
            JSONObject layer = layers.optJSONObject(i);
            if(layer != null) {
                setLayer(i, new Layer(layer));
            }
        }
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();

        JSONArray layers = new JSONArray();
        for(int i = 0; i < MAX_LAYERS; i++) {
            Layer layer = getLayer(i);
            if(layer != null) {
                layers.put(i, layer.toJson());
            }
        }
        json.put("layers", layers);

        return json;
    }

    public void draw(Canvas canvas, float radius) {
        canvas.save();
        canvas.clipRect(-radius, -radius, radius, radius);

        for(int i = 0; i < MAX_LAYERS; i++) {
            final Layer l = mLayers[i];
            if(l != null) {
                l.draw(canvas, radius);
            }
        }
        canvas.restore();
    }

    public void setLayer(int index, Layer layer) {
        mLayers[index] = layer;
    }

    public Layer getLayer(int index) {
        return mLayers[index];
    }

    public Layer[] getLayers() {
        return mLayers;
    }

    public void clearLayer(int index) {
        mLayers[index] = null;
    }
}
