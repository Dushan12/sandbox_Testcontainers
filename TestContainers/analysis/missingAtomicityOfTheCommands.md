The current project is web application with two endpoints:
POST /people/save
GET  /people

It is written in scala 3 with ZIO effects library

When a command is executed it can side effect in many ways. When i say command i mean controller action.
Can you analyze the code and see if there are instances where the action can finish partially and there is no transactions between the database commands. Of if the command is calling queue or api and the API returns error the database transaction gets reverted. It is important that any Controller action either completes or fails with no residual data apart from the logs.