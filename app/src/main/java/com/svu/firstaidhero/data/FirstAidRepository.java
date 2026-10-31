package com.svu.firstaidhero.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.svu.firstaidhero.data.dao.CaseDao;
import com.svu.firstaidhero.data.dao.ContactDao;
import com.svu.firstaidhero.data.dao.StatsDao;
import com.svu.firstaidhero.data.dao.TreatedCaseDao;

/**
 * FirstAidRepository
 * ------------------
 * Centralized access point for the database.
 * Implements the Repository pattern to abstract data sources from the UI layer.
 * Every Activity calls getInstance() once to obtain access to all DAOs.
 */
public class FirstAidRepository {

    // Singleton instance to ensure only one repository exists across the app
    private static FirstAidRepository instance;

    private final DatabaseHelper dbHelper;
    private final SQLiteDatabase db;

    private final CaseDao caseDao;
    private final ContactDao contactDao;
    private final TreatedCaseDao treatedCaseDao;
    private final StatsDao statsDao;

    /**
     * Private constructor initializes the database connection and all DAO instances.
     *
     * @param context The application context.
     */
    private FirstAidRepository(Context context) {
        // Initialize the DatabaseHelper and open a writable connection to the DB
        this.dbHelper = DatabaseHelper.getInstance(context);
        this.db = dbHelper.getWritableDatabase();

        // Instantiate DAOs, passing the active database connection
        this.caseDao = new CaseDao(db);
        this.contactDao = new ContactDao(db);
        this.treatedCaseDao = new TreatedCaseDao(db);
        this.statsDao = new StatsDao(db);
    }

    /**
     * Retrieves the single instance of FirstAidRepository, creating it if it doesn't exist.
     * Uses the synchronized keyword to ensure thread safety during initialization.
     *
     * @param ctx The application context.
     * @return The singleton instance of FirstAidRepository.
     */
    public static synchronized FirstAidRepository getInstance(Context ctx) {
        if (instance == null) {
            instance = new FirstAidRepository(ctx.getApplicationContext());
        }
        return instance;
    }

    /**
     * Provides access to the DAO responsible for managing first aid cases and steps.
     *
     * @return CaseDao instance.
     */
    public CaseDao cases()              { return caseDao; }

    /**
     * Provides access to the DAO responsible for managing emergency contacts.
     *
     * @return ContactDao instance.
     */
    public ContactDao contacts()        { return contactDao; }

    /**
     * Provides access to the DAO responsible for managing treated cases.
     *
     * @return TreatedCaseDao instance.
     */
    public TreatedCaseDao treated()     { return treatedCaseDao; }

    /**
     * Provides access to the DAO responsible for generating statistics and reports.
     *
     * @return StatsDao instance.
     */
    public StatsDao stats()             { return statsDao; }

    /**
     * Provides direct access to the SQLiteDatabase instance.
     * Intended for advanced use cases only; prefer using the provided DAOs.
     *
     * @return The raw SQLiteDatabase object.
     */
    public SQLiteDatabase rawDb()       { return db; }
}