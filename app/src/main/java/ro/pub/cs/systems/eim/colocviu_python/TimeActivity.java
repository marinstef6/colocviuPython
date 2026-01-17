package ro.pub.cs.systems.eim.colocviu_python;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;


import android.widget.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class TimeActivity extends AppCompatActivity {

    private EditText ipEditText, portEditText;
    private Button connectButton;
    private TextView timeTextView;

    private volatile boolean running = false;
    private Thread worker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time);

        ipEditText = findViewById(R.id.server_ip_edit_text);
        portEditText = findViewById(R.id.server_port_edit_text);
        connectButton = findViewById(R.id.connect_time_button);
        timeTextView = findViewById(R.id.time_text_view);

        connectButton.setOnClickListener(v -> {
            String ip = ipEditText.getText().toString().trim();
            String portStr = portEditText.getText().toString().trim();
            if (ip.isEmpty() || portStr.isEmpty()) {
                Toast.makeText(this, "IP/Port missing!", Toast.LENGTH_SHORT).show();
                return;
            }
            int port = Integer.parseInt(portStr);

            startClient(ip, port);
        });
    }

    private void startClient(String ip, int port) {
        stopClient();
        running = true;

        worker = new Thread(() -> {
            try (Socket socket = new Socket(ip, port);
                 BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                String line;
                while (running && (line = br.readLine()) != null) {
                    String time = line.trim();
                    runOnUiThread(() -> timeTextView.setText(time));
                }
            } catch (Exception e) {
                runOnUiThread(() -> timeTextView.setText("ERROR: " + e.getMessage()));
            }
        });

        worker.start();
    }

    private void stopClient() {
        running = false;
        if (worker != null) {
            worker.interrupt();
            worker = null;
        }
    }

    @Override
    protected void onDestroy() {
        stopClient();
        super.onDestroy();
    }
}