0.
    <property name="dbusername" value="root"/>
    <property name="dburl"  value="jdbc:mysql://localhost/store?user=root&amp;password=my-secret-pw" />
    <property name="dbclass" value="com.mysql.jdbc.Driver"/>

1. 

sudo docker stop some-mysql; sudo docker rm some-mysql
sudo docker run --name some-mysql -e MYSQL_ROOT_HOST=% -p 3306:3306 -e MYSQL_ROOT_PASSWORD=my-secret-pw -d mysql:latest
sudo docker exec -it some-mysql mysql -uroot -pmy-secret-pw -e "CREATE DATABASE store" 
sudo docker exec -it some-mysql mysql -uroot -pmy-secret-pw


sudo docker exec -it some-mysql mysql -uroot -pmy-secret-pw -e "use store;CREATE TABLE reviewersdb (id2 VARCHAR(30), name VARCHAR(30) );"
sudo docker exec -it some-mysql mysql -uroot -pmy-secret-pw -e "use store;INSERT INTO reviewersdb (id2,name)
VALUES ('123', 'Some value');"


2. 

Create data module with same name as table

Edit module def beanname /WEB-INF/data/assets/catalog/fields/myproduct.xml

<?xml version="1.0" encoding="UTF-8"?>

<properties beanname="dynamicDbSearcher"> 
  <property id="id" index="true" stored="true" editable="true" internalfield="true" searchtype="region" keyword="false" filter="false" sortable="true">Id 
    <name> 
      <language id="de"><![CDATA[Ich würde]]></language>  
      <language id="en"><![CDATA[Id]]></language>  
      <language id="es"><![CDATA[Carné de identidad]]></language>  
      <language id="fr"><![CDATA[Id]]></language> 
    </name> 
  </property>  
  <property id="name" index="true" stored="true" editable="true" multilanguage="true" internalfield="false" searchtype="region" keyword="false" filter="false" sortable="true">Name
    <name>
      <language id="de"><![CDATA[Name]]></language>
      <language id="en"><![CDATA[Name]]></language>
      <language id="es"><![CDATA[Nombre]]></language>
      <language id="fr"><![CDATA[Prénom]]></language>
    </name>
  </property> 
</properties>

3.

Configure the user and possibly reindex the users table

4. Restart container (heavy caching)
