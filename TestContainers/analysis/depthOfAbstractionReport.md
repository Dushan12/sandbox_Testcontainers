# Depth of Abstraction Analysis Report

## Overview
This report analyzes the depth of abstraction in the codebase by measuring consecutive function calls for each endpoint action.

## Endpoint Analysis

### POST /people/save

**Call Chain:**
1. `main.routes` → `savePerson(req)`
2. `savePerson` → `requestBody.asObject[Person]` + `ZIO.service[PeopleService]` + `peopleService.savePerson(person)`
3. `PeopleService.savePerson` → `personRepository.insertOne(person)`
4. `ApplicationRepository.insertOne` → `getPeopleCollection.insertOne(person.toMongoObject)`
5. `person.toMongoObject` → Document creation

**Depth: 5 levels**

```
main.routes
  └── savePerson
        ├── requestBody.asObject[Person]
        │     └── body.asString → fromJson[T]
        └── peopleService.savePerson
              └── personRepository.insertOne
                    └── getPeopleCollection.insertOne(toMongoObject)
```

### GET /people

**Call Chain:**
1. `main.routes` → `getPeople(req)`
2. `getPeople` → `ZIO.service[PeopleService]` + `peopleService.getPeople`
3. `PeopleService.getPeople` → `personRepository.getAll`
4. `ApplicationRepository.getAll` → `getPeopleCollection.find()` + `document.fromMongoObject`

**Depth: 4 levels**

```
main.routes
  └── getPeople
        └── peopleService.getPeople
              └── personRepository.getAll
                    └── getPeopleCollection.find → fromMongoObject
```

### Background Email Processing (drainingQueueAndSendMessagesWithRetry)

**Call Chain:**
1. `EmailService.drainingQueueAndSendMessagesWithRetry`
2. → `redisDatabase.acquireLock` + `personRepository.getAllEmailsPending`
3. → Loop: `personRepository.updateEmailStatus` + `sendEmail` + `personRepository.updateEmailStatus`
4. → `redisDatabase.releaseLock`

**Depth: 4 levels with internal branching**

## Analysis Summary

| Endpoint | Depth | Branching Factor |
|----------|-------|------------------|
| POST /people/save | 5 | Low |
| GET /people | 4 | Low |
| Email Processing | 4 | Medium |

## Assessment

**Positive Findings:**
- The abstraction depth is reasonable (4-5 levels)
- Clear separation of concerns: routes → service → repository → database
- Each layer has a clear responsibility

**Negative Findings:**
- Some complexity is hidden behind layers rather than compressed
- The extension pattern for `toMongoObject`/`fromMongoObject` adds abstraction without significant benefit over inline conversion

## Metric Score

**Grade: 7/10**

The codebase has a moderate depth of abstraction. The layering is logical (controller → service → repository) which is a common pattern. However, the depth could be reduced by:
1. Eliminating the service layer for simple CRUD operations
2. Using inline document conversion instead of extension methods

The abstraction is not excessive but also doesn't fully embrace "compression of complexity" where side effects would be consolidated in fewer places.
