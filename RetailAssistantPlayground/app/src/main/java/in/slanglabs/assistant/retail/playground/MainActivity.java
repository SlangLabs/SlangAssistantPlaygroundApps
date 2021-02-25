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
    private TextView mInitialising;
    private Button mPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mInitialising = findViewById(R.id.progress_text);
        mPlay = findViewById(R.id.play);

        if (!sAssistantInitialised) {
            mInitialising.setVisibility(View.VISIBLE);
            mPlay.setVisibility(View.INVISIBLE);
            initialiseRetailAssistant();
        } else {
            mPlay.setVisibility(View.VISIBLE);
            Intent retailUserJourney = new Intent(MainActivity.this, RetailJourneyActivity.class);
            startActivity(retailUserJourney);
        }
        SlangRetailAssistant.getUI().hideTrigger(this);

        mPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent retailUserJourney = new Intent(MainActivity.this, RetailJourneyActivity.class);
                startActivity(retailUserJourney);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SlangRetailAssistant.getUI().hideTrigger(this);
        mPlay.setVisibility(sAssistantInitialised ? View.VISIBLE : View.INVISIBLE);
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
                        Intent retailUserJourney = new Intent(MainActivity.this, RetailJourneyActivity.class);
                        MainActivity.this.startActivity(retailUserJourney);
                        mInitialising.setText("Initialised Successfully");
                    }
                });
            }

            @Override
            public void onAssistantInitFailure(String description) {
                Log.e(TAG, "onAssistantInitFailure:" + description);
                mInitialising.setText("Initialisation Failed:" + description);
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
                .setAPIKey("<API_KEY>")
                .setAssistantId("<ASSISTANT_ID>")
                .setDefaultLocale(SlangLocale.LOCALE_ENGLISH_IN)
                .setRequestedLocales(requestedLocales)
                .build();
        SlangRetailAssistant.initialize(this, config);
    }
}
