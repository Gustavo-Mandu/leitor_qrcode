package com.example.leitor_evento;

import android.os.Build;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;

public class DataHoraBrasilia {

    public static void main(String[] args) {
        // Obtenha a data e hora atuais no fuso horário de Brasília
        ZonedDateTime dataHoraBrasilia = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dataHoraBrasilia = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"));
        }

        // Formate a data e hora
        DateTimeFormatter formatter = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        }
        String dataHoraFormatada = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dataHoraFormatada = dataHoraBrasilia.format(formatter);
        }

        // Exibe a data e hora formatada
        System.out.println("Data e Hora em Brasília: " + dataHoraFormatada);

    }


}
