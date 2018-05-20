package br.com.willianantunes.repository;

import br.com.willianantunes.model.ChatTransaction;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ChatTransactionRepository extends CrudRepository<ChatTransaction, Long> {

    Optional<ChatTransaction> findByChatId(Integer chatId);
}