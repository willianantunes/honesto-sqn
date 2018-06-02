package br.com.willianantunes.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;

import br.com.willianantunes.model.ChatTransaction;
import br.com.willianantunes.route.LetMeQuitRoute;
import br.com.willianantunes.route.SetupCitizenDesireRoute;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.model.ModelCamelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.com.willianantunes.model.Politician;
import br.com.willianantunes.model.Room;
import br.com.willianantunes.repository.ChatTransactionRepository;
import br.com.willianantunes.repository.PoliticianRepository;
import br.com.willianantunes.repository.RoomRepository;
import lombok.Getter;

@Component
public class ScenarioBuilder {

    @Autowired
    private ChatTransactionRepository chatTransactionRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private PoliticianRepository politicianRepository;
    @PersistenceContext
    private EntityManager entityManager;
    
    @Getter
    private List<Room> rooms = new ArrayList<>();
    private List<ChatTransaction> chats = new ArrayList<>();

    @Transactional
    public ScenarioBuilder unbuild() {
        
        Query createNativeQuery = entityManager.createNativeQuery("SHOW TABLES");
        Object[] myTables = (Object[]) createNativeQuery.getResultList().get(0);

        Arrays.stream(myTables)
            .map(Object::toString)
            .filter(s -> s.startsWith("TB_"))
            .forEach(t -> entityManager.createNativeQuery("truncate table " + t).executeUpdate());

        return this;
    }

    public void prepareCamelEnvironment(ModelCamelContext camelContext) throws Exception {

        prepareCamelEnvironment(Optional.empty(), camelContext);
    }

    public void prepareCamelEnvironment(Optional<ThrowingConsumer<CamelContext>> clientRules, ModelCamelContext camelContext) throws Exception {

        if (!camelContext.getStatus().isStarted()) {

            camelContext.getRouteDefinition(SetupCitizenDesireRoute.ROUTE_ID_FIRST_CONTACT).adviceWith(camelContext,
                new AdviceWithRouteBuilder() {
                    @Override
                    public void configure() throws Exception {

                        replaceFromWith("direct:telegram-entrance");
                    }
                });

            camelContext.getRouteDefinition(LetMeQuitRoute.ROUTE_ID_AFTER_FIRST_CONTACT).adviceWith(camelContext,
                new AdviceWithRouteBuilder() {
                    @Override
                    public void configure() throws Exception {

                        weaveByToUri("telegram:bots").replace().to("mock:telegram-bot-exit");
                    }
                });

            clientRules.ifPresent(c -> c.accept(camelContext));
            camelContext.start();
        }
    }
    
    public void build() {

        Optional.ofNullable(rooms).ifPresent(roomRepository::save);
        Optional.ofNullable(chats).ifPresent(chatTransactionRepository::save);
    }    

    public RoomBuilder createRoom(Room room) {
        
        return new RoomBuilder(this, room);
    }

    public ScenarioBuilder createChatTransaction(ChatTransaction chatTransaction) {

        chats.add(chatTransaction);
        return this;
    }

    public static class RoomBuilder {
        
        private Room room;
        private ScenarioBuilder scenarioBuilder;        
        
        public RoomBuilder(ScenarioBuilder scenarioBuilder, Room room) {
            
            this.scenarioBuilder = scenarioBuilder;
            this.room = room;
        }

        public ScenarioBuilder withPolitician(Politician politician) {

            room.setPoliticians(new ArrayList<>());
            room.getPoliticians().add(politician);
            scenarioBuilder.rooms.add(room);
            return scenarioBuilder;
        }        
    }
}