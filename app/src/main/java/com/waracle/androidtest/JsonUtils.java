package com.waracle.androidtest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class JsonUtils {

        public static ArrayList<Cake> parseCakeList(String json) throws JSONException {
            ArrayList<Cake> cakeList = new ArrayList<>();
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                cakeList.add(parseCake(jsonArray.getString(i)));
            }
            return cakeList;
        }

        private static Cake parseCake(String json) throws JSONException {
            JSONObject cakeObj = new JSONObject(json);
            String title = cakeObj.getString("title");
            String desc = cakeObj.getString("desc");
            String image = cakeObj.getString("image");
            return new Cake(title, desc, image);
        }
}
