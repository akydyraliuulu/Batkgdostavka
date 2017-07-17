package dostavka.sistemosoft.com.batkgdostavka;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SignInActivity extends AppCompatActivity {

    String email = "";
    String password = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Авторизация");

        Button sign_in = (Button) findViewById(R.id.sign_in);
        sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et_name = (EditText) findViewById(R.id.et_email);
                EditText et_password = (EditText) findViewById(R.id.et_password);

                email = et_name.getText().toString();
                password = et_password.getText().toString();

                if (!email.equals("") && !password.equals("")) {

                    SharedPreferences sp = getSharedPreferences("auth_token", 0);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("user_email", email);
                    editor.putString("password", password);
                    editor.commit();

                    if (isOnline()) {
                        AsyncT asyncT = new AsyncT();
                        asyncT.execute();
                    }else{
                        final AlertDialog.Builder builder = new AlertDialog.Builder(SignInActivity.this);
                        builder.setMessage("Ошибка!Пожалуйста, проверьте интернет-соединение.");
                        builder.setPositiveButton("Ok ",new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }

                } else {
                    Toast.makeText(SignInActivity.this, "Пожалуйста, заполните все обязательные поля.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // API 5+ solution
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    class AsyncT extends AsyncTask<String, String, String> {

        private ProgressDialog progressDialog = new ProgressDialog(SignInActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Выполнение...");
            progressDialog.show();
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface arg0) {
                    AsyncT.this.cancel(true);
                }
            });
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                JSONObject json = (JSONObject) new JSONTokener(result).nextValue();
                Boolean success = json.getBoolean("success");
                if (success) {
                    JSONObject json2 = json.getJSONObject("data");
                    String token = (String) json2.get("auth_token");
                    JSONObject jsonObject = json2.getJSONObject("deliveryman");
                    String name = jsonObject.getString("name");
                    String telephone = jsonObject.getString("telephone");

                    Log.d("Token", token);
                    SharedPreferences sharedPreferences = getSharedPreferences("auth_token", 0);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("token", token);
                    editor.putString("name", name);
                    editor.putString("telephone", telephone);
                    editor.commit();

                    this.progressDialog.dismiss();
                    Intent intent = new Intent(SignInActivity.this, DetailActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else {
                    JSONObject object = new JSONObject("data");
                    String errors = object.getString("errors");
                    this.progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Ошибка: " + errors, Toast.LENGTH_LONG).show();
                }
                // End Loop


            } catch (JSONException e) {
                Log.e("JSONException", "Error: " + e.toString());
            } // catch (JSONException e)

        }

        @Override
        protected String doInBackground(String... params) {
            StringBuilder result = new StringBuilder();

            try {
                URL url = new URL("http://138.68.170.54/api/mobile/v10/authentications/sign_in"); //Enter URL here
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                httpURLConnection.setRequestMethod("POST"); // here you are telling that it is a POST request, which can be changed into "PUT", "GET", "DELETE" etc.
                httpURLConnection.setRequestProperty("Content-Type", "application/json"); // here you are setting the `Content-Type` for the data you are sending which is `application/json`
                //httpURLConnection.connect();

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("email", email);
                jsonObject.put("password", password);

                JSONObject jsonObject1 = new JSONObject();
                jsonObject1.put("deliveryman", jsonObject);

                String str = jsonObject1.toString();
                //str = str.replaceFirst(":", "=>");

                Log.d("Json ", str);

                OutputStream os = httpURLConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(str);
                writer.flush();
                writer.close();
                os.close();
                httpURLConnection.connect();
                InputStream in = new BufferedInputStream(httpURLConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                Log.d("Result ", result.toString());

            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.d("Kata", "Kata boldu 1");
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("Kata", "Kata boldu 2");
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d("Kata", "Kata boldu 3");
            }

            return result.toString();
        }
    }
}
