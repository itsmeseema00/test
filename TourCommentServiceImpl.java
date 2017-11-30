package com.vistana.onsiteconcierge.core.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.vistana.onsiteconcierge.core.dao.TourCommentRepository;
import com.vistana.onsiteconcierge.core.model.TourComment;
import com.vistana.onsiteconcierge.core.model.TourSequenceId;
import com.vistana.onsiteconcierge.core.service.TourCommentService;

@Service
public class TourCommentServiceImpl extends SaveDeleteServiceImpl<TourComment, TourSequenceId> implements TourCommentService {

	@Autowired
	private TourCommentRepository repository;

	@Override
	protected CrudRepository<TourComment, TourSequenceId> getRepository() {

		return repository;
	}

}
