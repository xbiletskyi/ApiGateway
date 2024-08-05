## Endpoints
### 1. Start trip search
#### URL 
`POST /gateway/api/v1/trips`
#### Parameters
| Name               | Type    | Required | Default    | Description                                                |
|--------------------|---------|----------|------------|------------------------------------------------------------|
| `jwtToken`         | String  | Yes      | None       | Authorization token                                        |
| `origin`           | String  | Yes      | None       | The IATA code of the departure airport                     |
| `destination`      | String  | No       | `origin`   | The IATA code of the destination airport                   |
| `departureAt`      | String  | Yes      | None       | The date to start the trip in the format `yyyy-MM-dd`      |
| `returnBefore`     | String  | No       | 3000-01-01 | The date to end the trip before in the format `yyyy-MM-dd` |
| `budget`           | double  | Yes      | None       | Maximum amount of money spent on trips                     |
| `maxStay`          | int     | No       | 1          | Maximum days between two flights                           |
| `minStay`          | int     | No       | 1          | Minimum days between two flights                           |
| `schengenOnly`     | boolean | No       | false      | If `true`, only includes flights within the Schengen Area  |
| `excludedCities`   | List<String> | No | None | Cities excluded from visiting |
| `timeLimitSeconds` | int     | No       | 10         | Working time for an algorithm to find feasible trips       |

#### Responses 
- **200 OK**
  - **Description**: Successfully added the request to queue

- **400 Bad request**
  - **Description**: Invalid parameters

- **401 Unauthorized**
  - **Description**: Authorization failed

- **409 Conflict**
  - **Description**: User already has a request in the queue

#### Example request
```bash
curl -X POST "/gateway/api/v1/trips" \
-H "Content-Type: application/json" \
-d '{
  "origin": "BTS",
  "destination": "BTS",
  "departureAt": "2024-07-28",
  "budget": 200,
  "maxStay": 2,
  "excludedCities": ["VIE", "BGY", "MLP"]
}'
```

### 2. Cancel trip search

#### URL
`DELETE /gateway/api/v1/trips`

#### Parameters
| Name               | Type    | Required | Default    | Description                                                |
|--------------------|---------|----------|------------|------------------------------------------------------------|
| `jwtToken`         | String  | Yes      | None       | Authorization token                                        |

#### Responses
- **200 OK**
  - **Description**: Request successfully removed

- **401 Unauthorized**
    - **Description**: Authorization failed

- **404 Not found**
  - **Description**: No request found for the user
  
#### Example request
```bash
curl -X DELETE "/gateway/api/v1/trips"
```

### 3. Retrieve queue position
#### URL
`GET /gateway/api/v1/trips/position`

#### Parameters
| Name               | Type    | Required | Default    | Description                                                |
|--------------------|---------|----------|------------|------------------------------------------------------------|
| `jwtToken`         | String  | Yes      | None       | Authorization token                                        |

#### Responses
- **200 OK**
  - **Description**: The user's request is found in the queue
  - **Body**:
    - ```json
        "rank": "int"
    ```

- **401 Unauthorized**
    - **Description**: Authorization failed

- **404 Not found**
  - **Description**: No request found for the user

### 4. Retrieve all results' description
#### URL
`GET /gateway/api/v1/trips`

#### Parameters
| Name               | Type    | Required | Default    | Description                                                |
|--------------------|---------|----------|------------|------------------------------------------------------------|
| `jwtToken`         | String  | Yes      | None       | Authorization token                                        |

#### Responses 
- **200 OK**
  - **Description**: At least one result was found
  - **Body**:
    - ```json
        [
            [
          {
            "totalPrice": "double",
            "totalFlights": "int",
            "uniqueCities": "int",
            "uniqueCountries": "int",
            "departureAt": "ISO 8601 date-time",
            "arrivalAt": "ISO 8601 date-time",
            "searchId: "UUID"
          },
          ...
        ],
      ...
      ]
    ```
    
- **401 Unauthorized**
  - **Description**: Authorization failed

- **404 Not found**
  - **Description**: No results were found for the user

#### Example request
```bash
curl -X GET "/gateway/api/v1/trips"
```

### 5. Retrieve the result
#### URL
`GET /gateway/api/v1/trips/{tripId}`

#### Parameters
| Name               | Type    | Required | Default    | Description                                                |
|--------------------|---------|----------|------------|------------------------------------------------------------|
| `jwtToken`         | String  | Yes      | None       | Authorization token                                        |

#### Responses 
- **200 OK**
  - **Description**: The result is found
  - **Body**:
    - ```json
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
    
### 6. Retrieve the notifications
#### URL
`GET /gateway/api/v1/notification`

#### Parameters
| Name               | Type    | Required | Default    | Description                                                |
|--------------------|---------|----------|------------|------------------------------------------------------------|
| `jwtToken`         | String  | Yes      | None       | Authorization token                                        |

#### Responses
- **200 OK**
  - **Description**: There are new notifications for the user
  - **Body**: 
    - ```json
        [
          {
            "topic": "String"
            "body": "div"
          },
          ...
      ]
    ```

- **401 Unauthorized**
    - **Description**: Authorization failed

- **404 Not found**
  - **Description**: There are no new notifications for the user

### 7. Create user
#### URL
`POST /gateway/api/v1/register`

#### Responses
- **200 OK**
  - **Description**: User registered successfully 

- **400 Bad request**: 
  - **Description**: Username is already taken

### 8. Log in
#### URL
`POST /gateway/api/v1/login`

#### Responses
- **200 OK**
  - **Description**: The user successfully logged in
  - **Body**: 
    - ```json
        "accessToken": "JWT",
        "refreshToken": "JWT"
    ```
    
- **400 Bad request**
  - **Description**: Invalid username or password

### 9. Log out
#### URL
`POST /gateway/api/v1/logout`

#### Parameters
| Name               | Type    | Required | Default    | Description                                                |
|--------------------|---------|----------|------------|------------------------------------------------------------|
| `jwtToken`         | String  | Yes      | None       | Authorization token                                        |

#### Responses
- **200 OK**
  - **Description**: User logged out successfully 

- **400 Bad request**
  - **Description**: Invalid JWT token
  
- **401 Unauthorized**
  - **Description**: Authorization failed

### 10. Refresh access token
#### URL
`POST /gateway/api/v1/refresh`

#### Parameters
| Name           | Type    | Required | Default    | Description   |
|----------------|---------|----------|------------|---------------|
| `refreshToken` | String  | Yes      | None       | Refresh token |

- **200 OK**:
  - **Description**: Access token was successfully updated
  - **Body**:
    - ```json
        "accessToken": "JWT",
        "refreshToken": "JWT"
    ```
    
- **400 Bad request**
  - **Description**: Invalid refresh token

- **401 Unauthorized**
    - **Description**: Authorization failed
