package mack.sp.friendlyhand.view.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import mack.sp.friendlyhand.R;

import static mack.sp.friendlyhand.view.activity.LoginActivity.APP_PREFS;
import static mack.sp.friendlyhand.view.activity.LoginActivity.EMAIL;
import static mack.sp.friendlyhand.view.activity.LoginActivity.SENHA;
import static mack.sp.friendlyhand.view.activity.SuperClassActivity.TIPOUSUARIO;

/**
 * Created by Giovana Rodrigues on 19/04/2018.
 */

public class InicialActivity extends AppCompatActivity {
    private Button btnLogin, btnCadastro;
    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.NoActionBarTheme);
        setContentView(R.layout.activity_initial);

        SharedPreferences prefs = getSharedPreferences(APP_PREFS, MODE_PRIVATE);
        String email = String.valueOf(prefs.getString(EMAIL, ""));
        String senha = String.valueOf(prefs.getString(SENHA, ""));

        if ((email.equals("") && senha.equals(""))) {

            btnCadastro = (Button) findViewById(R.id.btnCadastro);
            btnLogin = (Button) findViewById(R.id.btnLogin);

            linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
            Drawable background = linearLayout.getBackground();
            background.setAlpha(215);

            // Ir para tela de login
            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                    startActivity(new Intent(InicialActivity.this, LoginActivity.class));

                    // Apresentar animação de passagem da esquerda para direita
                    overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                }
            });

            // Ir para tela de cadastro
            btnCadastro.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                    startActivity(new Intent(InicialActivity.this, SignUpActivity.class));

                    // Apresentar animação de passagem da esquerda para direita
                    overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                }
            });
        } else {
            String tipoUsuario = prefs.getString(TIPOUSUARIO, "cliente");

            if (tipoUsuario.equalsIgnoreCase("cliente")) {
                finish();
                startActivity(new Intent(InicialActivity.this, MainActivityClient.class));

                // Apresentar animação de passagem da esquerda para direita
                overridePendingTransition(0,0);
            } else {
                finish();
                startActivity(new Intent(InicialActivity.this, MainActivityProfessional.class));

                // Apresentar animação de passagem da esquerda para direita
                overridePendingTransition(0, 0);
            }
        }
    }
}
