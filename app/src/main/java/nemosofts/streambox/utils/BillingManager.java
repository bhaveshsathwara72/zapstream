package nemosofts.streambox.utils;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import com.android.billingclient.api.*;
import java.util.List;

public class BillingManager {
    private static final String YEARLY_SUB_ID = "yearly_premium";
    private static final String LIFETIME_ID = "lifetime_access";

    private final BillingClient billingClient;
    private final Context context;
    private ProductDetails yearlyProduct, lifetimeProduct;
    private BillingCallback callback;

    public interface BillingCallback {
        void onBillingConnected();
        void onProductDetailsFetched(ProductDetails yearlyProduct, ProductDetails lifetimeProduct);
        void onPurchaseSuccess(Purchase purchase);
        void onPurchaseError(int responseCode, String message);
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

    private void connectToPlayBilling() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    callback.onBillingConnected();
                    queryProductDetails();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                connectToPlayBilling();
            }
        });
    }

    private void queryProductDetails() {
        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(List.of(
                        QueryProductDetailsParams.Product.newBuilder()
                                .setProductId(YEARLY_SUB_ID)
                                .setProductType(BillingClient.ProductType.SUBS)
                                .build(),
                        QueryProductDetailsParams.Product.newBuilder()
                                .setProductId(LIFETIME_ID)
                                .setProductType(BillingClient.ProductType.INAPP)
                                .build()
                ))
                .build();

        billingClient.queryProductDetailsAsync(params, (billingResult, productDetailsList) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                for (ProductDetails pd : productDetailsList) {
                    if (pd.getProductId().equals(YEARLY_SUB_ID)) yearlyProduct = pd;
                    else if (pd.getProductId().equals(LIFETIME_ID)) lifetimeProduct = pd;
                }
                callback.onProductDetailsFetched(yearlyProduct, lifetimeProduct);
            }
        });
    }

    public void launchPurchaseFlow(Activity activity, String productId) {
        ProductDetails targetProduct = productId.equals(YEARLY_SUB_ID) ? yearlyProduct : lifetimeProduct;
        if (targetProduct == null) return;

        BillingFlowParams.ProductDetailsParams productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(targetProduct)
                .build();

        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(List.of(productDetailsParams))
                .build();

        billingClient.launchBillingFlow(activity, billingFlowParams);
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