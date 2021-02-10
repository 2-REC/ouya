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

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowToast;
import org.robolectric.util.ActivityController;
import tv.ouya.console.api.OuyaFacade;
import tv.ouya.console.api.Product;
import tv.ouya.console.api.Receipt;
import tv.ouya.console.api.PurchaseResult;
import tv.ouya.console.api.TestOuyaFacade;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class IapSampleActivityTest {
    private ActivityController<IapSampleActivity> mController;
    private IapSampleActivity mActivity;
    private TestOuyaFacade mOUYAFacade;
    private LinearLayout mProductListView;
    private ListView mReceiptListView;

    @Before
    public void setup() {
        mOUYAFacade = new TestOuyaFacade();
        assertThat(OuyaFacade.getInstance()).isEqualTo(mOUYAFacade);

        mController = Robolectric.buildActivity(IapSampleActivity.class);
        if(mController == null) throw new RuntimeException();
        mController = mController.create();
        mController = mController.start();
        mController = mController.resume();
        mController = mController.visible();
        mActivity = mController.get();
//        mActivity = mController.create()
//                .start()
//                .resume()
//                .visible()
//                .get();
        callOnCreateAndFindViews();
    }

    public static Receipt newReceipt(String product, int price, String date, String gamer, String uuid, double localPrice, String currency) throws ParseException {
        SimpleDateFormat dateParser = new SimpleDateFormat("yyy-MM-dd'T'HH:mm:ss'Z'");
        dateParser.setTimeZone(TimeZone.getTimeZone("UTC"));
        return new Receipt(product, price, dateParser.parse(date), new Date(0), gamer, uuid, localPrice, currency);
    }

    @Test
    public void onCreate_initializesFacade() throws Exception {
        Context context = mOUYAFacade.getContextFromInit();
        assertThat(context).isInstanceOf(IapSampleActivity.class);
        IapSampleActivity activity = (IapSampleActivity)context;
        assertThat(mActivity).isEqualTo(activity);
        assertThat(IapSampleActivity.DEVELOPER_ID).isEqualTo(mOUYAFacade.getDeveloperId());
    }

    @Test
    public void onCreate_shouldSetReceiptListViewNonFocusable() throws Exception {
        assertThat(mActivity.findViewById(R.id.receipts).isFocusable()).isFalse();
    }

    @Test
    public void canShowProductInListView() throws Exception {
        Product product = new Product("SKU1", "red sock", 100, 1., "USD", 1., 0, "", "Sample    Developer", Product.Type.ENTITLEMENT);
        mActivity.addProducts(Arrays.asList(product));
        assertThat("red sock - $1.00").isEqualTo(getButton(0).getText().toString());
    }

    private Button getButton(int index) {
        return ((Button)((LinearLayout)mProductListView.getChildAt(index)).getChildAt(1));
    }

    @Test
    public void shouldGetProductsFromGateway() throws Exception {
        Product product1 = new Product("SKU1", "red sock", 100, 1., "USD", 1., 0, "", "Sample Developer", Product.Type.ENTITLEMENT);
        Product product2 = new Product("SKU2", "green sock", 100, 1., "USD", 1., 0, "", "Sample Developer", Product.Type.ENTITLEMENT);
        Product product3 = new Product("SKU3", "blue sock", 100, 1., "USD", 1., 0, "", "Sample Developer", Product.Type.ENTITLEMENT);
        mOUYAFacade.addProducts(product1, product2, product3);
        mOUYAFacade.expectRequestedIDs(IapSampleActivity.PRODUCT_IDENTIFIER_LIST);
        callOnCreateAndFindViews();
        mOUYAFacade.simulateProductListSuccessResponse();

        assertThat("green sock - $1.00").isEqualTo(getButton(1).getText().toString());
    }

    @Ignore
    @Test
    public void redisplayingReceiptsShouldClearOutPreviousReceipts() throws Exception {
        Product product = new Product("SKU1", "red sock", 100, 1., "USD", 1., 0, "", "Sample Developer", Product.Type.ENTITLEMENT);
        initializeWithProducts(product);

        ArrayList<Receipt> receipts = new ArrayList<Receipt>();
        receipts.add(newReceipt("sku2", 874, "1987-12-31T16:00:00Z", "gamer", "uuid", 8.74, "USD"));
        receipts.add(newReceipt("sku1", 123, "1999-12-31T16:00:00Z", "gamer", "uuid", 1.23, "USD"));
        mOUYAFacade.addReceipts(receipts);
        mOUYAFacade.simulateReceiptListSuccess();
        assertThat(((ListView)mActivity.findViewById(R.id.receipts)).getAdapter().getCount()).isEqualTo(2);

        mActivity.requestPurchase(product);
//        mOUYAFacade.simulatePurchaseSuccessResponse("{ \"identifier\":\"SKU1\", \"name\":\"red sock\", \"priceInCents\":\"100\"}");
        mOUYAFacade.simulatePurchaseSuccessResponse(new PurchaseResult(0, "uuid", "SKU1"));

        receipts = new ArrayList<Receipt>();
        receipts.add(newReceipt("sku1", 123, "2001-12-31T16:00:00Z", "gamer", "uuid", 1.23, "USD"));
        mOUYAFacade.addReceipts(receipts);
        mOUYAFacade.simulateReceiptListSuccess();
        assertThat(((ListView)mActivity.findViewById(R.id.receipts)).getCount()).isEqualTo(3);
    }

    @Ignore
    @Test
    public void shouldDisplayAllReceiptsWithNewestReceiptFirst() throws Exception {
        ArrayList<Receipt> receipts = new ArrayList<Receipt>();
        receipts.add(newReceipt("sku2", 874, "1987-12-31T16:00:00Z", "gamer", "uuid", 8.74, "USD"));
        receipts.add(newReceipt("sku1", 123, "1999-12-31T16:00:00Z", "gamer", "uuid", 1.23, "USD"));
        mOUYAFacade.addReceipts(receipts);
        mOUYAFacade.simulateReceiptListSuccess();
        assertThat(mReceiptListView.getCount()).isEqualTo(2);
        View child0 = mReceiptListView.getChildAt(0);

        SimpleDateFormat dateParser = new SimpleDateFormat("yyy-MM-dd'T'HH:mm:ss'Z'");
        dateParser.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = dateParser.parse("1999-12-31T16:00:00Z");

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String correctDisplayDate = simpleDateFormat.format(date);

        assertThat(((TextView)child0.findViewById(R.id.productId)).getText()).isEqualTo("sku1");
        assertThat(((TextView)child0.findViewById(R.id.date)).getText()).isEqualTo(correctDisplayDate);
        assertThat(((TextView)child0.findViewById(R.id.price)).getText()).isEqualTo("$1.23");
    }

    @Test
    public void shouldShutDownFacadeOnDestroy() throws Exception {
        mController.pause()
                .stop()
                .destroy();
        assertThat(mOUYAFacade.shutdownWasCalled()).isTrue();
    }

    @Test
    public void productListFailure_shouldShowToast() throws Exception {
        mOUYAFacade.simulateProductListFailure(5544, "SKU is bad", new Bundle());
        assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo("Could not fetch product information (error 5544: SKU is bad)");
    }

    @Ignore
    @Test
    public void receiptFailure_shouldPopToast() throws Exception {
        mOUYAFacade.simulateReceiptListFailure(3423, "Some failure", new Bundle());
        assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo("Could not fetch receipts (error 3423: Some failure)");
    }

    @Test
    public void clickingUuid_fetchesUuidAndPopsAlert() throws Exception {
        Robolectric.clickOn(mActivity.findViewById(R.id.gamer_uuid_button));
        mOUYAFacade.simulateGamerInfoSuccess("myUuid", "myUsername");
        assertThat(shadowOf(ShadowAlertDialog.getLatestAlertDialog()).getMessage().toString()).contains("myUuid");
        assertThat(shadowOf(ShadowAlertDialog.getLatestAlertDialog()).getMessage().toString()).contains("myUsername");
        assertThat(shadowOf(ShadowAlertDialog.getLatestAlertDialog()).getTitle()).isEqualTo("IAP Sample App");
    }

    @Test
    public void gamerUuidFailure_shouldPopToast() throws Exception {
        Robolectric.clickOn(mActivity.findViewById(R.id.gamer_uuid_button));
        mOUYAFacade.simulateGamerInfoFailure(7766, "not fetchable", new Bundle());
        assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo("Unable to fetch gamer UUID (error 7766: not fetchable)");
    }

    private void initiateAFailingPurchase(int errorCode, String errorMessage)
            throws GeneralSecurityException, UnsupportedEncodingException, JSONException {
        Product product = new Product("BAD_SKU", "bogus thing", 100, 1., "USD", 1., 0, "", "Sample Developer", Product.Type.ENTITLEMENT);
        initializeWithProducts(product);

        mActivity.requestPurchase(product);
        mOUYAFacade.simulatePurchaseFailureResponse(errorCode, errorMessage, new Bundle());
    }

    private void initializeWithProducts(Product... products) {
        mOUYAFacade.addProducts(products);
        callOnCreateAndFindViews();
        mOUYAFacade.simulateProductListSuccessResponse();
    }

    private void callOnCreateAndFindViews() {
        mActivity.onCreate(null);
        mProductListView = ((LinearLayout)mActivity.findViewById(R.id.products));
        mReceiptListView = ((ListView)mActivity.findViewById(R.id.receipts));
    }
}
