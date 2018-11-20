package mack.sp.friendlyhand.view.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class SuperClassActivity extends AppCompatActivity {
    public final static String APP_PREFS = "app_prefs", EMAIL = "email", SENHA = "senha", ID = "id", FOTO = "foto", NOME = "nome", TIPOUSUARIO = "tipousuario";
    public final static String IP = "192.168.43.73:8080";
    //public final static String IP = "192.168.0.15:8080";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
