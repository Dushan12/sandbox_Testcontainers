The current project is web application with two endpoints:
POST /people/save
GET  /people

It is written in scala 3 with ZIO effects library

Can you calculate the debth of each of this actions based on the function calls.
If a given action is calling five consecutive functions it is debth of 5
If a function calls 5 different functions that return it is debt 2

The debth of abstraction is worse when you want to hide complexity rather than have one function where every side effects happen there
which is called compression of complexity.

I want to analyze the codebase and use this as metric for code quality.