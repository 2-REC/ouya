package derek.android.ouyatest;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class GameView extends GLSurfaceView {
    private GameRenderer _renderer;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GameView(Context context) {
        super(context);
        init();
    }

    private void init() {
        _renderer = new GameRenderer();
        setRenderer(_renderer);
    }
}
