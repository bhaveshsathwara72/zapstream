package nemosofts.streambox.executor;

import org.json.JSONArray;
import org.json.JSONObject;

import nemosofts.streambox.callback.Callback;
import nemosofts.streambox.utils.ApplicationUtil;
import nemosofts.streambox.utils.AsyncTaskExecutor;
import okhttp3.RequestBody;

public class LoadTrial extends AsyncTaskExecutor<String, String, String> {

    private final RequestBody requestBody;
    private final Listener listener;
    private String success = "0";
    private String message = "";

    private String userType = "";
    private String userName = "";
    private String userPassword = "";
    private String dnsBase = "";

    public LoadTrial(Listener listener, RequestBody requestBody) {
        this.listener = listener;
        this.requestBody = requestBody;
    }

    @Override
    protected void onPreExecute() {
        listener.onStart();
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String strings) {
        try {
            String json = ApplicationUtil.responsePost(Callback.API_URL, requestBody);
            JSONObject mainJson = new JSONObject(json);
            JSONArray jsonArray = mainJson.getJSONArray(Callback.TAG_ROOT);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject c = jsonArray.getJSONObject(i);
                if (c.has("user_type")){
                    userType = c.getString("user_type");
                    userName = c.getString("user_name");
                    userPassword = c.getString("user_password");
                    dnsBase = c.getString("dns_base");
                } else {
                    success = c.getString(Callback.TAG_SUCCESS);
                    message = c.getString(Callback.TAG_MSG);
                }
            }
            return "1";
        } catch (Exception e) {
            ApplicationUtil.log("LoadStatus", "Error loading trial", e);
            return "0";
        }
    }

    @Override
    protected void onPostExecute(String s) {
        listener.onEnd(s, success, message, userType, userName, userPassword, dnsBase);
    }

    public interface Listener {
        void onStart();
        void onEnd(String success,
                   String apiSuccess,
                   String message, String type, String name, String password, String dns
        );
    }
}