The current project is web application with two endpoints:
POST /people/save
GET  /people

It is written in scala 3 with ZIO effects library

Can you analyze the data transfer objects and how many nullable parameters they have?
I can assume that if you have id nullable or all nullable that the same DTO is used for many usecases. For example Controller validation, data transfer, save, load, list. This is something that would assume that the model can be in valid state in the code but invalid in the domain language. Example User without firstname.
Having DTOs with lots of nullable properties is negative while strongly typed and validated DTOs are positive trait.

I want to analyze the codebase and use this as metric for code quality.