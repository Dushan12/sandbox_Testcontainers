# Number of Dependencies Injected in Classes Analysis Report

## Overview
This report analyzes the number of dependencies injected into each class/service to identify potential God Objects or separation of concerns issues.

## Dependency Analysis

### Main Application

```scala
object main extends ZIOAppDefault {
  // Dependencies provided via ZLayer
}
```

**Direct Dependencies:** 
- `EmailService`
- `PeopleService` (via routes environment)

**Dependency Count: 2**

**Status: GOOD**

### PeopleService

```scala
trait PeopleService {
  val personRepository: ApplicationRepository  // 1 dependency
}
```

**Dependency Count: 1**

**Status: EXCELLENT**

### EmailService

```scala
trait EmailService {
  // No constructor dependencies (stateless)
  // Runtime dependencies in methods: RedisDatabase, ApplicationRepository
}
```

**Constructor Dependencies: 0**
**Runtime Dependencies (method level): 2**

**Status: GOOD** - Uses ZIO environment for dependencies

### RedisDatabase

```scala
trait RedisDatabase {
  val redisDatabase: Redis  // 1 dependency
}
```

**Dependency Count: 1**

**Status: EXCELLENT**

### ApplicationRepository

```scala
trait ApplicationRepository {
  val client: MongoClient       // 1 dependency
  val config: ApplicationConfig // 1 dependency
}
```

**Dependency Count: 2**

**Status: EXCELLENT**

### ApplicationConfig

```scala
trait ApplicationConfig {
  // No dependencies (configuration values only)
}
```

**Dependency Count: 0**

**Status: EXCELLENT**

## Summary Statistics

| Class/Service | Dependencies | Status |
|--------------|--------------|--------|
| main | 2 | Good |
| PeopleService | 1 | Excellent |
| EmailService | 0 (2 runtime) | Good |
| RedisDatabase | 1 | Excellent |
| ApplicationRepository | 2 | Excellent |
| ApplicationConfig | 0 | Excellent |

### Aggregate Statistics

| Metric | Value |
|--------|-------|
| Total Classes | 6 |
| Total Dependencies | 6 |
| Mean Dependencies | 1.0 |
| Median Dependencies | 1 |
| Max Dependencies | 2 |
| Min Dependencies | 0 |
| Standard Deviation | 0.82 |

## Dependency Graph

```
                    ApplicationConfig
                          │
              ┌───────────┼───────────┐
              │           │           │
              ▼           ▼           ▼
        RedisDatabase  EmailService  ApplicationRepository
              │                       │
              └───────────┬───────────┘
                          │
                          ▼
                    PeopleService
                          │
                          ▼
                        main
```

## God Object Analysis

**Indicators of God Object:**
- Many dependencies (>5-7)
- Wide responsibility
- Many methods
- High coupling

**Findings:**
- ✅ No class has more than 2 dependencies
- ✅ Each service has focused responsibility
- ✅ Clear separation: config → infrastructure → domain → application

## Comparison to Industry Benchmarks

| Metric | This Codebase | Healthy Range | Warning Level |
|--------|---------------|---------------|---------------|
| Mean deps | 1.0 | 2-4 | >7 |
| Max deps | 2 | 5-7 | >10 |
| Std dev | 0.82 | 1-2 | >3 |

## Coupling Analysis

**Afferent Coupling (incoming dependencies):**
| Class | Depended On By |
|-------|----------------|
| ApplicationConfig | 4 services |
| ApplicationRepository | 2 services |
| RedisDatabase | 1 service |
| PeopleService | 1 (main) |
| EmailService | 1 (main) |

**Efferent Coupling (outgoing dependencies):**
| Class | Depends On |
|-------|------------|
| main | 2 services |
| ApplicationRepository | 2 (Client, Config) |
| PeopleService | 1 (Repository) |
| EmailService | 2 (runtime) |

## Metric Score

**Grade: 9/10**

**Positive Findings:**
- Excellent dependency counts (mean of 1.0)
- No God Objects detected
- Clear separation of concerns
- Follows Single Responsibility Principle
- ZIO environment pattern manages runtime dependencies elegantly

**Minor Considerations:**
- `EmailService` runtime dependencies (via ZIO environment) could grow
- Small codebase makes this metric less meaningful at current scale

**Recommendations:**
1. Monitor `EmailService` as email logic grows - may need splitting
2. Keep dependency injection explicit (current approach is good)
3. Consider dependency limits (e.g., max 5) as team standard
4. If a service needs many dependencies, consider facade pattern or splitting
