package mack.sp.friendlyhand.util;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class RestUtil {
    public static String readStream(InputStream in) {
        Scanner leitor = null;
        StringBuilder builder = new StringBuilder();

        try {
            leitor = new Scanner(in);

            while (leitor.hasNextLine()) {
                builder.append(leitor.nextLine());
            }

        } catch (Exception erro) {
            Log.w("SENAI", "Erro lendo stream: " + erro.getMessage());
        } finally {
            leitor.close();
        }

        return builder.toString();
    }

    public static String get(String url, Context contexto) throws IOException {
        URL endereco = new URL(url);
        HttpURLConnection conexao = null;
        InputStream in = null;
        String retorno = null;

        try {
            //Conecta na URL
            conexao = (HttpURLConnection) endereco.openConnection();
            conexao.setConnectTimeout(15000);
            conexao.setReadTimeout(15000);
            conexao.connect();

            //Trata a resposta
            int status = conexao.getResponseCode();

            if (status > HttpURLConnection.HTTP_BAD_REQUEST) {
                Log.e("MACK", "Erro: " + status);
                throw new RuntimeException("Erro: " + status);
            } else {
                in = conexao.getInputStream();
            }

            //Capturar e tratar o retorno
            retorno = readStream(in);
            Log.w("RETORNO", retorno);
            in.close();

        } catch (IOException erro) {
            erro.printStackTrace();
            throw erro;
        } finally {
            if (conexao != null) {
                conexao.disconnect();
            }
        }
        return retorno;
    }
}
