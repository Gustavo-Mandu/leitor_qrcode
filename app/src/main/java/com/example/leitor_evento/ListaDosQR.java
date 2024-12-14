package com.example.leitor_evento;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;

import java.util.ArrayList;



public class ListaDosQR extends AppCompatActivity {

    private static ListaDosQR instance;
    private static ArrayList<String> listaQR = new ArrayList<>();  // Lista para armazenar QR Codes
   // private ArrayAdapter<String> adapter;  // Adapter para exibir a lista na ListView
    private DatabaseReference databaseReference;
    private Button buttonInicial;
    private LinearLayout linearLayoutQR;
    private Button btnFirebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lista_dos_qr);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseReference = FirebaseDatabase.getInstance().getReference("QRScans");
        buttonInicial = findViewById(R.id.buttonInicial);
        btnFirebase = findViewById(R.id.btnFirebase);


        btnFirebase.setOnClickListener(v -> {
            String url = "https://firebase.google.com/?hl=pt-br";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });

        buttonInicial.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        });

        // Carrega os dados do Firebase ao iniciar a atividade
        //carregarDadosDoFirebase();
        carregarDadosDoFirebase();
        atualizarLayout();
 }
    private void carregarDados() {
        listaQR.add("QR Code Exemplo 1");
        listaQR.add("QR Code Exemplo 2");
    }

    public void carregarDadosDoFirebase(){
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                String qrContent = snapshot.child("content").getValue(String.class);
                String dataHora = snapshot.child("dataHora").getValue(String.class);
                if (qrContent != null && dataHora != null) {
                    listaQR.add("QR Code: " + qrContent + "\nData e Hora: " + dataHora);
                    atualizarLayout();
                    Log.d("FirebaseData", "QR Code adicionado: " + qrContent);
                } else {
                    Log.d("FirebaseData", "Dados incompletos ou nulos.");
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {
                // Lógica para atualizações, se necessário
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                // Lógica para remoção, se necessário
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {
                // Lógica para reordenação, se necessário
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ListaDosQR.this, "Erro ao carregar dados", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void atualizarLayout() {
        for (String qrContent : listaQR) {
            TextView textView = new TextView(this);
            textView.setText(qrContent);
            textView.setTextColor(Color.WHITE);
            linearLayoutQR.addView(textView);
        }
    }

    public static void adicionarQRCode(String qrContent) {
        listaQR.add(qrContent);
    }
}
   /* // Método para adicionar um QR Code à lista e atualizar a interface
    public void adicionarQRCode(String qrCode) {
        listaQR.add(qrCode);

        // Verifica se o adapter foi inicializado e notifica a mudança
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    // Método para obter a lista de QR Codes (caso necessário em outras partes do código)
    public ArrayList<String> getLista() {
        return listaQR;
    } */
