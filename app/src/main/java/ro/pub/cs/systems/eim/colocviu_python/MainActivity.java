package ro.pub.cs.systems.eim.colocviu_python;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.*;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
public class MainActivity extends AppCompatActivity {
    private EditText wordEditText;
    private Button defineButton, timeButton;
    private TextView definitionTextView;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            String def = intent.getStringExtra(Constants.EXTRA_DEF);
            Log.i(Constants.TAG, "[UI] RECEIVED DEF=" + def);
            definitionTextView.setText(def == null ? "" : def);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wordEditText = findViewById(R.id.word_edit_text);
        defineButton = findViewById(R.id.define_button);
        timeButton = findViewById(R.id.time_button);
        definitionTextView = findViewById(R.id.definition_text_view);

        defineButton.setOnClickListener(v -> {
            String word = wordEditText.getText().toString().trim();
            if (word.isEmpty()) {
                Toast.makeText(this, "Word missing!", Toast.LENGTH_SHORT).show();
                return;
            }
            definitionTextView.setText("Loading...");
            new DictionaryThread(this, word).start();
            IntentFilter filter = new IntentFilter(Constants.ACTION_DICT);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                registerReceiver(receiver, filter);
            }

        });

        timeButton.setOnClickListener(v -> startActivity(new Intent(this, TimeActivity.class)));
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter(Constants.ACTION_DICT);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            // varianta veche (4 parametri) – nu cere flag, dar dispare warning-ul
            registerReceiver(receiver, filter, null, null);
        }
    }

    @Override protected void onPause() {
            unregisterReceiver(receiver);
            super.onPause();
    }
}
