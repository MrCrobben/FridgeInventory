package com.example.fridgeinventory;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class ProductInfo extends AppCompatActivity {


    ProgressDialog pd;
    ObjectMapper mapper = new ObjectMapper();
    ImageView productImg;
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
    private class JsonTask extends AsyncTask<String, String, Product > {
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
        protected Product doInBackground(String... strings) {
            ByteArrayOutputStream res=null;
            Product product = null;
            Bitmap pic=null;
            String out = null;
            String productName=null;
            int i =0;

            try {
                InputStream is = getInputStreamFromUrl(strings[0]).getInputStream();
                res = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) != -1) {
                    res.write(buffer, 0, length);
                    ++i;
                }
                is.close();
                out = res.toString();
            }catch(IOException e){
                e.printStackTrace();
            }finally {
                try{
                    if(res!= null){
                        res.close();
                    }
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
            try {
                JsonNode jsonNode = mapper.readTree(out);
                productName = jsonNode.findValue("product_name").toString();
                JsonNode picNode = jsonNode.findValue("selected_images").findValue("front").findValue("thumb");
                HashMap<String,String> map = mapper.readValue(picNode.toString(), HashMap.class);
                String picUrl;
                if(map.containsKey("en")){
                    picUrl = map.get("en");
                }else{
                    Set<String> set = map.keySet();
                    Iterator<String> it = set.iterator();
                    picUrl = map.get(it.next());
                }
                picUrl =picUrl.replace('\"',' ').trim();
                InputStream is1 = getInputStreamFromUrl(picUrl).getInputStream();
                pic = BitmapFactory.decodeStream(is1);
                is1.close();

            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                return new Product(productName,pic);
            }
        }
        @Override
        protected void onPostExecute(Product result){
            super.onPostExecute(result);
            if(pd.isShowing()){
                pd.dismiss();
            }
            txtName = findViewById(R.id.txtName);
            productImg = findViewById(R.id.productImg);
            if(result == null){
                txtName.setText("Error");
            }else {
                txtName.setText(result.getProduct_name());
                productImg.setImageBitmap(result.getProduct_pic());
            }

        }
    }
    @Override
    public void onBackPressed() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Warning");
        alertDialog.setMessage("Do you want to save product in your inventory?");
        alertDialog.setPositiveButton("Yes - Don't press because it is not implemented yet", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //add saving product
            }
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(ProductInfo.this,MainActivity.class));
                ProductInfo.this.finish();
            }
        });
        AlertDialog alertWind = alertDialog.create();
        alertDialog.show();
    }

    private HttpURLConnection getInputStreamFromUrl(String strUrl){
        HttpURLConnection connection=null;
        URL url = null;
        try{
            url = new URL(strUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.addRequestProperty("User-agent","FridgeInventory");
            connection.addRequestProperty("Android","Version: 0.1");
            connection.addRequestProperty("FridgeInventory","https://github.com/MrCrobben/FridgeInventory");
            connection.connect();

            return connection;

        }catch (MalformedURLException e){
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}