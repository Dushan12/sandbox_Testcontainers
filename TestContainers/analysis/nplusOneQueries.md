The current project is web application with two endpoints:
POST /people/save
GET  /people

It is written in scala 3 with ZIO effects library

Can you analyze the code. Check all occurences where we have Query in for loop and grade the code.
If the code has query fire in db in for loop that is considered weak design.
If the code has single request to database it is considered good.

I want to analyze the codebase and use this as metric for code quality.