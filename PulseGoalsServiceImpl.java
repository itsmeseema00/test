package com.vistana.onsiteconcierge.core.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import com.querydsl.jpa.impl.JPAQuery;
import com.vistana.onsiteconcierge.core.dao.PulseGoalsRepository;
import com.vistana.onsiteconcierge.core.dto.PulseGoalsDto;
import com.vistana.onsiteconcierge.core.model.PulseGoals;
import com.vistana.onsiteconcierge.core.model.PulseGoalsId;
import com.vistana.onsiteconcierge.core.model.QPulseConfig;
import com.vistana.onsiteconcierge.core.model.QPulseGoals;
import com.vistana.onsiteconcierge.core.service.PulseGoalsService;

@Service
public class PulseGoalsServiceImpl extends SaveDeleteServiceImpl<PulseGoals, PulseGoalsId>
		implements PulseGoalsService {

	@Autowired
	private PulseGoalsRepository repository;

	@Override
	public List<PulseGoalsDto> findByOrganizationIdAndPropertyIdQuery(String organizationId, Integer propertyId) {

		QPulseGoals pulseGoals = new QPulseGoals("pulseGoals");
		QPulseConfig pulseConfig = new QPulseConfig("pulseConfig");
		List<PulseGoals> listOfPulseReport = new JPAQuery<PulseGoals>(getEntityManager()).from(pulseGoals)
				.innerJoin(pulseGoals.pulseConfig, pulseConfig)
				.where(pulseGoals.id.organizationId.eq(organizationId).and(pulseGoals.id.propertyId.eq(propertyId)))
				.fetch();
		return getPulseDto(listOfPulseReport);

	}

	@Override
	public void updatePulseGoalsDto(List<PulseGoalsDto> updatedPulseReports) {

		for (PulseGoalsDto eachPulseGoalsDto : updatedPulseReports) {
			QPulseGoals pulseGoals = new QPulseGoals("pulseGoals");
			getQueryFactory().update(pulseGoals)
					.where(pulseGoals.id.organizationId.eq(eachPulseGoalsDto.getOrganizationId())
							.and(pulseGoals.id.propertyId.eq(eachPulseGoalsDto.getPropertyId()))
							.and(pulseGoals.id.pulseGuestTypeId.eq(eachPulseGoalsDto.getPulseGuestTypeId())))
					.set(pulseGoals.monthlyBudget, eachPulseGoalsDto.getMonthlyBudget())
					.set(pulseGoals.penetrationGoal, eachPulseGoalsDto.getPenetrationGoal())
					.set(pulseGoals.showPercentage, eachPulseGoalsDto.getShowPercentage())
					.set(pulseGoals.shiftsLeft, eachPulseGoalsDto.getShiftsLeft()).execute();
		}

	}

	private List<PulseGoalsDto> getPulseDto(List<PulseGoals> listOfPulseReport) {
		return listOfPulseReport.stream().map(PulseGoalsDto::new).collect(Collectors.toList());
	}

	@Override
	protected CrudRepository<PulseGoals, PulseGoalsId> getRepository() {
		return repository;
	}

}
