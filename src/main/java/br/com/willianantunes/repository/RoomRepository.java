package br.com.willianantunes.repository;

import org.springframework.data.repository.CrudRepository;

import br.com.willianantunes.model.Room;

public interface RoomRepository extends CrudRepository<Room, Long> {

}