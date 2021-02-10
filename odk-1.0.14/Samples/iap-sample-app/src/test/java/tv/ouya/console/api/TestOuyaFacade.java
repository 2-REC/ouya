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

import android.content.Context;
import android.os.Bundle;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;

public class TestOuyaFacade extends OuyaFacade {
    private ArrayList<Product> mProducts = new ArrayList<Product>();
    private OuyaResponseListener<ArrayList<Product>> mProductListListener;
    private List<Purchasable> mExpectedProductListIds;
    private Purchasable mPurchaseRequestId;
    private Purchasable mExpectedPurchaseRequestId;
    private OuyaResponseListener<String> mPurchaseRequestListener;
    private Context mContextFromInit;
    private String mDeveloperId;
    private String mReceipt;
    private OuyaResponseListener<String> mReceiptListListener;
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
    public void init(Context context, String developerId) {
        mContextFromInit = context;
        mDeveloperId = developerId;
    }

    @Override
    public void requestProductList(List<Purchasable> purchasables, OuyaResponseListener<ArrayList<Product>> productListListener) {
        mProductListListener = productListListener;
        if (mExpectedProductListIds != null) {
            assertEquals(mExpectedProductListIds, purchasables);
        }
    }

    @Override
    public void requestPurchase(Purchasable purchasable, OuyaResponseListener<String> purchaseListener) {
        mPurchaseRequestId = purchasable;
        mPurchaseRequestListener = purchaseListener;
        if (mExpectedPurchaseRequestId != null) {
            assertEquals(mExpectedPurchaseRequestId, purchasable);
        }
    }

    @Override
    public void requestReceipts(OuyaResponseListener<String> receiptListListener) {
        mReceiptListListener = receiptListListener;
        mRequestReceiptsWasCalled = true;
    }

    @Override
    public void requestGamerInfo(OuyaResponseListener<GamerInfo> gamerUuidListener) {
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

    public void simulatePurchaseSuccessResponse(String product) {
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
        OuyaEncryptionHelper helper = new OuyaEncryptionHelper();
        List<Receipt> currentReceipts = new ArrayList<Receipt>();
        try {
            if (mReceipt != null && !mReceipt.isEmpty()) {
                currentReceipts = helper.parseJSONReceiptResponse(mReceipt);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        currentReceipts.addAll(receipts);

        Receipt[] receiptArray = currentReceipts.toArray(new Receipt[currentReceipts.size()]);
        ObjectMapper mapper = new ObjectMapper();
        try {
            mReceipt = mapper.writeValueAsString(receiptArray);
            // TODO: add fake encryption here (and remove the "if" in decryptReceiptResponse)
        } catch (IOException e) {
            e.printStackTrace();
        }
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
