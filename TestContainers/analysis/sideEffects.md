The current project is web application with two endpoints:
POST /people/save
GET  /people

It is written in scala 3 with ZIO effects library

It is general rule that side effects and global variables have issues with concurrency and thread safety. 
It is important that functions do not update global variables and if they side effect in any way they should not return anything. This is sign that the functions are doing more than one thing.

Side effects are causing bugs that happen ocasionally and are hard to find. 

I want to analyze the codebase and use this as metric for code quality.