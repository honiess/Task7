package Controller;

import java.text.DecimalFormat;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.mybeans.form.FormBeanException;
import org.mybeans.form.FormBeanFactory;

import databean.CustomerBean;
import model.Model;

public class CustomerBuyAction extends Action{
	
	private FormBeanFactory<BuyFundForm> formBeanFactory = FormBeanFactory<FormBean>.getInstance(BuyFundForm.class);
	
	private FundDAO fundDAO;
	private FundPriceHistoryDAO fundPriceHistoryDAO;
	private CustomerDAO customerDAO;
	private TransactionDAO transactionDAO;
	DecimalFormat formatter;
	
	public CustomerBuyFundAction(Model model) {
		fundDAO = model.getFundDAO();
		fundPriceHistoryDAO = model.getFundPriceHistoryDAO();
		customerDAO = model.getCustomerDAO();
		transactionDAO = model.getTransactionDAO();
	}
	
	@Override
	public String getName() {return "customerbyaction.do";}

	@Override
	public String perform(HttpServletRequest request) {
		// TODO Auto-generated method stub
		List<String> errors = new ArrayList<String>();
		request.setAttribute("errors", errors);
		
		try {
		String fundName = request.getParameter("getFundName");
		if (fundName != null) request.setAttribute("getFundName", fundName);
		
		FundGeneralInfoBean[] fundGeneralList = fundPriceHistoryDAO.getAllFundsGeneralInfo();
		request.setAttribute("fundGeneralList", fundGeneralList);
		
		int customerId = (Integer)request.getSession(false).getAttribute("customerId");
		CustomerBean customer = customerDAO.read(customerId);
		formatter = new DecimalFormat("#,##0.00");
		String cash = formatter.format(customer.getCash());
		request.setAttribute("cash", cash);		
		
		BuyFundForm form = formBeanFactory.create(request);
		if (!form.isPresent()) {
			return "customerbuyfund.jsp";
		}
		errors.addAll(form.getValidationErrors());
		if (errors.size() != 0) {
			return "customerbuyfund.jsp";
		}
		
		request.setAttribute("form", form);
		
		FunBean fund = fundDAO.read(form.getFundName());
		if (fund == null) {
			errors.add("Fund does not exist!");
			return "customerbuyfund.jsp";
		}
		
		double amount = form.getAmountAsDouble();
		double cash = customer.getCash();
		
		transactionDAO.buyFund(customerId, fund.getFundId(), amount);
		
		request.setAttribute("message", "Thank you! your request to buy " + form.getFundName() + 
		"has been queued until transaction day");
		
		return "customercomfirmation.jsp";
		} catch (MyDAOException e) {
			errors.add(e.getMessage());
			return "error.jsp";
		} catch (FormBeanException e) {
			errors.add(e.toString());
			return "error.jsp";
		}
	}
}
