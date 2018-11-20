package mack.sp.friendlyhand.util;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Giovana Rodrigues on 16/05/2018.
 */

public class Permissoes {
    public static boolean checarPermissoes(Activity activity, int requestCode, String... permissoes) {
        List<String> listaNegacoes = new ArrayList<>();

        for (String permissao : permissoes) {
            //Exibe o estado atual da permissão solicitada
            if (ContextCompat.checkSelfPermission(activity, permissao) != PackageManager.PERMISSION_GRANTED) {
                listaNegacoes.add(permissao);
            }
        }

        if (listaNegacoes.isEmpty()) {
            return true;
        } else {
            String[] permissoesNegadas = new String[listaNegacoes.size()];
            listaNegacoes.toArray(permissoesNegadas);
            ActivityCompat.requestPermissions(activity, permissoesNegadas, requestCode);
            return false;
        }
    }
}

