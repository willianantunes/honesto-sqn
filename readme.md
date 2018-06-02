
# Honesto SQN

[![Run in Postman](https://run.pstmn.io/button.svg)](https://app.getpostman.com/run-collection/1b4ee765643a60f5ba5f)

Uso de parte da stack [Serenata de Amor](https://serenata.ai/) como o [Jarbas](https://github.com/okfn-brasil/serenata-de-amor/tree/97a4a34e730f58ed1fa6bf4833a23e0e50e27cfa/jarbas#json-api-endpoints) para auxiliar cidadãos a fiscalizarem seus políticos pelo Telegram.

----

O projeto está em construção ainda então não espere muita coisa. Assim que novas situações forem incluídas este README será atualizado com 9dades!

## Preparando ambiente de desenvolvimento

O básico é o seguinte:

1. Docker Compose para facilitar as coisas
2. Token do robô do Telegram (obtenha pelo [BotFather](https://core.telegram.org/bots#creating-a-new-bot))
3. Ambiente de desenvolvimento do [Serenata de Amor](https://github.com/okfn-brasil/serenata-de-amor#using-docker)

Para o terceiro item faça o seguinte (leia o comentário final antes de executar):

	git clone https://github.com/okfn-brasil/serenata-de-amor.git && cd serenata-de-amor/ && cp contrib/.env.sample .env && cp rosie/config.ini.example rosie/requirements.txt rosie/rosie/. && docker-compose up -d

Não sei se existe algum problema com o projeto em si, mas não roda de primeira, pode lançar vários erros. O que fiz para funcionar no meu caso foi desabilitar a construção do serviço `research` no `docker-compose.yml`.

### Download de correções e novas features para rodar projeto

Como tive que criar e corrigir algumas situações no Apache Camel, dependendo de quando você baixar o repositório talvez a versão dos componentes no projeto principal ainda não tenham sido liberados para download no Maven. Para esse caso, ou você faz download do [meu fork](https://github.com/willianantunes/camel) e faz o build a partir da branch de trabalho com a versão necessária ou o download do [projeto oficial](https://github.com/apache/camel). 

## Fluxo mínimo viável

Já não faz jus a versão atual. Atualizarei em breve.

![Mapa de navegação por opções](docs/fluxos-honesto-sqn.png?raw=true "Mapa de navegação por opções")

## Recursos (visão de alto nível) para MVP

- [x] Controle de transação da conversa (máquina de estado).
- [x] Caso a opção não esteja disponível informar o usuário e finalizar a transação da conversa.
- [x] Listagem dos políticos configurados via opção _/atual_.
- [x] Excluir determinado político configurado previamente via opção _/retirar_.
- [x] Mapeamento dos serviços ofertados pelo Jarbas.
- [x] Pesquisar histórico do político via opção _/pesquisar_.
- [x] Informar usuário para esperar pacientemente até 3x com mensagens distintas já que o Jarbas demora para processar.
- [ ] Usuário escolher qual político deseja receber notificações de gastos suspeitos via opção _/configurar_.
- [ ] Envio de notificações (push notification) para usuários que configuraram o político que fez gasto suspeito.
- [ ] Atualização da figura _fluxo mínimo viável_.

## Frutos do projeto

- Contribuição ao projeto open-source [Apache Camel](http://camel.apache.org/) ([PR 2318](https://github.com/apache/camel/pull/2318) e [CAMEL-12478](https://issues.apache.org/jira/browse/CAMEL-12478)) aprimorando o componente de integração com [Telegram](https://github.com/apache/camel/blob/39c0d63d923bfe9236834ecb1c4470bb7e9e7eaa/components/camel-telegram/src/main/docs/telegram-component.adoc#telegram-component).
- Correção de defeito para configuração programática com Spring do _Rest Client_ ([PR 2350](https://github.com/apache/camel/pull/2350) e [CAMEL-12541](https://issues.apache.org/jira/browse/CAMEL-12541)) pois funcionava apenas via XML para uso do componente [CXF-RS](https://github.com/apache/camel/blob/39c0d63d923bfe9236834ecb1c4470bb7e9e7eaa/components/camel-cxf/src/main/docs/cxfrs-component.adoc#cxf-rs-component).

## Links para referência/estudo

- [Envio de mensagem customizada](https://core.telegram.org/bots#keyboards)
- [Componente do Telegram para Apache Camel](https://github.com/apache/camel/blob/a989fea98ce32f5f622c576bf3ea08c1782116e2/components/camel-telegram/src/main/docs/telegram-component.adoc#telegram-component)
- [Camel in Action, Second Edition](https://www.manning.com/books/camel-in-action-second-edition)