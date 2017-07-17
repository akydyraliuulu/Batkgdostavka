package dostavka.sistemosoft.com.batkgdostavka;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        new Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        if (isOnline()) {
                            SharedPreferences sharedPreferences = getSharedPreferences("auth_token", 0);
                            String token = sharedPreferences.getString("token", "");
                            if (!token.equals("")) {
                                Intent intent = new Intent(WelcomeActivity.this, DetailActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            } else {
                                Intent intent1 = new Intent(WelcomeActivity.this, SignInActivity.class);
                                intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent1);
                            }
                        }
                         else {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(WelcomeActivity.this);
                            builder.setMessage("Ошибка!Пожалуйста, проверьте интернет-соединение.");
                            builder.setPositiveButton("Ok ", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finishAffinity();
                                }
                            });
                            AlertDialog alertDialog = builder.create();
                            alertDialog.show();
                        }
                    }
                },
                1200);
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}
