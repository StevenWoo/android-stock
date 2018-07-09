package club.swoo.portfolioswoo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


public class StockAdapter extends RecyclerView.Adapter<StockAdapter.StockViewHolder> {

    private List<JSONObject> values;

    public class StockViewHolder extends RecyclerView.ViewHolder {
        public TextView txtHeader;
        public TextView txtFooter;
        public View layout;

        public StockViewHolder(View v){
            super(v);
            layout = v;
            txtHeader = v.findViewById(R.id.firstLine);
            txtFooter = v.findViewById(R.id.secondLine);
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
            holder.txtHeader.setText(data.getString("symbol"));
            holder.txtHeader.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    remove(position);
                }
            });

            holder.txtFooter.setText("Footer: " + data.getString("companyName"));
        }
        catch(JSONException json){

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
