package br.com.willianantunes.repository;

import org.springframework.data.repository.CrudRepository;

import br.com.willianantunes.model.Room;

import java.util.Optional;

public interface RoomRepository extends CrudRepository<Room, Long> {

    Optional<Room> findByChatId(Integer chatId);
}