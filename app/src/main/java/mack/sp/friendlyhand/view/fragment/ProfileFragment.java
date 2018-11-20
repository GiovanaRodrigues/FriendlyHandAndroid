package mack.sp.friendlyhand.view.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

import br.com.jansenfelipe.androidmask.MaskEditTextChangedListener;
import de.hdodenhof.circleimageview.CircleImageView;
import mack.sp.friendlyhand.R;
import mack.sp.friendlyhand.model.Cliente;
import mack.sp.friendlyhand.model.Endereco;
import mack.sp.friendlyhand.model.Prestador;
import mack.sp.friendlyhand.model.Usuario;
import mack.sp.friendlyhand.tasks.HandlerTask;
import mack.sp.friendlyhand.tasks.TaskRest;
import mack.sp.friendlyhand.util.JsonParser;
import mack.sp.friendlyhand.util.RestUtil;
import mack.sp.friendlyhand.view.activity.InicialActivity;
import mack.sp.friendlyhand.view.activity.SignUpActivity;

import static android.content.Context.MODE_PRIVATE;
import static android.text.InputType.TYPE_CLASS_TEXT;
import static mack.sp.friendlyhand.view.activity.LoginActivity.APP_PREFS;
import static mack.sp.friendlyhand.view.activity.LoginActivity.EMAIL;
import static mack.sp.friendlyhand.view.activity.LoginActivity.SENHA;
import static mack.sp.friendlyhand.view.activity.SuperClassActivity.FOTO;
import static mack.sp.friendlyhand.view.activity.SuperClassActivity.ID;
import static mack.sp.friendlyhand.view.activity.SuperClassActivity.IP;
import static mack.sp.friendlyhand.view.activity.SuperClassActivity.NOME;
import static mack.sp.friendlyhand.view.activity.SuperClassActivity.TIPOUSUARIO;

/**
 * Created by Giovana Rodrigues on 19/04/2018.
 */

public class ProfileFragment extends Fragment {
    public final static String urlCliente = "http://" + IP + "/friendlyhand/v1/cliente/";
    public final static String urlPrestador = "http://" + IP + "/friendlyhand/v1/prestador/";

    private EditText editNome, editCpf, editData, editCelular, editEmail, editSenha,
            editCep, editRua, editNumero, editBairro, editCidade, editEstado;
    private Button btnAtualizar, btnFoto;
    private CircleImageView imgPerfil;
    private MaskEditTextChangedListener maskCpf, maskCel, maskData, maskCep;

    private Long id;
    private Usuario usuario;
    private Cliente cliente;
    private Prestador prestador;
    private static int LOAD_IMAGE_RESULTS = 1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle(getResources().getString(R.string.perfil));

        inicializarComponentes(view);

        return view;
    }

    // ---------------------------------------------------------------- Métodos nativos

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getActivity().getSharedPreferences(APP_PREFS, MODE_PRIVATE);
        id = prefs.getLong(ID, 0);

        Log.e("ID", "" + id);

        setHasOptionsMenu(true);
        new BuscarPerfil().execute();
    }

    @Override
    public void onStart() {
        super.onStart();

        adicionarMascaras();

        btnFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pegarFotoDaGaleria();
            }
        });

        // Tornar a senha visível e trocar o ícone
        editSenha.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Drawables: 0 = Left, 1 = Top, 2 = Right, 3 = Bottom

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (editSenha.getRight() - editSenha.getCompoundDrawables()[2].getBounds().width())) {
                        Drawable[] drawables = editSenha.getCompoundDrawables();
                        Bitmap bitmap = ((BitmapDrawable) drawables[2]).getBitmap();
                        Bitmap bitmap2 = ((BitmapDrawable) getResources().getDrawable(R.mipmap.ic_visibility_black_24dp)).getBitmap();

                        if (bitmap == bitmap2) {
                            editSenha.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.ic_visibility_off_black_24dp, 0);
                            editSenha.setInputType((InputType.TYPE_CLASS_TEXT |
                                    InputType.TYPE_TEXT_VARIATION_PASSWORD));
                        } else {
                            editSenha.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.ic_visibility_black_24dp, 0);
                            editSenha.setInputType(TYPE_CLASS_TEXT);
                        }

                        return true;
                    }
                }
                return false;
            }
        });

        // Método de pesquisa
        StrictMode.ThreadPolicy politicaDeAcesso = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(politicaDeAcesso);

        editCep.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    buscarCep(editCep.getText().toString().replace("-", ""));
                }
            }
        });

        btnAtualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validarCampos();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.menu_perfil, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.delete:
                AlertDialog.Builder alerta = new AlertDialog.Builder(getActivity());
                alerta.setTitle(R.string.deletar_conta);
                alerta.setMessage(R.string.mensagem_deletar_conta);

                alerta.setPositiveButton(getResources().getString(R.string.sim), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences prefs = getActivity().getSharedPreferences(APP_PREFS, MODE_PRIVATE);

                        String tipoUsuario = prefs.getString(TIPOUSUARIO, "cliente");

                        if (tipoUsuario.equalsIgnoreCase("cliente")) {
                            JsonParser<Cliente> parser = new JsonParser<>(Cliente.class);
                            new TaskRest(TaskRest.RequestMethod.DELETE, handlerTaskDelete, getActivity()).execute(urlCliente + id);
                        } else {
                            JsonParser<Prestador> parser = new JsonParser<>(Prestador.class);
                            new TaskRest(TaskRest.RequestMethod.DELETE, handlerTaskDelete, getActivity()).execute(urlPrestador + id);
                        }
                        dialog.dismiss();
                    }
                });

                alerta.setNegativeButton(getResources().getString(R.string.nao), null);

                alerta.show();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        removerMascaras();
        if (requestCode == LOAD_IMAGE_RESULTS && resultCode == getActivity().RESULT_OK && data != null) {
            Uri selectedImage = data.getData();

            try {
                Bitmap bitmap = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(selectedImage));
                imgPerfil.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                Toast.makeText(getActivity(), "ERRO", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    // ------------------------------------------------------------ Métodos da classe

    private void inicializarComponentes(View view) {
        // Dados usuario
        editNome = (EditText) view.findViewById(R.id.edit_nome);
        editCpf = (EditText) view.findViewById(R.id.edit_cpf);
        editData = (EditText) view.findViewById(R.id.edit_data);
        editCelular = (EditText) view.findViewById(R.id.edit_celular);
        editEmail = (EditText) view.findViewById(R.id.edit_email);
        editSenha = (EditText) view.findViewById(R.id.edit_senha);

        // Dados endereço
        editCep = (EditText) view.findViewById(R.id.edit_cep);
        editRua = (EditText) view.findViewById(R.id.edit_rua);
        editNumero = (EditText) view.findViewById(R.id.edit_numero);
        editBairro = (EditText) view.findViewById(R.id.edit_bairro);
        editCidade = (EditText) view.findViewById(R.id.edit_cidade);
        editEstado = (EditText) view.findViewById(R.id.edit_estado);

        // Botões
        btnAtualizar = (Button) view.findViewById(R.id.btnSignUp);
        btnFoto = (Button) view.findViewById(R.id.btnFoto);

        // Foto de Perfil
        imgPerfil = (CircleImageView) view.findViewById(R.id.img_profile);

        // Máscaras dos Campos
        maskCpf = new MaskEditTextChangedListener("###.###.###-##", editCpf);
        maskCel = new MaskEditTextChangedListener("(##)#####-####", editCelular);
        maskData = new MaskEditTextChangedListener("##/##/####", editData);
        maskCep = new MaskEditTextChangedListener("#####-###", editCep);
    }

    public void adicionarMascaras() {
        editCpf.addTextChangedListener(maskCpf);
        editCelular.addTextChangedListener(maskCel);
        editData.addTextChangedListener(maskData);
        editCep.addTextChangedListener(maskCep);
    }

    public void removerMascaras() {
        editCpf.removeTextChangedListener(maskCpf);
        editCelular.removeTextChangedListener(maskCel);
        editData.removeTextChangedListener(maskData);
        editCep.removeTextChangedListener(maskCep);
    }

    private void validarCampos() {
        if (!editNome.getText().equals("") && !editBairro.getText().equals("") && !editCelular.getText().equals("")
                && !editCep.getText().equals("") && !editCidade.getText().equals("") && !editCpf.getText().equals("")
                && !editData.getText().equals("") && !editEmail.getText().equals("") && !editEstado.getText().equals("")
                && !editNumero.getText().equals("") && !editRua.getText().equals("") && !editSenha.getText().equals("")) {

            SharedPreferences prefs = getActivity().getSharedPreferences(APP_PREFS, MODE_PRIVATE);
            String tipoUsuario = prefs.getString(TIPOUSUARIO, "cliente");

            String[] data = editData.getText().toString().split("/");
            int mes = Integer.parseInt(data[1]) - 1;
            Calendar calendarHoje = Calendar.getInstance();
            Calendar calendarServico = Calendar.getInstance();
            calendarServico.set(Calendar.DAY_OF_MONTH, Integer.parseInt(data[0]));
            calendarServico.set(Calendar.MONTH, mes);
            calendarServico.set(Calendar.YEAR, Integer.parseInt(data[2]));

            if (calendarServico.get(Calendar.YEAR) > calendarHoje.get(Calendar.YEAR) - 18) {
                final AlertDialog alertDialog;
                final AlertDialog.Builder alerta = new AlertDialog.Builder(getActivity());
                alertDialog = alerta.create();

                View alertView = getActivity().getLayoutInflater().inflate(R.layout.alert_erro, null);

                final TextView txtMensagem = (TextView) alertView.findViewById(R.id.txtMensagem);
                txtMensagem.setText(getResources().getString(R.string.data_nascimento_invalida));

                alertDialog.setView(alertView);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        alertDialog.dismiss();
                    }
                }, 2500);

                alertDialog.show();
            } else {
                if (tipoUsuario.equalsIgnoreCase("cliente")) {
                    atualizarCliente();
                } else {
                    atualizarPrestador();
                }
            }
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.preencha_todos_campos), Toast.LENGTH_SHORT).show();
        }
    }

    private void atualizarCliente() {
        cliente = new Cliente();
        Bitmap fotoPerfil = ((BitmapDrawable) imgPerfil.getDrawable()).getBitmap();
        String stringFoto = bitmapToString(fotoPerfil);

        cliente.setNome(editNome.getText().toString());
        cliente.setCelular(editCelular.getText().toString());
        cliente.setCpf(editCpf.getText().toString());
        cliente.setFoto(stringFoto);
        cliente.setEmail(editEmail.getText().toString());
        cliente.setSenha(editSenha.getText().toString());
        cliente.setDataNascimento(editData.getText().toString());

        Endereco endereco = new Endereco();
        endereco.setId(usuario.getEndereco().getId());
        endereco.setBairro(editBairro.getText().toString());
        endereco.setCep(Integer.parseInt(editCep.getText().toString().replace("-", "")));
        endereco.setCidade(editCidade.getText().toString());
        endereco.setNumero(Integer.parseInt(editNumero.getText().toString()));
        endereco.setRua(editRua.getText().toString());
        endereco.setUf(editEstado.getText().toString());

        cliente.setEndereco(endereco);

        JsonParser<Cliente> parser = new JsonParser<>(Cliente.class);
        new TaskRest(TaskRest.RequestMethod.PUT, handlerTaskEdit, getActivity()).execute(urlCliente + id, parser.fromObject(cliente));
    }

    private void atualizarPrestador() {
        prestador = new Prestador();
        Bitmap fotoPerfil = ((BitmapDrawable) imgPerfil.getDrawable()).getBitmap();
        String stringFoto = bitmapToString(fotoPerfil);

        prestador.setNome(editNome.getText().toString());
        prestador.setCelular(editCelular.getText().toString());
        prestador.setCpf(editCpf.getText().toString());
        prestador.setFoto(stringFoto);
        prestador.setEmail(editEmail.getText().toString());
        prestador.setSenha(editSenha.getText().toString());
        prestador.setDataNascimento(editData.getText().toString());

        Endereco endereco = new Endereco();
        endereco.setId(usuario.getEndereco().getId());
        endereco.setBairro(editBairro.getText().toString());
        endereco.setCep(Integer.parseInt(editCep.getText().toString().replace("-", "")));
        endereco.setCidade(editCidade.getText().toString());
        endereco.setNumero(Integer.parseInt(editNumero.getText().toString()));
        endereco.setRua(editRua.getText().toString());
        endereco.setUf(editEstado.getText().toString());

        prestador.setEndereco(endereco);

        JsonParser<Prestador> parser = new JsonParser<>(Prestador.class);
        new TaskRest(TaskRest.RequestMethod.PUT, handlerTaskEdit, getActivity()).execute(urlPrestador + id, parser.fromObject(prestador));
    }

    // Pegar foto na galeria de fotos do celular
    private void pegarFotoDaGaleria() {
        // Criar Intent para acessar a galeria
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        // Iniciar Intent para retornar a foto da galeria
        startActivityForResult(i, LOAD_IMAGE_RESULTS); //LOAD_IMAGE_RESULTS
    }

    // Converter string para imagem
    private Bitmap stringToBitmap(String stringFoto) {
        Bitmap foto = null;

        try {
            byte[] encodeByte = Base64.decode(stringFoto, Base64.DEFAULT);
            foto = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        } catch (Exception e) {
            e.getMessage();
        }

        return foto;
    }

    // Converter imagem em string
    private String bitmapToString(Bitmap foto) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        foto.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();

        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    // Buscar endereço através do CEP
    private void buscarCep(String numeroCep) {
        String response = makeRequest("http://viacep.com.br/ws/" + numeroCep + "/json/");

        try {
            JSONObject json = new JSONObject(response);
            String logradouro = json.getString("logradouro");
            String bairro = json.getString("bairro");
            String cidade = json.getString("localidade");
            String estado = json.getString("uf");

            editRua.setText(logradouro);
            editBairro.setText(bairro);
            editCidade.setText(cidade);
            editEstado.setText(estado);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Fazer requisição
    private String makeRequest(String enderecoUrl) {
        HttpURLConnection conexao = null;
        URL url;
        String resposta = "";

        try {
            url = new URL(enderecoUrl);
            conexao = (HttpURLConnection) url.openConnection();
            resposta = readStream(conexao.getInputStream());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            conexao.disconnect();
        }
        return resposta;
    }

    // Retornar os dados
    private String readStream(InputStream in) {
        BufferedReader leitor = null;
        StringBuilder builder = new StringBuilder(); //Evitar vazamento de memória

        try {
            leitor = new BufferedReader(new InputStreamReader(in));
            String line = null;

            while ((line = leitor.readLine()) != null) {
                builder.append(line + "\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (leitor != null) {
                try {
                    leitor.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return builder.toString();
    }

    // ------------------------------------------------------------- Métodos de persistência
    // Buscar programas pendentes
    public class BuscarPerfil extends AsyncTask<Void, Void, String> {
        private String url;
        private ProgressDialog progresso;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progresso = new ProgressDialog(getActivity());
            progresso.setTitle(getResources().getString(R.string.aguarde));
            progresso.setMessage(getResources().getString(R.string.consultando));
            progresso.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            String retorno = null;
            SharedPreferences prefs = getActivity().getSharedPreferences(APP_PREFS, MODE_PRIVATE);

            String tipoUsuario = prefs.getString(TIPOUSUARIO, "cliente");

            Log.e("USUARIO", tipoUsuario);

            if (tipoUsuario.equalsIgnoreCase("cliente")) {
                url = urlCliente;
            } else {
                url = urlPrestador;
            }

            try {
                retorno = RestUtil.get(url + id, getActivity());
                Log.e("RETORNO", retorno);
            } catch (IOException | RuntimeException erro) {
                erro.printStackTrace();
            }

            try {
                GsonBuilder formatter = new GsonBuilder();
                formatter.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                    public Date deserialize(JsonElement json, Type typeOf, JsonDeserializationContext context) throws JsonParseException {
                        return new Date(json.getAsJsonPrimitive().getAsLong());
                    }
                });

                Gson form = formatter.create();

                if (retorno != null && !retorno.equals("")) {
                    JSONObject usuarioJson = new JSONObject(retorno);
                    usuario = form.fromJson(usuarioJson.toString(), Usuario.class);
                }

            } catch (JSONException | RuntimeException erro) {
                erro.printStackTrace();
            }

            return retorno;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progresso.dismiss();

            if (usuario != null) {
                // Dados usuario
                editNome.setText(usuario.getNome());
                editCpf.setText(usuario.getCpf());
                editData.setText(usuario.getDataNascimento());
                editCelular.setText(usuario.getCelular());
                editEmail.setText(usuario.getEmail());
                editSenha.setText(usuario.getSenha());

                // Dados endereço
                editCep.setText(String.format("%08d", usuario.getEndereco().getCep()));
                editRua.setText(usuario.getEndereco().getRua());
                editNumero.setText(String.valueOf(usuario.getEndereco().getNumero()));
                editBairro.setText(usuario.getEndereco().getBairro());
                editCidade.setText(usuario.getEndereco().getCidade());
                editEstado.setText(usuario.getEndereco().getUf());

                if (usuario.getFoto() != null) {
                    if (!usuario.getFoto().equals("")) {
                        imgPerfil.setImageBitmap(stringToBitmap(usuario.getFoto()));
                    }
                }
            }
        }
    }

    // Editar perfil
    public HandlerTask handlerTaskEdit = new HandlerTask() {
        @Override
        public void onPreHandle() {

        }

        @Override
        public void onSuccess(String valueRead) {
            SharedPreferences prefs = getActivity().getSharedPreferences(APP_PREFS, MODE_PRIVATE);
            String tipoUsuario = prefs.getString(TIPOUSUARIO, "cliente");

            final AlertDialog alertDialog;
            final AlertDialog.Builder alerta = new AlertDialog.Builder(getActivity());
            alertDialog = alerta.create();

            View alertView = getActivity().getLayoutInflater().inflate(R.layout.alert_success, null);

            final TextView txtMensagem = (TextView) alertView.findViewById(R.id.txtMensagem);
            txtMensagem.setText(getResources().getString(R.string.sucesso_perfil));

            alertDialog.setView(alertView);

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    alertDialog.dismiss();
                }
            }, 1500);

            alertDialog.show();

            if (tipoUsuario.equalsIgnoreCase("cliente")) {
                SharedPreferences.Editor editor = prefs.edit();

                editor.putString(TIPOUSUARIO, "cliente");
                editor.putString(NOME, cliente.getNome());
                editor.putString(FOTO, cliente.getFoto());
                editor.putString(EMAIL, cliente.getEmail());
                editor.putString(SENHA, cliente.getSenha());
                editor.commit();
            } else {
                SharedPreferences.Editor editor = prefs.edit();

                editor.putString(TIPOUSUARIO, "prestador");
                editor.putString(NOME, prestador.getNome());
                editor.putString(FOTO, prestador.getFoto());
                editor.putString(EMAIL, prestador.getEmail());
                editor.putString(SENHA, prestador.getSenha());
                editor.commit();
            }
        }

        @Override
        public void onError(Exception erro) {
            Log.e("ERRO", erro.getMessage());
            if (erro.getMessage().contains("401")) {
                Toast.makeText(getActivity(), getResources().getString(R.string.erro_cadastro), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), getResources().getString(R.string.erro_conexao), Toast.LENGTH_LONG).show();
            }
        }
    };

    public HandlerTask handlerTaskDelete = new HandlerTask() {
        @Override
        public void onPreHandle() {

        }

        @Override
        public void onSuccess(String valueRead) {
            SharedPreferences prefs = getActivity().getSharedPreferences(APP_PREFS, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            editor.putLong(ID, 0);
            editor.putString(TIPOUSUARIO, "");
            editor.putString(NOME, "");
            editor.putString(EMAIL, "");
            editor.putString(FOTO, "");
            editor.putString(SENHA, "");
            editor.commit();

            getActivity().finish();
            startActivity(new Intent(getActivity(), InicialActivity.class));

            // Apresentar animação de passagem da esquerda para direita
            getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        }

        @Override
        public void onError(Exception erro) {
            Log.e("ERRO", erro.getMessage());
            if (erro.getMessage().contains("401")) {
                Toast.makeText(getActivity(), getResources().getString(R.string.erro_deletar), Toast.LENGTH_LONG).show();
            } else if (erro.getMessage().contains("200")) {
                SharedPreferences prefs = getActivity().getSharedPreferences(APP_PREFS, MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();

                editor.putLong(ID, 0);
                editor.putString(TIPOUSUARIO, "");
                editor.putString(NOME, "");
                editor.putString(EMAIL, "");
                editor.putString(SENHA, "");
                editor.commit();

                getActivity().finish();
                startActivity(new Intent(getActivity(), InicialActivity.class));

                // Apresentar animação de passagem da esquerda para direita
                getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            } else {
                Toast.makeText(getActivity(), getResources().getString(R.string.erro_conexao), Toast.LENGTH_LONG).show();
            }
        }
    };
}
