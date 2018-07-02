package club.swoo.portfolioswoo;

import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {
    private static String TAG = "MainActivity";
    private RecyclerView mRecyclerView;
    private StockAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ImageView mButtonImage;

    private String BATCH_QUOTE_URL = "https://api.iextrading.com/1.0/stock/market/batch?symbols=%@&types=quote";
    private String QUOTE_URL = "https://api.iextrading.com/1.0/stock/%@/quote";

    private String KEY_PREFERENCES_PORTFOLIO = "portfolio.v1";


    private void getQuote(){
        OkHttpClient client = new OkHttpClient();

        String test_url1 = "https://api.iextrading.com/1.0/stock/AAPL/quote";

        HttpUrl.Builder urlBuilder1 = HttpUrl.parse(test_url1).newBuilder();
        String url1 = urlBuilder1.build().toString();

        Request request1 = new Request.Builder()
                .url(url1)
                .build();

        client.newCall(request1).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String myResponse = response.body().string();


                try {
                    JSONObject json = new JSONObject(myResponse);
                    Log.d(TAG, json.toString());
                    final String KEY_SYMBOL= "symbol";
                    final String KEY_COMPANY_NAME = "companyName";
                    final String KEY_LATEST_PRICE = "latestPrice";
                    final String KEY_PERCENT_CHANGE = "changePercent";

                    String symbol = json.getString(KEY_SYMBOL);
                    String company_name = json.getString(KEY_COMPANY_NAME);
                    String latest_price = json.getString(KEY_LATEST_PRICE);
                    double percent_change = json.getDouble(KEY_PERCENT_CHANGE);
                    percent_change *= 100.0;
                    Log.i(TAG, symbol + ":" + company_name + ":" + latest_price + ":" + percent_change);
                }
                catch(JSONException jsone){

                }
            }
        });

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventBatchQuotesDone eventBatchQuotesDone){
        if( eventBatchQuotesDone.mResult == true) {
            mAdapter = new StockAdapter(eventBatchQuotesDone.mJsonData);
            mRecyclerView.swapAdapter(mAdapter, true);
        }

    }

    private void batchGetQuote(){
        String test_url2 = "https://api.iextrading.com/1.0/stock/market/batch?symbols=" + "INTC" + ",MSFT" + "&types=quote";
        OkHttpClient client = new OkHttpClient();

        HttpUrl.Builder urlBuilder2 = HttpUrl.parse(test_url2).newBuilder();
        String url2 = urlBuilder2.build().toString();

        Request request2 = new Request.Builder()
                .url(url2)
                .build();
        client.newCall(request2).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String myResponse = response.body().string();
                EventBatchQuotesDone eventBatchQuotesDone = new EventBatchQuotesDone();
                eventBatchQuotesDone.mResult = false;

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
                    eventBatchQuotesDone.mJsonData = input;
                }
                catch(JSONException jsone){
                    eventBatchQuotesDone.mResult = false;
                }
                EventBus.getDefault().post(eventBatchQuotesDone);
            }
        });

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButtonImage = findViewById(R.id.imageView1);
        mButtonImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "hello click");
            }
        });
        mRecyclerView = findViewById(R.id.stock_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        String testThings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(KEY_PREFERENCES_PORTFOLIO, null);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        List<JSONObject> input = new ArrayList<>();
        mAdapter = new StockAdapter(input);
        mRecyclerView.setAdapter(mAdapter);
        getQuote();

        batchGetQuote();

    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}
