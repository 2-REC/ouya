package tv.ouya.sample.cc;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.*;
import tv.ouya.console.api.OuyaController;
import tv.ouya.sample.cc.emblem.Emblem;
import tv.ouya.sample.cc.emblem.Layer;

public class EmblemView extends SurfaceView implements View.OnClickListener{

    private static final float MOVE_SPEED = 0.015f;
    private static final float MOVE_MAX = 2f;
    private static final float SCALE_SPEED = 0.015f;
    private static final float SCALE_MIN = 0.1f;
    private static final float SCALE_MAX = 20f;
    private static final float ROTATE_SPEED = 1.5f;

    private static Paint sFocusPaint;
    static {
        sFocusPaint = new Paint();
        sFocusPaint.setARGB(255, 0, 200, 200);
        sFocusPaint.setStyle(Paint.Style.STROKE);
        sFocusPaint.setStrokeWidth(8);
    }
    private static Paint sGridPaint;
    static {
        final Bitmap transparencyBitmap = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888);
        final Canvas transparencyCanvas = new Canvas(transparencyBitmap);
        final Paint grayPaint = new Paint();
        grayPaint.setARGB(255, 192, 192, 192);

        transparencyCanvas.drawColor(Color.WHITE);
        transparencyCanvas.drawRect(0, 0, 8, 8, grayPaint);
        transparencyCanvas.drawRect(8, 8, 16, 16, grayPaint);

        sGridPaint = new Paint();
        sGridPaint.setShader(new BitmapShader(transparencyBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT));
    }

    private Emblem mEmblem;

    private final SurfaceHolder mHolder;
    private InputThread mInputThread;

    private int mSelectedLayer;
    private boolean mWantsToEdit;
    private OnChangeListener mChangeListener;
    private boolean mChangeMade = false;
    private boolean mEditingLastFrame = false;

    public EmblemView(Context context) {
        super(context);
        mHolder = getHolder();
        init();
    }

    public EmblemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHolder = getHolder();
        init();
    }

    public EmblemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mHolder = getHolder();
        init();
    }

    private void init() {
        mInputThread = new InputThread();
        mHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mInputThread.setRunning(true);
                mInputThread.start();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                boolean retry = true;
                mInputThread.setRunning(false);
                while (retry) {
                    try {
                        mInputThread.join();
                        retry = false;
                    } catch (InterruptedException e) {
                        // Ignore and keep trying
                    }
                }
            }
        });

        setFocusable(true);
        setWillNotDraw(false);
        super.setOnClickListener(this);
    }

    public void setOnChangeListener(OnChangeListener listener) {
        mChangeListener = listener;
    }

    @Override
    public void onClick(View v) {
        mWantsToEdit = true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return isEditing() || super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return isEditing() || super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return isEditing() || super.onGenericMotionEvent(event);
    }

    private void inputFrame() {
        OuyaController.startOfFrame();

        final boolean editing = isEditing();
        if(!mEditingLastFrame && editing) {
            mChangeMade = false;
        }
        if(mEditingLastFrame && !editing) {
            if(mChangeListener != null && mChangeMade) {
                mChangeListener.onChange();
            }
        }

        final Layer l = mEmblem.getLayer(mSelectedLayer);
        final OuyaController c = OuyaController.getControllerByPlayer(0);
        if(c != null && isEditing()) {
            if(c.getButton(OuyaController.BUTTON_A) || c.getButton(OuyaController.BUTTON_O)) {
                mWantsToEdit = false;
                return;
            }

            if(c.getButton(OuyaController.BUTTON_DPAD_RIGHT)) {
                l.posX += MOVE_SPEED;
                mChangeMade = true;
            }
            if(c.getButton(OuyaController.BUTTON_DPAD_LEFT)) {
                l.posX -= MOVE_SPEED;
                mChangeMade = true;
            }
            if(c.getButton(OuyaController.BUTTON_DPAD_DOWN)) {
                l.posY += MOVE_SPEED;
                mChangeMade = true;
            }
            if(c.getButton(OuyaController.BUTTON_DPAD_UP)) {
                l.posY -= MOVE_SPEED;
                mChangeMade = true;
            }
            l.posX = clamp(l.posX, -MOVE_MAX, MOVE_MAX);
            l.posY = clamp(l.posY, -MOVE_MAX, MOVE_MAX);

            final float LS_X = c.getAxisValue(OuyaController.AXIS_LS_X);
            final float LS_Y = c.getAxisValue(OuyaController.AXIS_LS_Y);
            final float RS_X = c.getAxisValue(OuyaController.AXIS_RS_X);
            final float RS_Y = c.getAxisValue(OuyaController.AXIS_RS_Y);

            if(Math.abs(LS_X) > OuyaController.STICK_DEADZONE) {
                l.scaleX += LS_X * SCALE_SPEED;
                l.scaleX = clamp(l.scaleX, SCALE_MIN, SCALE_MAX);
                mChangeMade = true;
            }
            if(Math.abs(LS_Y) > OuyaController.STICK_DEADZONE) {
                l.scaleY -= LS_Y*SCALE_SPEED;
                l.scaleY = clamp(l.scaleY, SCALE_MIN, SCALE_MAX);
                mChangeMade = true;
            }

            if(Math.abs(RS_X) > OuyaController.STICK_DEADZONE) {
                l.rotation += RS_X*ROTATE_SPEED;
                l.rotation %= 360f;
                mChangeMade = true;
            }
        }

        mEditingLastFrame = editing;
    }

    private static float clamp(final float value, float min, float max) {
        return Math.max(Math.min(value, max), min);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        render(canvas);
    }

    private void render(Canvas canvas) {
        if(mEmblem == null) return;

        final float padding = sFocusPaint.getStrokeWidth()/2;

        if(isEditing()) {
            canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), sGridPaint);
        }

        canvas.save();
        canvas.translate(getMeasuredWidth()/2, getMeasuredHeight()/2);
        mEmblem.draw(canvas, getMeasuredWidth()/2-padding*2);
        canvas.restore();

        if(isFocused()) {
            canvas.drawRect(padding, padding, getMeasuredWidth()-padding, getMeasuredHeight()-padding, sFocusPaint);
        }
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);

        if(!gainFocus) {
            mWantsToEdit = false;
        }
    }

    public boolean isEditing() {
        return isFocused()
                && mEmblem != null
                && mSelectedLayer >= 0
                && mEmblem.getLayer(mSelectedLayer) != null
                && mWantsToEdit;
    }

    public void setEmblem(Emblem emblem) {
        mEmblem = emblem;
    }

    public void setSelectedLayer(int layer) {
        mSelectedLayer = layer;
    }

    public static interface OnChangeListener {
        public void onChange();
    }

    private class InputThread extends Thread {
        private boolean mmRunning;

        public void setRunning(boolean running) {
            mmRunning = running;
        }

        @SuppressLint("WrongCall")
        @Override
        public void run() {
            final int FRAME_PERIOD = 1000 / 60; // 60 FPS
            final int MAX_FRAME_SKIPS = 5;
            Canvas canvas;

            long beginTime;		// the time when the cycle begun
            long timeDiff;		// the time it took for the cycle to execute
            int sleepTime;		// ms to sleep (<0 if we're behind)
            int framesSkipped;	// number of frames being skipped

            while(mmRunning) {
                canvas = null;
                try {
                    canvas = mHolder.lockCanvas();
                    synchronized (mHolder) {
                        beginTime = System.currentTimeMillis();
                        framesSkipped = 0;

                        inputFrame();
                        postInvalidate();

                        timeDiff = System.currentTimeMillis() - beginTime;
                        sleepTime = (int)(FRAME_PERIOD - timeDiff);

                        if (sleepTime > 0) {
                            try {
                                Thread.sleep(sleepTime);
                            } catch (InterruptedException e) {}
                        }

                        while (sleepTime < 0 && framesSkipped < MAX_FRAME_SKIPS) {
                            inputFrame();

                            sleepTime += FRAME_PERIOD;
                            framesSkipped++;
                        }
                    }
                } finally {
                    if (canvas != null) {
                        mHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }
    }
}
