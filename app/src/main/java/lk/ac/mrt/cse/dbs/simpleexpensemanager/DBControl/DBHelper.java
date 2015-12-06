package lk.ac.mrt.cse.dbs.simpleexpensemanager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Account;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Transaction;


public class DBHelper extends SQLiteOpenHelper {
    
    private static final int EXPENSE = 0;
    private static final int INCOME = 1;
    private static DBHelper db = null;

    private static final String TABLE1_CREATE = "create table "+Constants.TABLE_1+" ("+
            Constants.ACCOUNT_NO+" text primary key, "+Constants.BANK_NAME+" text not null, "+
            Constants.ACCOUNT_HOLDER_NAME+" text not null, "+Constants.BALANCE+" real);";

    private static final String TABLE2_CREATE = "create table "+Constants.TABLE_2+" ("+
            Constants.DATE+" text not null, "+Constants.ACCOUNT+" text not null, "+
            Constants.EXPENSE_TYPE+" integer not null, "+Constants.AMOUNT+" real," +
            " FOREIGN KEY ("+Constants.ACCOUNT+") REFERENCES "+Constants.TABLE_1+
            " ("+Constants.ACCOUNT_NO+"));";


    private DBHelper(Context context)
    {
        super(context, Constants.DATABASE_NAME , null, Constants.DATABASE_VERSION);
    }

    public static DBHelper getInstance(Context context){
        if(db==null){
            synchronized (DBHelper.class){
                db = new DBHelper(context);
            }
        }
        return db;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE1_CREATE);
        db.execSQL(TABLE2_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists "+Constants.TABLE_1);
        db.execSQL("drop table if exists "+Constants.TABLE_2);
        onCreate(db);
    }



    public List<String> getAccountNumberList(){
        List<String> noList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select "+Constants.ACCOUNT_NO+" from "+Constants.TABLE_1+ ";", null );
        if(res.moveToFirst())
            while(res.isAfterLast()==false){
                noList.add(res.getString(res.getColumnIndex(Constants.ACCOUNT_NO)));
                res.moveToNext();
            }
        db.close();
        return noList;
    }
    public List<Account> getAccountList(){
        List<Account> acList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from "+Constants.TABLE_1+ ";", null );
        if(res.moveToFirst())
            while(res.isAfterLast()==false){
                int acNoIndex = res.getColumnIndex(Constants.ACCOUNT_NO);
                int bankNameIndex = res.getColumnIndex(Constants.BANK_NAME);
                int acHNIndex = res.getColumnIndex(Constants.ACCOUNT_HOLDER_NAME);
                int balanceIndex = res.getColumnIndex(Constants.BALANCE);

                Account ac = new Account(res.getString(acNoIndex),res.getString(bankNameIndex),
                        res.getString(acHNIndex),res.getDouble(balanceIndex));
                acList.add(ac);
                res.moveToNext();
            }
        db.close();
        return acList;
    }
    public Account getAccount(String accountNo){

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from "+Constants.TABLE_1+
                " where "+Constants.ACCOUNT_NO+" = "+accountNo+";", null );
        if(res.getCount()==0)
            return null;
        int acNoIndex = res.getColumnIndex(Constants.ACCOUNT_NO);
        int bankNameIndex = res.getColumnIndex(Constants.BANK_NAME);
        int acHNIndex = res.getColumnIndex(Constants.ACCOUNT_HOLDER_NAME);
        int balanceIndex = res.getColumnIndex(Constants.BALANCE);

        Account ac = new Account(res.getString(acNoIndex),res.getString(bankNameIndex),
                res.getString(acHNIndex),res.getDouble(balanceIndex));
        db.close();
        return ac;
    }
    public boolean addAccount(Account account) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Constants.ACCOUNT_NO,account.getAccountNo());
        contentValues.put(Constants.BANK_NAME, account.getBankName());
        contentValues.put(Constants.ACCOUNT_HOLDER_NAME, account.getAccountHolderName());
        contentValues.put(Constants.BALANCE, account.getBalance());

        long result = db.insert(Constants.TABLE_1, null, contentValues);
        db.close();
        if(result==-1)
            return false;
        return true;
    }
    public boolean removeAccount(String accountNo){
        SQLiteDatabase db = this.getWritableDatabase();
        if(db.delete(Constants.TABLE_1, Constants.ACCOUNT_NO + " = " + accountNo, null)==0) {
            db.close();
            return false;
        }
        db.close();
        return true;
    }
    public boolean updateBalance(String accountNo, ExpenseType expenseType, double amount){
        SQLiteDatabase db = this.getReadableDatabase();
        String[] args =  { accountNo};
        Cursor res =  db.rawQuery("select "+Constants.BALANCE+" from "+Constants.TABLE_1+" where "+Constants.ACCOUNT_NO+" = ?", args);
        if(res.getCount()==0) {
            db.close();
            return false;
        }
        res.moveToFirst();
        Double newBalance = res.getDouble(res.getColumnIndex(Constants.BALANCE));
        Log.d("MyApp",String.valueOf(newBalance));
        switch (expenseType) {
            case EXPENSE:
                newBalance -= amount;
                break;
            case INCOME:
                newBalance += amount;
                break;
        }
        Log.d("MyApp",String.valueOf(newBalance));
        db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Constants.BALANCE,newBalance);
        db.update(Constants.TABLE_1, contentValues, Constants.ACCOUNT_NO + " = ?", new String[]{ accountNo});
        db.close();
        return true;
    }
    public boolean logTransaction(Transaction transaction) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Constants.DATE,transaction.getDate().toString());
        contentValues.put(Constants.ACCOUNT,transaction.getAccountNo());
        if(transaction.getExpenseType()==ExpenseType.EXPENSE)
            contentValues.put(Constants.EXPENSE_TYPE, DBHelper.EXPENSE);
        else
            contentValues.put(Constants.EXPENSE_TYPE, DBHelper.INCOME);
        contentValues.put(Constants.AMOUNT, transaction.getAmount());
        try {
            db.insert(Constants.TABLE_2, null, contentValues);
        }catch(SQLiteConstraintException ex){
            return false;
        }
        db.close();
        return true;
    }
    public List<Transaction> getAllTransactionLogs() {
        List<Transaction> teList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from "+Constants.TABLE_2+ ";", null );
        if(res.moveToFirst())
            while(res.isAfterLast() == false){
                int dateIndex = res.getColumnIndex(Constants.DATE);
                int accountIndex = res.getColumnIndex(Constants.ACCOUNT);
                int expenseTypeIndex = res.getColumnIndex(Constants.EXPENSE_TYPE);
                int amountIndex = res.getColumnIndex(Constants.AMOUNT);

                ExpenseType expenseType;
                if(res.getInt(expenseTypeIndex)==DBHelper.EXPENSE)
                    expenseType = ExpenseType.EXPENSE;
                else
                    expenseType = ExpenseType.INCOME;

                Transaction tr = new Transaction(getDate(res.getString(dateIndex)),res.getString(accountIndex),
                    expenseType,res.getDouble(amountIndex));
                teList.add(tr);
                res.moveToNext();
            }
        db.close();
        return teList;
    }

    public static Date getDate(String da){
        Date date = null;
        DateFormat fomatter = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
        try {
            date = fomatter.parse(da);
        } catch (ParseException ex) {

        }
        return date;
    }
}
