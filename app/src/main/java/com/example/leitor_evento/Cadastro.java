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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class Cadastro extends AppCompatActivity {

    private FirebaseAuth auth;

    private TextInputEditText editUsername, editUsername2, editPassword;
    private TextView txtLogar;
    private Button btnCadastro;
    private ConstraintLayout main;
    private ProgressBar progressCadastro;
    private final Timer timer = new Timer();
    private TimerTask timerTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();



        OnBackPressedDispatcher dispatcher = getOnBackPressedDispatcher();
        dispatcher.addCallback(this, new OnBackPressedCallback(true) {
            int i = 0;
            @Override
            public void handleOnBackPressed() {
                i++;
                if(i == 2)
                    finish();
                else Toast.makeText(Cadastro.this, "Precione mais uma vez para sair.", Toast.LENGTH_SHORT).show();
            }
        });

        editUsername = findViewById(R.id.editUsername);
        editUsername2 = findViewById(R.id.editUsername2);
        editPassword = findViewById(R.id.editPassword);
        txtLogar = findViewById(R.id.txtLogar);
        btnCadastro = findViewById(R.id.btnCadastro);
        main = findViewById(R.id.main);
        progressCadastro = findViewById(R.id.progressCadastro);

        progressCadastro.setEnabled(false);

        editUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnCadastro.setEnabled(!TextUtils.isEmpty(s));
                if (btnCadastro.isEnabled()){
                    btnCadastro.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                } else {
                    btnCadastro.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Não precisamos fazer nada aqui
            }
        });

        txtLogar.setOnClickListener(v -> {
            Intent intentGo = new Intent(Cadastro.this, Login.class);
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
                    constraintSet.setVerticalBias(R.id.containerCadastro, 0.1f);
                } else { // Quando o teclado está oculto
                    constraintSet.setVerticalBias(R.id.containerCadastro, 0.468f);
                }
                constraintSet.applyTo(main);
            }
        });

        btnCadastro.setOnClickListener(v -> {
            String email = (editUsername.getText()).toString();
            String username = (editUsername2.getText()).toString();
            String password = (editPassword.getText()).toString();

            timerTask = new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(password.length() < 6 || username.isEmpty()){
                                Toast.makeText(Cadastro.this, "A senha deve conter 6 caracteres no minímo e um nome deve ser inserido.", Toast.LENGTH_SHORT).show();
                                progressCadastro.getIndeterminateDrawable().setColorFilter(
                                        ContextCompat.getColor(Cadastro.this, R.color.sem_cor),
                                        PorterDuff.Mode.SRC_IN);
                                btnCadastro.setText("Cadastrar");
                            }
                            else{
                            auth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(Cadastro.this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                FirebaseUser user = auth.getCurrentUser();
                                                if (user != null) {
                                                    // Registro bem-sucedido
                                                    Toast.makeText(Cadastro.this, "Usuário cadastrado com sucesso", Toast.LENGTH_SHORT).show();
                                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                            .setDisplayName(username)
                                                            .build();
                                                    user.updateProfile(profileUpdates);
                                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            } else {
                                                // Falha no registro
                                                Toast.makeText(Cadastro.this, "Digite o email corretamente" /*+ task.getException().getMessage()*/, Toast.LENGTH_SHORT).show();
                                                progressCadastro.getIndeterminateDrawable().setColorFilter(
                                                        ContextCompat.getColor(Cadastro.this, R.color.sem_cor),
                                                        PorterDuff.Mode.SRC_IN);
                                                btnCadastro.setText("Cadastrar");
                                            }
                        }
                    });
                }
            }});
        }};
            fecharTeclado();
            if (progressCadastro != null) {
                btnCadastro.setText("");
                progressCadastro.getIndeterminateDrawable().setColorFilter(
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

}