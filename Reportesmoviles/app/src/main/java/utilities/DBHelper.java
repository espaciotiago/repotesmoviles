package utilities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Tiago on 15/08/16.
 */
public class DBHelper extends SQLiteOpenHelper {
    //----------------------------------------------------------------------------------------------------------------
    //DB info
    //----------------------------------------------------------------------------------------------------------------
    private static final String LOG="DatabaseHelper";
    private static final int DATABASE_VERSION=1;
    private static final String DATABASE_NAME="reporteCiudadano";
    //----------------------------------------------------------------------------------------------------------------
    //Table's names
    //----------------------------------------------------------------------------------------------------------------
    public static final String TABLE_USER="User";

    //----------------------------------------------------------------------------------------------------------------
    //Creation querys
    //----------------------------------------------------------------------------------------------------------------
    public static final String CREATE_TABLE_USER="CREATE TABLE "
            + TABLE_USER + "(name TEXT, mail TEXT, password TEXT, phone TEXT, id TEXT, town TEXT, gender TEXT, image TEXT)";
    //----------------------------------------------------------------------------------------------------------------
    //METHODS
    //----------------------------------------------------------------------------------------------------------------

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.e(LOG, " Created");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_USER);
    }

    //Get the existen user
    public User userExists(){
        User user=null;
        SQLiteDatabase db=this.getReadableDatabase();
        String selectQuery="SELECT * FROM "+ TABLE_USER;
        Cursor c = db.rawQuery(selectQuery, null);
        try {
            if (c != null) {
                c.moveToFirst();
                String name = c.getString(c.getColumnIndex("name"));
                String mail = c.getString(c.getColumnIndex("mail"));
                String password = c.getString(c.getColumnIndex("password"));
                String phone = c.getString(c.getColumnIndex("phone"));
                String id = c.getString(c.getColumnIndex("id"));
                String town = c.getString(c.getColumnIndex("town"));
                String gender = c.getString(c.getColumnIndex("gender"));
                String image = c.getString(c.getColumnIndex("image"));

                user = new User(name,mail,password,phone,id,gender,image);

            }
            return user;
        }catch (Exception e){
            return null;
        }
    }

    //Create a new user
    public String createUser(User user){
        User existe=userExists();
        if(existe==null) {
            SQLiteDatabase db = this.getReadableDatabase();
            ContentValues values = new ContentValues();
            values.put("name", user.getName());
            values.put("mail", user.getMail());
            values.put("password", user.getPassword());
            values.put("phone", user.getPhone());
            values.put("id", user.getId());
            values.put("town", "");
            values.put("gender", user.getGender());
            values.put("image", user.getImage());

            long i = db.insert(TABLE_USER, null, values);
            return "" + i;
        }else{
            return "Ya en bd";
        }
    }

    //Update a user
    public void updateUser(String name, String idNum,String phone,String picture,String mail,String password){

        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues values = new ContentValues();
        if(name!=null && !name.equals("null") && !name.equals("")) {
            values.put("name", name);
        }
        if(mail!=null && !mail.equals("null") && !mail.equals("")) {
            values.put("mail", mail);
        }
        if(phone!=null && !phone.equals("null") && !phone.equals("")){
            values.put("phone", phone);
        }
        if(picture!=null && !picture.equals("null") && !picture.equals("")) {
            values.put("image", picture);
        }
        if(password!=null && !password.equals("null") && !password.equals("")){
            Log.e("Pas upd","in" + " " + password);
            values.put("password", password);
        }
        db.update(TABLE_USER, values, "id = ?", new String[]{idNum});
    }

    //Delete user
    public void deleteUser(){
        SQLiteDatabase db=this.getReadableDatabase();
        db.delete(TABLE_USER,null,null);
    }
}
