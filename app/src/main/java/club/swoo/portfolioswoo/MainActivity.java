package club.swoo.portfolioswoo;

import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private StockAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private String BATCH_QUOTE_URL = "https://api.iextrading.com/1.0/stock/market/batch?symbols=%@&types=quote";
    private String QUOTE_URL = "https://api.iextrading.com/1.0/stock/%@/quote";

    private String KEY_PREFERENCES_PORTFOLIO = "portfolio.v1";

    OkHttpClient mClient = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = findViewById(R.id.stock_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        String testThings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(KEY_PREFERENCES_PORTFOLIO, null);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        List<JSONObject> input = new ArrayList<>();
        try {
            for (int i = 0; i < 100; i++) {
                JSONObject jsonPlaceHolder = new JSONObject();

                jsonPlaceHolder.put("test", "value" + String.valueOf(i));
                input.add(jsonPlaceHolder);
            }// define an adapter
        }
        catch(JSONException jsone){

        }
        mAdapter = new StockAdapter(input);
        mRecyclerView.setAdapter(mAdapter);
        OkHttpClient client = new OkHttpClient();

        String test_url = "https://api.iextrading.com/1.0/stock/AAPL/quote";

        HttpUrl.Builder urlBuilder = HttpUrl.parse(test_url).newBuilder();
        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String myResponse = response.body().string();


                try {
                    JSONObject json = new JSONObject(myResponse);
                    Log.d("test x", json.toString());
                }
                catch(JSONException jsone){

                }
            }
        });
    }
}
