Data from the database that is not needed, and lack of projection.
For example getCurrentLoggedInUser() is loaded with all the data related to them but also the application only needs the token.
This is not exclusive, maybe the domain model is partially reading it or the frontend is only templateing partially. This case means that the same DTO is used for many things and results in data transfer over network that is unnecessary, ultimately resulting with too much network traffic, especially if there are many database requests.
