The current project is web application with two endpoints:
POST /people/save
GET  /people

It is written in scala 3 with ZIO effects library

Read after update pattern is considered bad practice that is from older times where you would update something and reload the whole object because you would assume that the database triggers did some additional work. This is bad if you use eventual consistency and you expect lag between save and changes applied.

Having updates and then imediate get by id of database object is weak design.
Having updates that are done in the database and then do rely only on the memory object is better as it assumes that there is separate process that will get it by id when needed.

I want to analyze the codebase and use this as metric for code quality.