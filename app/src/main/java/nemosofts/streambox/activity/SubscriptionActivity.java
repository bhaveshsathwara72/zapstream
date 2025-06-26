package nemosofts.streambox.activity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.ProductDetails;
import nemosofts.streambox.R;
import nemosofts.streambox.utils.BillingManager;

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
                billingManager.launchPurchaseFlow(this, "yearly_premium_sub"));

        findViewById(R.id.btn_lifetime).setOnClickListener(v ->
                billingManager.launchPurchaseFlow(this, "lifetime_access"));
    }

    @Override
    public void onBillingConnected() {
        // No action needed here
    }

    @Override
    public void onProductDetailsFetched(ProductDetails yearlyProduct, ProductDetails lifetimeProduct) {
        if (yearlyProduct != null) {
            tvYearlyPrice.setText(yearlyProduct.getOneTimePurchaseOfferDetails().getFormattedPrice());
            double yearlyPrice = yearlyProduct.getOneTimePurchaseOfferDetails().getPriceAmountMicros() / 1_000_000.0;
            double monthlyPrice = yearlyPrice / 12.0;
            tvYearlyPerMonth.setText(String.format("$%.2f billed monthly", monthlyPrice));
        }

        if (lifetimeProduct != null) {
            tvLifetimePrice.setText(lifetimeProduct.getOneTimePurchaseOfferDetails().getFormattedPrice());
        }
    }

    @Override
    public void onPurchaseSuccess(Purchase purchase) {
        Toast.makeText(this, "Subscription activated successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onPurchaseError(int responseCode, String message) {
        Toast.makeText(this, "Purchase failed: " + message, Toast.LENGTH_SHORT).show();
    }
}