The current project is web application with two endpoints:
POST /people/save
GET  /people

It is written in scala 3 with ZIO effects library

Can you analyze the codebase and see if the number of dependencies of the classes is too big.
Usually if classes have 20 dependencies there is problem with God Object or even some separation of concerns. Having small number of injected classes as mean from all classes is good while large number is bad. You can even take the deviation into consideration and devise grade.

I want to analyze the codebase and use this as metric for code quality.