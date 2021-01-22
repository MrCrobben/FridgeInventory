package com.example.fridgeinventory;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ProductInfo extends AppCompatActivity {


    ProgressDialog pd;
    JsonNode jsonNode;
    ObjectMapper mapper = new ObjectMapper();
    //ImageView productImg;
    TextView txtName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_productinfo);
        Bundle extra = getIntent().getExtras();     
        String barcode = extra.getString("barcode");
        String uri = "https://world.openfoodfacts.org/api/v0/product/" + barcode + ".json";
        new JsonTask().execute(uri);

    }

    @SuppressLint("StaticFieldLeak")
    private class JsonTask extends AsyncTask<String, String, String> {
        private JsonTask(){
            super();
        }
        protected void onPreExecute(){
            super.onPreExecute();
            pd = new ProgressDialog(ProductInfo.this);
            pd.setMessage("Please wait for data to load!");
            pd.setCancelable(false);
            pd.show();
        }
        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection  connection= null;
            BufferedReader reader = null;

            try{
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.addRequestProperty("User-agent","Android");
                connection.addRequestProperty("Version","Version: 0.1");
                connection.addRequestProperty("FridgeInventory","https://github.com/MrCrobben/FridgeInventory");
                connection.connect();

                InputStream is = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb=new StringBuilder();
                String line;
                while((line=reader.readLine())!= null){
                    sb.append(line);
                }
                return sb.toString();
            }catch (MalformedURLException e){
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }finally {
                if(connection != null){
                    connection.disconnect();
                }
                try{
                    if(reader!= null){
                        reader.close();
                    }
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);
            if(pd.isShowing()){
                pd.dismiss();
            }
            try {
                jsonNode = mapper.readTree(result);
                txtName = findViewById(R.id.txtName);
                txtName.setText(jsonNode.findValue("product_name").asText());
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }catch(Exception e){
                txtName.setText(String.valueOf(result ==null));
            }
        }
    }
}