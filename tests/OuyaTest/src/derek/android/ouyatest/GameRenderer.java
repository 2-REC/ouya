package derek.android.ouyatest;

import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class GameRenderer implements GLSurfaceView.Renderer {

    static public GameRenderer s_instance = null;
    static public final float BOARD_WIDTH = 40.0f;
    static public final float BOARD_HEIGHT = 40.0f;

    private final List<RenderObject> objects;
    private final List<RenderObject> toBeAdded;
    private final List<RenderObject> toBeDeleted;

    private float _red = 0.9f;
    private float _green = 0.2f;
    private float _blue = 0.2f;
    private float width = 1920;
    private float height = 1080;

    public GameRenderer() {
        s_instance = this;
        objects = new LinkedList<RenderObject>();
        toBeAdded = new ArrayList<RenderObject>();
        toBeDeleted = new ArrayList<RenderObject>();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();

        // orthographic
        float ratio = width/height;
        float extraSpacePerSide = ((BOARD_WIDTH * ratio) - BOARD_WIDTH ) / 2.0f;
        float left = 0.0f - extraSpacePerSide;
        float right = BOARD_WIDTH + extraSpacePerSide;
        gl.glOrthof(left, right, BOARD_HEIGHT, 0, -10.0f, 10.0f);
        //gl.glFrustumf(-10.0f, 10.0f, 10.0f, -10.0f, 0.1f, 100.0f);
        gl.glViewport(0, 0, (int) width, (int) height);
        gl.glMatrixMode(GL10.GL_MODELVIEW);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glClearColor(_red, _green, _blue, 1.0f);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int w, int h) {
        width = w;
        height = h;
        gl.glViewport(0, 0, w, h);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        gl.glLoadIdentity();

        synchronized (objects) {

            synchronized (toBeDeleted) {
                for(RenderObject o : toBeDeleted) {
                    objects.remove(o);
                }
                toBeDeleted.clear();
            }
            synchronized (toBeAdded) {
                for(RenderObject o : toBeAdded) {
                    objects.add(o);
                }
                toBeAdded.clear();
            }

            for(RenderObject o : objects) {
                o.update();
                o.doRender(gl);
            }
        }
    }

    public void addRenderObject(RenderObject obj) {
        synchronized (toBeAdded) {
            toBeAdded.add(obj);
        }
    }

    public void removeRenderObject(RenderObject obj) {
        synchronized (toBeDeleted) {
            toBeDeleted.add(obj);
        }
    }

    public RenderObject getCollidingObject(RenderObject obj) {
        for(RenderObject o : objects) {
            if (o != obj && o instanceof Wall) {
                if (o.doesCollide(obj)) {
                    return o;
                }
            }
        }
        for(RenderObject o : objects) {
            if (o != obj && !(o instanceof Wall)) {
                if (o.doesCollide(obj)) {
                    return o;
                }
            }
        }
        return null;
    }
}
