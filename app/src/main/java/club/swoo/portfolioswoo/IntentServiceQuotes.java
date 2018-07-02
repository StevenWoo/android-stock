package club.swoo.portfolioswoo;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

public class IntentServiceQuotes extends IntentService {
    private static final String ACTION_SINGLE_QUOTE = "quote";
    private static final String ACTION_BATCH_QUOTE = "batch_quote";

    private static final String PARAMETER_SYMBOL = "symbol";
    private static final String PARAMETER_SYMBOL_LIST = "symbols";
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

            }
            else if( ACTION_BATCH_QUOTE.equals(action)){

            }
            else {

            }
        }
    }
}
