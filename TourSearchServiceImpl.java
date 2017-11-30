package com.vistana.onsiteconcierge.core.service.impl;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.vistana.onsiteconcierge.core.dao.TourRepository;
import com.vistana.onsiteconcierge.core.dto.TourSearchDto;
import com.vistana.onsiteconcierge.core.model.QTour;
import com.vistana.onsiteconcierge.core.model.Tour;
import com.vistana.onsiteconcierge.core.model.TourId;
import com.vistana.onsiteconcierge.core.service.TourSearchService;

@Service
public class TourSearchServiceImpl extends SaveServiceImpl<Tour, TourId> implements
    TourSearchService {

    @Autowired
    private TourRepository repository;

    @Override
    public TourRepository getRepository() {
        return repository;
    }

    public void setRepository(TourRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Tour> tourSearch(TourSearchDto tourSearchDto, String organization,
        String internalProperty) {

        QTour tour = new QTour("TOUR");
        BooleanBuilder tourSearchBuilder = new BooleanBuilder();

        if (tourSearchDto.getShowBeginDate() != null && tourSearchDto.getShowEndDate() != null) {
            Date start = Date.from(
                tourSearchDto.getShowBeginDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date end = Date.from(
                tourSearchDto.getShowEndDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
            tourSearchBuilder.and(tour.showDate.between(start, end));
        }

        if (!StringUtils.isBlank(tourSearchDto.getFirstName())) {
            tourSearchBuilder.and(tour.firstName.like(tourSearchDto.getFirstName()));
        }

        if (!StringUtils.isBlank(tourSearchDto.getLastName())) {
            tourSearchBuilder.and(tour.lastName.like(tourSearchDto.getLastName()));
        }

        if (!StringUtils.isBlank(tourSearchDto.getTripTicketNum())) {
            tourSearchBuilder.and(tour.id.tripTicketNumber.like(tourSearchDto.getTripTicketNum()));
        }

        if (!StringUtils.isBlank(tourSearchDto.getTourType())) {
            tourSearchBuilder.and(tour.tourType.like(tourSearchDto.getTourType()));
        }

        if (tourSearchDto.getShowInd() != null && !tourSearchDto.getShowInd().isEmpty()) {
            tourSearchBuilder
                .andAnyOf(tour.showed.in(tourSearchDto.getShowInd()), tour.showed.isEmpty(),
                    tour.showed.isNull());
        }

        if (!StringUtils.isBlank(tourSearchDto.getQualified())) {
            tourSearchBuilder.andAnyOf(tour.qualified.like(tourSearchDto.getQualified()),
                tour.qualified.isEmpty(), tour.qualified.isNull());
        }

        if (!StringUtils.isBlank(tourSearchDto.getToured())) {
            tourSearchBuilder
                .andAnyOf(tour.toured.like(tourSearchDto.getToured()), tour.toured.isEmpty(),
                    tour.toured.isNull());
        }

        if (!StringUtils.isBlank(tourSearchDto.getSolicitorId1())) {
            tourSearchBuilder.and(tour.solicitor1.like(tourSearchDto.getSolicitorId1()));
        }

        if (!StringUtils.isBlank(tourSearchDto.getSalesCenter())) {
            tourSearchBuilder.and(tour.salesCenter.like(tourSearchDto.getSalesCenter()));
        }

        if (!StringUtils.isBlank(tourSearchDto.getSalesLine())) {
            tourSearchBuilder.and(tour.salesLine.like(tourSearchDto.getSalesLine()));
        }

        if (!StringUtils.isBlank(tourSearchDto.getTourManifestCode())) {
            tourSearchBuilder.and(tour.tourManifestCode.like(tourSearchDto.getTourManifestCode()));
        }

        if (!StringUtils.isBlank(tourSearchDto.getMarketingSource())) {
            tourSearchBuilder
                .and(tour.marketingSourceCode.like(tourSearchDto.getMarketingSource()));
        }

        if (!StringUtils.isBlank(tourSearchDto.getTourLocationCode())) {
            tourSearchBuilder.and(tour.tourLocationCode.like(tourSearchDto.getTourLocationCode()));
        }

        tourSearchBuilder.and(tour.id.organizationId.eq(organization))
            .and(tour.id.internalPropertyId.eq(internalProperty));

        return new JPAQuery<Tour>(getEntityManager()).from(tour).where(tourSearchBuilder).fetch();
    }

}
