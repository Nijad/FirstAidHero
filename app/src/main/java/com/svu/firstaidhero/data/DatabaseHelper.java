package com.svu.firstaidhero.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * DatabaseHelper
 * --------------
 * ينشئ قاعدة بيانات FirstAid Hero من سكربت SQL موجود في
 * assets/databases/firstaid_setup.sql عند أول تشغيل للتطبيق.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    public static final String DB_NAME = "firstaid_hero.db";
    public static final int DB_VERSION = 2;

    private static final String SCRIPT_PATH = "databases/firstaid_setup.sql";

    private final Context context;

    private static DatabaseHelper instance;

    /** مفرد (Singleton) لتفادي فتح عدة اتصالات بنفس القاعدة */
    public static synchronized DatabaseHelper getInstance(Context ctx) {
        if (instance == null) {
            instance = new DatabaseHelper(ctx.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        executeScriptFromAssets(db, SCRIPT_PATH);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading DB from " + oldVersion + " to " + newVersion);

        // نحافظ على بيانات المستخدم قبل الحذف
        db.execSQL("CREATE TABLE IF NOT EXISTS _tmp_contacts AS " +
                "SELECT * FROM EmergencyContacts WHERE type='custom'");
        db.execSQL("CREATE TABLE IF NOT EXISTS _tmp_treated AS " +
                "SELECT * FROM TreatedCases");

        db.execSQL("DROP TABLE IF EXISTS ActionSteps");
        db.execSQL("DROP TABLE IF EXISTS FirstAidCases");
        db.execSQL("DROP TABLE IF EXISTS EmergencyContacts");
        db.execSQL("DROP TABLE IF EXISTS TreatedCases");

        onCreate(db);

        // استرجاع بيانات المستخدم
        db.execSQL("INSERT INTO EmergencyContacts (name, phone, type, category) " +
                "SELECT name, phone, type, category FROM _tmp_contacts");
        db.execSQL("INSERT INTO TreatedCases (case_id, description, date, photo_path, procedures) " +
                "SELECT case_id, description, date, photo_path, procedures FROM _tmp_treated");

        db.execSQL("DROP TABLE IF EXISTS _tmp_contacts");
        db.execSQL("DROP TABLE IF EXISTS _tmp_treated");
    }

    private void executeScriptFromAssets(SQLiteDatabase db, String assetPath) {
        BufferedReader reader = null;
        try {
            InputStream is = context.getAssets().open(assetPath);
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("--")) continue;
                builder.append(line).append("\n");
            }

            String[] statements = builder.toString().split(";");
            db.beginTransaction();
            int executed = 0;
            int failed = 0;
            try {
                for (String stmt : statements) {
                    String sql = stmt.trim();
                    if (sql.isEmpty()) continue;
                    try {
                        db.execSQL(sql);
                        executed++;
                    } catch (Exception e) {
                        failed++;
                        // لا نوقف العملية بسبب جملة واحدة
                        Log.w(TAG, "Statement failed: " + sql.substring(0,
                                Math.min(80, sql.length())) + " | " + e.getMessage());
                    }
                }
                db.setTransactionSuccessful();
                Log.i(TAG, "DB created: " + executed + " statements OK, " + failed + " failed.");
            } finally {
                db.endTransaction();
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to read SQL script from assets", e);
            // لا نرمي استثناء — ننشئ الجداول يدويًا كخطة بديلة
            createTablesManually(db);
        } finally {
            if (reader != null) {
                try { reader.close(); } catch (IOException ignored) { }
            }
        }
    }

    /** خطة بديلة عند فشل قراءة assets. */
    private void createTablesManually(SQLiteDatabase db) {
        Log.w(TAG, "Creating tables manually (fallback)");
        db.execSQL("CREATE TABLE IF NOT EXISTS FirstAidCases (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "title TEXT NOT NULL," +
                "symptoms TEXT NOT NULL," +
                "severity TEXT NOT NULL," +
                "is_common INTEGER NOT NULL DEFAULT 0," +
                "keywords TEXT DEFAULT '')");
        db.execSQL("CREATE TABLE IF NOT EXISTS ActionSteps (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "case_id INTEGER NOT NULL," +
                "step_number INTEGER NOT NULL," +
                "instruction TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS EmergencyContacts (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "phone TEXT NOT NULL," +
                "type TEXT NOT NULL DEFAULT 'custom'," +
                "category TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS TreatedCases (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "case_id INTEGER," +
                "description TEXT NOT NULL," +
                "date TEXT NOT NULL," +
                "photo_path TEXT," +
                "procedures TEXT NOT NULL)");
    }
}