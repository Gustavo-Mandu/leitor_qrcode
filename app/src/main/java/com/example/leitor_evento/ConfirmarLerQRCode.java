package com.example.leitor_evento;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class ConfirmarLerQRCode extends AppCompatActivity {

    private TextInputEditText editUsername, editPassword;
    private TextView txtVoltar;
    private Button btnConfirmar;
    private ConstraintLayout main;
    private ProgressBar progressLogin;
    private final Timer timer = new Timer();
    private TimerTask timerTask;
    private View containerLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_confirmar_ler_qrcode);
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
                else Toast.makeText(ConfirmarLerQRCode.this, "Precione mais uma vez para sair.", Toast.LENGTH_SHORT).show();
            }
        });

        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        txtVoltar = findViewById(R.id.txtVoltar);
        btnConfirmar = findViewById(R.id.btnConfirmar);
        main = findViewById(R.id.main);
        containerLogin = findViewById(R.id.containerLogin);
        progressLogin = findViewById(R.id.progressLogin);

        progressLogin.setEnabled(false);

        String qrData = getIntent().getStringExtra("qrData");

        editUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnConfirmar.setEnabled(!TextUtils.isEmpty(s));
                if (btnConfirmar.isEnabled()){
                    btnConfirmar.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                } else {
                    btnConfirmar.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Não precisamos fazer nada aqui
            }
        });

        txtVoltar.setOnClickListener(v -> {
            Intent intentGo = new Intent(ConfirmarLerQRCode.this, MainActivity.class);
            startActivity(intentGo);
            finish();
        });

        main.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Verifica o tamanho do teclado
                Rect rect = new Rect();
                main.getWindowVisibleDisplayFrame(rect);
                int screenHeight = main.getRootView().getHeight();
                int keypadHeight = screenHeight - rect.bottom;

                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(main);

                if (keypadHeight > screenHeight * 0.15) { // Quando o teclado está visível
                    constraintSet.setVerticalBias(R.id.containerLogin, 0.1f);
                } else { // Quando o teclado está oculto
                    constraintSet.setVerticalBias(R.id.containerLogin, 0.468f);
                }
                constraintSet.applyTo(main);
            }
        });

        btnConfirmar.setOnClickListener(v -> {
           // String username = Objects.requireNonNull(editUsername.getText()).toString();
           // String password = Objects.requireNonNull(editPassword.getText()).toString();

            timerTask = new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (editUsername.getText().toString().contains("admin") && editPassword.getText().toString().contains("123")){
                            Toast.makeText(ConfirmarLerQRCode.this, "Login de Admin bem-sucedido!", Toast.LENGTH_SHORT).show();
                                progressLogin.getIndeterminateDrawable().setColorFilter(
                                        ContextCompat.getColor(getApplicationContext(), R.color.sem_cor),
                                        PorterDuff.Mode.SRC_IN);
                                btnConfirmar.setText("Confirmar");
                            iniciarLeituraQR();

                        } else {
                                Toast.makeText(ConfirmarLerQRCode.this, "Login fracassado!", Toast.LENGTH_SHORT).show();
                                progressLogin.getIndeterminateDrawable().setColorFilter(
                                        ContextCompat.getColor(ConfirmarLerQRCode.this, R.color.sem_cor),
                                        PorterDuff.Mode.SRC_IN
                                );
                                btnConfirmar.setText("Confirmar");
                            }
                        }
                    });
                }
            };
            fecharTeclado();
            if (progressLogin != null) {
                btnConfirmar.setText("");
                progressLogin.getIndeterminateDrawable().setColorFilter(
                        ContextCompat.getColor(this, R.color.white),
                        PorterDuff.Mode.SRC_IN
                );
            }
            timer.schedule(timerTask, 1000);
        });

    }
    private void fecharTeclado(){
        View view = this.getCurrentFocus();
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void iniciarLeituraQR() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Escaneie o QR Code");
        integrator.setCameraId(0);  // Câmera traseira
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(true);
        integrator.initiateScan();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                // Exibe o conteúdo do QR Code
                Toast.makeText(this, "Conteúdo: " + result.getContents(), Toast.LENGTH_LONG).show();
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("QRScans");

                String key = databaseReference.push().getKey();
                databaseReference.child(key).setValue(result.getContents());
            } else {
                Toast.makeText(this, "Leitura cancelada", Toast.LENGTH_SHORT).show();
            }
        }
    }
}