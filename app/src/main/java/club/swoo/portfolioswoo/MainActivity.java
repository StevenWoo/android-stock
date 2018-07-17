package club.swoo.portfolioswoo;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;



public class MainActivity extends AppCompatActivity {
    private static String TAG = "MainActivity";
    private RecyclerView mRecyclerView;
    private StockAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    SwipeRefreshLayout mSwipeRefreshLayout;
    private ImageView mButtonImage;


    private String KEY_PREFERENCES_PORTFOLIO = "portfolio.v1";

    private String m_Text = "";

    private void getSymbolFromUser(Context context, View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Stock symbol");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint(context.getString(R.string.stock_hint));
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_Text = input.getText().toString();
                Log.i(TAG, m_Text);
                //getQuote(m_Text);
                IntentServiceQuotes.startActionQuote(getApplicationContext(), m_Text);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }



    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventBatchQuotesDone eventBatchQuotesDone){
        mSwipeRefreshLayout.setRefreshing(false);
        if( eventBatchQuotesDone.mResult == true) {

            mAdapter = new StockAdapter(eventBatchQuotesDone.mJSONData);
            mRecyclerView.swapAdapter(mAdapter, true);
            if(!eventBatchQuotesDone.mJSONData.isEmpty()) {
                // move from list to JSONArray to serialize
//                int size = eventBatchQuotesDone.mJSONData.size();
                JSONArray jsonArray = new JSONArray(eventBatchQuotesDone.mJSONData);
//                for( int idx = 0; idx < size; ++idx){
//                    jsonArray.put(eventBatchQuotesDone.mJSONData.get(idx));
//                }
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).
                        edit().
                        putString(KEY_PREFERENCES_PORTFOLIO, jsonArray.toString()).
                        commit();

            }
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventQuoteDone eventQuoteDone){
        if( eventQuoteDone.mResult == true) {
            Log.i(TAG, "test");
            if( eventQuoteDone.mJSONData != null) {
                // need to
                //  a.) update adapter
                //  b.) save list serialized to preferences
                List<JSONObject> updatedList = mAdapter.getValues();
                updatedList.add(eventQuoteDone.mJSONData);
                mAdapter = new StockAdapter(updatedList);
                mRecyclerView.swapAdapter(mAdapter, true);
                JSONArray jsonNewSave = new JSONArray(updatedList);
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).
                        edit().
                        putString(KEY_PREFERENCES_PORTFOLIO, jsonNewSave.toString()).
                        commit();

            }

        }
    }




    private String constructSymbolList(){
        String quotesList = "";
        String testThings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(KEY_PREFERENCES_PORTFOLIO, null);
        if( testThings != null){
            try {
                JSONArray jsonArrayThings = new JSONArray(testThings);
                if(jsonArrayThings.length() > 0 ){
                    // have starting symbols
                    int length = jsonArrayThings.length();
                    for( int idx = 0; idx < length; ++idx){
                        JSONObject oneQuote = jsonArrayThings.getJSONObject(idx);
                        if( idx > 0 ){
                            quotesList = quotesList + ",";
                        }
                        quotesList = quotesList + oneQuote.getString("symbol");
                    }
                }
            }
            catch(JSONException jsonE){
                Log.i(TAG, "JSON Exception");
            }
        }
        return quotesList;
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
                getSymbolFromUser(getApplicationContext(), view);
            }
        });
        mRecyclerView = findViewById(R.id.stock_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        String quotesList = constructSymbolList();
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        List<JSONObject> input = new ArrayList<>();
        mAdapter = new StockAdapter(input);
        mRecyclerView.setAdapter(mAdapter);

        mSwipeRefreshLayout = findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                IntentServiceQuotes.startActionBatchQuotes(getApplicationContext(), constructSymbolList());

                mSwipeRefreshLayout.setRefreshing(true);
            }
        });
        IntentServiceQuotes.startActionBatchQuotes(getApplicationContext(), quotesList);
        mSwipeRefreshLayout.setRefreshing(true);

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.DOWN | ItemTouchHelper.UP) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                Toast.makeText(MainActivity.this, "", Toast.LENGTH_SHORT).show();
                //Remove swiped item from list and notify the RecyclerView

                int position = viewHolder.getAdapterPosition();


                List<JSONObject> updatedList = mAdapter.getValues();
                if( position < updatedList.size()){
                    JSONObject deleteObject = updatedList.get(position);
                    try {
                        String symbolToDelete = deleteObject.getString(FieldKeyConstants.KEY_SYMBOL);
                        String testThings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(KEY_PREFERENCES_PORTFOLIO, null);
                        if (testThings != null) {
                            JSONArray jsonArrayThings = new JSONArray(testThings);
                            JSONArray jsonNewArray = new JSONArray();
                            // convert jsonarray to list<JSONObject for adapter>
                            List<JSONObject> newAdapterData = new ArrayList<>();

                            if (jsonArrayThings.length() > 0) {
                                for (int i = 0, len = jsonArrayThings.length(); i < len; i++) {
                                    JSONObject obj = jsonArrayThings.getJSONObject(i);
                                    String val = jsonArrayThings.getJSONObject(i).getString(FieldKeyConstants.KEY_SYMBOL);
                                    if (!val.equals(symbolToDelete)) {
                                        jsonNewArray.put(obj);
                                        newAdapterData.add(obj);
                                    }
                                }
                            }
                            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).
                                    edit().
                                    putString(KEY_PREFERENCES_PORTFOLIO, jsonNewArray.toString()).
                                    commit();

                            mAdapter = new StockAdapter(newAdapterData);
                            mRecyclerView.swapAdapter(mAdapter, true);


                        }
                    }
                    catch(JSONException jsonE){
                        Log.i(TAG, "JSON exception");
                    }
                }

            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
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
