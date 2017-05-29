package com.tnmlicitacoes.app.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.tnmlicitacoes.app.BuildConfig;
import com.tnmlicitacoes.app.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import static com.tnmlicitacoes.app.utils.LogUtils.LOG_ERROR;

public class SendEmailTask extends AsyncTask<Void, Void, String> {

    private static final String TAG = "SendEmailTask";

    private ProgressDialog mProgressDialog;

    private Context mContext;

    private HashMap<String, String> mParamsMap;

    public SendEmailTask(Context ctx, HashMap<String, String> map) {
        mContext = ctx;
        mParamsMap = map;
    }

    private String getPostString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder paramString = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()) {
            if(first)
                first = false;
            else
                paramString.append('&');

            paramString.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            paramString.append('=');
            paramString.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return paramString.toString();
    }

    @Override
    protected String doInBackground(Void... params) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String resJson = null;

        try {
            final String SERVICES_URL = "http://api-hexvex.rhcloud.com/api/servicos/";
            final String APP_ID = "12680db8-106e-4305-941d-2bada9853f2c";
            final String BASE_SERVICES_URL = SERVICES_URL + APP_ID;
            final String EMAIL_SERVICE = BASE_SERVICES_URL + "/email";

            String postData = getPostString(mParamsMap);

            URL url = new URL(EMAIL_SERVICE);

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(15000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setDoOutput(true);
            urlConnection.connect();

            OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
            out.write(postData);
            out.flush();

            int responseCode = urlConnection.getResponseCode();

            if(responseCode == HttpsURLConnection.HTTP_OK) {
                String line = null;
                reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                while((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }

                resJson = sb.toString();
            } else {
                resJson = "Ocorreu um erro!";
                return resJson;
            }
        } catch (IOException e) {
            return null;
        } finally{

            if (urlConnection != null) {
                urlConnection.disconnect();
            }

            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    if(BuildConfig.DEBUG)
                        LOG_ERROR(TAG, "Error closing stream " + e.getMessage());
                }
            }
        }

        try {
            JSONObject jsonObj = new JSONObject(resJson);
            return jsonObj.getString("message");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressDialog = ProgressDialog.show(mContext, mContext.getString(R.string.waiting_text),  mContext.getString(R.string.sending_email));
    }

    protected void onPostExecute(String info) {

        if(info != null) {
            if(info.equals("success")) {
                mProgressDialog.dismiss();
                Toast.makeText(mContext, mContext.getString(R.string.send_notice_email_success), Toast.LENGTH_SHORT).show();
            } else {
                mProgressDialog.dismiss();
                Toast.makeText(mContext, mContext.getString(R.string.send_notice_email_error), Toast.LENGTH_SHORT).show();
            }
        } else {
            mProgressDialog.dismiss();
            Toast.makeText(mContext, mContext.getString(R.string.send_notice_email_error), Toast.LENGTH_SHORT).show();
        }
    }
}
