/*
 * Copyright (C) 2012 OUYA, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tv.ouya.sample.game;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;
import org.json.JSONException;
import tv.ouya.console.api.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static tv.ouya.sample.game.Options.Level.*;
import static tv.ouya.sample.game.Options.getInstance;

public class OptionsActivity extends OuyaActivity {
    /**
     * The Log Tag for this Activity
     */

    private static final String LOG_TAG = "OptionsActivity";

    /**
     * The application key. This is used to decrypt encrypted receipt responses. This should be replaced with the
     * application key obtained from the OUYA developers website.
     */

    private static byte[] applicationKey;


    private Map<Options.Level, RadioButton> levelToRadioButton;
    private Map<RadioButton, Options.Level> radioButtonToLevel;

    static private final String PREV_SELECTED_LEVEL_KEY = "last_selected_level";

    private static final String DEVELOPER_ID = "310a8f51-4d6e-4ae5-bda0-b93878e5f5d0";

    private OuyaFacade mOuyaFacade;

    /**
     * The outstanding purchase request UUIDs mapped to the purchasable the request relates to.
     */
    private final Map<String, String> mOutstandingPurchaseRequests = new HashMap<String, String>();

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.options);

        levelToRadioButton = new HashMap<Options.Level, RadioButton>();
        levelToRadioButton.put(FREEDOM, ((RadioButton) findViewById(R.id.radio_freedom)));
        levelToRadioButton.put(ALLEYWAY, ((RadioButton) findViewById(R.id.radio_alleyway)));
        levelToRadioButton.put(BOXY, ((RadioButton) findViewById(R.id.radio_boxy)));

        try {
            InputStream inputStream = getResources().openRawResource(R.raw.key);
            applicationKey = new byte[inputStream.available()];
            inputStream.read(applicationKey);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create a reverse map
        radioButtonToLevel = new HashMap<RadioButton, Options.Level>();
        for(Options.Level level : levelToRadioButton.keySet()) {
            radioButtonToLevel.put(levelToRadioButton.get(level), level);
        }

        // Initialize the UI
        processReceipts(null);
        toggleProgressIndicator(true);

        mOuyaFacade = OuyaFacade.getInstance();

        Bundle developerInfo = new Bundle();
        developerInfo.putString(OuyaFacade.OUYA_DEVELOPER_ID, DEVELOPER_ID);
        developerInfo.putByteArray(OuyaFacade.OUYA_DEVELOPER_PUBLIC_KEY, applicationKey);
        mOuyaFacade.init(this, developerInfo);

        String prevSelectedLevel = mOuyaFacade.getGameData(PREV_SELECTED_LEVEL_KEY);
        if (prevSelectedLevel != null) {
            Options.Level prevLevel = Options.Level.valueOf(prevSelectedLevel);
            Options.getInstance().setLevel(prevLevel);
        }

        levelToRadioButton.get(Options.getInstance().getLevel()).setChecked(true);

        Button quit = (Button) findViewById(R.id.back_button);
        quit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        requestReceipts();
    }

    @Override
    protected void onDestroy() {
        mOuyaFacade.shutdown();
        super.onDestroy();
    }

    private void requestReceipts() {
        if (!OuyaFacade.isRunningOnOUYASupportedHardware(this)) {
            Toast.makeText(this, "You're not running on supported hardware!", Toast.LENGTH_SHORT).show();
        }

        mOuyaFacade.requestReceipts(this, new OuyaResponseListener<Collection<Receipt>>() {
            @Override
            public void onSuccess(Collection<Receipt> receipts) {
                processReceipts(receipts);
            }

            @Override
            public void onFailure(int errorCode, String errorMessage, Bundle optionalData) {
                processReceipts(null);
                Toast.makeText(OptionsActivity.this, "Error checking purchases!\nAdditional levels not available...\n\nError " + errorCode + ": " + errorMessage + ")", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancel() {
                processReceipts(null);
                Toast.makeText(OptionsActivity.this, "You cancelled checking purchases!\nAdditional levels not available...", Toast.LENGTH_LONG).show();
            }
        });
    }

    private final String purchaseText = " [NEEDS PURCHASING]";
    private void setNeedsPurchaseText(RadioButton rb, boolean needsToBePurchased) {
        String text = rb.getText().toString();

        if (needsToBePurchased) {
            if (!text.endsWith(purchaseText)) {
                text += purchaseText;
            }
        } else {
            if (text.endsWith(purchaseText)) {
                text = text.replace(purchaseText, "");
            }
        }

        rb.setText(text);
    }

    private boolean needsPurchasing(RadioButton rb) {
        String text = rb.getText().toString();
        return text.endsWith(purchaseText);
    }

    private String getProductIdForLevel(Options.Level level) {
        switch(level) {
            case ALLEYWAY:
                return "level_alleyway";
            case BOXY:
                return "level_boxy";
        }
        return null;
    }

    private void toggleProgressIndicator(boolean progressVisible) {
        findViewById(R.id.progress_indicator).setVisibility(progressVisible ? View.VISIBLE : View.GONE);
        findViewById(R.id.levels).setVisibility(progressVisible ? View.GONE : View.VISIBLE);
    }

    private void processReceipts( Collection<Receipt> receipts ) {
        setNeedsPurchaseText(levelToRadioButton.get(ALLEYWAY), true);
        setNeedsPurchaseText(levelToRadioButton.get(BOXY), true);

        toggleProgressIndicator(false);

        if (receipts == null) {
            return;
        }

        for (Receipt r : receipts) {
            if (r.getIdentifier().equals(getProductIdForLevel(ALLEYWAY))) {
                setNeedsPurchaseText(levelToRadioButton.get(ALLEYWAY), false);
            } else if (r.getIdentifier().equals(getProductIdForLevel(BOXY))) {
                setNeedsPurchaseText(levelToRadioButton.get(BOXY), false);
            }
        }
    }

    private void requestPurchase(final Options.Level level)
        throws GeneralSecurityException, JSONException, UnsupportedEncodingException {
        final String productId = getProductIdForLevel(level);

        Purchasable purchasable = new Purchasable(productId);
        String orderId = purchasable.getOrderId();

        synchronized (mOutstandingPurchaseRequests) {
            mOutstandingPurchaseRequests.put(orderId, productId);
        }

        mOuyaFacade.requestPurchase(this, purchasable, new OuyaResponseListener<PurchaseResult>() {
            @Override
            public void onSuccess(PurchaseResult result) {
                String responseProductId;
                synchronized (mOutstandingPurchaseRequests) {
                    responseProductId = mOutstandingPurchaseRequests.remove(result.getOrderId());
                }
                if(responseProductId == null || !responseProductId.equals(productId)) {
                    onFailure(OuyaErrorCodes.THROW_DURING_ON_SUCCESS, "Purchased product is not the same as purchase request product", Bundle.EMPTY);
                    return;
                }

                if (responseProductId.equals(getProductIdForLevel(level))) {
                    setNeedsPurchaseText(levelToRadioButton.get(level), false);
                    Toast.makeText(OptionsActivity.this, "Level purchased!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(int errorCode, String errorMessage, Bundle optionalData) {
                levelToRadioButton.get(FREEDOM).setChecked(true);
                Toast.makeText(OptionsActivity.this, "Error making purchase!\n\nError " + errorCode + "\n" + errorMessage + ")", Toast.LENGTH_LONG).show();

            }

            @Override
            public void onCancel() {
                levelToRadioButton.get(FREEDOM).setChecked(true);
                Toast.makeText(OptionsActivity.this, "You cancelled the purchase!", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void onLevelRadioButtonClicked(View view) {
        RadioButton rb = ((RadioButton) view);
        if (!rb.isChecked()) {
            return;
        }

        if (needsPurchasing(rb)) {
            try {
                requestPurchase(radioButtonToLevel.get(rb));
            } catch (Exception e) {
                Log.e(LOG_TAG, "Problem trying to purchase level", e);
            }
        } else {
            Options.Level level = radioButtonToLevel.get(view);
            selectLevel(level);
        }
    }

    private void selectLevel(Options.Level level) {
        getInstance().setLevel(level);
        mOuyaFacade.putGameData(PREV_SELECTED_LEVEL_KEY, level.toString());
    }
}
