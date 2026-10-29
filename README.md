# Känner du boten? – Backend

Backenden till chattspelet **Känner du boten?**, där spelare chattar med varandra och en hemlig AI och röstar på vem de tror är boten.

**Spela här:** https://whoisthebot-frontend.onrender.com

Frontenden har en mer utförlig beskrivning av hela projektet, finns här: [WhoIsTheBot-frontend](https://github.com/Kippzta/WhoIsTheBot-frontend)

---

## Teknik

- Quarkus, Java 21
- Hibernate ORM med Panache
- PostgreSQL
- WebSockets Next
- Quarkus LangChain4j med OpenAI
- JUnit och Mockito för tester

---

## Köra projektet lokalt

### Krav
- Java 21
- [Docker Desktop](https://www.docker.com/products/docker-desktop/), igång. Quarkus Dev Services startar en PostgreSQL-databas i Docker automatiskt.
- En OpenAI API-nyckel

### 1. Klona repot

```bash
git clone https://github.com/Kippzta/WhoIsTheBot-backend.git
cd WhoIsTheBot-backend
```

### 2. Lägg in OpenAI-nyckeln

Kopiera exempelfilen och döp den till `.env` och fyll i din riktiga nyckel där:

```bash
cp .env.example .env
```

`.env` finns i `.gitignore`, så nyckeln hamnar aldrig på GitHub.

### 3. Starta

```bash
./mvnw quarkus:dev
```

Backenden körs på http://localhost:8080. Databasen skapas automatiskt, och frågorna i `src/main/resources/import.sql` läses in.

Starta sedan frontenden enligt [frontend-README:n](https://github.com/Kippzta/WhoIsTheBot-frontend).

### Köra testerna

```bash
./mvnw test
```

---

## API

### REST

| Metod | Adress | Body | Svar |
|---|---|---|---|
| POST | `/api/rooms/create` | `{ "username": "Erik" }` | `201`, spelarens `playerId` och rummet |
| POST | `/api/rooms/join` | `{ "username": "Anna", "roomCode": "ABCDE" }` | `201`, spelarens `playerId` och rummet |

### WebSocket

```
/ws/rooms/{roomCode}/{playerId}
```

Vid anslutning läser backenden rumskoden och `playerId` från adressen, och kontrollerar att spelaren finns och tillhör rummet.
Annars stängs anslutningen. Alla meddelanden skickas som `{ "type": ..., "data": ... }`. Se tabellen över meddelandetyper i [frontend-README:n](https://github.com/Kippzta/WhoIsTheBot-frontend).

---

## Struktur

```
src/main/java/org/whoisthebot/
├── ai/           BotAiService: prompten till OpenAI
├── DTOs/         det som skickas till och från frontenden
├── entity/       GameRoom, Player, ChatMessage, Question
├── enums/        GameStatus, WebSocketMessageType
├── generator/    RoomCodeGenerator, AliasGenerator
├── repository/   Panache-repositories
├── resource/     REST-endpoints
├── service/      GameRoomService, ChatService, BotService, VoteService
└── websocket/    GameRoomSocket
```

| Service | Ansvar |
|---|---|
| `GameRoomService` | skapa, gå med, starta spelet, avsluta chatten |
| `ChatService` | kontrollera och spara chattmeddelanden |
| `BotService` | låta boten svara utifrån de senaste meddelandena |
| `VoteService` | ta emot röster och räkna ut resultatet |

---

## Drift

Backenden körs på **Render** med Docker (se `Dockerfile`), och databasen på **Neon**. Följande miljövariabler sätts i Render:

| Variabel | Beskrivning |
|---|---|
| `DB_URL` | JDBC-adressen till databasen på Neon |
| `DB_USERNAME` | databasens användarnamn |
| `DB_PASSWORD` | databasens lösenord |
| `FRONTEND_URL` | frontendens adress, för CORS |
| `OPENAI_API_KEY` | nyckeln till OpenAI |

Render sätter `PORT` automatiskt. Inställningarna läses av `%prod`-raderna i `application.properties`.

> Databasen byggs om vid varje start (`drop-and-create`), så rum och chattar försvinner när backenden startas om.

---

## Användning av generativ AI

Se [frontend-README:n](https://github.com/Kippzta/WhoIsTheBot-frontend#användning-av-generativ-ai).