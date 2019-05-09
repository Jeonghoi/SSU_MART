package com.jeonghoi.ssumart;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by remna on 2017-09-04.
 */



public class Weather {

    public static String data = "";
    private static String stationName = "";
    private static String code = "";
    private static String weatherName = "";
    private static int temperature = 100;


    public static String getStationName(){return Weather.stationName;}
    public static String getCode() {return Weather.code;}
    public static String getWeatherName() {return Weather.weatherName;}
    public static int getTemperature(){return Weather.temperature;}
    public static void printWeather() { System.out.println("Station : " + stationName); System.out.println("WeatherName : " + weatherName); System.out.println("Code : " + code); System.out.println("Temperature : " + temperature);}

    public static void getWeatherData() {

        JSONObject weatherJson;

        if (data != null) {
            try {
                weatherJson = new JSONObject(data);
                weatherJson = new JSONObject(weatherJson.getString("weather"));
                JSONArray weatherJsonArray = new JSONArray(weatherJson.getString("minutely"));
                weatherJson = new JSONObject(String.valueOf(weatherJsonArray.getJSONObject(0)));
                weatherJson = new JSONObject(weatherJson.getString("station"));
                stationName = weatherJson.getString("name");
                weatherJson = new JSONObject(String.valueOf(weatherJsonArray.getJSONObject(0)));
                weatherJson = new JSONObject(weatherJson.getString("sky"));
                weatherName = weatherJson.getString("name");
                code = weatherJson.getString("code");
                weatherJson = new JSONObject(weatherJsonArray.getString(0));
                weatherJson = new JSONObject(weatherJson.getString("temperature"));
                temperature = weatherJson.getInt("tc");
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}
