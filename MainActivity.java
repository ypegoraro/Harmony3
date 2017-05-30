package yasmin.harmony.harmony;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    Spinner spinner;
    int id;
    static SoundPool sp;
    static int counter = 0;
    static private int soundIDp1;
    static boolean plays = false, loaded = false;
    static float actVolume, maxVolume, volume;
    static TextView statusMessage;
    AudioManager audioManager;
    Button btn;
    ConnectionThread connect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connect = new ConnectionThread("98:D3:31:90:60:7A");
        connect.start();

        try {
            Thread.sleep(1000);
        } catch (Exception E) {
            E.printStackTrace();
        }


        btn = (Button) findViewById(R.id.button);
        statusMessage = (TextView) findViewById(R.id.statusMessage);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connect.write("L02".getBytes());
                Intent i = getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage( getBaseContext().getPackageName() );
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                //connect.start();
            }
        });

        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        actVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        volume = actVolume / maxVolume;

        sp = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        sp.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool sp, int sampleId, int status) {
                loaded = true;
            }
        });


        spinner = (Spinner) findViewById(R.id.spinner);

        spinner.setOnItemSelectedListener(this);

        List<String> categories = new ArrayList<String>();
        categories.add("Piano");
        categories.add("Acordeon");
        categories.add("Guitarra");
        categories.add("Sax");
        categories.add("Violino");
        categories.add("Xilofone");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);

        /*if(id == 1)
            soundIDp1 = sp.load(this, R.raw.piano1, 1);
        else if(id == 2)
            soundIDp1 = sp.load(this, R.raw.accordion1, 1);*/
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        String item = parent.getItemAtPosition(position).toString();
        if(item.equals("Piano")){
            id = 1;
            soundIDp1 = sp.load(this, R.raw.piano1, 1);

        }

        if(item.equals("Acordeon")) {
            id = 2;
            soundIDp1 = sp.load(this, R.raw.accordion1, 1);
        }


        // Showing selected spinner item
        Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
    }
    public void onNothingSelected(AdapterView<?> arg0) {
        id = 1;
    }

    public static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            Bundle bundle = msg.getData();
            byte[] data = bundle.getByteArray("data");
            String dataString = new String(data);


            if (dataString.equals("---N"))
                statusMessage.setText("Ocorreu um erro durante a conexão D:");
            if (dataString.equals("---S"))
                statusMessage.setText("Conectado :D");

            /* Esse método é invocado na Activity principal
                sempre que a thread de conexão Bluetooth recebe
                uma mensagem.
             */

            if (dataString.equals("t2")) {
                if (loaded && !plays) {
                    sp.play(soundIDp1, volume, volume, 1, 0, 1f);
                    counter = counter++;
                    plays = true;
                }
                plays = false;
            }
        }
    };
}
