# Analysis Java Dependence
 
#Setup analysis java server 
```
docker build -t analysis_java -f src/main/docker/Dockerfile .
docker-compose up
```

#Useage 
the server is running
```
GET http://localhost:8080/greet/world
```

analysis API
```
POST http://localhost:8080/analysis/class/{class}
POST http://localhost:8080/analysis/package/{package}
```