package tv.ouya.sample.cc;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import tv.ouya.console.api.content.OuyaContent;
import tv.ouya.console.api.content.OuyaMod;

import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener, OuyaContent.SearchListener, OuyaContent.InitializedListener, AdapterView.OnItemClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private GridView mGrid;
    private View mSpinner;
    private TextView mBrowseTitle;
    private TextView mBrowseButton;

    private EmblemAdapter mAdapter;
    private boolean mBrowsingOnline;
    private boolean mLoadQueued;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        findViewById(R.id.create).setOnClickListener(this);

        mSpinner = findViewById(R.id.loading_spinner);

        mBrowseTitle = (TextView) findViewById(R.id.browse_title);
        mBrowseButton = (TextView) findViewById(R.id.browse_toggle);
        mBrowseButton.setOnClickListener(this);

        mAdapter = new EmblemAdapter(this);
        mGrid = (GridView) findViewById(R.id.grid);
        mGrid.setEmptyView(findViewById(R.id.empty_text));
        mGrid.setAdapter(mAdapter);
        mGrid.setOnItemClickListener(this);

        try {
            ((TextView)findViewById(R.id.version)).setText(getPackageManager().getPackageInfo(getPackageName(),0).versionName);
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        OuyaContent.getInstance().registerInitializedListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Load/reload mods in onResume because the rate/flag calls will pause our activity, and this will allow us
        // to refresh our data
        loadEmblems();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.create:
                startActivity(new Intent(this, EditEmblemActivity.class));
                break;
            case R.id.browse_toggle:
                mBrowsingOnline = !mBrowsingOnline;
                mBrowseTitle.setText(mBrowsingOnline ? R.string.online : R.string.local);
                mBrowseButton.setText(mBrowsingOnline ? R.string.browse_local : R.string.browse_online);
                loadEmblems();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final OuyaMod mod = mAdapter.getItem(position);
        EmblemActionDialog.showDialog(this, mod, new EmblemActionDialog.ActionCompleteListener() {
            @Override
            public void onComplete(OuyaMod updatedMod) {
                // Replace entry with updated mod.
                mAdapter.replaceMod(mod, updatedMod);

                // Check for any actions that would remove entries from the list
                if (!mBrowsingOnline && !updatedMod.isInstalled()) {
                    mAdapter.removeMod(updatedMod);
                }
                if (mBrowsingOnline && !updatedMod.isPublished()) {
                    mAdapter.removeMod(updatedMod);
                }
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private void loadEmblems() {
        final OuyaContent content = OuyaContent.getInstance();
        if(!content.isInitialized()) {
            // Defer the content loading until we've initialized
            mLoadQueued = true;
            return;
        }

        // Show loading UI
        mSpinner.setVisibility(View.VISIBLE);
        mGrid.setVisibility(View.INVISIBLE);
        mGrid.getEmptyView().setVisibility(View.INVISIBLE);

        if(mBrowsingOnline) {
            content.search(OuyaContent.SortMethod.CreatedAt, this);
        } else {
            content.getInstalled(this);
        }
    }

    @Override
    public void onInitialized() {
        if(mLoadQueued) {
            mLoadQueued = false;
            loadEmblems();
        }
    }

    @Override
    public void onDestroyed() {
    }

    @Override
    public void onResults(List<OuyaMod> ouyaMods, int i) {
        // Hide loading UI
        mSpinner.setVisibility(View.GONE);
        mGrid.setVisibility(View.VISIBLE);

        mAdapter.setMods(ouyaMods);
    }

    @Override
    public void onError(int i, String s) {
        Log.w(TAG, "Error loading content: "+s);
    }
}
