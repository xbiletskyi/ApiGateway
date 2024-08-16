# Gateway Microservice

## Introduction
The 'Gateway' microservices is part of a larger project SkyVoyager. This documentation covers the technical details of this microservice only.  
[The overall project description](https://medium.com/@vidime.sa.buduci.rok/explore-europe-by-plane-using-this-tool-0cb52ac69b8b).

## Purpose Overview
The microservice handles any request's authorization and queue for trip finding requests.
It also ensures correct parameters being transmitted to other services.

## Technologies used
- Java 21
- Spring Boot
- Gradle
- Redis
- RabbitMQ

## Architecture design
![Architecture design](./images/GatewayService.jpg)
### Description
The architecture design may appear a bit complicated but here is the explanation of the bottom part:  
- **Trip Request Service** is responsible for queuing trips to RabbitMQ (sends the request as a message) and storing additional information to Redis to ensure order and some extendable functionality
- **Redis Database** stores cached details about each request which provides an ability to remove or modify requests after queuing
- **Rabbit MQ** receives trip requests as messages and send them in the right order to Trip Request Listener as its only subscriber
- **Trip Request Listener** receives the requests and forwards them to Trip Service
- **Trip Request Validator** serves to reject inadequate requests before processing them through the queue  
*All other requests are forwarded to corresponding services without additional logic however some internal modifications are still applied*

## Endpoints
### 1. Start trip search
#### URL 
`POST /gateway/api/v1/trips`
#### Parameters (Body)
| Name               | Type         | Required | Default    | Description                                                |
|--------------------|--------------|----------|------------|------------------------------------------------------------|
| `origin`           | String       | Yes      | None       | The IATA code of the departure airport                     |
| `destination`      | String       | No       | `origin`   | The IATA code of the destination airport                   |
| `departureAt`      | String       | Yes      | None       | The date to start the trip in the format `yyyy-MM-dd`      |
| `returnBefore`     | String       | No       | 3000-01-01 | The date to end the trip before in the format `yyyy-MM-dd` |
| `budget`           | double       | Yes      | None       | Maximum amount of money spent on trips                     |
| `maxStay`          | int          | No       | 1          | Maximum days between two flights                           |
| `minStay`          | int          | No       | 1          | Minimum days between two flights                           |
| `schengenOnly`     | boolean      | No       | false      | If `true`, only includes flights within the Schengen Area  |
| `excludedAirports` | List<String> | No       | None       | Airports excluded from visiting                            |
| `timeLimitSeconds` | int          | No       | 10         | Working time for an algorithm to find feasible trips       |
#### Authorization header
| Name            | Type | Format         | Description                            |
|-----------------|------|----------------|----------------------------------------|
| `Authorization` | JWT  | Bearer <token> | Necessary authorization with JWT token |
#### Responses 
- **200 OK**
  - **Description**: Successfully added the request to queue
  - **Body**: `null`
- **400 Bad request**
  - **Description**: Invalid parameters
  - **Body**: `null`
- **401 Unauthorized**
  - **Description**: Authorization failed
  - **Body**: `null`
- **409 Conflict**
  - **Description**: User already has a request in the queue
  - **Body**: `null`
#### Example request
```bash
curl -X POST http://localhost:8080/gateway/api/v1/trips \
-H "Authorization: Bearer <token>" \
-H "Content-Type: application/json" \
-d '{
    "origin": "BTS",
    "departureAt": "2024-10-10",
    "budget": 300,
    "timeLimitSeconds": 300,
    "schengenOnly": "true"
}'
```

### 2. Cancel trip search
#### URL
`DELETE /gateway/api/v1/trips`
#### Authorization header
| Name            | Type | Format         | Description                            |
|-----------------|------|----------------|----------------------------------------|
| `Authorization` | JWT  | Bearer <token> | Necessary authorization with JWT token |
#### Responses
- **200 OK**
  - **Description**: Request successfully removed
  - **Body**: `null`
- **401 Unauthorized**
  - **Description**: Authorization failed
  - **Body**: `null`
- **404 Not found**
  - **Description**: No request found for the user
  - **Body**: `null`
#### Example request
```bash
curl -X DELETE "/gateway/api/v1/trips" \
-H "Authorization: Bearer <token>"
```

### 3. Retrieve queue position
#### URL
`GET /gateway/api/v1/trips/position`
#### Authorization header
| Name            | Type | Format         | Description                            |
|-----------------|------|----------------|----------------------------------------|
| `Authorization` | JWT  | Bearer <token> | Necessary authorization with JWT token |
#### Responses
- **200 OK**
  - **Description**: The user's request is found in the queue
    - **Body**:
      ```text
      "int"
      ```
- **401 Unauthorized**
  - **Description**: Authorization failed
  - **Body**: `null`
- **404 Not found**
  - **Description**: No request found for the user
  - **Body**: `null`
#### Example request
```bash
curl -X GET "/gateway/api/v1/trips/position" \
-H "Authorization: Bearer <token>"
```
#### Example successful response
```text
1
```

### 4. Retrieve the results 
#### URL
`GET /gateway/api/v1/trips`
#### Authorization header
| Name            | Type | Format         | Description                            |
|-----------------|------|----------------|----------------------------------------|
| `Authorization` | JWT  | Bearer <token> | Necessary authorization with JWT token |
#### Responses 
- **200 OK**
  - **Description**: At least one result was found
  - **Body**:
  ```json
  [
    {
      "totalPrice": "double",
      "totalFlights": "int",
      "uniqueCities": "int",
      "uniqueCountries": "int",
      "departureAt": "ISO 8601 date-time",
      "arrivalAt": "ISO 8601 date-time",
      "requestId": "UUID",
      "tripSchedule": 
      [
        {
          "flightNumber": "string (IATA airline designator followed by numeric identifier)",
          "departureAt": "ISO 8601 date-time",
          "originAirportName": "String",
          "originAirportCode": "IATA code",
          "originCountryCode": "ISO 3166 country code",
          "destinationAirportName": "String",
          "destinationAirportCode": "IATA code",
          "destinationCountryCode": "ISO 3166 country code",
          "price": "double",
          "currencyCode": "ISO 4217 currency code"
        },
        ...
      ]
    },
    ...
  ]
  ```
- **401 Unauthorized**
  - **Description**: Authorization failed
  - **Body**: `null`
- **404 Not found**
  - **Description**: No results were found for the user
  - **Body**: `null`
#### Example request
```bash
curl -X GET "/gateway/api/v1/trips" \
-H "Authorization: Bearer <token>"
```
#### Example successful response
```json
[
  {
    "totalPrice": 171.96,
    "totalFlights": 3,
    "uniqueCities": 3,
    "uniqueCountries": 3,
    "departureAt": "2024-09-11T05:55:00",
    "arrivalAt": "2024-09-13T12:05:00",
    "requestId": "61ac0848-f96c-4b23-acaf-b0d87e664a0d",
    "tripSchedule": 
    [
      {
        "flightNumber": "FR1055",
        "departureAt": "2024-09-12T15:10:00",
        "originAirportName": "Brussels Charleroi",
        "originAirportCode": "CRL",
        "originCountryCode": "be",
        "destinationAirportName": "Warsaw Modlin",
        "destinationAirportCode": "WMI",
        "destinationCountryCode": "pl",
        "price": 14.99,
        "currencyCode": "EUR"
      },
      {
        "flightNumber": "FR87",
        "departureAt": "2024-09-11T05:55:00",
        "originAirportName": "Vienna",
        "originAirportCode": "VIE",
        "originCountryCode": "at",
        "destinationAirportName": "Brussels Charleroi",
        "destinationAirportCode": "CRL",
        "destinationCountryCode": "be",
        "price": 14.99,
        "currencyCode": "EUR"
      },
      ...
    ]
  },
  ...
]
```

### 5. Retrieve the result
#### URL
`GET /gateway/api/v1/trips/{requestId}`
#### Parameters (Query)
| Name        | Type | Required | Default    | Description                                             |
|-------------|------|----------|------------|---------------------------------------------------------|
| `requestId` | UUID | Yes      | None       | Unique request identifier previously issued by the user |
#### Authorization header
| Name            | Type | Format         | Description                            |
|-----------------|------|----------------|----------------------------------------|
| `Authorization` | JWT  | Bearer <token> | Necessary authorization with JWT token |
#### Responses 
- **200 OK**
  - **Description**: At least one trip was found
  - **Body**:
    ```json
        [
          {
            "totalPrice": "double",
            "totalFlights": "int",
            "uniqueCities": "int",
            "uniqueCountries": "int",
            "departureAt": "ISO 8601 date-time",
            "arrivalAt": "ISO 8601 date-time",
            "tripSchedule": [
              {
                "flightNumber": "string (IATA airline designator followed by numeric identifier)",
                "departureAt": "ISO 8601 date-time",
                "originAirportName": "String",
                "originAirportCode": "IATA code",
                "originCountryCode": "ISO 3166 country code",
                "destinationAirportName": "String",
                "destinationAirportCode": "IATA code",
                "destinationCountryCode": "ISO 3166 country code",
                "price": "double",
                "currencyCode": "ISO 4217 currency code"
              }
              ...
            ]
          }
          ...
        ]
    ```
- **204 No Content**
  - **Description**: No 
  - **Body**: `null`
- **401 Unauthorized**
  - **Description**: Authorization failed
  - **Body**: `null`
#### Example request
```bash
curl -X GET http://localhost:8080/gateway/api/v1/trips/<id> \
-H "Authorization: Bearer <token>"
```
#### Example successful response
```json
[
  {
    "totalPrice": 145.51,
    "totalFlights": 7,
    "uniqueCities": 7,
    "uniqueCountries": 6,
    "departureAt": "2024-10-11T11:30:00",
    "arrivalAt": "2024-10-17T05:45:00",
    "requestId": "4a505846-7656-43d2-a463-723d5c822c4d",
    "tripSchedule": 
    [
      {
        "flightNumber": "FR7163",
        "departureAt": "2024-10-13T22:10:00",
        "originAirportName": "Paris Beauvais",
        "originAirportCode": "BVA",
        "originCountryCode": "fr",
        "destinationAirportName": "Barcelona Girona",
        "destinationAirportCode": "GRO",
        "destinationCountryCode": "es",
        "price": 21.74,
        "currencyCode": "EUR"
      },
      ...
    ]
  },
  ...
]
```

### 6. Retrieve requests
#### URL
`GET /gateway/api/v1/trips/requests`
#### Authorization header
| Name            | Type | Format         | Description                            |
|-----------------|------|----------------|----------------------------------------|
| `Authorization` | JWT  | Bearer <token> | Necessary authorization with JWT token |
#### Responses
- **200 OK**
  - **Description**: At least one request was found
  - **Body**:
    ```json
     [
      {
      "userId": "UUID",
      "origin": "IATA code",
      "destination": "IATA code",
      "departureAt": "ISO 8601 date-time",
      "returnBefore": "ISO 8601 date-time",
      "budget": "double",
      "maxStay": "int",
      "minStay": "int",
      "schengenOnly": "boolean",
      "limitTimeSeconds": "int",
      "excludedAirports": "List<IATA code>"
      },
      ...
    ]
    ```
- **204 No Content**
  - **Description**: No request was found for the specified user
  - **Body**: `null`
- **401 Unauthorized**
  - **Description**: Authorization failed
  - **Body**: `null`
- **500 Internal Server Error**
  - **Description**: Data retrieval error
  - **Body**: `null`
#### Example request
```bash
curl -X GET http://localhost:8080/gateway/api/v1/trips/requests \
-H "Authorization: Bearer <token>"
```
#### Example successful response
```json
[
    {
        "origin": "BTS",
        "destination": "BTS",
        "departureAt": "2024-10-10T00:00:00",
        "returnBefore": "3000-01-01T00:00:00",
        "budget": 300.0,
        "maxStay": 1,
        "minStay": 1,
        "schengenOnly": false,
        "timeLimitSeconds": 100,
        "excludedAirports": [
            ""
        ],
        "requestId": "0b5fa0a2-cd93-4ba4-b837-1dac6fd8e4be"
    },
    ...
]
```

### 7. Get results preview
#### URL
`GET /gateway/api/v1/preview`
#### Authorization header
| Name            | Type | Format         | Description                            |
|-----------------|------|----------------|----------------------------------------|
| `Authorization` | JWT  | Bearer <token> | Necessary authorization with JWT token |
#### Responses
- **200 OK**
  - **Description**: At least one result was found
  - **Body**:
  ```json
    [
      {
        "totalPrice": "double",
        "totalFlights": "int",
        "uniqueCities": "int",
        "uniqueCountries": "int",
        "departureAt": "ISO 8601 date-time",
        "arrivalAt": "ISO 8601 date-time",
        "requestId": "UUID"
      },
      ...
    ]
  ```
- **204 No Content**
  - **Description**: No results found for the specified request/user
  - **Body**: `null`
- **401 Unauthorized**
  - **Description**: Authorization failed
  - **Body**: `null`
- **500 Internal Server Error**
  - **Description**: Data retrieval error
  - **Body**: `null`
#### Example request
```bash
curl -X GET http://localhost:8080/gateway/api/v1/trips/preview \
-H "Authorization: Bearer <token>"
```
#### Example successful response
```json
[
    {
        "totalPrice": 75.96,
        "totalFlights": 4,
        "uniqueCities": 3,
        "uniqueCountries": 2,
        "departureAt": "2024-11-11T06:00:00",
        "arrivalAt": "2024-11-14T08:15:00",
        "requestId": "d5faa357-eb00-456d-afd8-37e0ec4f1fa7"
    },
    ...
]
```

### 8. Create user
#### URL
`POST /gateway/api/v1/register`
#### Parameters (Body)
| Name       | Type   | Required | Default | Description                             |
|------------|--------|----------|---------|-----------------------------------------|
| `username` | String | Yes      | None    | Main user identifier                    |
| `password` | String | Yes      | None    | Secret password used for authentication |
| `email`    | String | Yes      | None    | User's email address                    |
#### Responses
- **200 OK**
  - **Description**: User registered successfully
  - **Body**: `null`
- **400 Bad request**: 
  - **Description**: Username is already taken
  - **Body**: `null`
- **500 Internal Server Error**
  - **Description**: Registration internal error
  - **Body**: `null`
#### Example request
```bash
curl -X POST http://localhost:8080/gateway/api/v1/register \
-d '{
    "username": "user",
    "password": "password",
    "email": "email@example.com"
}'
```

### 9. Log in
#### URL
`POST /gateway/api/v1/login`
#### Parameters (Body)
| Name       | Type   | Required | Default | Description                                  |
|------------|--------|----------|---------|----------------------------------------------|
| `username` | String | Yes      | None    | User identifier to be logged in              |
| `password` | String | Yes      | None    | User secret key to get access to its account |
#### Responses
- **200 OK**
  - **Description**: The user successfully logged in
  - **Body**: 
    ```json
        "accessToken": "JWT",
        "refreshToken": "JWT"
    ```
- **400 Bad request**
  - **Description**: Invalid username or password
  - **Body**: `null`
- **500 Internal Server Error**
  - **Description**: Login internal error
  - **Body**: `null`
#### Example request
```bash
curl -X POST http://localhost:8080/gateway/api/v1/login \
-d '{
    "username": "user",
    "password": "password"
}'
```
#### Example successful response
```json
{
    "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJleHAiOjE3MjM3MzI5MzgsInN1YiI6IjdiMDc1NGNlLWE2MTctNDgyOC04NjMxLWRmMTA2ZWU4ZDRiOSJ9.wEamla4VKspRxvf04eermw6zxE5xQ8fIn6qqaSkQkqo",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJleHAiOjE3MjM3NjgzMzgsInN1YiI6IjdiMDc1NGNlLWE2MTctNDgyOC04NjMxLWRmMTA2ZWU4ZDRiOSJ9.F-FgW5asx4ZRk5pyTv2PyXU0DV2XwcoQ5NR9B2RAnHY"
}
```

### 10. Log out
#### URL
`POST /gateway/api/v1/logout`
#### Authorization header
| Name            | Type | Format         | Description                                                        |
|-----------------|------|----------------|--------------------------------------------------------------------|
| `Authorization` | JWT  | Bearer <token> | JWT refresh token passed within authorization header to invalidate |
#### Responses
- **200 OK**
  - **Description**: User logged out successfully
  - **Body**: `null`
- **400 Bad request**
  - **Description**: Invalid JWT token
  - **Body**: `null`
- **401 Unauthorized**
  - **Description**: Authorization failed
  - **Body**: `null`
- **500 Internal Server Error**
  - **Description**: Logout internal error
  - **Body**: `null`

#### Example request
```bash
curl -X POST http://localhost:8080/gateway/api/v1/logout \
-H "Authorization: Bearer <token>"
```

### 11. Refresh access token
#### URL
`GET /gateway/api/v1/refresh`
#### Authorization header
| Name            | Type | Format         | Description                                          |
|-----------------|------|----------------|------------------------------------------------------|
| `Authorization` | JWT  | Bearer <token> | JWT refresh token passed within authorization header |
#### Responses
- **200 OK**:
  - **Description**: Access token was successfully updated
  - **Body**:
    ```json
        "accessToken": "JWT",
        "refreshToken": "JWT"
    ```
- **400 Bad request**
  - **Description**: Invalid refresh token
  - **Body**: `null`
- **401 Unauthorized**
    - **Description**: Authorization failed
    - **Body**: `null`
- **500 Internal Server Error**
  - **Description**: Refresh internal error
  - **Body**: `null`

#### Example request
```bash
curl -X GET http://localhost:8080/gateway/api/v1/refresh \
-H "Authorization: Bearer <token>"
```
#### Example successful response
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJleHAiOjE3MjM3NDkzMjUsInN1YiI6IjdiMDc1NGNlLWE2MTctNDgyOC04NjMxLWRmMTA2ZWU4ZDRiOSJ9.Ud1c4U8_fR5GCWbl5jJbrdjqxCcL-AGZpFCLOs2pGhs",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJleHAiOjE3MjM3ODUyMTIsInN1YiI6IjdiMDc1NGNlLWE2MTctNDgyOC04NjMxLWRmMTA2ZWU4ZDRiOSJ9.KpCEs8mZ1RXsH83eBqUHaJgr-rgsTvIzlm_mjdR750o"
}
```
[//]: # (### X. Retrieve the notifications)

[//]: # (#### URL)

[//]: # (`GET /gateway/api/v1/notification`)

[//]: # ()
[//]: # (#### Parameters)

[//]: # (| Name               | Type    | Required | Default    | Description                                                |)

[//]: # (|--------------------|---------|----------|------------|------------------------------------------------------------|)

[//]: # (| `jwtToken`         | String  | Yes      | None       | Authorization token                                        |)

[//]: # ()
[//]: # (#### Responses)

[//]: # (- **200 OK**)

[//]: # (  - **Description**: There are new notifications for the user)

[//]: # (  - **Body**: )

[//]: # (    - ```json)

[//]: # (        [)

[//]: # (          {)

[//]: # (            "topic": "String")

[//]: # (            "body": "div")

[//]: # (          },)

[//]: # (          ...)

[//]: # (      ])

[//]: # (    ```)

[//]: # ()
[//]: # (- **401 Unauthorized**)

[//]: # (    - **Description**: Authorization failed)

[//]: # ()
[//]: # (- **404 Not found**)

[//]: # (  - **Description**: There are no new notifications for the user)