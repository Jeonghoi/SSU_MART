package com.jeonghoi.ssumart;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by remna on 2017-09-06.
 */

public class NEWS {

    public static String data = "";
    public static List<String> newsList;


    static void setNews()
    {
        if(newsList != null)
           newsList.clear();

        newsList = new ArrayList<>();
        JSONObject newsJson;
        if(data != null) {
            try {
                newsJson = new JSONObject(data);
                JSONArray newsJsonArr = new JSONArray(newsJson.getString("items"));
                for(int i=0; i<newsJsonArr.length(); i++)
                {
                    newsJson = new JSONObject(newsJsonArr.getString(i));
                   newsList.add(newsJson.getString("title"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
