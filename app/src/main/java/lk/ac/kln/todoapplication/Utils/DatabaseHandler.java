package lk.ac.kln.todoapplication.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import lk.ac.kln.todoapplication.Model.ToDoModel;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int VERSION = 3; // bumped for auth + user_id
    private static final String NAME = "ToDoListDatabase";

    // tables
    private static final String TODO_TABLE = "todo";
    private static final String USERS_TABLE = "users";

    private static final String ID = "id";
    private static final String TASK = "task";
    private static final String STATUS = "status";
    private static final String DESCRIPTION = "description";
    private static final String DUEDATE = "dueDate";
    private static final String TAGS = "tags";
    private static final String USER_ID = "user_id"; // foreign key (owner)

    // users columns
    private static final String USERCOL_ID = "id";
    private static final String USERCOL_USERNAME = "username";
    private static final String USERCOL_PASSWORDHASH = "passwordHash";
    private static final String USERCOL_SALT = "salt";

    private static final String CREATE_TODO_TABLE =
            "CREATE TABLE " + TODO_TABLE + " (" +
                    ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    TASK + " TEXT, " +
                    STATUS + " INTEGER, " +
                    DESCRIPTION + " TEXT, " +
                    DUEDATE + " TEXT, " +
                    TAGS + " TEXT, " +
                    USER_ID + " INTEGER" +
                    ");";

    private static final String CREATE_USERS_TABLE =
            "CREATE TABLE " + USERS_TABLE + " (" +
                    USERCOL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    USERCOL_USERNAME + " TEXT UNIQUE, " +
                    USERCOL_PASSWORDHASH + " TEXT, " +
                    USERCOL_SALT + " TEXT" +
                    ");";

    private SQLiteDatabase db;

    public DatabaseHandler(Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TODO_TABLE);
        db.execSQL(CREATE_USERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (oldVersion < 3) {
            try {
                db.execSQL(CREATE_USERS_TABLE);
            } catch (Exception ignored) {}
            try {
                db.execSQL("ALTER TABLE " + TODO_TABLE + " ADD COLUMN " + USER_ID + " INTEGER");
            } catch (Exception ignored) {}
        }

    }

    public void openDatabase() {
        if (db == null || !db.isOpen()) {
            db = this.getWritableDatabase();
        }
    }

    public void closeDatabase() {
        if (db != null && db.isOpen()) {
            db.close();
            db = null;
        }
    }

    public long insertUser(String username, String passwordHash, String salt) {
        if (db == null || !db.isOpen()) openDatabase();
        ContentValues cv = new ContentValues();
        cv.put(USERCOL_USERNAME, username);
        cv.put(USERCOL_PASSWORDHASH, passwordHash);
        cv.put(USERCOL_SALT, salt);
        return db.insert(USERS_TABLE, null, cv);
    }

    public boolean isUsernameTaken(String username) {
        if (db == null || !db.isOpen()) openDatabase();
        Cursor cur = null;
        try {
            cur = db.query(USERS_TABLE, new String[]{USERCOL_ID},
                    USERCOL_USERNAME + " = ?", new String[]{username},
                    null, null, null);
            return cur != null && cur.moveToFirst();
        } finally {
            if (cur != null) cur.close();
        }
    }

    public int authenticateUser(String username, String attemptedHash, String attemptedSalt) {
        return -1;
    }

    // get user record
    public UserRecord getUserByUsername(String username) {
        if (db == null || !db.isOpen()) openDatabase();
        Cursor cur = null;
        try {
            cur = db.query(USERS_TABLE, null, USERCOL_USERNAME + " = ?",
                    new String[]{username}, null, null, null);
            if (cur != null && cur.moveToFirst()) {
                int id = cur.getInt(cur.getColumnIndexOrThrow(USERCOL_ID));
                String passwordHash = cur.getString(cur.getColumnIndexOrThrow(USERCOL_PASSWORDHASH));
                String salt = cur.getString(cur.getColumnIndexOrThrow(USERCOL_SALT));
                return new UserRecord(id, username, passwordHash, salt);
            }
            return null;
        } finally {
            if (cur != null) cur.close();
        }
    }

    public static class UserRecord {
        public final int id;
        public final String username;
        public final String passwordHash;
        public final String salt;
        public UserRecord(int id, String username, String passwordHash, String salt) {
            this.id = id;
            this.username = username;
            this.passwordHash = passwordHash;
            this.salt = salt;
        }
    }

    public long insertTask(ToDoModel task) {
        if (db == null || !db.isOpen()) openDatabase();
        ContentValues cv = new ContentValues();
        cv.put(TASK, task.getTask());
        cv.put(STATUS, task.getStatus());
        cv.put(DESCRIPTION, task.getDescription());
        cv.put(DUEDATE, task.getDueDate());
        cv.put(TAGS, task.getTags());
        cv.put(USER_ID, task.getUserId());
        return db.insert(TODO_TABLE, null, cv);
    }

    public List<ToDoModel> getTasksByUser(int userId) {
        List<ToDoModel> taskList = new ArrayList<>();
        if (db == null || !db.isOpen()) openDatabase();
        Cursor cur = null;
        try {
            cur = db.query(TODO_TABLE, null, USER_ID + " = ?", new String[]{String.valueOf(userId)},
                    null, null, null);
            if (cur != null && cur.moveToFirst()) {
                do {
                    ToDoModel t = new ToDoModel();
                    t.setId(cur.getInt(cur.getColumnIndexOrThrow(ID)));
                    t.setTask(cur.getString(cur.getColumnIndexOrThrow(TASK)));
                    t.setStatus(cur.getInt(cur.getColumnIndexOrThrow(STATUS)));
                    int descIndex = cur.getColumnIndex(DESCRIPTION);
                    int dueIndex = cur.getColumnIndex(DUEDATE);
                    int tagsIndex = cur.getColumnIndex(TAGS);
                    if (descIndex != -1) t.setDescription(cur.getString(descIndex));
                    if (dueIndex != -1) t.setDueDate(cur.getString(dueIndex));
                    if (tagsIndex != -1) t.setTags(cur.getString(tagsIndex));
                    t.setUserId(userId);
                    taskList.add(t);
                } while (cur.moveToNext());
            }
        } finally {
            if (cur != null) cur.close();
        }
        return taskList;
    }

    public int updateStatus(int id, int status) {
        if (db == null || !db.isOpen()) openDatabase();
        ContentValues cv = new ContentValues();
        cv.put(STATUS, status);
        return db.update(TODO_TABLE, cv, ID + " = ?", new String[]{String.valueOf(id)});
    }

    public int updateTask(int id, String taskText, String description, String dueDate, String tags) {
        if (db == null || !db.isOpen()) openDatabase();
        ContentValues cv = new ContentValues();
        cv.put(TASK, taskText);
        cv.put(DESCRIPTION, description);
        cv.put(DUEDATE, dueDate);
        cv.put(TAGS, tags);
        return db.update(TODO_TABLE, cv, ID + " = ?", new String[]{String.valueOf(id)});
    }

    public int updateTask(int id, String taskText) {
        if (db == null || !db.isOpen()) openDatabase();
        ContentValues cv = new ContentValues();
        cv.put(TASK, taskText);
        return db.update(TODO_TABLE, cv, ID + " = ?", new String[]{String.valueOf(id)});
    }

    public int deleteTask(int id) {
        if (db == null || !db.isOpen()) openDatabase();
        return db.delete(TODO_TABLE, ID + " = ?", new String[]{String.valueOf(id)});
    }
}
