package com.jeonghoi.ssumart;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Time;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.naver.speech.clientapi.SpeechRecognitionResult;

import org.java_websocket.WebSocket;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import static com.jeonghoi.ssumart.NanoHttp.getLocalIpAddress;


public class MainActivity extends AppCompatActivity {

    public static WebSocket comm;
    public NanoHttp myServer;
    public SimpleServer wsServer;
    public MyDataBase db;

    Time mTime;
    Location location;
    Handler timeHandler;
    Handler weatherHandler;
    Handler getNewsHandler;
    Handler showNewsHandler;
    Runnable run_Time;
    Runnable run_getNews;
    Runnable run_showNews;
    Runnable run_Weather;
    int WEATHER_RESET_TIME = 1000;
    int NEWS_RESET_TIME = 1000;
    int NEWS_INDEX=0;


    private boolean isstart = false;
    private boolean isHome = true;

    private String wake_word = "바보야";
    private String map_word = "지도";
    private String return_word = "홈으로";
    private String show_db = "데이터베이스";
    private String celcius_up = "온도 올려";
    private String celcius_down = "온도 내려";
    private String led_on = "불 켜";
    private String led_off = "불 꺼";

    private boolean ismap = false;



    //--------------------//

    private static final String TAG = MainActivity.class.getSimpleName();
    static final String CLIENT_ID = "VV0WpX9CMx_yMW_la4A5";
    // 1. "내 애플리케이션"에서 Client ID를 확인해서 이곳에 적어주세요.
    // 2. build.gradle (Module:app)에서 패키지명을 실제 개발자센터 애플리케이션 설정의 '안드로이드 앱 패키지 이름'으로 바꿔 주세요

    private RecognitionHandler handler;
    private static NaverRecognizer naverRecognizer;

    private static TextView txtResult;
    private static String mResult;

    private AudioWriterPCM writer;

    private int idx;
    private String mResult_2;

    public NaverTTSTask mNaverTTSTask;

    static String[] mTextString;

    //-------------------//

    // Handle speech recognition Messages.
    private void handleMessage(Message msg) {
        switch (msg.what) {
            case R.id.clientReady:
                // Now an user can speak.
                txtResult.setText("Connected");
                writer = new AudioWriterPCM(
                        Environment.getExternalStorageDirectory().getAbsolutePath() + "/NaverSpeechTest");
                writer.open("Test");
                break;

            case R.id.audioRecording:
                writer.write((short[]) msg.obj);
                break;

            case R.id.partialResult:
                // Extract obj property typed with String.
                mResult = (String) (msg.obj);
                txtResult.setText(mResult);
                break;

            case R.id.finalResult:
                // Extract obj property typed with String array.
                // The first element is recognition result for speech.
                SpeechRecognitionResult speechRecognitionResult = (SpeechRecognitionResult) msg.obj;
                List<String> results = speechRecognitionResult.getResults();
                StringBuilder strBuf = new StringBuilder();
                for(String result : results) {
                    strBuf.append(result);
                    strBuf.append("\n");
                }
                mResult = strBuf.toString();

                idx = mResult.indexOf("\n");
                mResult_2 = mResult.substring(0, idx);

                txtResult.setText(mResult_2);
                Toast.makeText(getApplicationContext(), mResult_2,Toast.LENGTH_LONG).show();
                break;

            case R.id.recognitionError:
                if (writer != null) {
                    writer.close();
                }

                mResult = "Error code : " + msg.obj.toString();
                txtResult.setText(mResult);
                break;

            case R.id.clientInactive:
                if (writer != null) {
                    writer.close();
                }

                idx = mResult.indexOf("\n");
                mResult_2 = mResult.substring(0, idx);

                results(mResult_2);

                break;
        }
    }
    //------------------//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_NoTitleBar_Fullscreen);
        setContentView(R.layout.activity_main);


        //-------//

        txtResult = (TextView) findViewById(R.id.txt_result);

        handler = new RecognitionHandler(this);
        naverRecognizer = new NaverRecognizer(this, handler, CLIENT_ID);

        //-------//

        mTime = new Time();

        run_Weather = new Runnable() {
            @Override
            public void run() {
                sendRequest4Weather();
                Weather.getWeatherData();
                showWeather();
                weatherHandler.postDelayed(run_Weather, WEATHER_RESET_TIME);
            }
        };

        run_getNews = new Runnable() {
            @Override
            public void run() {
                sendRequest4News();
                NEWS.setNews();
                getNewsHandler.postDelayed(run_getNews, NEWS_RESET_TIME);
            }
        };

        run_showNews = new Runnable() {
            @Override
            public void run() {
                showNews();
                getNewsHandler.postDelayed(run_showNews, 8000);
            }
        };

        run_Time = new Runnable() {
            @Override
            public void run() {
                mTime.setToNow();
                showTime(mTime);
                timeHandler.postDelayed(run_Time, 1000);
            }
        };

        timeHandler = new Handler();
        timeHandler.postDelayed(run_Time, 1000);
        weatherHandler = new Handler();
        weatherHandler.postDelayed(run_Weather, WEATHER_RESET_TIME);
        getNewsHandler = new Handler();
        showNewsHandler = new Handler();
        getNewsHandler.postDelayed(run_getNews, NEWS_RESET_TIME);
        showNewsHandler.postDelayed(run_showNews, 8000);

        myServer = new NanoHttp(getApplicationContext());
        new Thread() {
            public void run() {
                try {
                    wsServer = new SimpleServer(new InetSocketAddress(NanoHttp.ipAddress, 8090));
                    wsServer.run();
                }catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
        Toast.makeText(getApplicationContext(),getLocalIpAddress(),Toast.LENGTH_LONG).show();
        db = new MyDataBase(getApplicationContext());
        db.init_tables();
        db.load_values();
        SimpleServer.db=db;
        //db.setCelcius(35);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(isstart == false) {
            mResult = "";
            txtResult.setText("Connecting...");
            naverRecognizer.recognize();

            isstart = true;
        }

        ///////
        // NOTE : initialize() must be called on start time.
        naverRecognizer.getSpeechRecognizer().initialize();
        ////////
    }

    @Override
    protected void onStop() {
       super.onStop();
        // NOTE : release() must be called on stop time.
        if(ismap == false)
            naverRecognizer.getSpeechRecognizer().release();
        ///
    }

    @Override
    protected void onResume() {
        super.onResume();
      }

    private void results(String string){

        String mText;

        //사용자가 입력한 텍스트를 이 배열변수에 담는다.
        if(mResult_2.matches(".*"+wake_word+".*")) {
            if (mResult_2.length() > 0) { //한글자 이상 1
                mText = "안녕하세요";
                mTextString = new String[]{mText};

                //AsyncTask 실행
                mNaverTTSTask = new NaverTTSTask();
                mNaverTTSTask.execute(mTextString);
            } else {
                Toast.makeText(MainActivity.this, "안녕이라고 말 하세요", Toast.LENGTH_SHORT).show();
                return;
            }
        }else if(mResult_2.matches(".*"+map_word+".*") && ismap == false) {
            mText = "지도를 켜겠습니다.";
            mTextString = new String[]{mText};

            //AsyncTask 실행
            mNaverTTSTask = new NaverTTSTask();
            mNaverTTSTask.execute(mTextString);

            ismap = true;
            isHome = false;
            startActivity(new Intent(MainActivity.this, Map.class));
            //startActivity(new Intent(MainActivity.this, MapActivity.class));

        }else if(mResult_2.matches(".*"+return_word+".*") && ismap == true) {

            if(!isHome) {

                mText = "메인화면으로 갑니다";
                mTextString = new String[]{mText};

                Log.d("............", "22222222");

                //AsyncTask 실행
                mNaverTTSTask = new NaverTTSTask();
                mNaverTTSTask.execute(mTextString);

                ismap = false;
                isHome = true;
                startActivity(new Intent(this, MainActivity.class));
            }
        }else if(mResult_2.matches(".*"+show_db+".*")) {
            mText = "보여드리겠습니다.";
            mTextString = new String[]{mText};

            //AsyncTask 실행
            mNaverTTSTask = new NaverTTSTask();
            mNaverTTSTask.execute(mTextString);

            Toast.makeText(getApplicationContext(),db.getDB(),Toast.LENGTH_LONG).show();
            naverRecognizer.recognize();
        }
        else if(mResult_2.matches(".*"+celcius_up+".*")) {
            db.setCelcius(db.celcius + 1.0);
            Toast.makeText(getApplicationContext(), db.getDB(), Toast.LENGTH_LONG).show();
            naverRecognizer.recognize();
        }
        else if(mResult_2.matches(".*"+celcius_down+".*")) {
            db.setCelcius(db.celcius-1.0);
            Toast.makeText(getApplicationContext(),db.getDB(),Toast.LENGTH_LONG).show();
            naverRecognizer.recognize();

        }else if(mResult_2.matches(".*"+led_on+".*")) {
            db.setLed(1);

            mText = "불을 켰습니다.";
            mTextString = new String[]{mText};

            //AsyncTask 실행
            mNaverTTSTask = new NaverTTSTask();
            mNaverTTSTask.execute(mTextString);

            //Toast.makeText(getApplicationContext(),db.getDB(),Toast.LENGTH_LONG).show();
            naverRecognizer.recognize();

        }else if(mResult_2.matches(".*"+led_off+".*")) {
            db.setLed(0);

            mText = "불을 껐습니다.";
            mTextString = new String[]{mText};

            //AsyncTask 실행
            mNaverTTSTask = new NaverTTSTask();
            mNaverTTSTask.execute(mTextString);

            //Toast.makeText(getApplicationContext(),db.getDB(),Toast.LENGTH_LONG).show();
            naverRecognizer.recognize();

        }else if(mResult_2.matches(".*"+"오늘"+".*") && mResult_2.matches(".*"+"날씨"+".*") && mResult_2.matches(".*"+"알려줘"+".*")) {
            mText = "메인화면으로 갑니다";
            //mTextString = new String[]{(String) weatherSummaryView.getText()};

            //AsyncTask 실행
            mNaverTTSTask = new NaverTTSTask();
            mNaverTTSTask.execute(mTextString);

        }else {
            mResult = "";
            txtResult.setText("Connecting...");
            naverRecognizer.recognize();
        }
    }

    //////////////
    // Declare handler for handling SpeechRecognizer thread's Messages.
    static class RecognitionHandler extends Handler {
        private final WeakReference mActivity;

        RecognitionHandler(Activity activity) {
            mActivity = new WeakReference(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = (MainActivity) mActivity.get();
            if (activity != null) {
                activity.handleMessage(msg);
            }
        }
    }
    //////////////

    public void sendRequest4News(){
        // RequestQueue를 새로 만들어준다.
        RequestQueue news_que = Volley.newRequestQueue(this);
        // Request를 요청 할 URL

        // String url = "https://api.rss2json.com/v1/api.json?rss_url=http%3A%2F%2Ffs.jtbc.joins.com%2FRSS%2Fsports.xml"; // 스포츠 뉴스
        String url = "https://api.rss2json.com/v1/api.json?rss_url=http%3A%2F%2Ffs.jtbc.joins.com%2F%2FRSS%2Fnewsflash.xml"; // 속보
        // StringRequest를 보낸다.
        StringRequest locationRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        NEWS.data = response;
                        NEWS_RESET_TIME = 300000;
                        Log.e("News", "Response is: " + response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
             /*   if(error != null)
                Log.e("error", error.getMessage());*/
            }
        });
        // RequestQueue에 현재 Task를 추가해준다.
        news_que.add(locationRequest);
    }

    public void sendRequest4Weather() {

        location = new Location();
        // RequestQueue를 새로 만들어준다.
        RequestQueue location_que = Volley.newRequestQueue(this);
        // Request를 요청 할 URL
        String url = "http://ip-api.com/json";
        // StringRequest를 보낸다.
        StringRequest locationRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            location.setLatitude(jsonObject.getDouble("lat"));
                            location.setLongitude(jsonObject.getDouble("lon"));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Log.e("location", "Response is: " + response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                /*if(error != null)
                 Log.e("error", error.getMessage());*/
            }
        });
        // RequestQueue에 현재 Task를 추가해준다.
        location_que.add(locationRequest);

        if (location.getLongitude() == 0)
            return;

        String lat = String.valueOf(location.getLatitude());
        String lon = String.valueOf(location.getLongitude());

        // RequestQueue를 새로 만들어준다.
        RequestQueue weather_que = Volley.newRequestQueue(this);
        // Request를 요청 할 URL
        url = "http://apis.skplanetx.com/weather/current/minutely?version=1&lat=" + lat + "&lon=" + lon + "&appKey=c16f5be9-24d5-3e95-a391-01ea8e4adc97";
        // StringRequest를 보낸다.
        StringRequest weatherRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Weather.data = response;
                        Log.e("weather", "Response is: " + response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
      /*          if(error != null)
                Log.e("error", error.getMessage());*/
            }
        });
        // RequestQueue에 현재 Task를 추가해준다.
        weather_que.add(weatherRequest);
    }

    void showNews()
    {
        if(NEWS_RESET_TIME == 1000)
            return;
        if(NEWS.newsList.isEmpty())
            return;
        if(NEWS_INDEX == NEWS.newsList.size())
            NEWS_INDEX = 0;

        TextView newsTitle = (TextView) findViewById(R.id.NewsTitle);
        ImageView newsImg = (ImageView) findViewById(R.id.NewsImg);
            newsTitle.setText(NEWS.newsList.get(NEWS_INDEX));
        newsTitle.setTypeface(Typeface.createFromAsset(getAssets(), "news_font.ttf"));
        newsImg.setImageResource(R.drawable.jtbc_news);
        NEWS_INDEX++;
    }

    void showWeather() {
        if (Weather.getTemperature() == 100)
            return;
        WEATHER_RESET_TIME = 60000;

        TextView station = (TextView) findViewById(R.id.Station);
        TextView weatherName = (TextView) findViewById(R.id.WeatherName);
        ImageView weatherimg = (ImageView) findViewById(R.id.Weatherimg);

        String text1 = Weather.getStationName() + "/" + Weather.getTemperature() + "°C";

        station.setText(text1);
        station.setTypeface(Typeface.createFromAsset(getAssets(), "weather_font.ttf"));
        weatherName.setText(Weather.getWeatherName());
        weatherName.setTypeface(Typeface.createFromAsset(getAssets(), "weather_font.ttf"));

        boolean isAm = true;
        if(mTime.hour > 20 || mTime.hour < 7)
            isAm = false;

        switch (Weather.getCode()) {
            case "SKY_A00":
                weatherimg.setImageResource(R.drawable.a00);
                break;
            case "SKY_A01":
                if(isAm)
                    weatherimg.setImageResource(R.drawable.a01_am);
                else
                    weatherimg.setImageResource(R.drawable.a01_pm);
                break;
            case "SKY_A02":
                if(isAm)
                    weatherimg.setImageResource(R.drawable.a02_am);
                else
                    weatherimg.setImageResource(R.drawable.a02_pm);
                break;
            case "SKY_A03":
                if(isAm)
                    weatherimg.setImageResource(R.drawable.a03_am);
                else
                    weatherimg.setImageResource(R.drawable.a03_pm);
                break;
            case "SKY_A04":
                if(isAm)
                    weatherimg.setImageResource(R.drawable.a04_am);
                else
                    weatherimg.setImageResource(R.drawable.a04_pm);
                break;
            case "SKY_A05":
                if(isAm)
                    weatherimg.setImageResource(R.drawable.a05_am);
                else
                    weatherimg.setImageResource(R.drawable.a05_pm);
                break;
            case "SKY_A06":
                if(isAm)
                    weatherimg.setImageResource(R.drawable.a06_am);
                else
                    weatherimg.setImageResource(R.drawable.a06_pm);
                break;
            case "SKY_A07":
                weatherimg.setImageResource(R.drawable.a07);
                break;
            case "SKY_A08":
                weatherimg.setImageResource(R.drawable.a08);
                break;
            case "SKY_A09":
                weatherimg.setImageResource(R.drawable.a09);
                break;
            case "SKY_A10":
                weatherimg.setImageResource(R.drawable.a10);
                break;
            case "SKY_A11":
                weatherimg.setImageResource(R.drawable.a11);
                break;
            case "SKY_A12":
                weatherimg.setImageResource(R.drawable.a12);
                break;
            case "SKY_A13":
                weatherimg.setImageResource(R.drawable.a13);
                break;
            case "SKY_A14":
                weatherimg.setImageResource(R.drawable.a14);
                break;
        }
    }


    void showTime(Time mTime) {
        int hours, minutes, seconds, weekday, date, month;
        hours = mTime.hour;
        minutes = mTime.minute;
        seconds = mTime.second;
        weekday = mTime.weekDay;
        date = mTime.monthDay;
        month = mTime.month + 1;
        String cur_ampm = "AM";
        TextView tvDate = (TextView) findViewById(R.id.Date);
        TextView tvTime = (TextView) findViewById(R.id.Time);
        TextView tvAmPm = (TextView) findViewById(R.id.AmPm);

        if (hours == 0) {
            hours = 12;
        }
        if (hours > 12) {
            hours = hours - 12;
            cur_ampm = "PM";
        }

        String text_time = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        String day_of_week = "";
        switch (weekday) {
            case 0:
                day_of_week = "일요일";
                break;
            case 1:
                day_of_week = "월요일";
                break;
            case 2:
                day_of_week = "화요일";
                break;
            case 3:
                day_of_week = "수요일";
                break;
            case 4:
                day_of_week = "목요일";
                break;
            case 5:
                day_of_week = "금요일";
                break;
            case 6:
                day_of_week = "토요일";
                break;
        }
        String text_date = String.format("%d월 %d일 %s", month, date, day_of_week);
        tvTime.setText(text_time);
        tvTime.setTypeface(Typeface.createFromAsset(getAssets(), "weather_font.ttf"));
        tvDate.setText(text_date);
        tvDate.setTypeface(Typeface.createFromAsset(getAssets(), "weather_font.ttf"));
        tvAmPm.setText(cur_ampm);
        tvAmPm.setTypeface(Typeface.createFromAsset(getAssets(), "weather_font.ttf"));
    }

    public static class NaverTTSTask extends AsyncTask<String[], Void, String> {

        @Override
        protected String doInBackground(String[]... strings) {
            //여기서 서버에 요청
            //APIExamTTS.main(mTextString);

            String clientId = "VV0WpX9CMx_yMW_la4A5";//애플리케이션 클라이언트 아이디값";
            String clientSecret = "jjbuTvcuo6";//애플리케이션 클라이언트 시크릿값";
            try {
                String text = URLEncoder.encode(mTextString[0], "UTF-8");
                String apiURL = "https://openapi.naver.com/v1/voice/tts.bin";
                URL url = new URL(apiURL);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("X-Naver-Client-Id", clientId);
                con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
                // post request
                String postParams = "speaker=mijin&speed=3&text=" + text;
                con.setDoOutput(true);
                con.setDoInput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());///여기서 에러 난다?
                Log.d(TAG, String.valueOf(wr));
                wr.writeBytes(postParams);
                wr.flush();
                wr.close();
                int responseCode = con.getResponseCode();
                BufferedReader br;
                if (responseCode == 200) { // 정상 호출
                    InputStream is = con.getInputStream();
                    int read = 0;
                    byte[] bytes = new byte[1024];
                    //폴더를 만들어 줘야 겠다. 없으면 새로 생성하도록 해야 한다. 일단 Naver폴더에 저장하도록 하자.
                    File dir = new File(Environment.getExternalStorageDirectory() + "/", "Naver");
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    // 랜덤한 이름으로 mp3 파일 생성
                    //String tempname = Long.valueOf(new Date().getTime()).toString();
                    String tempname = "naverttstemp"; //하나의 파일명으로 덮어쓰기 하자.
                    File f = new File(Environment.getExternalStorageDirectory() + File.separator + "Naver/" + tempname + ".mp3");
                    f.createNewFile();
                    OutputStream outputStream = new FileOutputStream(f);
                    while ((read = is.read(bytes)) != -1) {
                        outputStream.write(bytes, 0, read);
                    }
                    is.close();

                    //여기서 바로 재생하도록 하자. mp3파일 재생 어떻게 하지? 구글링!
                    String Path_to_file = Environment.getExternalStorageDirectory() + File.separator + "Naver/" + tempname + ".mp3";
                    MediaPlayer audioPlay = new MediaPlayer();
                    audioPlay.setDataSource(Path_to_file);
                    audioPlay.prepare();//이걸 해줘야 하는군. 없으면 에러난다.
                    audioPlay.start();
                    //재생하고 나서 파일을 지워줘야 하나? 이거참 고민이네... if문으로 분기 시켜야 하나?
                    //아니면 유니크한 파일로 만들지 말고 하나의 파일명으로 저장하게 할수도 있을듯...

                    //재생완료 확인~~
                    audioPlay.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        public void onCompletion(MediaPlayer mp) {
                            Log.i(TAG, "The Playing music is Completed.");


                            mResult = "";
                            txtResult.setText("Connecting...");
                            naverRecognizer.recognize();
                        }
                    });

                } else {  // 에러 발생
                    br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();
                    while ((inputLine = br.readLine()) != null) {
                        response.append(inputLine);
                    }
                    br.close();
                }
            } catch (Exception e) {
                System.out.println(e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //방금 받은 파일명의 mp3가 있으면 플레이 시키자. 맞나 여기서 하는거?
            //아닌가 파일을 만들고 바로 실행되게 해야 하나? AsyncTask 백그라운드 작업중에...?

        }
    }
}