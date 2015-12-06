package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl;

import android.content.Context;

import java.util.List;
import java.util.Map;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.AccountDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.exception.InvalidAccountException;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Account;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.database.DBHelper;


public class PersistentAccountDAO implements AccountDAO {
    DBHelper db = null;

    public PersistentAccountDAO(Context context){
        this.db = DBHelper.getInstance(context);
    }

    @Override
    public List<String> getAccountNumbersList() {
        return db.getAccountNumberList();
    }

    @Override
    public List<Account> getAccountsList() {
        return db.getAccountList();
    }

    @Override
    public Account getAccount(String accountNo) throws InvalidAccountException {
        Account account = db.getAccount(accountNo);
        if (account!=null){
            return account;
        }
        String msg = "Account " + accountNo + " is invalid.";
        throw new InvalidAccountException(msg);
    }

    @Override
    public void addAccount(Account account) {
        db.addAccount(account);
    }

    @Override
    public void removeAccount(String accountNo) throws InvalidAccountException {
        if (!db.removeAccount(accountNo)) {
            String msg = "Account " + accountNo + " is invalid.";
            throw new InvalidAccountException(msg);
        }
    }

    @Override
    public void updateBalance(String accountNo, ExpenseType expenseType, double amount) throws InvalidAccountException {
        if(!db.updateBalance(accountNo,expenseType,amount)){
            String msg = "Account " + accountNo + " is invalid.";
            throw new InvalidAccountException(msg);
        }
    }
}
