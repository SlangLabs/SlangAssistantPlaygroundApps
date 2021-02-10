package in.slanglabs.assistant.retail.playground;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import in.slanglabs.assistants.retail.AssistantConfiguration;
import in.slanglabs.assistants.retail.NavigationUserJourney;
import in.slanglabs.assistants.retail.OrderManagementUserJourney;
import in.slanglabs.assistants.retail.SearchUserJourney;
import in.slanglabs.assistants.retail.SlangRetailAssistant;
import in.slanglabs.platform.SlangLocale;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "RetailPlayground";
    private static boolean sAssistantInitialised;
    private Button mSearchJourney, mOrderJourney;
    private TextView mInitialising;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSearchJourney = findViewById(R.id.search_journey);
        mOrderJourney = findViewById(R.id.order_journey);
        mInitialising = findViewById(R.id.progress_text);

        if (!sAssistantInitialised) {
            mSearchJourney.setVisibility(View.INVISIBLE);
            mOrderJourney.setVisibility(View.INVISIBLE);
            mInitialising.setVisibility(View.VISIBLE);
            initialiseRetailAssistant();
        } else {
            mSearchJourney.setVisibility(View.VISIBLE);
            mOrderJourney.setVisibility(View.VISIBLE);
            mInitialising.setVisibility(View.INVISIBLE);
        }
        SlangRetailAssistant.getUI().hideTrigger(this);

        mSearchJourney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent searchJourney = new Intent(MainActivity.this, RetailJourneyActivity.class);
                searchJourney.putExtra("journey", "search");
                MainActivity.this.startActivity(searchJourney);
            }
        });

        mOrderJourney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent orderJourney = new Intent(MainActivity.this, RetailJourneyActivity.class);
                orderJourney.putExtra("journey", "order");
                MainActivity.this.startActivity(orderJourney);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SlangRetailAssistant.getUI().hideTrigger(this);
    }

    private void initialiseRetailAssistant() {
        //Enabled languages.
        HashSet<Locale> requestedLocales = new HashSet<>();
        requestedLocales.add(SlangLocale.LOCALE_ENGLISH_IN);
        requestedLocales.add(SlangLocale.LOCALE_HINDI_IN);

        SlangRetailAssistant.setLifecycleObserver(new SlangRetailAssistant.LifecycleObserver() {
            @Override
            public void onAssistantInitSuccess() {
                Log.e(TAG, "onAssistantInitSuccess");
                sAssistantInitialised = true;
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        mSearchJourney.setVisibility(View.VISIBLE);
                        mOrderJourney.setVisibility(View.VISIBLE);
                        mInitialising.setVisibility(View.INVISIBLE);
                    }
                });
            }

            @Override
            public void onAssistantInitFailure(String description) {
                Log.e(TAG, "onAssistantInitFailure:" + description);
            }

            @Override
            public void onAssistantInvoked() {
                Log.e(TAG, "onAssistantInvoked");
                //Clear all the journey contexts so user can start afresh.
                OrderManagementUserJourney.getContext().clear();
                SearchUserJourney.getContext().clear();
                NavigationUserJourney.getContext().clear();
            }

            @Override
            public void onAssistantClosed(boolean isCancelled) {
                Log.e(TAG, "onAssistantClosed");
            }

            @Override
            public void onAssistantLocaleChanged(Locale changedLocale) {
                Log.e(TAG, "onAssistantLocaleChanged:" + changedLocale);
            }

            @Override
            public void onUnrecognisedUtterance(String utterance) {
                Log.e(TAG, "onUnrecognisedUtterance:" + utterance);
            }
        });

        //Initialise assistant
        AssistantConfiguration config = new AssistantConfiguration.Builder()
                .setAPIKey("742334ac406b43ae84dcae707cc5326a")
                .setAssistantId("2510ead4ee754e879854248177538d66")
                .setDefaultLocale(SlangLocale.LOCALE_ENGLISH_IN)
                .setEnvironment(SlangRetailAssistant.Environment.STAGING)
                .setRequestedLocales(requestedLocales)
                .build();
        SlangRetailAssistant.initialize(this, config);
    }
}
