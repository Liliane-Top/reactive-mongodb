This application is part of the Udemy course Spring 6 by John Thompson.
The module is called Spring Data MongoDB.
To practice my knowledge on recative programming while using a MongoDB for persistence.
To be able to run this code you can do 2 things:
1: you can install MongoDB or 
2: you can run MongoDB in a Docker container.

Option 1: Installing MongoDB

Please refer to the official [MongoDB documentation](https://www.mongodb.com/docs/manual/administration/install-community/) documentation for installation instructions.

Option 2: Running MongoDB in Docker

Please refer to the official [MongoDB Docker Image](https://hub.docker.com/_/mongo) here. 
If you have to download [Docker desktop version](https://docs.docker.com/desktop/install) look here. 
Or Optional, run via docker compose. Save following as stack.yml

```
#Use root/example as user/password credentials
version: '3.1'
 
services:
 
  mongo:
    image: mongo
    restart: always
    environment:
      MONGO_INITDB_ROOT_USERNAME: root
      MONGO_INITDB_ROOT_PASSWORD: example
    ports:
    - "27017:27017"
 
  mongo-express:
    image: mongo-express
    restart: always
    ports:
      - 8081:8081
    environment:
      ME_CONFIG_MONGODB_ADMINUSERNAME: root
      ME_CONFIG_MONGODB_ADMINPASSWORD: example
      ME_CONFIG_MONGODB_URL: mongodb://root:example@mongo:27017/
 ```
      
User the command docker-compose -f stack.yml up  from same directory as file. The Docker compose file will expose a database browser on http://localhost:8081.

I use a popular MongoDB client called Studio 3T. To install it look [here](https://studio3t.com/). 
