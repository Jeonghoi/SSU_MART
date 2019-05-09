package com.jeonghoi.ssumart;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import static com.jeonghoi.ssumart.MainActivity.comm;

class MyDataBase {
    ContactDBHelper dbHelper = null;
    Context context;

    public static double celcius;
    public static double humidity;
    public static int gas;
    public static int fire;
    public static int led;

    public MyDataBase(Context context){
        this.context = context;
    }

    public void init_tables() {
        dbHelper = new ContactDBHelper(context);
    }

    public void load_values() {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(ContactDBCtrct.SQL_SELECT, null);

        if (cursor.moveToFirst()) {

            celcius = cursor.getInt(0);
            humidity = cursor.getInt(1);
            gas = cursor.getInt(2);
            fire = cursor.getInt(3);
            led = cursor.getInt(4);

            // no (INTEGER) 값 가져오기.
            //int no = cursor.getInt(0);
            //EditText editTextNo = (EditText) findViewById(R.id.editTextNo);
            //editTextNo.setText(Integer.toString(no));

            // name (TEXT) 값 가져오기
            //String name = cursor.getString(1);
            //EditText editTextName = (EditText) findViewById(R.id.editTextName);
            //editTextName.setText(name);

            // phone (TEXT) 값 가져오기
            //String phone = cursor.getString(2);
            //EditText editTextPhone = (EditText) findViewById(R.id.editTextPhone);
            //editTextPhone.setText(phone);

            // over20 (INTEGER) 값 가져오기.
            //int over20 = cursor.getInt(3);
            /*
            CheckBox checkBoxOver20 = (CheckBox) findViewById(R.id.checkBoxOver20);
            if (over20 == 0) {
                checkBoxOver20.setChecked(false);
            } else {
                checkBoxOver20.setChecked(true);
            }
            */
        }
    }
    public String getDB(){
        return " (" +
                "celcius" + celcius + ", " +
                "humidity" + humidity + "', " +
                "gas" + gas + "', " +
                "fire" + fire + "', " +
                "led" + led + "', " +
                ")" ;
    }
    public void setCelcius(double celcius){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL(ContactDBCtrct.SQL_DELETE);

        Cursor cursor = db.rawQuery(ContactDBCtrct.SQL_SELECT, null);

            String sqlInsert = ContactDBCtrct.SQL_INSERT +
                    "(" +
                    Double.toString(celcius)+ ", " +
                    Double.toString(humidity) + ", " +
                    Integer.toString(gas) + ", " +
                    Integer.toString(fire) + ", " +
                    Integer.toString(led) +
                    ")";

            db.execSQL(sqlInsert);
            load_values();
    }
    public void setLed(int led){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL(ContactDBCtrct.SQL_DELETE);

        Cursor cursor = db.rawQuery(ContactDBCtrct.SQL_SELECT, null);

        String sqlInsert = ContactDBCtrct.SQL_INSERT +
                "(" +
                Double.toString(celcius)+ ", " +
                Double.toString(humidity) + ", " +
                Integer.toString(gas) + ", " +
                Integer.toString(fire) + ", " +
                Integer.toString(led) +
                ")";

        db.execSQL(sqlInsert);
        load_values();
        if(led==1)
            comm.send("LED_ON_DONE");
        else
            comm.send("LED_OFF_DONE");
        //켜라메소드 호출
    }
    public void save_values() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.execSQL(ContactDBCtrct.SQL_DELETE);

        /*EditText editTextNo = (EditText) findViewById(R.id.editTextNo) ;
        int no = Integer.parseInt(editTextNo.getText().toString()) ;

        EditText editTextName = (EditText) findViewById(R.id.editTextName) ;
        String name = editTextName.getText().toString() ;

        EditText editTextPhone = (EditText) findViewById(R.id.editTextPhone) ;
        String phone = editTextPhone.getText().toString() ;

        CheckBox checkBoxOver20 = (CheckBox) findViewById(R.id.checkBoxOver20) ;
        boolean isOver20 = checkBoxOver20.isChecked() ;

        String sqlInsert = ContactDBCtrct.SQL_INSERT +
                " (" +
                Integer.toString(no) + ", " +
                "'" + name + "', " +
                "'" + phone + "', " +
                ((isOver20 == true) ? "1" : "0") +
                ")" ;

        db.execSQL(sqlInsert) ;
        */
        String sqlInsert = ContactDBCtrct.SQL_INSERT +
                " (" +
                celcius + ", " +
                "'" + humidity + "', " +
                "'" + gas + "', " +
                "'" + fire + "', " +
                "'" + led + "', " +
                ")";

        db.execSQL(sqlInsert);
    }

    public void delete_values() {
        SQLiteDatabase db = dbHelper.getWritableDatabase() ;

        db.execSQL(ContactDBCtrct.SQL_DELETE) ;

        /*
        EditText editTextNo = (EditText) findViewById(R.id.editTextNo) ;
        editTextNo.setText("") ;

        EditText editTextName = (EditText) findViewById(R.id.editTextName) ;
        editTextName.setText("") ;

        EditText editTextPhone = (EditText) findViewById(R.id.editTextPhone) ;
        editTextPhone.setText("") ;

        CheckBox checkBoxOver20 = (CheckBox) findViewById(R.id.checkBoxOver20) ;
        checkBoxOver20.setChecked(false) ;
        */
    }

}
