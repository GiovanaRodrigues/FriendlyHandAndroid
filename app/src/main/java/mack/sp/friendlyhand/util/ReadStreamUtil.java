package mack.sp.friendlyhand.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Giovana Rodrigues on 16/05/2018.
 */

public class ReadStreamUtil {
    public static String readStream(InputStream inputStream) throws Exception {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder builder = new StringBuilder();
            String linha = null;
            while ((linha = reader.readLine()) != null) {
                builder.append(linha + "\n");
            }
            reader.close();
            return builder.toString();
        } catch (Exception erro) {
            throw erro;
        }
    }
}
