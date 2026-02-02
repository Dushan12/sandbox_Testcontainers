Check all occurences where we have Query in for loop and grade the code.
If the code has query fire in db in for loop that is considered weak design.
If the code has single request to database it is considered good.
A query that is running in for loop has many downtimes. First it is not optimal to connect to database and fetch one at a time as opposed to all at once. And other is that these queries are usually part of ORM lazy loading, they cause small queries that are fast to execute, they do not appear in the database profiler but have significant impact on the CPU of the database server.