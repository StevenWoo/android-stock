package club.swoo.portfolioswoo;

import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private StockAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private String KEY_PREFERENCES_PORTFOLIO = "portfolio.v1";
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

    }
}
