package br.com.willianantunes.component;

import java.util.function.Supplier;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class TransactionalContext {
    
    public <T> T execute(Supplier<T> supplier) {

        return supplier.get();
    }
}