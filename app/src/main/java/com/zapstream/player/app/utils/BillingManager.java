package com.zapstream.player.app.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;

import java.util.Arrays;
import java.util.List;

public class BillingManager {
    public static final String YEARLY_SUB_ID = "yearly_premium";
    public static final String LIFETIME_ID = "lifetime_access";

    private final BillingClient billingClient;
    private final Context context;
    private ProductDetails yearlyProduct, lifetimeProduct;
    private BillingCallback callback;
    int maxTriesConnection = 3;
    int triedConnection = 0;

    public interface BillingCallback {
        void onBillingConnected();

        void onProductDetailsFetched(ProductDetails product);

        void onPurchaseSuccess(Purchase purchase);

        void onPurchaseError(int responseCode, String message);

        void onPurchaseChecked(String purchasedProductId); // null if no product is purchased
    }

    public BillingManager(Context context, BillingCallback callback) {
        this.context = context;
        this.callback = callback;

        billingClient = BillingClient.newBuilder(context)
                .setListener(this::handlePurchase)
                .enablePendingPurchases()
                .build();

        connectToPlayBilling();
    }

    public void checkExistingPurchases() {
        billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build(),
                (billingResultSubs, purchasesListSubs) -> {
                    if (billingResultSubs.getResponseCode() == BillingClient.BillingResponseCode.OK && purchasesListSubs != null) {
                        for (Purchase purchase : purchasesListSubs) {
                            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED && purchase.isAcknowledged()) {
                                callback.onPurchaseChecked(purchase.getProducts().get(0)); // Return product ID
                                return;
                            }
                        }
                    }

                    // If no subscription found, check INAPP
                    billingClient.queryPurchasesAsync(
                            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(),
                            (billingResultInapp, purchasesListInapp) -> {
                                if (billingResultInapp.getResponseCode() == BillingClient.BillingResponseCode.OK && purchasesListInapp != null) {
                                    for (Purchase purchase : purchasesListInapp) {
                                        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED && purchase.isAcknowledged()) {
                                            callback.onPurchaseChecked(purchase.getProducts().get(0));
                                            return;
                                        }
                                    }
                                }
                                callback.onPurchaseChecked(null);
                            }
                    );
                }
        );
    }


    private void connectToPlayBilling() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    callback.onBillingConnected();
                    queryAllProductDetails();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                connectToPlayBilling();
            }
        });
    }

    private void queryAllProductDetails() {
        querySubsProduct();
        queryInAppProduct();
    }

    private void querySubsProduct() {
        Log.d("Billing", "🔄 querySubsProduct() called");

        List<QueryProductDetailsParams.Product> subsList = Arrays.asList(
                QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(YEARLY_SUB_ID)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
        );

        QueryProductDetailsParams subsParams = QueryProductDetailsParams.newBuilder()
                .setProductList(subsList)
                .build();

        if (!billingClient.isReady()) {
            Log.e("Billing", "BillingClient is NOT ready for SUBS ❌");
            return;
        }

        billingClient.queryProductDetailsAsync(subsParams, (billingResult, productDetailsList) -> {
            Log.d("Billing", "[SUBS] Response Code: " + billingResult.getResponseCode());
            Log.d("Billing", "[SUBS] Debug Message: " + billingResult.getDebugMessage());

            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                if (productDetailsList.isEmpty()) {
                    Log.e("Billing", "[SUBS] ProductDetails list is empty ❌");
                } else {
                    for (ProductDetails pd : productDetailsList) {
                        Log.d("Billing", "[SUBS] ✅ Found product: " + pd.getProductId());
                        if (pd.getProductId().equals(YEARLY_SUB_ID)) {
                            yearlyProduct = pd;
                        }
                    }
                }
                callback.onProductDetailsFetched(yearlyProduct);
            } else {
                Log.e("Billing", "[SUBS] queryProductDetailsAsync failed: " + billingResult.getDebugMessage());
            }
        });
    }

    private void queryInAppProduct() {
        Log.d("Billing", "🔄 queryInAppProduct() called");

        List<QueryProductDetailsParams.Product> inappList = Arrays.asList(
                QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(LIFETIME_ID)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
        );

        QueryProductDetailsParams inappParams = QueryProductDetailsParams.newBuilder()
                .setProductList(inappList)
                .build();

        if (!billingClient.isReady()) {
            Log.e("Billing", "BillingClient is NOT ready for INAPP ❌");
            return;
        }

        billingClient.queryProductDetailsAsync(inappParams, (billingResult, productDetailsList) -> {
            Log.d("Billing", "[INAPP] Response Code: " + billingResult.getResponseCode());
            Log.d("Billing", "[INAPP] Debug Message: " + billingResult.getDebugMessage());

            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                if (productDetailsList.isEmpty()) {
                    Log.e("Billing", "[INAPP] ProductDetails list is empty ❌");
                } else {
                    for (ProductDetails pd : productDetailsList) {
                        Log.d("Billing", "[INAPP] ✅ Found product: " + pd.getProductId());
                        if (pd.getProductId().equals(LIFETIME_ID)) {
                            lifetimeProduct = pd;
                        }
                    }
                }
                callback.onProductDetailsFetched(lifetimeProduct);
            } else {
                Log.e("Billing", "[INAPP] queryProductDetailsAsync failed: " + billingResult.getDebugMessage());
            }
        });
    }


    public void launchPurchaseFlow(Activity activity, String productId) {
        ProductDetails targetProduct = productId.equals(YEARLY_SUB_ID) ? yearlyProduct : lifetimeProduct;

        if (targetProduct == null) {
            Log.e("Billing", "❌ ProductDetails is null for productId: " + productId);
            return;
        }

        BillingFlowParams.ProductDetailsParams productDetailsParams;

        if (targetProduct.getProductType().equals(BillingClient.ProductType.SUBS)) {
            List<ProductDetails.SubscriptionOfferDetails> offerDetailsList = targetProduct.getSubscriptionOfferDetails();
            if (offerDetailsList == null || offerDetailsList.isEmpty()) {
                Log.e("Billing", "❌ No subscription offer details available.");
                return;
            }

            String offerToken = offerDetailsList.get(0).getOfferToken(); // use best offer if needed

            productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(targetProduct)
                    .setOfferToken(offerToken)
                    .build();
        } else {
            // INAPP product (one-time purchase)
            productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(targetProduct)
                    .build();
        }

        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(List.of(productDetailsParams))
                .build();

        BillingResult result = billingClient.launchBillingFlow(activity, billingFlowParams);
        Log.d("Billing", "launchBillingFlow result: " + result.getResponseCode() + " - " + result.getDebugMessage());
    }

    private void handlePurchase(BillingResult billingResult, List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (Purchase purchase : purchases) {
                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                    if (!purchase.isAcknowledged()) {
                        AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();
                        billingClient.acknowledgePurchase(acknowledgePurchaseParams, ackResult -> {
                            if (ackResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                                callback.onPurchaseSuccess(purchase);
                            }
                        });
                    } else {
                        callback.onPurchaseSuccess(purchase);
                    }
                }
            }
        } else {
            callback.onPurchaseError(billingResult.getResponseCode(), billingResult.getDebugMessage());
        }
    }
}