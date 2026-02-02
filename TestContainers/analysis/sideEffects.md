It is general rule that side effects and global variables have issues with concurrency and thread safety. 
It is important that functions do not update global variables and if they side effect in any way they should not return anything. This is sign that the functions are doing more than one thing.
Side effects are causing bugs that happen ocasionally and are hard to find. 
