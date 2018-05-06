package br.com.willianantunes.repository;

import org.springframework.data.repository.CrudRepository;

import br.com.willianantunes.model.Politician;

public interface PoliticianRepository extends CrudRepository<Politician, Long> {

}