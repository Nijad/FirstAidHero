package com.svu.firstaidhero.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.svu.firstaidhero.data.dao.CaseDao;
import com.svu.firstaidhero.data.dao.ContactDao;
import com.svu.firstaidhero.data.dao.StatsDao;
import com.svu.firstaidhero.data.dao.TreatedCaseDao;

/**
 * نقطة الوصول الموحدة لقاعدة البيانات.
 * كل Activity تستدعي getInstance() مرة وتحصل على كل الـ DAOs.
 */
public class FirstAidRepository {

    private static FirstAidRepository instance;

    private final DatabaseHelper dbHelper;
    private final SQLiteDatabase db;

    private final CaseDao caseDao;
    private final ContactDao contactDao;
    private final TreatedCaseDao treatedCaseDao;
    private final StatsDao statsDao;

    private FirstAidRepository(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
        this.db = dbHelper.getWritableDatabase();

        this.caseDao = new CaseDao(db);
        this.contactDao = new ContactDao(db);
        this.treatedCaseDao = new TreatedCaseDao(db);
        this.statsDao = new StatsDao(db);
    }

    public static synchronized FirstAidRepository getInstance(Context ctx) {
        if (instance == null) {
            instance = new FirstAidRepository(ctx.getApplicationContext());
        }
        return instance;
    }

    public CaseDao cases()              { return caseDao; }
    public ContactDao contacts()        { return contactDao; }
    public TreatedCaseDao treated()     { return treatedCaseDao; }
    public StatsDao stats()             { return statsDao; }

    /** للاستخدامات المتقدمة فقط. */
    public SQLiteDatabase rawDb()       { return db; }
}