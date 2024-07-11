package com.example.productscanner;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private EditText barcodeInput;
    private TextView productInfoTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        barcodeInput = findViewById(R.id.barcode_input);
        productInfoTextView = findViewById(R.id.product_info);

        Button fetchProductButton = findViewById(R.id.fetch_product_button);
        fetchProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String barcode = barcodeInput.getText().toString();
                if (!barcode.isEmpty()) {
                    new FetchProductInfoTask().execute(barcode);
                }
            }
        });

        Button scanBarcodeButton = findViewById(R.id.scan_barcode_button);
        scanBarcodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BarcodeScannerActivity.class);
                startActivityForResult(intent, 1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String barcode = data.getStringExtra("barcode");
            new FetchProductInfoTask().execute(barcode);
        }
    }

    private class FetchProductInfoTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            String barcode = params[0];
            try {
                ProductApi productApi = new ProductApi(barcode);
                return productApi.getProductInfo();
            } catch (IOException | JSONException e) {
                Log.e(TAG, "Error fetching product info", e);
                return null;
            }
        }

        private String formatJsonData(JSONObject jsonObject, String key) throws JSONException {
            JSONObject dataObject = jsonObject.getJSONObject(key);

            StringBuilder dataInfo = new StringBuilder();
            Iterator<String> keys = dataObject.keys();

            while(keys.hasNext()) {
                String dataKey = keys.next();
                String value = dataObject.getString(dataKey);

                String formattedKey = dataKey.substring(0, 1).toUpperCase() + dataKey.substring(1).replace("-", " ");

                dataInfo.append(formattedKey).append(": ").append(value).append("\n");
            }

            return dataInfo.toString();
        }

        @Override
        protected void onPostExecute(JSONObject product) {
            if (product != null) {
                try {
                    String productName = product.getString("product_name_pl");
                    String brands = product.getString("brands");
                    String ingredients = product.getString("ingredients_text_pl");
                    String nutrientLevels = formatJsonData(product, "nutrient_levels");
                    String ecoScore = product.getString("ecoscore_score");
                    String ecoGrade = product.getString("ecoscore_grade");

                    String productInfo = "Nazwa produktu: \n" + productName + "\n\n"
                            + "Marki: \n" + brands + "\n\n"
                            + "Składniki: \n" + ingredients + "\n\n"
                            + "Poziomy składników odżywczych: \n" + nutrientLevels + "\n"
                            + "Wynik eco: \n" + ecoScore + "\n\n"
                            + "Poziom eco: \n" + ecoGrade;

                    productInfoTextView.setText(productInfo);

                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing product info", e);
                }
            } else {
                productInfoTextView.setText("Failed to fetch product info");
            }
        }
    }
}
