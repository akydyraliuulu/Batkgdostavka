package dostavka.sistemosoft.com.batkgdostavka;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
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
import java.util.ArrayList;

public class DetailActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private String sub_id = "";
    private static int VIEWPAGE = 1;
    LinearLayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setTitle("Доставка");

        recyclerView = (RecyclerView) findViewById(R.id.item_list);
        recyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        EndlessRecyclerViewScrollListener scrollListener = new EndlessRecyclerViewScrollListener(mLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                loadNextDataFromApi(page);
            }
        };
        recyclerView.addOnScrollListener(scrollListener);

        if (isOnline()) {
            new GetClients().execute();
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(DetailActivity.this);
            builder.setMessage("Ошибка!Пожалуйста, проверьте интернет-соединение.");
            builder.setPositiveButton("Ok ", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    private void loadNextDataFromApi(int page) {
        VIEWPAGE = page;
        new GetClientsMore().execute();
        //Toast.makeText(getApplicationContext()," Load More " + page,Toast.LENGTH_LONG).show();
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
                finishAffinity();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class GetClients extends AsyncTask<String, String, String> {

        private ProgressDialog progressDialog = new ProgressDialog(DetailActivity.this);
        HttpURLConnection httpURLConnection;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Загрузка абонентов...");
            progressDialog.show();
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface arg0) {
                    GetClients.this.cancel(false);
                }
            });
        }


        @Override
        protected String doInBackground(String... params) {
            StringBuilder result = new StringBuilder();

            try {
                SharedPreferences sharedPreferences = getSharedPreferences("auth_token", 0);
                String token = sharedPreferences.getString("token", "");
                URL url = new URL("http://138.68.170.54/api/mobile/v10/subscribers/index?page=" + VIEWPAGE);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET"); // here you are telling that it is a POST request, which can be changed into "PUT", "GET", "DELETE" etc.
                httpURLConnection.setRequestProperty("Authorization", "Token token=" + token);
               // httpURLConnection.connect();
                InputStream in = new BufferedInputStream(httpURLConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                httpURLConnection.disconnect();
            }


          //  Log.d("Adapter", result.toString());
            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Log.d("Adapter", result);
            ArrayList<RecyclerItem> clientList = new ArrayList<RecyclerItem>();

            try {
                JSONArray jArray = new JSONArray(result);

                for (int i = 0; i < jArray.length(); i++) {

                    JSONObject jObject = jArray.getJSONObject(i);

                    JSONObject firm = jObject.getJSONObject("firm");
                    String firm_name = firm.getString("name");
                    String firm_count = jObject.getString("pcs");
                    String telephone = jObject.getString("telephone");
                    String address = jObject.getString("address");
                    String client_id = jObject.getString("id");


                    RecyclerItem recyclerItem = new RecyclerItem();

                    recyclerItem.setName1(firm_name);
                    recyclerItem.setCount1(firm_count);
                    recyclerItem.setId(client_id);
                    recyclerItem.setAddress(address);
                    recyclerItem.setTelephone(telephone);
                    clientList.add(recyclerItem);

                } // End Loop

                this.progressDialog.dismiss();

            } catch (JSONException e) {
                Log.e("JSONException", "Error: " + e.toString());
            } // catch (JSONException e)

            recyclerView.setAdapter(new MyAdapter(DetailActivity.this, clientList));
        }
    }

    private class GetClientsMore extends AsyncTask<String, String, String> {

        private ProgressDialog progressDialog = new ProgressDialog(DetailActivity.this);
        HttpURLConnection httpURLConnection;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Загрузка абонентов...");
            progressDialog.show();
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface arg0) {
                    GetClientsMore.this.cancel(false);
                }
            });
        }


        @Override
        protected String doInBackground(String... params) {
            StringBuilder result = new StringBuilder();

            try {
                SharedPreferences sharedPreferences = getSharedPreferences("auth_token", 0);
                String token = sharedPreferences.getString("token", "");
                URL url = new URL("http://138.68.170.54/api/mobile/v10/subscribers/index?page=" + VIEWPAGE);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET"); // here you are telling that it is a POST request, which can be changed into "PUT", "GET", "DELETE" etc.
                httpURLConnection.setRequestProperty("Authorization", "Token token=" + token);
                // httpURLConnection.connect();
                InputStream in = new BufferedInputStream(httpURLConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                httpURLConnection.disconnect();
            }


            //  Log.d("Adapter", result.toString());
            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Log.d("Adapter", result);
            ArrayList<RecyclerItem> clientList = new ArrayList<RecyclerItem>();

            try {
                JSONArray jArray = new JSONArray(result);

                for (int i = 0; i < jArray.length(); i++) {

                    JSONObject jObject = jArray.getJSONObject(i);

                    JSONObject firm = jObject.getJSONObject("firm");
                    String firm_name = firm.getString("name");
                    String firm_count = jObject.getString("pcs");
                    String telephone = jObject.getString("telephone");
                    String address = jObject.getString("address");
                    String client_id = jObject.getString("id");

                    RecyclerItem recyclerItem = new RecyclerItem();

                    recyclerItem.setName1(firm_name);
                    recyclerItem.setCount1(firm_count);
                    recyclerItem.setId(client_id);
                    recyclerItem.setAddress(address);
                    recyclerItem.setTelephone(telephone);
                    clientList.add(recyclerItem);

                } // End Loop

                this.progressDialog.dismiss();

            } catch (JSONException e) {
                Log.e("JSONException", "Error: " + e.toString());
            } // catch (JSONException e)

            recyclerView.setAdapter(new MyAdapter(DetailActivity.this, clientList));
        }
    }

    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

        private ArrayList<RecyclerItem> listItems;
        private Context mContext;

        MyAdapter(DetailActivity mContext, ArrayList<RecyclerItem> lisItems) {
            this.mContext = mContext;
            this.listItems = lisItems;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_detail_list, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {

            final RecyclerItem itemList = listItems.get(position);
            holder.txtname1.setText(itemList.getName1());
            holder.txtcount1.setText(itemList.getCount1());
            holder.txtAddress.setText(itemList.getAddress());
            holder.txtPhone.setText(itemList.getTelephone());
            holder.txtCheckbox.setChecked(false);

            holder.txtPhone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String tel = holder.txtPhone.getText().toString();
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", tel, null));
                    startActivity(intent);
                }
            });

        }

        @Override
        public int getItemCount() {
            return listItems.size();
        }

         class ViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener {

            TextView txtname1;
            TextView txtcount1;
            TextView txtAddress;
            TextView txtPhone;
            CheckBox txtCheckbox;

            ViewHolder(View itemView) {
                super(itemView);
                txtname1 = (TextView) itemView.findViewById(R.id.name1_id);
                txtcount1 = (TextView) itemView.findViewById(R.id.count1_id);
                txtAddress = (TextView) itemView.findViewById(R.id.address_id);
                txtPhone = (TextView) itemView.findViewById(R.id.phone_id);
                txtCheckbox = (CheckBox) itemView.findViewById(R.id.checkbox_id);
                txtCheckbox.setOnCheckedChangeListener(this);
            }

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(DetailActivity.this);
                    builder.setMessage("Вы уверены что хотите удалить из списка?");
                    builder.setPositiveButton("Да ", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            removeAt(getAdapterPosition(), this);
                            //notifyDataSetChanged();
                            Toast.makeText(mContext, "Доставлено", Toast.LENGTH_LONG).show();
                        }
                    });
                    builder.setNegativeButton("Нет ", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            txtCheckbox.setChecked(false);
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            }
        }

        void removeAt(int position, DialogInterface.OnClickListener viewHolder) {
            final RecyclerItem itemList = listItems.get(position);
            sub_id = itemList.getId();
            new AsyncT().execute();
            listItems.remove(position);
            notifyItemRemoved(position);
        }
    }

    private class AsyncT extends AsyncTask<String, String, String> {

        private ProgressDialog progressDialog = new ProgressDialog(DetailActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Доставка...");
            progressDialog.show();
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface arg0) {
                    AsyncT.this.cancel(false);
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

                    this.progressDialog.dismiss();

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
                SharedPreferences sharedPreferences = getSharedPreferences("auth_token", 0);
                String token = sharedPreferences.getString("token", "");
                URL url = new URL("http://138.68.170.54/api/mobile/v10/delivery_histories/deliveryman"); //Enter URL here
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                httpURLConnection.setRequestMethod("POST"); // here you are telling that it is a POST request, which can be changed into "PUT", "GET", "DELETE" etc.
                httpURLConnection.setRequestProperty("Content-Type", "application/json"); // here you are setting the `Content-Type` for the data you are sending which is `application/json`
                httpURLConnection.setRequestProperty("Authorization", "Token token=" + token);
                //httpURLConnection.connect();

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("subscriber_id", sub_id);

                JSONObject jsonObject1 = new JSONObject();
                jsonObject1.put("delivery_history", jsonObject);

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


//    public class RecyclerItem {
//
//        private String name;
//        private String address;
//        private String phone;
//        private Boolean checkbox;
//        private String id;
//
//        public RecyclerItem() {
//            this.name = name;
//            this.address = address;
//            this.phone = phone;
//        }
//
//        public String getName() {
//            return name;
//        }
//
//        public void setName(String name) {
//            this.name = name;
//        }
//
//        public String getAddress() {
//            return address;
//        }
//
//        public void setAddress(String address) {
//            this.address = address;
//        }
//
//        public String getPhone() {
//            return phone;
//        }
//
//        public void setPhone(String phone) {
//            this.phone = phone;
//        }
//
//        public Boolean getCheckbox() {
//            return checkbox;
//        }
//
//        public void setCheckbox(Boolean checkbox) {
//            this.checkbox = checkbox;
//        }
//
//        public void setId(String id) {
//            this.id = id;
//        }
//    }

}
