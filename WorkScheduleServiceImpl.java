package com.vistana.onsiteconcierge.core.service.impl;

import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.vistana.onsiteconcierge.core.dao.WorkSchedulePredicates;
import com.vistana.onsiteconcierge.core.dao.WorkScheduleRepository;
import com.vistana.onsiteconcierge.core.model.QPerson;
import com.vistana.onsiteconcierge.core.model.QWorkSchedule;
import com.vistana.onsiteconcierge.core.model.WorkSchedule;
import com.vistana.onsiteconcierge.core.model.WorkScheduleId;
import com.vistana.onsiteconcierge.core.service.WorkScheduleService;
import com.vistana.util.DateHelper;

@Service
public class WorkScheduleServiceImpl extends SaveDeleteServiceImpl<WorkSchedule, WorkScheduleId>
		implements WorkScheduleService {

	@Autowired
	private WorkScheduleRepository repository;

	@Override
	public List<WorkSchedule> findByOrganizationAndProperty(String organization, Integer property) {

		return this.repository.findByIdOrganizationIdAndIdPropertyId(organization, property);
	}

	@Override
	public List<WorkSchedule> findWithPerson(List<Integer> personIds, String organization, Integer property, Date start,
			Date end) {

		QPerson person = new QPerson("person");
		QWorkSchedule workSchedule = new QWorkSchedule("workSchedule");
		Date startZeroed = DateHelper.dateZeroTime(start);

		Date endZeroed = DateHelper.dateZeroTime(end);
		endZeroed = DateHelper.dateAdd(endZeroed, Calendar.DATE, 1);

		BooleanExpression expr = WorkSchedulePredicates.idByOrganizationProperty(workSchedule, organization, property)
				.and(workSchedule.id.workDate.goe(startZeroed.toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
						.and(workSchedule.id.workDate
								.lt(endZeroed.toInstant().atZone(ZoneId.systemDefault()).toLocalDate())));
		if (personIds != null && personIds.size() > 0) {
			expr = expr.and(workSchedule.id.personId.in(personIds));
		}

		return getQueryFactory().selectFrom(workSchedule).leftJoin(workSchedule.person, person).fetchJoin().where(expr)
				.fetch();
	}

	@Override
	public List<WorkSchedule> findWithPersonId(String organization, Integer property, Integer personId) {

		return (repository).findByIdOrganizationIdAndIdPropertyIdAndIdPersonId(organization, property, personId);
	}

	@Override
	public CrudRepository<WorkSchedule, WorkScheduleId> getRepository() {

		return repository;
	}

	@Override
	@Transactional
	public void save(List<WorkSchedule> toSave) {

		if (toSave != null) {

			repository.save(toSave);
		}
	}

}
