package com.vistana.onsiteconcierge.core.service.impl;

import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.starwood.feeds.vistana.VistanaConciergeFeedServicesService;
import com.starwood.feeds.vistana.VistanaConciergeServicesService;
import com.starwood.sgcservices.service.gcconciergefeed.ArrayOfXsdGCConciergeInputObject;
import com.starwood.sgcservices.service.gcconciergefeed.GCConciergeFeedServiceRequestDTO;
import com.starwood.sgcservices.service.gcconciergefeed.GCConciergeFeedServiceResponseDTO;
import com.starwood.sgcservices.service.gcconciergefeed.GCConciergeInputObject;
import com.starwood.sgcservices.service.gcconciergefeed.GCConciergeOptInOutputObject;
import com.vistana.onsiteconcierge.core.model.LeadContact;
import com.vistana.onsiteconcierge.core.service.ConciergeService;

@Service
public class ConciergeServiceImpl implements ConciergeService {

	private final static Logger logger = LoggerFactory.getLogger(ConciergeServiceImpl.class);

	private final String apiKey = "33464A389AF1C36BA2113EA1A1EC8";

	private final String service_url = "https://apiconnect.starwoodhotels.com/vistanasvcs/concierge";

	private VistanaConciergeFeedServicesService vcfs;

	private VistanaConciergeServicesService vcs;

	@Override
	public List<LeadContact> emailOptIn(List<LeadContact> leadContacts) {

		GCConciergeFeedServiceRequestDTO request = new GCConciergeFeedServiceRequestDTO();
		ArrayOfXsdGCConciergeInputObject paramArrayOfXsdGCConciergeInputObject = new ArrayOfXsdGCConciergeInputObject();
		Map<Long, GCConciergeOptInOutputObject> optInResponse = new HashMap<>();

		leadContacts.forEach(lead -> {
			GregorianCalendar gregorianCalendar = new GregorianCalendar();
			GCConciergeInputObject inquiry = new GCConciergeInputObject();
			XMLGregorianCalendar xmlGrogerianCalendar = null;
			gregorianCalendar.setTime(lead.getArrivalDate());
			try {
				xmlGrogerianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
				xmlGrogerianCalendar.setTimezone(DatatypeConstants.FIELD_UNDEFINED);

			} catch (DatatypeConfigurationException e) {
				logger.warn("unable to convert date to xmlGrogerianCalendar");
			}

			inquiry.setArrivalDate(xmlGrogerianCalendar);
			inquiry.setEmailAddress(lead.getGuest().getEmailAddress());
			inquiry.setPropertyID(lead.getId().getPropertyId());
			inquiry.setReservationNumber(Long.parseLong(lead.getId().getReservationConfirmationNum()));

			paramArrayOfXsdGCConciergeInputObject.getReservation().add(inquiry);
		});
		request.setReservations(paramArrayOfXsdGCConciergeInputObject);
		GCConciergeFeedServiceResponseDTO resp = vcfs.readEmailOptIn(request);

		if (!resp.isSuccessIndicator()) {
			logger.error("emailOptIn() not successful");
			throw new IllegalStateException("emailOptIn() not successful");
		}

		if (resp.getReservations() != null) {
			resp.getReservations().getReservation().forEach(obj -> {
				optInResponse.put(obj.getReservationNumber(), obj);
			});
			leadContacts.forEach(lead -> {
				GCConciergeOptInOutputObject found = optInResponse
						.get(Long.parseLong(lead.getId().getReservationConfirmationNum()));
				lead.setOptInFlag(found.isIsOptIn());
				lead.getGuest().setOptInFlag(found.isIsOptIn());
				lead.setOptInLastRefreshDtm(Timestamp.valueOf(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime()));
				lead.getGuest()
						.setOptInLastRefreshDtm(Timestamp.valueOf(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime()));
				lead.setRemarksText("Starwood Marketable Email Status as of "
						+ Timestamp.valueOf(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime()) + " : Result = "
						+ found.isIsOptIn(), true);
				if (found.isIsOptIn().equals(true)) {
					lead.getGuest().setOptOutUrl(found.getUnsubUrl());
				}
			});
		}
		return leadContacts;
	}

	@SuppressWarnings("rawtypes")
	@PostConstruct
	public void init() {

		vcs = new VistanaConciergeServicesService();
		vcfs = vcs.getVistanaConciergeFeedServicesPort();
		BindingProvider bindingProvider = (BindingProvider) vcfs;

		// Add HTTP request Headers
		Map<String, List<String>> requestHeaders = new HashMap<String, List<String>>();
		requestHeaders.put("apiKey", Arrays.asList(apiKey));

		bindingProvider.getRequestContext().put(MessageContext.HTTP_REQUEST_HEADERS, requestHeaders);
		bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, service_url);

		List<Handler> handlerChain = new ArrayList<Handler>();

		((BindingProvider) vcfs).getBinding().setHandlerChain(handlerChain);
	}
}
