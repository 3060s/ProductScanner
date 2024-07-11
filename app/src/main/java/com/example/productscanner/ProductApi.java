package com.example.productscanner;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.io.IOException;
import java.net.URL;

public class ProductApi {

    private final String productUrl = "https://world.openfoodfacts.org/api/v3/product/";
    private final String productId;
    private final String url;

    public ProductApi(String productId) {
        this.productId = productId;
        this.url = productUrl + productId + ".json";
    }

    public JSONObject getProductInfo() throws IOException, JSONException {
        URL url = new URL(this.url);
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.connect();

        BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
        StringBuilder stringBuilder = new StringBuilder();

        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }

        String response = stringBuilder.toString();
        JSONObject jsonObject = new JSONObject(response);

        return jsonObject.getJSONObject("product");
    }
}
