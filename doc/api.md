<link href="https://raw.github.com/simonlc/Markdown-CSS/master/markdown.css" rel="stylesheet"></link>

# Prizeword api

Status codes in use: 200, 201, 302, 401, 403, 404, 500.

# Authentication

Set of endpoints for registering and authecating user. There are two major work flows. First one uses social networks with ouath2 protocol (currently Facebook and vkontakte are supported). Second &mdash; simple registration, log in and forgot password flow.

## OAUTH2 endpoints

Api uses oauth [Web Server Flow](http://tools.ietf.org/html/draft-ietf-oauth-v2-02#section-3.5.2) for registering and authorizing users.

**`GET /:provider_name/login`**

This action returns redirect URL pointing to Login Dialog Url for `:provider_name`. Client application must show this page in browser window (Webview in case of IOS application). After user authorizes, provider will redirect him to `/:provider_name/authorize`. Application should call this endpoint to obtain session token.

*Request:*

- `provider_name`: Authentication provider name (facebook, vkontakte)

*Response:*

302 redirect page to providers Login Dialog url page.

**`GET /:provider_name/authorize`**

Endpoint for final step in OAUTH2 authorization process. Web server will get access token using `code` and return session token.

*Request:*

- `provider_name`: Authentication provider name (facebook, vkontakte)
- `code`: Authorization code from OAUTH provider

*Response:*

JSON object, representing containing session token and user information:

    {
      "session_token": "dS435wsjdDF23d2K",
      "me": {
        "name": "Sherlock",
        "surname": "Holmes",
        "userpic_url": null,
        "email": "sherlock@example.com",
        "birthdate": null,
        "city": "London",
        "solved": 120,
        "position": 2,
        "month_score": 1124,
        "high_score": 10124,
        "dynamics": 1,
        "hints": 8
      }
    }

**`GET /:provider_name/friends`**

List user's friends in provided social network.

*Request:*

- `session_token`: Session token
- `provider_name`: Authentication provider name (facebook, vkontakte)

*Response:*

If user user has no access token for given network this request will return 403 response code with json:

    {
      "error": "NotConnectedError",
      "message": "User not connected with Facebook"
    }

If user has valid access token, then it will return list of friends:

    {
      "friends": [
        {
          "provider": "facebook",
          "id": "32422",
          "name": "Irene Adler",
          "email": "irene@example.com",
          "invite_sent": true,
          "invite_used": true,
          "invited_at": "2004-12-21",
          "userpic_url": "http://example.com/irene.png"
        },
        {
          "provider": "facebook",
          "id": "232",
          "name": "Mrs. Hudson",
          "email": "hudson@example.com",
          "invite_sent": false,
          "invite_used": false,
          "invited_at": "2004-12-21",
          "userpic_url": "http://example.com/hudson.png"
        },
        {
          "provider": "facebook",
          "id": "1003"
          "name": "G. Lestrade",
          "email": "lestrade@example.com",
          "invite_sent": false,
          "invite_used": false,
          "invited_at": "2004-12-21",
          "userpic_url": "http://example.com/lestrade.png"
        }
      ]
    }

**`PUT /:provider_name/invite`**

Send invite to user

*Request:*

- `session_token`: Session token
- `provider_name`: Authentication provider name (facebook, vkontakte)
- `ids`: Coma-separated list if user ids, e.g. "1003,232"

*Response:*

201 response in case of success.

## Simple flow

This is straight forward registration flow. User can register in application using `/signup` endpoint, restore password

**`POST /signup`**

If userpic is included in request, then whole request should be encoded with `multipart/form-data`.

*Request:*

- `email`: Email address
- `name`: Name
- `surname`: Surname
- `password`: Password 4 to 16 symbols
- `[birthdate]`: Birthdate in ISO 8601 format
- `[userpic]`: image data
- `[city]`: city

*Response:*

JSON object, representing containing session token and user information:

    {
      "session_token": "dS435wsjdDF23d2K",
      "me": {
        "name": "Sherlock",
        "surname": "Holmes",
        "userpic_url": null,
        "email": "sherlock@example.com",
        "birthdate": null,
        "city": "London",
        "position": 2,
        "solved": 120,
        "month_score": 1124,
        "high_score": 10124,
        "dynamics": 1,
        "hints": 8
      }
    }

**`POST /login`**

*Request:*

- `email`: Email address
- `password`: Password

JSON object, representing containing session token and user information:

    {
      "session_token": "dS435wsjdDF23d2K",
      "me": {
        "name": "Sherlock",
        "surname": "Holmes",
        "userpic_url": null,
        "email": "sherlock@example.com",
        "birthdate": null,
        "city": "London",
        "solved": 120,
        "position": 2,
        "month_score": 1124,
        "high_score": 10124,
        "dynamics": 1,
        "hints": 8
      }
    }

## Common authorization endpoints

**`POST /forgot_password`**

It will trigger send password reset link to user email.

*Request:*

- `email`: Email address

*Response:*

201 in case of success, 404 if email is not registered.

**`GET /password_reset`**

Html page for password reset. Link to it will be mailed in within `POST /forgot_password` request. This technically is not part of API, but included here to provide full picture.

*Request:*

- `token`: password reset token, automatically generated token for password reset.

*Response:*

404 code if token is not found or if token is already used and 200 for valid token.

**`POST /password_reset`**

This post request will be triggered by password reset form. It will change user password.

*Request:*

- `token`: password reset token, automatically generated token for password reset.
- `password`: new password for user.

*Response:*

404 code if token is not found or if token is already used, 403 if `password` field is empty and 200 for valid token.

**`GET /me`**

Get basic info for logged in user.

*Request:*

- `session_token`: Session token.

*Response:*

    {
      "me": {
        "name": "Sherlock",
        "surname": "Holmes",
        "userpic_url": null,
        "email": "sherlock@example.com",
        "birthdate": null,
        "city": "London",
        "solved": 120,
        "position": 2,
        "month_score": 1124,
        "high_score": 10124,
        "dynamics": 1,
        "hints": 8
      }
    }

**`POST /me`**

Update basic information. If userpic is included in request, then whole request should be encoded with `multipart/form-data`.

*Request:*

- `session_token`: Session token
- `[name]`: Name
- `[password]`: Name
- `[surname]`": Surname
- `[userpic]`: Base64 encoded userpic
- `[email]`: Email
- `[birthdate]`: Birthdate in ISO 8601 format
- `[city]`: City

*Response:*

    {
      "me": {
        "name": "Sherlock",
        "surname": "Holmes",
        "userpic_url": null,
        "email": "sherlock@example.com",
        "birthdate": null,
        "city": "London",
        "solved": 120,
        "position": 2,
        "month_score": 1124,
        "high_score": 10124,
        "dynamics": 1,
        "hints": 8
      }
    }

# Users

**`GET /users`**

Set of routes describing information about users.

*Request:*

- `[session_token]`: if session token for current user, response will include scoring information about user.
- `[page]`: paginate collection of users.

*Response:*

    {
      "me": {
        "name": "Sherlock",
        "surname": "Holmes",
        "userpic_url": null,
        "email": "sherlock@example.com",
        "birthdate": null,
        "city": "London",
        "solved": 120,
        "position": 2,
        "month_score": 1124,
        "high_score": 10124,
        "dynamics": 1,
        "hints": 8
      },
      "users": [
        {
          "name": "James",
          "surname": "Moriarty",
          "userpic_url": "http://example.org/userpic.png",
          "email": "james@example.com",
          "birthdate": "1860-11-03",
          "city": "London",
          "solved": 130,
          "position": 1,
          "month_score": 1324,
          "high_score": 99997,
          "dynamics": -2
        },
        {
          "name": "Sherlock",
          "surname": "Holmes",
          "userpic_url": null,
          "email": "sherlock@example.com",
          "birthdate": null,
          "city": "London",
          "solved": 120,
          "position": 2,
          "month_score": 1124,
          "high_score": 10124,
          "dynamics": 1
        },
        {
          "name": "John",
          "surname": "Watson",
          "userpic_url": null,
          "email": "john@example.com",
          "birthdate": "1867-04-01",
          "city": "London",
          "solved": 10,
          "position": 3,
          "month_score": 340,
          "high_score": 340,
          "dynamics": 0
        }]
    }

# Puzzle

**`GET /puzzles`**

List puzzles for given month and their status, if month is not present, show information about current month.


*Request:*

- `session_token`: Session token.
- `[year]`: Year
- `[month]`: Month

*Response:*

    {
      "score": 23180,
      "sets": [
        {
          "id": "23",
          "name": "1rst Golden Set",
          "bought": true,
          "type": "golden",
          "solved": 3,
          "total": 12,
          "percent": 20,
          "score": 23000,
          "puzzles": [
            {
              "name": "A Study in Scarlet",
              "issuedAt": "2012-03-05",
              "set_id": "23",
              "progress": 100,
              "base_score": 100,
              "time_given": 60000,
              "time_left": 30001,
              "data": {...},
              "score": 1000,
              "solved": true
            },
            {
              "name": "The Adventure of the Greek Interpreter",
              "issuedAt": "2012-23-04",
              "set_id": "23",
              "progress": 100,
              "base_score": 150,
              "time_given": 60080,
              "time_left": 30001,
              "data": {...},
              "score": 1100,
              "solved": true
            },
            ...
          ]
        },
        {
          "id": 2233,
          "name": "Free set",
          "type": "free",
          "bought": true,
          "solved": 7,
          "total": 7,
          "percent": 100,
          "score": 80,
          "puzzles": [
            {
              "name": "The Sign of the Four",
              "issuedAt": "2012-03-05",
              "set_id": "2233",
              "progress": 100,
              "base_score": 100,
              "time_given": 60000,
              "time_left": 30001,
              "data": {...},
              "score": 1000,
              "solved": true
            },
            {
              "name": "The Adventure of the Crooked Man",
              "issuedAt": "2012-23-04",
              "set_id": "2233",
              "progress": 100,
              "base_score": 150,
              "time_given": 60080,
              "time_left": 30001,
              "data": {...},
              "score": 1100,
              "solved": true
            },
            ...
          ]
        }
      ]
    }

Three dots (...) in documentation mean that part of payload was skipped to keep clarity.

**`PUT /puzzles/:id`**

Method for storing arbitrary data, about given puzzle.

*Request:*

- `id`: Id of the puzzle
-  all post data for this request will be saved.

*Response:*

    { "message": "ok" }

**`GET /puzzles/:id`**

Method for retrieving data stored with `PUT /puzzles/"id`.

*Request:*

- `id`: Id of the puzzle

*Response:*

Data stored with `PUT /puzzles/"id`.

**`GET /sets_available`**

Sets available sets for buying (shows this month only)

*Request:*

No parameters available.

*Response:*

    [
      {
       "id":"7e4e7bc7-a484-4b67-8c4c-1727f041047a",
        "puzzles":[{"name":"puzzle1"},{"name":"puzzle2"}],
        "month":12,
        "year":2012,
        "moth":10,
        "name":"Cool set",
        "type":"golden"
      }
    ]

**`POST /sets/:id/buy`**

Buy set.

*Request:*

- `id`: Id of set to buy
- `receipt-data`: receipt data from itunes

*Response:*

    {
      "score": 23180,
      "sets": [
        {
          "id": "23",
          "name": "1rst Golden Set",
          "bought": true,
          "type": "golden",
          "solved": 3,
          "total": 12,
          "percent": 20,
          "score": 23000,
          "puzzles": [
            {
              "name": "A Study in Scarlet",
              "issuedAt": "2012-03-05",
              "set_id": "23",
              "progress": 100,
              "base_score": 100,
              "time_given": 60000,
              "time_left": 30001,
              "data": {...},
              "score": 1000,
              "solved": true
            },
            {
              "name": "The Adventure of the Greek Interpreter",
              "issuedAt": "2012-23-04",
              "set_id": "23",
              "progress": 100,
              "base_score": 150,
              "time_given": 60080,
              "time_left": 30001,
              "data": {...},
              "score": 1100,
              "solved": true
            },
            ...
          ]
        },
        {
          "id": 2233,
          "name": "Free set",
          "type": "free",
          "bought": true,
          "solved": 7,
          "total": 7,
          "percent": 100,
          "score": 80,
          "puzzles": [
            {
              "name": "The Sign of the Four",
              "issuedAt": "2012-03-05",
              "set_id": "2233",
              "progress": 100,
              "base_score": 100,
              "time_given": 60000,
              "time_left": 30001,
              "data": {...},
              "score": 1000,
              "solved": true
            },
            {
              "name": "The Adventure of the Crooked Man",
              "issuedAt": "2012-23-04",
              "set_id": "2233",
              "progress": 100,
              "base_score": 150,
              "time_given": 60080,
              "time_left": 30001,
              "data": {...},
              "score": 1100,
              "solved": true
            },
            ...
          ]
        }
      ]
    }

Three dots (...) in documentation mean that part of payload was skipped to keep clarity.

**`POST /hints`**

Add or removes hints from user.


*Request:*

- `session_token`: Session token.
- `[hints_change]`: amount to change.

*Response:*

    {
      "me": {
        "name": "Sherlock",
        "surname": "Holmes",
        "userpic_url": null,
        "email": "sherlock@example.com",
        "birthdate": null,
        "city": "London",
        "solved": 120,
        "position": 2,
        "month_score": 1124,
        "high_score": 10124,
        "dynamics": 1,
        "hints": 8
      }
    }

**`POST /score`**

Add or removes points from user score. This endpoint can be used score for activities other than puzzle solving (inviting friends, achivements, ect.)

*Request:*

- `session_token`: Session token.
- `[score]`: Amount to change score.
- `[solved]`: Amount to change puzzles solved.

*Response:*

    {
      "me": {
        "name": "Sherlock",
        "surname": "Holmes",
        "userpic_url": null,
        "email": "sherlock@example.com",
        "birthdate": null,
        "city": "London",
        "solved": 120,
        "position": 2,
        "month_score": 1124,
        "high_score": 10124,
        "dynamics": 1,
        "hints": 8
      }
    }

## Admin endpoints

**`GET /sets`**

**`POST /sets`**

**`PUT /sets/:id`**

**`DELETE /sets/:id`**

**`PUT /service_message`**

**`DELETE /service_message`**

## Puzzle data format

Puzzle data is stored as json object. It has following properties:

- `id`: Id of crossword
- `set_id`: Id of the crossword set
- `name`: Name of the crossword
- `issuedAt`: Date of the issue
- `base_score`: Score for solving puzzle without time bonuses.
- `time_given`: Time in seconds to solve this puzzle
- `height`: height of the field in cells
- `width`: width of the field in cells
- `questions`: array of `question` objects

Each `question` can have one of following properties:

- `column`: column number for question cell
- `row`: row number for question cell
- `question_text`: text of the question
- `answer`: answer to the question, lower case.
- `answer_position`: position of answer relative to cell, can be one of the following:
  `north:left`, `north:top`, `north:right`, `north-east:left`,
  `north-east:bottom`, `east:top`, `east:right`, `east:bottom`,
  `south-east:left`, `south-east:top`, `south:left`, `south:right`,
  `south:bottom`, `south-west:top`, `south-west:right`, `west:left`,
  `west:top`, `west:bottom`, `north-west:right`, `north-west:bottom`.

Example:

    {
      "id": "2dsf3",
      "set_id": "2342",
      "name": "Simple one",
      "issuedAt": "2012-23-04",
      "base_score": 100,
      "time_given": 60000,
      "height": 3,
      "width": 9,
      "questions": [
        {
          "column": 1,
          "row": 1,
          "width": 1,
          "height": 1,
          "question_text": "Inference in which the conclusion is of no greater generality than the premises",
          "answer": "deduction",
          "answer_position": "east:right"
        },
        {
          "column": 3,
          "row": 2,
          "width": 1,
          "height": 1,
          "question_text": "A small weapon",
          "answer": "knive",
          "answer_position": "south:right"
        }
      ]
    }


