Unnecessary interfaces that have only one implementation suggest that the interface is created as premature implementation. Most of the time the interface is not needed and the need for having two implementations will never happen.
If there is polymorphism it should exist for reason. One interface with one implementation is bad since it creates cognitive load that is making the reading of the code harder.

