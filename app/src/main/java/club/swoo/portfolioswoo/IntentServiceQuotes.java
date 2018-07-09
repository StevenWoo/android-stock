package club.swoo.portfolioswoo;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class IntentServiceQuotes extends IntentService {
    private static final String ACTION_SINGLE_QUOTE = "quote";
    private static final String ACTION_BATCH_QUOTE = "batch_quote";

    private static final String PARAMETER_SYMBOL = "symbol";
    private static final String PARAMETER_SYMBOL_LIST = "symbols";

    private static final String TAG = "IntentServiceQuotes";

    private String BATCH_QUOTE_URL = "https://api.iextrading.com/1.0/stock/market/batch?symbols=%s&types=quote";
    private String QUOTE_URL = "https://api.iextrading.com/1.0/stock/%s/quote";

    public IntentServiceQuotes() {
        super("IntentServiceQuotes");
    }
    public static void startActionQuote(Context context, String symbol){
        Intent intent = new Intent(context, IntentServiceQuotes.class);
        intent.setAction(ACTION_SINGLE_QUOTE);
        intent.putExtra(PARAMETER_SYMBOL, symbol);
        context.startService(intent);
    }
    public static void startActionBatchQuotes(Context context, String symbols){
        Intent intent = new Intent(context, IntentServiceQuotes.class);
        intent.setAction(ACTION_BATCH_QUOTE);
        intent.putExtra(PARAMETER_SYMBOL_LIST, symbols);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if( ACTION_SINGLE_QUOTE.equals(action)){
                String symbol = intent.getStringExtra(PARAMETER_SYMBOL);
                handleQuote(symbol);
            }
            else if( ACTION_BATCH_QUOTE.equals(action)){
                String symbolList = intent.getStringExtra(PARAMETER_SYMBOL_LIST);
                handleBatchQuote(symbolList);
            }
            else {
                Log.i(TAG, "undefined action in service");
            }
        }
    }

    private void handleQuote(String symbol){
        OkHttpClient client = new OkHttpClient();

        EventQuoteDone eventQuoteDone = new EventQuoteDone();
        eventQuoteDone.mResult = false;
        String urlString = String.format(QUOTE_URL, symbol);
        HttpUrl.Builder urlBuilder = HttpUrl.parse(urlString).newBuilder();
        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            Response response = client.newCall(request).execute();

            if(response.isSuccessful()) {
                Headers responseHeaders = response.headers();
                for (int i = 0; i < responseHeaders.size(); i++) {
                    System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
                }
                final String myResponse = response.body().string();


                try {
                    JSONObject json = new JSONObject(myResponse);
                    Log.d(TAG, json.toString());

                    eventQuoteDone.mJSONData = json;
                    eventQuoteDone.mResult = true;
                }
                catch(JSONException jsone){
                    Log.i(TAG, "JSON Exception");
                }

            }
        }
        catch(IOException ioEx) {
            Log.i(TAG, "IOException");
        }

        EventBus.getDefault().post(eventQuoteDone);
    }

    private void handleBatchQuote(String symbolList){
        EventBatchQuotesDone eventBatchQuotesDone = new EventBatchQuotesDone();
        eventBatchQuotesDone.mResult = false;
        String stringUrl = String.format(BATCH_QUOTE_URL, symbolList);

        OkHttpClient client = new OkHttpClient();

        HttpUrl.Builder urlBuilder = HttpUrl.parse(stringUrl).newBuilder();
        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            Response response = client.newCall(request).execute();

            if(response.isSuccessful()) {
                Headers responseHeaders = response.headers();
                for (int i = 0; i < responseHeaders.size(); i++) {
                    System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
                }
                final String myResponse = response.body().string();


                try {
                    JSONObject json = new JSONObject(myResponse);
                    Log.d(TAG, json.toString());
                    Iterator<?> keys = json.keys();
                    List<JSONObject> input = new ArrayList<>();
                    eventBatchQuotesDone.mResult = true;

                    while( keys.hasNext() ) {
                        String key = (String)keys.next();
                        if ( json.get(key) instanceof JSONObject ) {
                            try {
                                JSONObject jsonSub = json.getJSONObject(key);
                                Log.i("TAG", jsonSub.toString());
                                JSONObject jsonQuote = jsonSub.getJSONObject("quote");
                                input.add(jsonQuote);

                            }
                            catch(Exception ex){
                                eventBatchQuotesDone.mResult = false;
                            }

                        }
                    }
                    eventBatchQuotesDone.mJSONData = input;
                }
                catch(JSONException jsone){
                    Log.i(TAG, "JSON Exception");
                }

            }
        }
        catch(IOException ioEx) {
            Log.i(TAG, "IOException");
        }
        EventBus.getDefault().post(eventBatchQuotesDone);
    }
}
