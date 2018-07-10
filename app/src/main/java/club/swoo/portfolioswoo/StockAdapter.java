package club.swoo.portfolioswoo;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


public class StockAdapter extends RecyclerView.Adapter<StockAdapter.StockViewHolder> {

    private final String TAG = "StockAdapter";
    private List<JSONObject> values;

    public class StockViewHolder extends RecyclerView.ViewHolder {
        public TextView mSymbol;
        public TextView mPrice;
        public TextView mDescription;
        public View layout;

        public StockViewHolder(View v){
            super(v);
            layout = v;
            mSymbol = v.findViewById(R.id.symbolField);
            mDescription = v.findViewById(R.id.companyNameField);
            mPrice = v.findViewById(R.id.priceField);
        }
    }

    public void add(int position, JSONObject item) {
        values.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(int position) {
        values.remove(position);
        notifyItemRemoved(position);
    }

    public StockAdapter(List<JSONObject> myDataset) {
        values = myDataset;
    }

    @Override
    public StockAdapter.StockViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(
                parent.getContext());
        View v = inflater.inflate(R.layout.stock_table_cell, parent, false);
        StockViewHolder vh = new StockViewHolder(v);
        return vh;
    }

    @Override
    public int getItemCount() {
        return values.size();
    }

    @Override
    public void onBindViewHolder(StockAdapter.StockViewHolder holder, final int position) {
        final JSONObject data = values.get(position);
        try {
            holder.mSymbol.setText(data.getString(FieldKeyConstants.KEY_SYMBOL));
//            holder.mSymbol.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    remove(position);
//                }
//            });

            holder.mDescription.setText(data.getString(FieldKeyConstants.KEY_COMPANY_NAME));
            holder.mPrice.setText(data.getString(FieldKeyConstants.KEY_LATEST_PRICE));
        }
        catch(JSONException json){
            Log.i(TAG, "JSONException");
        }
    }

    @Override
    public void onBindViewHolder(StockAdapter.StockViewHolder holder, int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }

    public List<JSONObject> getValues() {
        return values;
    }
}
