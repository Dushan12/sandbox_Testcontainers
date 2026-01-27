The current project is web application with two endpoints:
POST /people/save
GET  /people

It is written in scala 3 with ZIO effects library

Can you analyze the codebase and see if there are unnecessary interfaces with only one implementation?
If there is polymorphism it should exist for reason. One interface with one implementation is bad since it creates cognitive load that is making the reading of the code harder.

I want to analyze the codebase and use this as metric for code quality.