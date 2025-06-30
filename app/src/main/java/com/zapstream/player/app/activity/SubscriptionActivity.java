package com.zapstream.player.app.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.zapstream.player.app.R;
import com.zapstream.player.app.utils.BillingManager;

import java.util.List;
import java.util.Locale;

public class SubscriptionActivity extends AppCompatActivity implements BillingManager.BillingCallback {
    private BillingManager billingManager;
    private TextView tvYearlyPrice, tvYearlyPerMonth, tvLifetimePrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);

        tvYearlyPrice = findViewById(R.id.tv_yearly_price);
        tvYearlyPerMonth = findViewById(R.id.tv_yearly_per_month);
        tvLifetimePrice = findViewById(R.id.tv_lifetime_price);

        billingManager = new BillingManager(this, this);

        findViewById(R.id.btn_yearly).setOnClickListener(v ->
                billingManager.launchPurchaseFlow(this, BillingManager.YEARLY_SUB_ID));

        findViewById(R.id.btn_lifetime).setOnClickListener(v ->
                billingManager.launchPurchaseFlow(this, BillingManager.LIFETIME_ID));
    }

    @Override
    public void onBillingConnected() {
        // No action needed here
    }

    @Override
    public void onProductDetailsFetched(ProductDetails product) {
        if (product != null && product.getProductId().equals(BillingManager.YEARLY_SUB_ID)) {
            List<ProductDetails.SubscriptionOfferDetails> offerDetailsList = product.getSubscriptionOfferDetails();
            if (offerDetailsList != null && !offerDetailsList.isEmpty()) {
                ProductDetails.PricingPhase pricingPhase = offerDetailsList
                        .get(0) // First offer
                        .getPricingPhases()
                        .getPricingPhaseList()
                        .get(0); // First phase

                String formattedPrice = pricingPhase.getFormattedPrice();
                long priceMicros = pricingPhase.getPriceAmountMicros();

                double yearlyPrice = priceMicros / 1_000_000.0;
                double monthlyPrice = yearlyPrice / 12.0;

                tvYearlyPrice.setText(formattedPrice);
                tvYearlyPerMonth.setText(String.format(Locale.US, "₹%.2f/mo (billed annually)", monthlyPrice));
            } else {
                Log.e("Billing", "No subscription offer details found.");
            }
        }
        if (product != null && product.getProductId().equals(BillingManager.LIFETIME_ID)) {
            tvLifetimePrice.setText(product.getOneTimePurchaseOfferDetails().getFormattedPrice());
        }
    }

    @Override
    public void onPurchaseSuccess(Purchase purchase) {
        Toast.makeText(this, "Subscription activated successfully!", Toast.LENGTH_SHORT).show();
        restartApp(this);
    }

    @Override
    public void onPurchaseError(int responseCode, String message) {
        Toast.makeText(this, "Purchase failed: " + message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPurchaseChecked(String purchasedProductId) {

    }

    public void restartApp(Context context) {
        Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage(context.getPackageName());

        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
            Runtime.getRuntime().exit(0); // Kill the current process
        }
    }
}