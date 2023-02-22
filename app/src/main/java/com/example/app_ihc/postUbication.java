package com.example.app_ihc;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class postUbication {
    public String[] enviar(Double myLatitude, Double myLongitude, Context a){
        final String[] s = {"null"};
        String url="https://tuslineas.com/interaccion/register.php";
        StringRequest postRequest= new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject= new JSONObject(response);
                    String resultado= jsonObject.getString("success");
                    Log.i("resultado ", resultado);
                    //Toast.makeText(this,"id usuario "+jsonObject.getString("success"),Toast.LENGTH_LONG).show();
                    s[0] ="id usuario "+jsonObject.getString("success");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e("error",volleyError.getMessage());
            }
        }

        ) {
            protected Map<String, String> getParams() {
                Map<String, String>params =  new HashMap<>();
                params.put("latitud",Double.toString(myLatitude));
                params.put("longitud",Double.toString(myLongitude));

                return params;
            }
        } ;

        Volley.newRequestQueue(a).add(postRequest);
        return  s;
    }

}
