package mack.sp.friendlyhand.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import mack.sp.friendlyhand.R;
import mack.sp.friendlyhand.util.ReadStreamUtil;

/**
 * Created by Giovana Rodrigues on 16/05/2018.
 */

public class TaskRest extends AsyncTask<String, Void, String> {
    private ProgressDialog progresso;
    // Tipos de requisição
    public enum RequestMethod {
        GET, POST, PUT, DELETE
    }

    // Possível Exception
    private Exception exception;
    // Tipo de método chamado
    private RequestMethod method;
    // Tratar eventos relacionados à task
    private HandlerTask handlerTask;
    // Activity que está sendo executada
    private Context context;

    private String token;

    /**
     * @param method      Tipo de Requisição REST a ser realizada
     * @param handlerTask Handler com as ações a serem realizadas em cada etapa da task
     */
    public TaskRest(RequestMethod method, HandlerTask handlerTask, Context context) {
        this.method = method;
        this.handlerTask = handlerTask;
        this.context = context;
    }

    /**
     * @param method      Tipo de Requisição REST a ser realizada
     * @param handlerTask Handler com as ações a serem realizadas em cada etapa da task
     * @param token       Token Authorization para realizar a requisição REST
     */
    public TaskRest(RequestMethod method, HandlerTask handlerTask, Context context, String token) {
        this(method, handlerTask, context);
        this.token = token;
    }

    @Override
    protected void onPreExecute() {
        handlerTask.onPreHandle();
        progresso = new ProgressDialog(context);
        progresso.setTitle(context.getResources().getString(R.string.aguarde));
        progresso.setMessage(context.getResources().getString(R.string.salvando));
        progresso.show();
    }

    @Override
    protected String doInBackground(String... params) {
        String retorno = null;
        try {
            String endereco = params[0];
            URL url = new URL(endereco);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);

            if (token != null) {
                connection.setRequestProperty("Authorization", token);
            }

            connection.setRequestMethod(method.toString());
            // caso seja um POST ou PUT, deverá haver um segundo parâmetro que corresponde ao json que deve ser enviado
            // no corpo da requisição
            if (method == RequestMethod.POST || method == RequestMethod.PUT) {
                String json = params[1];

                connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                // habilita a escrita na requisição
                connection.setDoOutput(true);
                // obtem um objeto OutputStream para gravar dados na requisição
                OutputStream outputStream = connection.getOutputStream();
                // escreve o JSON no corpo da requisição
                outputStream.write(json.getBytes("UTF-8"));
                // libera o output e fecha o recurso
                outputStream.flush();
                outputStream.close();
            }

            int responseCode = connection.getResponseCode();
            switch (method) {
                case GET:
                case POST:
                case PUT:
                    if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK) {
                        // Ler o corpo da resposta
                        InputStream inputStream = connection.getInputStream();
                        retorno = ReadStreamUtil.readStream(inputStream);
                        inputStream.close();

                    } else {
                        throw new Exception(method.toString() + ";" + responseCode);
                    }
                    break;
                case DELETE:
                    if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                        retorno = "";
                    } else {
                        throw new Exception(method.toString() + ";" + responseCode);
                    }
            }
            connection.disconnect();
        } catch (IndexOutOfBoundsException erro) {
            exception = new Exception("Está faltando um parâmetro para a execução do método");
        } catch (Exception erro) {
            exception = erro;
        }
        return retorno;
    }

    @Override
    protected void onPostExecute(String s) {
        if (s != null) {
            handlerTask.onSuccess(s);
        } else {
            handlerTask.onError(exception);
        }

        progresso.dismiss();
    }
}
