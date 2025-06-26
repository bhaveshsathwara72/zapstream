package nemosofts.streambox.utils.advertising;

import android.app.Activity;
import android.content.Context;

import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;

import nemosofts.streambox.callback.Callback;
import nemosofts.streambox.utils.ApplicationUtil;

public class GDPRChecker {

    private static final String TAG = "GDPRChecker";
    private Activity activity;
    private ConsentInformation consentInformation;
    Context context;

    public GDPRChecker() {
        throw new IllegalStateException("Utility class");
    }

    public GDPRChecker(Activity activity) {
        this.activity = activity;
        this.consentInformation = UserMessagingPlatform.getConsentInformation(activity);
    }

    public GDPRChecker(Context context) {
        this.context = context;
    }

    public void check() {
        if (Boolean.TRUE.equals(Callback.getIsAdsStatus())){
            initGDPR();
        }
    }

    public boolean canLoadAd() {
        int status = UserMessagingPlatform.getConsentInformation(context).getConsentStatus();
        return (status == ConsentInformation.ConsentStatus.OBTAINED
                || status == ConsentInformation.ConsentStatus.NOT_REQUIRED
                || status == ConsentInformation.ConsentStatus.UNKNOWN
        );
    }

    private void initGDPR() {
        ConsentRequestParameters parameters = new ConsentRequestParameters.Builder()
                .setTagForUnderAgeOfConsent(false)
                .build();

        consentInformation.requestConsentInfoUpdate(activity, parameters, () -> {
            if (consentInformation.isConsentFormAvailable()) {
                loadForm();
            }
        }, formError -> ApplicationUtil.log(TAG, "onFailedToUpdateConsentInfo: " + formError.getMessage()));
    }

    public void loadForm() {
        UserMessagingPlatform.loadConsentForm(activity, consentForm -> {
            if (consentInformation.getConsentStatus() == ConsentInformation.ConsentStatus.REQUIRED
                    || consentInformation.getConsentStatus() == ConsentInformation.ConsentStatus.UNKNOWN) {
                consentForm.show(activity, formError -> {
                    if (consentInformation.getConsentStatus() != ConsentInformation.ConsentStatus.OBTAINED) {
                        // App can start requesting ads.
                        loadForm();
                    }
                });
            }
        }, formError -> loadForm());
    }
}