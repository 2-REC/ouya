package tv.ouya.sample.cc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import tv.ouya.console.api.OuyaController;
import tv.ouya.console.api.content.OuyaContent;
import tv.ouya.console.api.content.OuyaMod;
import tv.ouya.console.api.content.OuyaModScreenshot;
import tv.ouya.sample.cc.emblem.Emblem;
import tv.ouya.sample.cc.emblem.Layer;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

public class EditEmblemActivity extends Activity implements AdapterView.OnItemClickListener, View.OnClickListener {

    private static final String TAG = EditEmblemActivity.class.getSimpleName();

    public static final String EXTRA_MOD_UUID = "modUuid";
    private static final Random sRand = new Random();

    private String mModUuid;
    private OuyaMod mMod;
    private Emblem mEmblem;

    private EmblemView mEmblemView;
    private EditText mEmblemTitle;
    private EditText mEmblemDesc;
    private Button mSaveButton;
    private Button mShapeButton;
    private Button mColorButton;
    private Button mClearButton;

    private ListView mLayerList;
    private LayerAdapter mLayerAdapter;
    private int mSelectedLayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.edit_emblem);
        mEmblemView = (EmblemView) findViewById(R.id.emblem);
        mEmblemTitle = (EditText) findViewById(R.id.title);
        mEmblemDesc = (EditText) findViewById(R.id.desc);
        mSaveButton = (Button) findViewById(R.id.save);
        mShapeButton = (Button) findViewById(R.id.shape);
        mColorButton = (Button) findViewById(R.id.color);
        mClearButton = (Button) findViewById(R.id.clear);

        final OuyaContent content = OuyaContent.getInstance();
        if(getIntent().hasExtra(EXTRA_MOD_UUID)) {
            // UUID was specified, so we'll load a local emblem
            mModUuid = getIntent().getStringExtra(EXTRA_MOD_UUID);
            if(content.isInitialized()) {
                loadEmblemFromMod();
            } else {
                // Wait until we've initialized
                content.registerInitializedListener(new OuyaContent.InitializedListener() {
                    @Override
                    public void onInitialized() {
                        loadEmblemFromMod();
                    }
                    @Override
                    public void onDestroyed() {
                    }
                });
            }
        } else {
            // New emblem
            setEmblem(new Emblem());
        }

        mSaveButton.setOnClickListener(this);
        mShapeButton.setOnClickListener(this);
        mColorButton.setOnClickListener(this);
        mClearButton.setOnClickListener(this);

        final TextWatcher fieldListener = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                updateButtons();
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        };
        mEmblemTitle.addTextChangedListener(fieldListener);
        mEmblemDesc.addTextChangedListener(fieldListener);

        mLayerAdapter = new LayerAdapter(this, mEmblem);

        mLayerList = (ListView) findViewById(R.id.layer_list);
        mLayerList.setAdapter(mLayerAdapter);
        mLayerList.setOnItemClickListener(this);

        mEmblemView.setOnChangeListener(new EmblemView.OnChangeListener() {
            @Override
            public void onChange() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLayerAdapter.notifyDataSetChanged();
                    }
                });
            }
        });

        selectLayer(0);
    }

    private void loadEmblemFromMod() {
        mMod = OuyaContent.getInstance().getLocal(mModUuid);
        mEmblemTitle.setText(mMod.getTitle());
        mEmblemDesc.setText(mMod.getDescription());

        InputStream is = null;
        try {
            // Load emblem from file
            is = mMod.openFile(Emblem.FILE_NAME);
            setEmblem(new Emblem(new JSONObject(IOUtils.toString(is))));
        } catch (Exception e) {
            Log.e(TAG, "Error reading emblem from mod.", e);
            finish();
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    private void saveEmblem() {
        if(mMod == null) {
            mMod = OuyaContent.getInstance().create();
        }

        OutputStream os = null;
        try {
            OuyaMod.Editor e = mMod.edit();

            // Clear out old data
            for(String s : mMod.getFilenames()) {
                e.deleteFile(s);
            }
            for(OuyaModScreenshot s : mMod.getScreenshots()) {
                e.removeScreenshot(s);
            }

            e.setTitle(mEmblemTitle.getText().toString());
            e.setDescription(mEmblemDesc.getText().toString());
            e.setCategory("emblem");

            // Save emblem json to file
            os = e.newFile(Emblem.FILE_NAME);
            IOUtils.write(mEmblem.toJson().toString().trim(), os);

            // Render emblem for screenshot
            int emblemSize = 1080;
            Bitmap screenshot = Bitmap.createBitmap(1920, 1080, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(screenshot);
            c.drawColor(Color.WHITE); // Emblem is rendered to JPEG, loses transparency
            c.save();
            c.translate(1920 / 2, 1080 / 2);
            c.clipRect(-emblemSize/2, -emblemSize/2, emblemSize/2, emblemSize/2);
            mEmblem.draw(c, emblemSize / 2);
            c.restore();
            e.addScreenshot(screenshot);

            // Save changes
            e.save(new OuyaContent.SaveListener() {
                @Override
                public void onSuccess(OuyaMod ouyaMod) {
                    Toast.makeText(EditEmblemActivity.this, "Saved!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(OuyaMod ouyaMod, int errorCode, String message) {
                    Log.e(TAG, "Error saving mod: "+message);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error saving emblem.", e);
            finish();
        } finally {
            IOUtils.closeQuietly(os);
        }
    }

    private void setEmblem(Emblem emblem) {
        mEmblem = emblem;
        mEmblemView.setEmblem(mEmblem);
        updateButtons();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectLayer(position);
    }

    private void selectLayer(int position) {
        mLayerList.setItemChecked(position, true);
        mSelectedLayer = position;
        mEmblemView.setSelectedLayer(position);

        mEmblemView.requestFocus();
        updateButtons();
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getAction() == KeyEvent.ACTION_DOWN) {
            OuyaController.onKeyDown(event.getKeyCode(), event);
        }
        if(event.getAction() == KeyEvent.ACTION_UP) {
            OuyaController.onKeyUp(event.getKeyCode(), event);
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        OuyaController.onGenericMotionEvent(event);
        return super.dispatchGenericMotionEvent(event);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.save:
                saveEmblem();
                break;
            case R.id.shape:
                pickShape();
                break;
            case R.id.color:
                Layer l = mEmblem.getLayer(mSelectedLayer);
                if(!layerEmpty(l)) {
                    l.color = Color.HSVToColor(new float[] {sRand.nextFloat()*360f, .5f, .5f});
                    mEmblemView.invalidate();
                    mLayerAdapter.notifyDataSetChanged();
                }
                break;
            case R.id.clear:
                mEmblem.setLayer(mSelectedLayer, null);
                mEmblemView.invalidate();
                mLayerAdapter.notifyDataSetChanged();
                break;
        }
        updateButtons();
    }

    private void pickShape() {
        final ShapeAdapter adapter = new ShapeAdapter(this);
        new AlertDialog.Builder(this)
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Layer l = mEmblem.getLayer(mSelectedLayer);
                        if (l == null) {
                            l = new Layer();
                            l.color = Color.HSVToColor(new float[]{sRand.nextFloat() * 360f, .5f, .5f});
                            mEmblem.setLayer(mSelectedLayer, l);
                        }
                        l.shape = adapter.getItem(which);

                        mEmblemView.invalidate();
                        mLayerAdapter.notifyDataSetChanged();
                        updateButtons();
                    }
                })
                .show();
    }

    private void updateButtons() {
        int nonEmptyLayers = 0;
        for(Layer l : mEmblem.getLayers()) {
            if(!layerEmpty(l)) {
                nonEmptyLayers++;
            }
        }
        Layer selected = mEmblem.getLayer(mSelectedLayer);

        mSaveButton.setEnabled(nonEmptyLayers > 0
                && mEmblemTitle.getText().toString().trim().length() > 0
                && mEmblemDesc.getText().toString().trim().length() > 0
        );
        mColorButton.setEnabled(!layerEmpty(selected));
        mClearButton.setEnabled(!layerEmpty(selected));
    }

    private boolean layerEmpty(Layer l) {
        return l == null || l.shape == null;
    }
}
