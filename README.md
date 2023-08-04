# GameBuddy Match Service
GameBuddy Match Service is a microservice in the GameBuddy application architecture, responsible for handling matchmaking and chat functionality. This service allows users to find and connect with other gamers who share similar interests and gaming preferences. The service also facilitates real-time messaging between matched gamers with Websocket.


## APIs (Soon I will prepare the swagger document.)

### Chat APIs

##### WebSocket Chat Endpoint

- URL: /chat
- Description: Endpoint for real-time messaging between gamers.
- Request Payload: Message object containing chat details.
- Action: When a message is received, it is saved in the database, and a notification is - sent to the receiver via a WebSocket subscription.

##### GET /messages/get/{friendId}

- Description: Get conversation messages between the authenticated user and a friend.
- Request Header: Authorization (Bearer Token)
- Path Variable: friendId (The ID of the friend to fetch messages with)
- Response: ConversationResponse

##### GET /messages/get/inbox

- Description: Get the inbox messages for the authenticated user.
- Request Header: Authorization (Bearer Token)
- Response: InboxResponse

##### POST /messages/report/{messageId}

- Description: Report a chat message as inappropriate or abusive.
- Request Header: Authorization (Bearer Token)
- Path Variable: messageId (The ID of the message to report)
- Response: DefaultMessageResponse



### Matching APIs

##### GET /match/get/recommendations

- Description: Get recommended gamers for the authenticated user.
- Request Header: Authorization (Bearer Token)
- Response: RecommendationResponse

##### GET /match/get/selected/game/{gameId}

- Description: Get recommended gamers for the selected game for the authenticated user.
- Request Header: Authorization (Bearer Token)
- Path Variable: gameId (The ID of the selected game)
- Response: RecommendationResponse

##### POST /match/accept

- Description: Accept a matched gamer.
- Request Header: Authorization (Bearer Token)
- Request Body: GamerRequest object containing gamer id.
- Response: DefaultMessageResponse

##### POST /match/decline

- Description: Decline a matched gamer.
- Request Header: Authorization (Bearer Token)
- Request Body: GamerRequest object containing gamer id.
- Response: DefaultMessageResponse



## Getting Started

1. Clone the GameBuddy Match Service repository from GitHub.

2. Open the project with your preferred IDE. (Use Gradle.)

3. Configure the necessary database and messaging services (e.g., PostgreSQL, MongoDB, AI Prediction Service).
> You have to run the Gamebuddy-Model (AI Prediction) service in order to get user recommendations.

4. Update the application.yml file with the database credential.

5. Run the application using Gradle or your preferred IDE. (Initial port is 4567. You can change it from application.yml)

## Gradle Commands
To build, test, and run the GameBuddy Match Service, you can use the following Gradle commands:

### Clean And Build
To clean the build artifacts and build the project, run:

`./gradlew clean build`

> The built JAR file will be located in the build/libs/ directory.

### Test
To run the tests for your GameBuddy Match Service, you can use the following Gradle command:

`./gradlew test`

> This command will execute all the unit tests in the project. The test results will be displayed in the console, indicating which tests passed and which ones failed.

Additionally, if you want to generate test reports, you can use the following command:

`./gradlew jacocoTestReport`

> This will generate test reports using the JaCoCo plugin. The test reports can be found in the build/reports/tests and build/reports/jacoco directories. The JaCoCo report will provide code coverage information to see how much of your code is covered by the tests.

### Spotless Code Formatter
This project has Spotless rules. If the code is unformatted, building the project will generate error. To format the code according to the configured Spotless rules, run:

`./gradlew spotlessApply`

### Sonarqube Analysis
To perform a SonarQube analysis of the project, first, ensure you have SonarQube configured and running. Then, run:

`./gradlew sonarqube`

### Run 
To run the GameBuddy Match Service locally using Gradle, use the following command:

`./gradlew bootRun`

> This will start the service, and you can access the APIs at http://localhost:4567.

## Dockerizing the Project
To containerize the GameBuddy Match Service using Docker, follow the steps below:

1. Make sure you have Docker installed on your system. You can download Docker from the official website: https://www.docker.com/get-started

2. Project already has a Dockerfile. Examine the Dockerfile in the root directory of the project. The Dockerfile define the container image configuration.

3. Build the Docker image using the Dockerfile. Open a terminal and navigate to the root directory of the project.

 `docker build -t gamebuddy-match-service .`

 This will create a Docker image with the name **gamebuddy-match-service**.

4. Run the Docker container from the image you just built.

 `docker run -d -p 4567:4567 --name gamebuddy-match gamebuddy-match-service`

 This will start the GameBuddy Match Service container, and it will be accessible at http://localhost:4567.

 
## LICENSE
This project is licensed under the MIT License.
