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

package tv.ouya.console.api;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static junit.framework.Assert.assertEquals;

public class TestOuyaFacade extends OuyaFacade {
    private ArrayList<Product> mProducts = new ArrayList<Product>();
    private OuyaResponseListener<List<Product>> mProductListListener;
    private List<Purchasable> mExpectedProductListIds;
    private Purchasable mPurchaseRequestId;
    private Purchasable mExpectedPurchaseRequestId;
    private OuyaResponseListener<PurchaseResult> mPurchaseRequestListener;
    private Context mContextFromInit;
    private String mDeveloperId;
    private Collection<Receipt> mReceipt;
    private OuyaResponseListener<Collection<Receipt>> mReceiptListListener;
    private boolean mShutdownWasCalled;
    private boolean mRequestReceiptsWasCalled;
    private OuyaResponseListener<GamerInfo> mGamerInfoListener;

    public TestOuyaFacade() {
        OuyaFacade.setInstance(this);
        clear();
    }

    public void addProducts(Product... products) {
        mProducts.addAll(Arrays.asList(products));
    }

    @Override
    public void init(Context context, Bundle data) {
        mContextFromInit = context;
        mDeveloperId = data.getString(OuyaFacade.OUYA_DEVELOPER_ID);
    }

    @Override
    public void requestProductList(Activity currentActivity, List<Purchasable> purchasables, OuyaResponseListener<List<Product>> productListListener) {
        mProductListListener = productListListener;
        if (mExpectedProductListIds != null) {
            assertEquals(mExpectedProductListIds, purchasables);
        }
    }

    @Override
    public void requestPurchase(Activity currentActivity, Purchasable purchasable, OuyaResponseListener<PurchaseResult> purchaseListener) {
        mPurchaseRequestId = purchasable;
        mPurchaseRequestListener = purchaseListener;
        if (mExpectedPurchaseRequestId != null) {
            assertEquals(mExpectedPurchaseRequestId, purchasable);
        }
    }

    @Override
    public void requestReceipts(Activity currentActivity, OuyaResponseListener<Collection<Receipt>> receiptListListener) {
        mReceiptListListener = receiptListListener;
        mRequestReceiptsWasCalled = true;
    }

    @Override
    public void requestGamerInfo(Activity currentActivity, OuyaResponseListener<GamerInfo> gamerUuidListener) {
        mGamerInfoListener = gamerUuidListener;
    }

    @Override
    public void shutdown() {
        mShutdownWasCalled = true;
    }

    public void simulateProductListSuccessResponse() {
        mProductListListener.onSuccess(mProducts);
    }

    public void expectRequestedIDs(List<Purchasable> expectedIDs) {
        mExpectedProductListIds = expectedIDs;
    }

    public void expectPurchaseRequestID(String expectedID) {
        mExpectedPurchaseRequestId = new Purchasable(expectedID);
    }

    public boolean requestPurchaseWasCalled() {
        return mPurchaseRequestId != null;
    }

    public boolean shutdownWasCalled() {
        return mShutdownWasCalled;
    }

    public void simulatePurchaseSuccessResponse(PurchaseResult product) {
        mPurchaseRequestListener.onSuccess(product);
    }

    public void simulatePurchaseFailureResponse(int errorCode, String errorMessage, final Bundle optionalData) {
        mPurchaseRequestListener.onFailure(errorCode, errorMessage, optionalData);
    }

    public void simulateProductListFailure(int errorCode, String errorMessage, final Bundle optionalData) {
        mProductListListener.onFailure(errorCode, errorMessage, optionalData);
    }

    public void simulateReceiptListFailure(int errorCode, String errorMessage, final Bundle optionalData) {
        mReceiptListListener.onFailure(errorCode, errorMessage, optionalData);
    }

    public Context getContextFromInit() {
        return mContextFromInit;
    }

    public String getDeveloperId() {
        return mDeveloperId;
    }

    public void clear() {
        mProducts = new ArrayList<Product>();
        mProductListListener = null;
        mExpectedProductListIds = null;
        mPurchaseRequestId = null;
        mExpectedPurchaseRequestId = null;
        mPurchaseRequestListener = null;
    }

    public void addReceipts(ArrayList<Receipt> receipts) {
        mReceipt = receipts;
    }

    public void simulateReceiptListSuccess() {
        mReceiptListListener.onSuccess(mReceipt);
    }

    public void simulateGamerInfoSuccess(String gamerUuid, String gamerUsername) {
        mGamerInfoListener.onSuccess(new GamerInfo(gamerUuid, gamerUsername));
    }

    public void simulateGamerInfoFailure(int errorCode, String errorMessage, final Bundle optionalData) {
        mGamerInfoListener.onFailure(errorCode, errorMessage, new Bundle());
    }

    public boolean requestReceiptsWasCalled() {
        return mRequestReceiptsWasCalled;
    }

    public void resetCalledFlags() {
        mRequestReceiptsWasCalled = mShutdownWasCalled = false;
        mPurchaseRequestId = null;
    }
}
