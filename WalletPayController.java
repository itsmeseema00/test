package org.rbfcu.netbranch.card.controller;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.rbfcu.netbranch.card.bean.WalletPayForm;
import org.rbfcu.netbranch.card.service.WalletPayService;
import org.rbfcu.netbranch.card.utility.WalletPayUtility;
import org.rbfcu.netbranch.card.validator.WalletPayValidator;
import org.rbfcu.netbranch.card.web.CardSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

@Controller
@SessionAttributes("walletPayForm")
public class WalletPayController {

	private static final Logger LOG = LoggerFactory.getLogger(WalletPayController.class);

	@Autowired
	private WalletPayValidator walletPayValidator;
	@Autowired
	private WalletPayService walletPayService;

	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(walletPayValidator);
	}

	//@EvaluateRisk(riskFlow = RiskFlow.WALLET_PAY)
	@RequestMapping(value = { "/mobileapp/card/loadwalletPay" })

	public String loadWalletPay(WalletPayForm walletPayForm, BindingResult result, HttpServletRequest request) {
		walletPayForm.setWalletPayCertify(false);

		if (!result.hasErrors()) {
			return "card.loadWalletPay";
		} else {
			request.setAttribute("errorMessage", true);
			return "phone.fail";
		}
	}

	@RequestMapping(value = { "/mobileapp/card/submitWalletPay" })
	public String submitWalletPay(@Validated WalletPayForm walletPayForm, BindingResult result, HttpServletRequest request) throws Exception {

		if (result.hasErrors()) {
			LOG.error("Error occured during validation.");
			return "card.loadWalletPay";
		}

		CardSession cardSession = new CardSession(request);

		String selectedCardNumber = getCardNumberFromSession(cardSession);
		Date selectedCardExpireDate = getCardExpirationDateFromSession(cardSession);
		PrivateKey privateKey = new WalletPayUtility().getRsaPrivateKey();
		byte[] encryptedByte =
				new WalletPayUtility().encrypt(walletPayService.getTavFormat(selectedCardNumber, selectedCardExpireDate), privateKey);
		String enct = encryptedByte.toString();

		PublicKey publicKey = new WalletPayUtility().getRsaPublicKey();
		String decryptedValue = new WalletPayUtility().decrypt(enct, publicKey);//to see if its working

		walletPayService.logSuccessActivity(request, selectedCardNumber);
		return "card.confirmWalletPay";

	}

	public String getCardNumberFromSession(CardSession cardSession) {
		if (cardSession.getSelectedCreditCard() != null) {
			return cardSession.getSelectedCreditCard().getCardNumber();
		} else if (cardSession.getSelectedDebitCard() != null) {
			return cardSession.getSelectedDebitCard().getCardNumber();
		} else
			return StringUtils.EMPTY;
	}

	public Date getCardExpirationDateFromSession(CardSession cardSession) {
		if (cardSession.getSelectedCreditCard() != null) {
			return cardSession.getSelectedCreditCard().getExpireDate();
		} else if (cardSession.getSelectedDebitCard() != null) {
			return cardSession.getSelectedDebitCard().getExpireDate();
		} else
			return null;
	}

}