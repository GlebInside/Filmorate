# java-filmorate
## DB Diagram
https://github.com/GlebInside/filmorate/blob/add-friends-likes/DBDiagram.png
### SQL query examples
1. Get top 10 films by likes: 

`select title, count(*) from films inner join likes on films.id = likes.film_id group by title order by 2 desc limit 10` 

2. Get incoming friedshipRequests for user with id = @user_id:

`select name from users u inner join friendshiprequest fr on u.id = fr.from where status = 'awaiting' and fr.to = @user_id`
