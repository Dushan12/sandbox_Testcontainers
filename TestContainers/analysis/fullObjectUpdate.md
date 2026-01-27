The current project is web application with two endpoints:
POST /people/save
GET  /people

It is written in scala 3 with ZIO effects library

Can you analyze the code and get all the instances where full object update is done vs just the changed properties?
For example if you do ORM update on the whole object loaded from the database for example person.save() it creates problems if you updated one set of the fields and other actor updated other set of fields. The latter will override the properties of the former. Instead of full object update we should only update the changed properties. 

I want to analyze the codebase and use this as metric for code quality.