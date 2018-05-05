package br.com.willianantunes.repository;

import org.springframework.data.repository.CrudRepository;

import br.com.willianantunes.model.ChatTransaction;

public interface ChatTransactionRepository extends CrudRepository<ChatTransaction, Long> {

}