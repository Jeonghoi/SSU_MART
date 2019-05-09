package com.jeonghoi.ssumart;

public class ContactDBCtrct {
    private ContactDBCtrct() {};
    public static final String TBL_CONTACT = "CONTACT_T";
    public static final String COL_CELCIUS = "CELCIUS";
    public static final String COL_HUMIDITY = "HUMIDITY";
    public static final String COL_GAS = "GAS";
    public static final String COL_FIRE = "FIRE";
    public static final String COL_LED = "LED";

    // CREATE TABLE IF NOT EXISTS CONTACT_T (NO INTEGER NOT NULL, NAME TEXT, PHONE TEXT, OVER20 INTEGER)
    public static final String SQL_CREATE_TBL = "CREATE TABLE IF NOT EXISTS " + TBL_CONTACT + " " +
            "(" +
                COL_CELCIUS + " DOUBLE" + ", " +
                COL_HUMIDITY + " DOUBLE" + ", " +
                COL_GAS + " INTEGER" + ", " +
                COL_FIRE + " INTEGER" + ", " +
                COL_LED + " INTEGER" +
            ")";

    // DROP TABLE IF EXISTS CONTACT_T
    public static final String SQL_DROP_TBL = "DROP TABLE IF EXISTS " + TBL_CONTACT;

    // SELECT * FROM CONTACT_T
    public static final String SQL_SELECT = "SELECT * FROM " + TBL_CONTACT;

    // INSERT OR REPLACE INTO CONTACT_T (NO, NAME, PHONE, OVER20) VALUES (x, x, x, x)
    // INSERT OR REPLACE INTO CONTACT_T (CELCIUS, HUMIDITY, GAS, FIRE, LED) VALUES  (1.0, 0.0, 0, 0, 0)
    public static final String SQL_INSERT = "INSERT OR REPLACE INTO " + TBL_CONTACT + " " +
            "(" + COL_CELCIUS + ", " + COL_HUMIDITY + ", " + COL_GAS + ", " + COL_FIRE + ", " + COL_LED + ") VALUES ";

    // DELETE FROM CONTACT_Tpublic
    static final String SQL_DELETE = "DELETE FROM " + TBL_CONTACT;
}
