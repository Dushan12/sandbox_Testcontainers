# N+1 Queries Analysis Report

## Overview
This report analyzes the codebase for N+1 query patterns where database queries are executed inside loops, leading to performance degradation.

## Query Pattern Analysis

### Identified N+1 Pattern

**Location: `EmailService.drainingQueueAndSendMessagesWithRetry`**

```scala
emails <- personRepository.getAllEmailsPending  // 1 query to get all emails
results <- ZIO.collectAll(emails.map { email =>
  (for {
    _ <- personRepository.updateEmailStatus(email.id, "PENDING")  // N queries
    _ <- sendEmail(email)
    _ <- personRepository.updateEmailStatus(email.id, "DONE")     // N queries
  } yield 1)
})
```

**Query Count Analysis:**
- 1 initial query: `getAllEmailsPending`
- N queries: `updateEmailStatus` to "PENDING" (once per email)
- N queries: `updateEmailStatus` to "DONE" (once per email)

**Total: 1 + 2N queries** for N emails

### Endpoint Query Analysis

| Endpoint | Query Count | Pattern |
|----------|-------------|---------|
| POST /people/save | 1 | Single insert - Good |
| GET /people | 1 | Single find - Good |
| Email Processing | 1 + 2N | N+1 Pattern - Bad |

## Detailed Analysis

### Good Patterns

**Single Insert (savePerson):**
```scala
def insertOne(person: Person): ZIO[Any, Throwable, Boolean] = {
  ZIO.succeed(getPeopleCollection.insertOne(person.toMongoObject).wasAcknowledged())
}
```
- Single database operation
- No loops involved

**Batch Fetch (getAll):**
```scala
def getAll: ZIO[Any, Throwable, immutable.List[Person]] = {
  ZIO.attempt {
    val records = getPeopleCollection.find().map { x => x.fromMongoObject }
    // ... collect results
  }
}
```
- Single query fetches all records
- Iteration is over in-memory cursor, not database calls

### Problematic Pattern

**Email Status Updates:**
```scala
emails.map { email =>
  for {
    _ <- personRepository.updateEmailStatus(email.id, "PENDING")
    _ <- sendEmail(email)
    _ <- personRepository.updateEmailStatus(email.id, "DONE")
  } yield 1
}
```

**Why this is problematic:**
1. Each email triggers 2 individual update queries
2. For 100 emails, this creates 200 update queries
3. Network latency compounds with each query

**Recommended Solution:**
```scala
// Batch update to PENDING
_ <- personRepository.updateManyEmailStatus(emails.map(_.id), "PENDING")

// Process emails
results <- ZIO.collectAll(emails.map(sendEmail))

// Batch update to DONE for successful ones
_ <- personRepository.updateManyEmailStatus(successfulIds, "DONE")
```

## Impact Assessment

| Scenario | Current (1+2N) | Optimized (3) | Savings |
|----------|----------------|---------------|---------|
| 10 emails | 21 queries | 3 queries | 86% |
| 100 emails | 201 queries | 3 queries | 99% |
| 1000 emails | 2001 queries | 3 queries | 99.9% |

## Metric Score

**Grade: 6/10**

**Positive Findings:**
- Main endpoints (save, get) use single queries
- No N+1 in the primary user-facing paths
- Bulk fetch pattern is correctly implemented

**Negative Findings:**
- Email processing has a clear N+1 pattern
- Individual updates in loop instead of bulk operations
- No use of MongoDB's `updateMany` for batch updates

**Recommendations:**
1. Implement `updateManyEmailStatus` using MongoDB's `updateMany`
2. Consider using bulk write operations for status transitions
3. Use `bulkWrite` with ordered operations if order matters
