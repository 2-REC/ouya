package tv.ouya.sample.cc.emblem;

import android.graphics.*;
import org.json.JSONException;
import org.json.JSONObject;

public class Layer {
    public Shape shape;
    public int color = Color.BLACK;
    public boolean filled = true;
    public float posX;
    public float posY;
    public float scaleX = .8f;
    public float scaleY = .8f;
    public float rotation;

    private static Path sPath = new Path();
    private static Paint sPaint = new Paint();
    private static Matrix sMatrix = new Matrix();

    static {
        sPaint.setStrokeWidth(8f);
        sPaint.setAntiAlias(true);
    }

    public Layer() {
    }

    public Layer(JSONObject json) throws JSONException {
        shape = Shape.valueOf(json.getString("shape"));
        color = json.getInt("color");
        filled = json.getBoolean("filled");
        posX = (float) json.getDouble("posX");
        posY = (float) json.getDouble("posY");
        scaleX = (float) json.getDouble("scaleX");
        scaleY = (float) json.getDouble("scaleY");
        rotation = (float) json.getDouble("rotation");
    }

    public JSONObject toJson() throws JSONException {
        final JSONObject json = new JSONObject();
        json.put("shape", shape.toString());
        json.put("color", color);
        json.put("filled", filled);
        json.put("posX", posX);
        json.put("posY", posY);
        json.put("scaleX", scaleX);
        json.put("scaleY", scaleY);
        json.put("rotation", rotation);
        return json;
    }

    public synchronized void draw(Canvas canvas, float radius) {
        if(shape == null) {
            return;
        }

        sMatrix.reset();
        sMatrix.setScale(scaleX, scaleY);
        sMatrix.postRotate(rotation);
        sMatrix.postTranslate(posX, posY);
        sMatrix.postScale(radius, radius);

        sPath.reset();
        sPath.set(shape.path);
        sPath.transform(sMatrix);

        sPaint.setColor(color);
        sPaint.setStyle(filled ? Paint.Style.FILL : Paint.Style.STROKE);

        canvas.drawPath(sPath, sPaint);
    }
}
