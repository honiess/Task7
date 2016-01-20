package controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.mybeans.form.FormBeanException;
import org.mybeans.form.FormBeanFactory;

import FilterAndConstant.Constants;
import Model.VisitorDAO;
import databean.VisitorBean;
import Model.Model;
import Model.TransactionDAO;
import Model.VisitorDAO;


public class VisitorLogAction extends Action{
	private FormBeanFactory<LoginForm> formBeanFactory 
		= FormBeanFactory<FormBean>.getInstance(LoginForm.class);
	
	private VisitorDAO visitorDAO;
	private TransactionDAO transactionDAO;
	private PositionDAO positionDAO;
	
	public VisitorLogAction(Model model) {
		visitorDAO = model.getCustormerDAO();
		transactionDAO = model.getTransactionDAO();
		positionDAO = model.getPositionDAO();
	}
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return Constants.visitorLogAction;
	}

	@Override
	public String perform(HttpServletRequest request) {
		// TODO Auto-generated method stub
		HttpSession session = request.getSession();
		List<String> errors = new ArrayList<String>();
        request.setAttribute("errors",errors);
		
		if (session.getAttribute("cusomerId") != null) {
			return Constants.visitorViewAccountAction;
		}
		
		if (session.getAttribute("employeeId") != null) {
        	return Constants.employeeMainPanelJsp;
        }
		
		try {
			List<String> errors = new ArrayList<String>();
	        request.setAttribute("errors",errors);
	        
	        if (!form.isPresent()) {
	            return Constants.mainPage;
	        }
	        
	        errors.addAll(form.getValidationErrors());
	        if (errors.size() != 0) {
	            return Constants.mainPage;
	        }
	        
	        VisitorBean visitor = visitorDAO.read(form.getUserName());
	        
	        if (customer == null) {
	            errors.add("Incorrect/Invalid Customer Username");
	            return Constants.mainPage;
	        }
	        
	        if (!customer.checkPassword(form.getPassword())) {
	            errors.add("Incorrect/Invalid Password");
	            return Constants.mainPage;
	        }
	        
	        int visitorId = visitor.getVisitorId();
	        session.setAttribute("visitorId", visitorId);
	        Date lastTradeDate = transactionDAO.getCustomerLastTradeDate(visitorId);
			customer.setLastTradeDate(lastTradeDate);
			session.setAttribute("firstname", visitor.getFirstName());
			session.setAttribute("lastname", visitor.getLastName());
			
			return Constants.visitorViewAccountJsp;
		} catch (FormBeanException e) {
			errors.add(e.getMessage());
			return "errors.jsp";
		} catch (MyDAOException e) {
			errors.add(e.getMessage());
			return "errors.jsp";
		}
	}

}
