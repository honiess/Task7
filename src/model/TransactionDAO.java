package model;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import org.genericdao.DAOException;
import org.genericdao.GenericDAO;
import org.genericdao.MatchArg;
import org.genericdao.RollbackException;
import org.genericdao.Transaction;
import org.genericdao.ConnectionPool;

import databean.TransactionBean;

public class TransactionDAO extends GenericDAO<TransactionBean>{
	public TransactionDAO(ConnectionPool cp, String tableName)
			throws  DAOException {
		super(TransactionBean.class, tableName, cp);
	}
	
	public void requestCheck(TransactionBean requestCheckBean) throws RollbackException {
		create(requestCheckBean);
	}
	
	public void depositCheck(TransactionBean depositCheckBean) throws RollbackException {
		create(depositCheckBean);
	}
	
	public void buyFund(TransactionBean buyFundBean) throws RollbackException {
		create(buyFundBean);
	}
	
	public void sellFund(TransactionBean sellFundBean) throws RollbackException {
		// these data will be stored in transaction table
		create(sellFundBean);
	}
	
	
	//sycronize?
	/**
	 * Process all pending buyfund transactions
	 * After TransitionDay, update all committed shares, execute date to "transaction" table 
	 * @param map <FundId,TradingPrice>
	 * @param date Execute
	 * @throws RollbackException
	 */
	public void processBuyFundTransaction(HashMap<Integer, Long> map, Date date) throws RollbackException {
		//retrieve all buy orders information 
		//get the buyFundTransactionBean
		TransactionBean[] bTransactions = match(MatchArg.equals("transactionType", 1), MatchArg.equals("executeDate", null));
		//calculate shares and update shares and execute date
		if(bTransactions == null || bTransactions.length == 0) {
			return;
		}
		
		for(TransactionBean bean : bTransactions) {
			long price = map.get(bean.getFundId());
			// deal  with format of shares
			bean.setShares( bean.getAmount() / price);
			bean.setExecuteDate(date);
			update(bean);
		}
	}
	
	public void processSellFundTransaction(HashMap<Integer, Integer> map, Date date) throws RollbackException{
		TransactionBean[] sTransactions = match(MatchArg.equals("transactionType", 2), MatchArg.equals("executeDate", null));
		if (sTransactions == null || sTransactions.length == 0) {
			return;
		}
		
		for(TransactionBean bean : sTransactions) {
			long price = map.get(bean.getFundId());
			bean.setAmount(price * bean.getShares());
			update(bean);
		}
 	}
	
	public void processRequestCheckTransaction(Date date) throws RollbackException {
		TransactionBean[] rcTransactions = match(MatchArg.equals("transactionType", 3), MatchArg.equals("executeDate", null));
		if (rcTransactions == null || rcTransactions.length == 0) {
			return;
		}
		
		for(TransactionBean bean : rcTransactions) {
			bean.setExecuteDate(date);
			update(bean);
		}
	}
	
	public void processDepositCheckTransaction(Date date) throws RollbackException {
		TransactionBean[] rcTransactions = match(MatchArg.equals("transactionType", 4), MatchArg.equals("executeDate", null));
		if (rcTransactions == null || rcTransactions.length == 0) {
			return;
		}
		
		for(TransactionBean bean : rcTransactions) {
			bean.setExecuteDate(date);
			update(bean);
		}
	}
	
	public TransactionBean[] getLastDayTransaction(Date date) throws RollbackException {
		TransactionBean[] beans = match(MatchArg.equals("executeDate", date));
		return beans;
	}
	
	public TransactionBean[] getTransactionHistory(int visitorId) throws RollbackException {
		TransactionBean[] beans = match(MatchArg.equals("visitorId", visitorId));
		return beans;
	}
	
}

