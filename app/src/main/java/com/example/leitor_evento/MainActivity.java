package com.example.leitor_evento;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.ArrayList;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private Button btnLerQRcode, btnGerarQRcode, btnListaQRcode;

    private EditText qrInfoEditText;
    private ListView listaQRListView;
    private ListaDosQR listaDosQR;

    private TextView txtLogar;
    private ImageView imgQrCode;
    private Spinner eventSpinner;
    private String receba;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        OnBackPressedDispatcher dispatcher = getOnBackPressedDispatcher();
        dispatcher.addCallback(this, new OnBackPressedCallback(true) {
            int i = 0;
            @Override
            public void handleOnBackPressed() {
                i++;
                if(i == 2)
                    finish();
                else Toast.makeText(MainActivity.this, "Precione mais uma vez para sair.", Toast.LENGTH_SHORT).show();
            }
        });

        eventSpinner = findViewById(R.id.eventSpinner);
        btnLerQRcode = findViewById(R.id.btnLerQRcode);
        btnGerarQRcode = findViewById(R.id.btnGerarQRcode);
        txtLogar = findViewById(R.id.txtLogar);
        imgQrCode = findViewById(R.id.imgQrCode);
        btnListaQRcode = findViewById(R.id.txtDesc);

        String txtNome = getIntent().getStringExtra("editUsername");
        String txtSenha = getIntent().getStringExtra("editPassword");

        String[] events = {"Evento A", "Evento B", "Evento C"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, events);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eventSpinner.setAdapter(adapter);



        String dataHoraAtual = getDataHoraBrasilia();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("QRScans");
        btnGerarQRcode.setOnClickListener(v -> {
            // sla mano tem que ver
            // String nome = "Rafael";
            int largura = 550;
            int altura = 550;
            try {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String uid = user.getUid();
                String userName = user.getDisplayName();
                String email = user.getEmail();
                String event = eventSpinner.getSelectedItem().toString();

                if (userName == null){ // kk só por descargo de consciência
                    userName = "Usuario";
                }

                // Formate os dados como JSON
                JSONObject qrData = new JSONObject();
                qrData.put("email", email);
                qrData.put("event", event);
                qrData.put("dataQueQRcodeFoiLido", dataHoraAtual);
                qrData.put("UID", uid);
                qrData.put("nome", userName);

                //String key = databaseReference.push().getKey();
               // databaseReference.child(key).setValue(qrData.toString());

                receba = qrData.toString();

                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                Bitmap bitMap = barcodeEncoder.encodeBitmap(qrData.toString(), BarcodeFormat.QR_CODE, largura, altura);
                imgQrCode.setImageBitmap(bitMap);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        btnLerQRcode.setOnClickListener(v -> {
            Intent intentGo = new Intent(MainActivity.this, ConfirmarLerQRCode.class);
            intentGo.putExtra("qrData", receba);
            startActivity(intentGo);
            finish();
        });

        txtLogar.setOnClickListener(v -> {
            Intent intentGo = new Intent(MainActivity.this, Login.class);
            startActivity(intentGo);
            finish();
        });

      btnListaQRcode.setOnClickListener(v -> {
          Intent intent = new Intent(MainActivity.this, ConfirmarListaQRCode.class);
          startActivity(intent);
          finish();
      });

    }
    private String getDataHoraBrasilia() {
        // Obtemos a data e hora no fuso horário de Brasília
        ZonedDateTime dataHoraBrasilia = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dataHoraBrasilia = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"));
        }

        // Formatamos a data e hora no padrão desejado
        DateTimeFormatter formatter = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        }
            return dataHoraBrasilia.format(formatter);  // Retorna a data e hora formatada como String
    }
}

