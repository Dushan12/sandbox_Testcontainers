The current project is web application with two endpoints:
POST /people/save
GET  /people

It is written in scala 3 with ZIO effects library

Can you analyze the semantics of the *.feature files and see if the semantics is appearing
in the codebase. I want to understand if the programming language contains metalinguistic abstractions and if the 
concepts of the domain knowledge written in the feature files. If the feature files have semantics like "User", "Person", "List", "Save" but 
in the code you can see plain json and arrays of string that is a low score.

If the metalinguistics is also used in the code that is high score.

I want to analyze the codebase and use this as metric for code quality.