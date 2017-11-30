package com.vistana.onsiteconcierge.core.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import com.vistana.onsiteconcierge.core.dao.RoomRepository;
import com.vistana.onsiteconcierge.core.model.Room;
import com.vistana.onsiteconcierge.core.model.RoomId;
import com.vistana.onsiteconcierge.core.service.RoomService;

@Service
public class RoomServiceImpl extends SaveDeleteServiceImpl<Room, RoomId> implements RoomService {

	@Autowired
	private RoomRepository repository;

	@Override
	protected CrudRepository<Room, RoomId> getRepository() {

		return repository;
	}

	@Override
	public Iterable<Room> nonTransSave(List<Room> room) {
		return this.repository.save(room);
	}

}
