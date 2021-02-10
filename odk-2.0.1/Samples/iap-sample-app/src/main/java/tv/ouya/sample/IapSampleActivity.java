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

package tv.ouya.sample;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import org.json.JSONException;
import tv.ouya.console.api.*;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.*;

import static tv.ouya.console.api.OuyaController.BUTTON_O;

public class IapSampleActivity extends Activity {

    /**
     * The tag for log messages
     */

    private static final String LOG_TAG = "OUYAIapSample";

    /**
     * Log onto the developer website (you should have received a URL, a username and a password in email)
     * and get your developer ID. Plug it in here. Use your developer ID, not your developer UUID.
     * <p/>
     * The current value is just a sample developer account. You should change it.
     */
    public static final String DEVELOPER_ID = "310a8f51-4d6e-4ae5-bda0-b93878e5f5d0";

    /**
     * The application key. This is used to decrypt encrypted receipt responses. This should be replaced with the
     * application key obtained from the OUYA developers website.
     */

    private static final byte[] APPLICATION_KEY = {
            (byte) 0x30,(byte) 0x81,(byte) 0x9f,(byte) 0x30,(byte) 0x0d,(byte) 0x06,(byte) 0x09,(byte) 0x2a,
            (byte) 0x86,(byte) 0x48,(byte) 0x86,(byte) 0xf7,(byte) 0x0d,(byte) 0x01,(byte) 0x01,(byte) 0x01,
            (byte) 0x05,(byte) 0x00,(byte) 0x03,(byte) 0x81,(byte) 0x8d,(byte) 0x00,(byte) 0x30,(byte) 0x81,
            (byte) 0x89,(byte) 0x02,(byte) 0x81,(byte) 0x81,(byte) 0x00,(byte) 0xdc,(byte) 0xbe,(byte) 0x5f,
            (byte) 0x43,(byte) 0x14,(byte) 0x48,(byte) 0xb1,(byte) 0xb3,(byte) 0x0d,(byte) 0x2f,(byte) 0x7d,
            (byte) 0x69,(byte) 0x02,(byte) 0xda,(byte) 0xae,(byte) 0x19,(byte) 0xcd,(byte) 0x0f,(byte) 0xc8,
            (byte) 0x70,(byte) 0x58,(byte) 0x72,(byte) 0x30,(byte) 0xf5,(byte) 0xd1,(byte) 0x18,(byte) 0xea,
            (byte) 0x98,(byte) 0x3d,(byte) 0x50,(byte) 0x3c,(byte) 0xcb,(byte) 0xb2,(byte) 0x1b,(byte) 0xf7,
            (byte) 0x65,(byte) 0x4c,(byte) 0xb0,(byte) 0x82,(byte) 0x0e,(byte) 0x43,(byte) 0xc4,(byte) 0x67,
            (byte) 0x58,(byte) 0x05,(byte) 0x18,(byte) 0xf9,(byte) 0x45,(byte) 0x20,(byte) 0xcb,(byte) 0x14,
            (byte) 0x4a,(byte) 0xb7,(byte) 0xa7,(byte) 0x55,(byte) 0x83,(byte) 0x45,(byte) 0x6e,(byte) 0x5d,
            (byte) 0x93,(byte) 0xf7,(byte) 0xe2,(byte) 0x5d,(byte) 0x8e,(byte) 0x3b,(byte) 0xf3,(byte) 0x93,
            (byte) 0x6c,(byte) 0x30,(byte) 0xe0,(byte) 0x13,(byte) 0xd5,(byte) 0x21,(byte) 0xf1,(byte) 0x21,
            (byte) 0x90,(byte) 0xa4,(byte) 0xed,(byte) 0x07,(byte) 0x51,(byte) 0x78,(byte) 0x56,(byte) 0xa6,
            (byte) 0xcb,(byte) 0x15,(byte) 0x99,(byte) 0x46,(byte) 0xc4,(byte) 0xb8,(byte) 0xc7,(byte) 0xbd,
            (byte) 0xd8,(byte) 0x1c,(byte) 0x87,(byte) 0x76,(byte) 0xc8,(byte) 0x54,(byte) 0x85,(byte) 0x2a,
            (byte) 0x51,(byte) 0xcf,(byte) 0x5b,(byte) 0xd2,(byte) 0xc7,(byte) 0x3a,(byte) 0xbd,(byte) 0x1b,
            (byte) 0x42,(byte) 0x11,(byte) 0x65,(byte) 0xae,(byte) 0x17,(byte) 0xbb,(byte) 0x55,(byte) 0xf4,
            (byte) 0x58,(byte) 0x54,(byte) 0x9f,(byte) 0xfa,(byte) 0x59,(byte) 0x5c,(byte) 0xbf,(byte) 0xda,
            (byte) 0xfe,(byte) 0xbe,(byte) 0x34,(byte) 0xc6,(byte) 0xc3,(byte) 0x02,(byte) 0x03,(byte) 0x01,
            (byte) 0x00,(byte) 0x01,
    };

    /**
     * Before this app will run, you must define some purchasable items on the developer website. Once
     * you have defined those items, put their Product IDs in the List below.
     * <p/>
     * The Product IDs below are those in our developer account. You should change them.
     */
    public static final List<Purchasable> PRODUCT_IDENTIFIER_LIST = Arrays.asList(
        new Purchasable("long_sword"),
        new Purchasable("sharp_axe"),
        new Purchasable("awesome_sauce"),
        new Purchasable("cat_facts"),
        new Purchasable("__DECLINED__THIS_PURCHASE")
    );

    /**
     * The saved instance state key for products
     */

    private static final String PRODUCTS_INSTANCE_STATE_KEY = "Products";

    /**
     * The saved instance state key for receipts
     */

    private static final String RECEIPTS_INSTANCE_STATE_KEY = "Receipts";

    /**
     * The ID used to track the activity started by an authentication intent during a purchase.
     */

    private static final int PURCHASE_AUTHENTICATION_ACTIVITY_ID = 1;

    /**
     * The ID used to track the activity started by an authentication intent during a request for
     * the gamers UUID.
     */

    private static final int GAMER_UUID_AUTHENTICATION_ACTIVITY_ID = 2;

    /**
     * The receipt adapter will display a previously-purchased item in a cell in a ListView. It's not part of the in-app
     * purchase API. Neither is the ListView itself.
     */
    private ListView receiptListView;

    /**
     * Your game talks to the OuyaFacade, which hides all the mechanics of doing an in-app purchase.
     */
    private OuyaFacade ouyaFacade;

    private List<Product> mProductList;
    private List<Receipt> mReceiptList;

    /**
     * The outstanding purchase request UUIDs.
     */

    private final Map<String, Product> mOutstandingPurchaseRequests = new HashMap<String, Product>();

    /**
     * Broadcast listener to handle re-requesting the receipts when a user has re-authenticated
     */

    private BroadcastReceiver mAuthChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            requestReceipts();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle developerInfo = new Bundle();
        developerInfo.putString(OuyaFacade.OUYA_DEVELOPER_ID, DEVELOPER_ID);
        developerInfo.putByteArray(OuyaFacade.OUYA_DEVELOPER_PUBLIC_KEY, APPLICATION_KEY);

        ouyaFacade = OuyaFacade.getInstance();
        ouyaFacade.init(this, developerInfo);

        // Uncomment this line to test against the server using "fake" credits.
        // This will also switch over to a separate "test" purchase history.
        //ouyaFacade.setTestMode();

        setContentView(R.layout.sample_app);

        receiptListView = (ListView) findViewById(R.id.receipts);
        receiptListView.setFocusable(false);

        /*
         * In order to avoid "application not responding" popups, Android demands that long-running operations
         * happen on a background thread. Listener objects provide a way for you to specify what ought to happen
         * at the end of the long-running operation. Examples of this pattern in Android include
         * android.os.AsyncTask.
         */
        findViewById(R.id.gamer_uuid_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchGamerInfo();
            }
        });

        // Attempt to restore the product and receipt list from the savedInstanceState Bundle
        if(savedInstanceState != null) {
            if(savedInstanceState.containsKey(PRODUCTS_INSTANCE_STATE_KEY)) {
                Parcelable[] products = savedInstanceState.getParcelableArray(PRODUCTS_INSTANCE_STATE_KEY);
                mProductList = new ArrayList<Product>(products.length);
                for(Parcelable product : products) {
                    mProductList.add((Product) product);
                }
                addProducts();
            }
            if(savedInstanceState.containsKey(RECEIPTS_INSTANCE_STATE_KEY))  {
                Parcelable[] receipts = savedInstanceState.getParcelableArray(RECEIPTS_INSTANCE_STATE_KEY);
                mReceiptList = new ArrayList<Receipt>(receipts.length);
                for(Parcelable receipt : receipts) {
                    mReceiptList.add((Receipt) receipt);
                }
                addReceipts();
            }
        }

        // Request the product list if it could not be restored from the savedInstanceState Bundle
        if(mProductList == null) {
            requestProducts();
        }

        // Make sure the receipt ListView starts empty if the receipt list could not be restored
        // from the savedInstanceState Bundle.
        if(mReceiptList == null) {
            receiptListView.setAdapter(new ReceiptAdapter(this, new Receipt[0]));
        }
    }

    /**
     * Request an up to date list of receipts and start listening for any account changes
     * whilst the application is running.
     */
    @Override
    public void onStart() {
        super.onStart();

        // Request an up to date list of receipts for the user.
        requestReceipts();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Register to receive notifications about account changes. This will re-query
        // the receipt list in order to ensure it is always up to date for whomever
        // is logged in.
        IntentFilter accountsChangedFilter = new IntentFilter(AccountManager.LOGIN_ACCOUNTS_CHANGED_ACTION);
        registerReceiver(mAuthChangeReceiver, accountsChangedFilter);
    }

    /**
     * Unregister the account change listener when the application is paused.
     */
    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(mAuthChangeReceiver);
    }

    /**
     * Check for the result from a call through to the authentication intent. If the authentication was
     * successful then re-try the purchase.
     */

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        Log.d(LOG_TAG, "Processing activity result");

        // Forward this result to the facade, in case it is waiting for any activity results
        if(ouyaFacade.processActivityResult(requestCode, resultCode, data)) {
            return;
        }

        if(resultCode == RESULT_OK) {
            switch (requestCode) {
                case GAMER_UUID_AUTHENTICATION_ACTIVITY_ID:
                    fetchGamerInfo();
                    break;
                case PURCHASE_AUTHENTICATION_ACTIVITY_ID:
                    restartInterruptedPurchase();
                    break;
            }
        }
    }

    /**
     * Restart an interrupted purchase.
     */

    private void restartInterruptedPurchase() {
        final String suspendedPurchaseId = OuyaPurchaseHelper.getSuspendedPurchase(this);
        if(suspendedPurchaseId == null) {
            return;
        }

        try {
            for(Product thisProduct : mProductList) {
                if(suspendedPurchaseId.equals(thisProduct.getIdentifier())) {
                    requestPurchase(thisProduct);
                    break;
                }
            }
        } catch (Exception ex) {
            Log.e(LOG_TAG, "Error during purchase request", ex);
            showError(ex.getMessage());
        }
    }

    /**
     * Save the products and receipts if we're going for a restart
     */

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        if(mProductList != null) {
            outState.putParcelableArray(PRODUCTS_INSTANCE_STATE_KEY, mProductList.toArray(new Product[mProductList.size()]));
        }
        if(mReceiptList != null) {
            outState.putParcelableArray(RECEIPTS_INSTANCE_STATE_KEY, mReceiptList.toArray(new Receipt[mReceiptList.size()]));
        }
    }

    /*
     * The IAP Facade registers a broadcast receiver with Android. You should take care to call shutdown(),
     * which unregisters the broadcast receiver, when you're done with the IAP Facade.
     */
    @Override
    protected void onDestroy() {
        ouyaFacade.shutdown();
        super.onDestroy();
    }

    /**
     * Get the list of products the user can purchase from the server.
     */
    private synchronized void requestProducts() {
        Log.d(LOG_TAG, "Requesting products");
        ouyaFacade.requestProductList(this, PRODUCT_IDENTIFIER_LIST, new CancelIgnoringOuyaResponseListener<List<Product>>() {
            @Override
            public void onSuccess(final List<Product> products) {
                mProductList = products;
                addProducts();
            }

            @Override
            public void onFailure(int errorCode, String errorMessage, Bundle optionalData) {
                // Your app probably wants to do something more sophisticated than popping a Toast. This is
                // here to tell you that your app needs to handle this case: if your app doesn't display
                // something, the user won't know of the failure.
                Toast.makeText(IapSampleActivity.this, "Could not fetch product information (error " + errorCode + ": " + errorMessage + ")", Toast.LENGTH_LONG).show();
            }
        });
    }

    private synchronized void fetchGamerInfo() {
        Log.d(LOG_TAG, "Requesting gamerinfo");
        ouyaFacade.requestGamerInfo(this, new CancelIgnoringOuyaResponseListener<GamerInfo>() {
            @Override
            public void onSuccess(GamerInfo result) {
                new AlertDialog.Builder(IapSampleActivity.this)
                        .setTitle(getString(R.string.alert_title))
                        .setMessage(getResources().getString(R.string.userinfo, result.getUsername(), result.getUuid()))
                        .setPositiveButton(R.string.ok, null)
                        .show();
            }

            @Override
            public void onFailure(int errorCode, String errorMessage, Bundle optionalData) {
                Log.w(LOG_TAG, "fetch gamer UUID error (code " + errorCode + ": " + errorMessage + ")");
                boolean wasHandledByAuthHelper =
                        OuyaAuthenticationHelper.
                                handleError(
                                        IapSampleActivity.this, errorCode, errorMessage,
                                        optionalData, GAMER_UUID_AUTHENTICATION_ACTIVITY_ID,
                                        new OuyaResponseListener<Void>() {
                                            @Override
                                            public void onSuccess(Void result) {
                                                fetchGamerInfo();   // Retry the fetch if the error was handled.
                                            }

                                            @Override
                                            public void onFailure(int errorCode, String errorMessage,
                                                                  Bundle optionalData) {
                                                showError("Unable to fetch gamer UUID (error " +
                                                        errorCode + ": " + errorMessage + ")");
                                            }

                                            @Override
                                            public void onCancel() {
                                                showError("Unable to fetch gamer UUID");
                                            }
                                        });

                if (!wasHandledByAuthHelper) {
                    showError("Unable to fetch gamer UUID (error " + errorCode + ": " + errorMessage + ")");
                }
            }
        });
    }

    /**
     * Request the receipts from the users previous purchases from the server.
     */

    private synchronized void requestReceipts() {
        Log.d(LOG_TAG, "Requesting receipts");
        ouyaFacade.requestReceipts(this, new ReceiptListener());
    }

    /**
     * Add all of the products for this application to the UI as buttons for the user to click.
     */

    private void addProducts() {
        if(mProductList != null) {
            for (Product product : mProductList) {
                ((ViewGroup) findViewById(R.id.products)).addView(makeButton(product));
            }
        }
    }

    /**
     * Change the Adapter on the receipt ListView to show the currently known receipts.
     */

    private void addReceipts() {
        if(mReceiptList != null) {
            receiptListView.setAdapter(
                    new ReceiptAdapter(IapSampleActivity.this, mReceiptList.toArray(new Receipt[mReceiptList.size()]))
            );
        }
    }

    @Deprecated // Testing only
    public void addProducts(List<Product> products) {
        for (Product product : products) {
            ((ViewGroup) findViewById(R.id.products)).addView(makeButton(product));
        }
    }

    /**
     * Create a button to show the user which they can click on to purchase the item.
     *
     * @param item The item that can be purchased by clicking on the button.
     *
     * @return The Button to show in the UI.
     */

    private View makeButton(Product item) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.product_item, null, false);
        String buttonText = item.getName() + " - " + item.getFormattedPrice();
        Button button = (Button) view.findViewById(R.id.purchase_product_button);
        button.setOnClickListener(new RequestPurchaseClickListener());
        button.setText(buttonText);
        button.setTag(item);
        return view;
    }

    /*
     * This will be called when the user clicks on an item in the ListView.
     */
    public void requestPurchase(final Product product)
        throws GeneralSecurityException, UnsupportedEncodingException, JSONException {

        Purchasable purchasable = new Purchasable(product.getIdentifier());
        String orderId = purchasable.getOrderId();

        synchronized (mOutstandingPurchaseRequests) {
            mOutstandingPurchaseRequests.put(orderId, product);
        }
        ouyaFacade.requestPurchase(this, purchasable, new PurchaseListener(product));
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(keyCode == BUTTON_O) {
            View focusedButton = getCurrentFocus();
            focusedButton.performClick();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    /**
     * OnClickListener to handle purchase requests.
     */

    public class RequestPurchaseClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            try {
                requestPurchase((Product) v.getTag());
            } catch(Exception ex) {
                Log.e(LOG_TAG, "Error requesting purchase", ex);
                showError(ex.getMessage());
            }
        }
    }

    /**
     * Display an error to the user. We're using a toast for simplicity.
     */

    private void showError(final String errorMessage) {
        Toast.makeText(IapSampleActivity.this, errorMessage, Toast.LENGTH_LONG).show();
    }

    /**
     * The callback for when the list of user receipts has been requested.
     */
    private class ReceiptListener implements OuyaResponseListener<Collection<Receipt>>
    {
        /**
         * Handle the successful fetching of the data for the receipts from the server.
         *
         * @param receiptResponse The response from the server.
         */
        @Override
        public void onSuccess(Collection<Receipt> receipts) {
            mReceiptList = new ArrayList<Receipt>(receipts);
            IapSampleActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addReceipts();
                }
            });
        }

        /**
         * Handle a failure. Because displaying the receipts is not critical to the application we just show an error
         * message rather than asking the user to authenticate themselves just to start the application up.
         *
         * @param errorCode An HTTP error code between 0 and 999, if there was one. Otherwise, an internal error code from the
         *                  Ouya server, documented in the {@link OuyaErrorCodes} class.
         *
         * @param errorMessage Empty for HTTP error codes. Otherwise, a brief, non-localized, explanation of the error.
         *
         * @param optionalData A Map of optional key/value pairs which provide additional information.
         */

        @Override
        public void onFailure(int errorCode, String errorMessage, Bundle optionalData) {
            Log.w(LOG_TAG, "Request Receipts error (code " + errorCode + ": " + errorMessage + ")");
            showError("Could not fetch receipts (error " + errorCode + ": " + errorMessage + ")");
        }

        /*
         * Handle user canceling
         */
        @Override
        public void onCancel()
        {
            showError("User cancelled getting receipts");
        }
    }

    /**
     * The callback for when the user attempts to purchase something. We're not worried about
     * the user cancelling the purchase so we extend CancelIgnoringOuyaResponseListener, if
     * you want to handle cancelations differently you should extend OuyaResponseListener and
     * implement an onCancel method.
     *
     * @see tv.ouya.console.api.CancelIgnoringOuyaResponseListener
     * @see tv.ouya.console.api.OuyaResponseListener#onCancel()
     */
    private class PurchaseListener implements OuyaResponseListener<PurchaseResult> {
        /**
         * The ID of the product the user is trying to purchase. This is used in the
         * onFailure method to start a re-purchase if they user wishes to do so.
         */

        private Product mProduct;

        /**
         * Constructor. Store the ID of the product being purchased.
         */

        PurchaseListener(final Product product) {
            mProduct = product;
        }

        /**
         * Handle a successful purchase.
         *
         * @param result The response from the server.
         */
        @Override
        public void onSuccess(PurchaseResult result) {

            Product storedProduct;
            synchronized (mOutstandingPurchaseRequests) {
                storedProduct = mOutstandingPurchaseRequests.remove(result.getOrderId());
            }

            // If the cached product doesn't have the same ProductID that was actually purchased OR
            // If the cached product doesn't have the same ProductID that we wanted to be purchased
            // Then error out
            if (storedProduct == null ||
                !storedProduct.getIdentifier().equals(result.getProductIdentifier()) ||
                !storedProduct.getIdentifier().equals(mProduct.getIdentifier())) {
                onFailure(OuyaErrorCodes.THROW_DURING_ON_SUCCESS, "Purchased product is not the same as purchase request product", Bundle.EMPTY);
                return;
            }

            requestReceipts();
        }

        @Override
        public void onFailure(int errorCode, String errorMessage, Bundle optionalData) {
        }

        /*
         * Handling the user canceling
         */
        @Override
        public void onCancel() {
            showError("User cancelled purchase");
        }
    }

}
