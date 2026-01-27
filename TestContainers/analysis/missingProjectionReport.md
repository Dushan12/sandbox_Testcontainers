# Missing Projection and Unnecessary Data Load Analysis Report

## Overview
This report analyzes whether the application loads more data from the database than needed, causing unnecessary network traffic and memory usage.

## Data Loading Analysis

### GET /people Endpoint

**Repository Method:**
```scala
def getAll: ZIO[Any, Throwable, immutable.List[Person]] = {
  ZIO.attempt {
    val records = getPeopleCollection.find().map { x => x.fromMongoObject }
    // ... collect all records
  }
}
```

**What's Loaded:** All fields (`id`, `firstName`, `lastName`, `email`)

**What's Used:** All fields are serialized to JSON response

```scala
Response.json(personResults.map(_.toJson).toJson.mkString(""))
```

**Analysis:**
- ✅ All loaded fields are used in the response
- ✅ No unnecessary data loading for this endpoint

**Status: GOOD** - Full projection matches usage

### Email Processing

**Repository Method:**
```scala
def getAllEmailsPending: ZIO[Any, Throwable, List[EmailStatus]] = {
  ZIO.attempt {
    val records = getPeopleCollection.find(BsonDocument("status", BsonString("PENDING")))
      .map { x => x.fromEmailStatusMongoObject }
    // ... collect all records
  }
}
```

**What's Loaded:** All EmailStatus fields (`id`, `personId`, `requestedAt`, `status`)

**What's Used in sendEmail:**
```scala
def sendEmail(emailStatus: EmailStatus): ZIO[Any, Throwable, Unit]
```

**Analysis:**
- ⚠️ `requestedAt` may not be needed for sending
- ⚠️ `status` is already known (it's "PENDING" from the query)
- The `sendEmail` method is a stub that uses `ZIO.succeed(())`

**Status: MINOR CONCERN** - Potentially loads unused fields

### Person Model Usage

**Model:**
```scala
case class Person(id: String, firstName: String, lastName: String, email: String)
```

**Analysis by Use Case:**

| Use Case | Fields Needed | Fields Loaded | Efficiency |
|----------|---------------|---------------|------------|
| Save person | All | All | 100% |
| List people | All (for display) | All | 100% |
| Update person | id, firstName | N/A (direct update) | 100% |

## Projection Opportunities

### Current State (No Projection)
```scala
getPeopleCollection.find()  // Loads all fields
```

### With Projection (If Needed)
```scala
getPeopleCollection.find()
  .projection(Projections.include("id", "firstName"))  // Only needed fields
```

## Network Traffic Analysis

**Person Document Size:**
- id: ~36 bytes (UUID)
- firstName: ~10-20 bytes
- lastName: ~10-20 bytes  
- email: ~20-40 bytes
- **Total: ~80-120 bytes per person**

**At Scale:**
| Records | Without Projection | With Minimal Projection | Savings |
|---------|-------------------|------------------------|---------|
| 100 | ~10KB | ~5KB | 50% |
| 10,000 | ~1MB | ~500KB | 50% |
| 1,000,000 | ~100MB | ~50MB | 50% |

## DTO Reuse Analysis

The same `Person` case class is used for:
1. Request deserialization (save)
2. Database storage
3. API response (list)

**Analysis:**
- ✅ In this small codebase, the DTO serves all purposes well
- ⚠️ As requirements diverge, separate DTOs might be needed

## Metric Score

**Grade: 8/10**

**Positive Findings:**
- Current endpoints use all loaded data
- No obvious over-fetching in main use cases
- Simple model means minimal waste
- Direct field updates avoid loading objects just to modify them

**Minor Concerns:**
- No projections used in queries (loads all fields always)
- EmailStatus query loads fields that may not be needed
- No pagination on `getAll` (would load entire collection)

**Recommendations:**
1. Add pagination to `getAll` to prevent loading unbounded data:
   ```scala
   def getAll(limit: Int, offset: Int): ZIO[Any, Throwable, List[Person]]
   ```
2. Consider projections if specific views need subset of fields
3. Create separate response DTOs if API requirements diverge from storage model
4. Add indexes to support efficient queries as data grows
