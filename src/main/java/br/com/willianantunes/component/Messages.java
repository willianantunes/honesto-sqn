package br.com.willianantunes.component;

import java.util.Locale;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Component;

@Component
public class Messages {

    public static final String COMMAND_START = "command.start";
    
    public static final String COMMAND_CONFIGURAR = "command.configurar";
    
    public static final String COMMAND_ATUAL = "command.atual";
    public static final String COMMAND_ATUAL_NO_ONE = "command.atual.no-one";    
    
    public static final String COMMAND_RETIRAR_CONFIGURED = "command.retirar.configured";
    public static final String COMMAND_RETIRAR_COMPLETED = "command.retirar.completed";
    public static final String COMMAND_RETIRAR_WRONG_OPTION = "command.retirar.wrong-option";
    public static final String COMMAND_RETIRAR_NOT_CONFIGURED = "command.retirar.not-configured";

    public static final String COMMAND_RESEARCH = "command.research";
    public static final String COMMAND_RESEARCH_OUTPUT_START = "command.research.output.start";
    public static final String COMMAND_RESEARCH_OUTPUT_MORE = "command.research.output.more";
    public static final String COMMAND_RESEARCH_OUTPUT_ENTRY = "command.research.output.entry";
    public static final String COMMAND_RESEARCH_OUTPUT_NOTHING = "command.research.output.nothing";
    public static final String COMMAND_RESEARCH_OUTPUT_MORE_ENTRIES = "command.research.output.more-entries";
    public static final String COMMAND_RESEARCH_COMPLETED = "command.research.completed";
    public static final String COMMAND_RESEARCH_LOADING_ONE = "command.research.loading.one";
    public static final String COMMAND_RESEARCH_LOADING_TWO = "command.research.loading.two";
    public static final String COMMAND_RESEARCH_LOADING_THREE = "command.research.loading.three";
    public static final String COMMAND_RESEARCH_LOADING_TIMEOUT = "command.research.loading.timeout";
    public static final String COMMAND_RESEARCH_BUTTON_SATISFIED = "command.research.button.satisfied";
    public static final String COMMAND_RESEARCH_BUTTON_MORE = "command.research.button.more";
    
    public static final String COMMAND_INVALID = "command.invalid";
    public static final String COMMAND_NOT_AVAILABLE = "command.not-available";    
    
    @Autowired
    private MessageSource messageSource;

    private MessageSourceAccessor accessor;

    @PostConstruct
    private void init() {
        
        accessor = new MessageSourceAccessor(messageSource, Locale.forLanguageTag("pt-BR"));
    }

    public String get(String code) {

        return accessor.getMessage(code);
    }
    
    public String get(String code, Object... args) {

        return accessor.getMessage(code, args);
    }
}