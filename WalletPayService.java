package org.rbfcu.netbranch.card.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.rbfcu.business.log.ActivityType;
import org.rbfcu.business.log.OnlineActivity;
import org.rbfcu.netbranch.common.service.LogClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class WalletPayService {
	private static final Logger LOG = LoggerFactory.getLogger(WalletPayService.class);
	private static String CARD_EXPIRATION_DATE = "N";
	private static String UNIQUE_TOKEN_REFERENCE = "N";

	public String getTavFormat(String selectedCardNumber, Date selectedCardExpireDate) {
		String concatenatedString = selectedCardNumber.concat(getMonthYearFormat(selectedCardExpireDate)).concat(UNIQUE_TOKEN_REFERENCE);
		return concatenatedString;
	}

	private String getMonthYearFormat(Date date) {
		LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		String year = String.valueOf(localDate.getYear());
		return String.valueOf(localDate.getMonthValue()) + "/" + year.substring(Math.max(year.length() - 2, 0));
	}

	public void logSuccessActivity(HttpServletRequest request, String selectedCardNumber) {
		OnlineActivity activity = new OnlineActivity();
		activity.setActivityType(ActivityType.WALLET_PAY_SUCCESS);
		activity.setKey1(selectedCardNumber);
		activity.setKey2("");
		activity.setKey3("");//add in future
		activity.setDetail("Card Number with " + selectedCardNumber + " successfully added to wallet pay.");

		new LogClientService(request).logActivity(activity);
	}

}
