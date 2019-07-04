sudo docker stop some-mysql; sudo docker rm some-mysql
sudo docker run --name some-mysql -e MYSQL_ROOT_HOST=% -p 3306:3306 -e MYSQL_ROOT_PASSWORD=my-secret-pw -d mysql:latest
sudo docker exec -it some-mysql mysql -uroot -pmy-secret-pw -e "CREATE DATABASE store" 
sudo docker exec -it some-mysql mysql -uroot -pmy-secret-pw

