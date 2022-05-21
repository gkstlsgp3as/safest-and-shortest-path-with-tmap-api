package com.catsruletheworld.tmapyourguard;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DBhelper extends SQLiteOpenHelper {
    private static String DBpath = "";
    private static String DBname = "safeDB.db";
    private static String alarmTable = "alarmbell";
    private static String cctvTable = "cctv";
    private static String fireTable = "firestation";
    private static String policeTable = "policeoffice";

    private static final String TAG = "DBhelper";
    private final Context mContext;
    private SQLiteDatabase DB;

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.disableWriteAheadLogging();
    }

    public DBhelper(Context context){
        super(context, DBname, null, 1);

        if (Build.VERSION.SDK_INT >= 17){
            DBpath = context.getApplicationInfo().dataDir + "/databases/";
        }
        else {
            DBpath = "/data/data/" + context.getPackageName() + "/databases/";
        }
        this.mContext = context;
    }

    public boolean DBopen() throws SQLException {
        if(!DBexist()) {
            DBcreate();
        }

        String path = DBpath + DBname;
        DB = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.CREATE_IF_NECESSARY);
        Log.e(TAG, "DB opened");

        return DB != null;
    }

    public boolean DBexist(){
        File file = new File(DBpath + DBname);
        return file.exists();
    }

    public void DBcreate() throws SQLException{
        this.getReadableDatabase();
        this.close();

        try{
            DBcopy();
            Log.e(TAG, DBname + "created");
        }
        catch(IOException ioException){
            // Error Message
            Log.e(TAG, DBname + "not created");
            throw new Error(TAG);
        }
    }

    public void DBcopy() throws IOException{

        InputStream inputStream  = mContext.getAssets().open(DBname);
        String fileName = DBpath + DBname;
        OutputStream outputStream = new FileOutputStream(fileName);

        byte[] buffer = new byte[1024];
        int length;
        while((length = inputStream.read(buffer)) > 0){
            outputStream.write(buffer, 0, length);
        }

        outputStream.flush();
        outputStream.close();
        inputStream.close();
    }

    public List getTableData() {
        List fireList = new ArrayList();
        String sql = "SELECT * FROM " + fireTable;
        Cursor mCursor = DB.rawQuery(sql, null);

        if (mCursor != null){
            while(mCursor.moveToNext()){
                DBfire fire = new DBfire();

                fire.setLat(mCursor.getDouble(0));
                fire.setLon(mCursor.getDouble(1));

                fireList.add(fire);
            }
        }
        return fireList;
    }

    public void DBclose(){
        if (DB != null){
            DB.close();
        }
    }

    @Override
    public synchronized void close(){
        DBclose();
        super.close();
    }
}