
# Honesto SQN

[![Run in Postman](https://run.pstmn.io/button.svg)](https://app.getpostman.com/run-collection/1b4ee765643a60f5ba5f)

Uso de parte da stack [Serenata de Amor](https://serenata.ai/) para auxiliar cidadãos a fiscalizar seus políticos pelo Telegram por enquanto, porque a ideia é incluir outros serviços também.

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

## Fluxo mínimo viável

![Mapa de navegação por opções](docs/fluxos-honesto-sqn.png?raw=true "Mapa de navegação por opções")

## Frutos do projeto

- [Contribuição ao projeto open-source Apache Camel aprimorando o componente de integração com Telegram](https://github.com/apache/camel/pull/2318)

## Links para referência/estudo

- [Envio de mensagem customizada](https://core.telegram.org/bots#keyboards)
- [Componente do Telegram para Apache Camel](https://github.com/apache/camel/blob/a989fea98ce32f5f622c576bf3ea08c1782116e2/components/camel-telegram/src/main/docs/telegram-component.adoc#telegram-component)