package nl.top.reactivemongodb.config;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;

@Configuration
public class MongoConfig extends AbstractReactiveMongoConfiguration {

    @Override
    protected String getDatabaseName() {
        return "sfg";
    }

    //we also require a bean to set up a mongoDB client
    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create();
    }

    //this is required if you use a Docker image in which you have set up client credentials
//    @Override
//    protected void configureClientSettings(MongoClientSettings.Builder builder) {
//        builder.credential(MongoCredential.createCredential("root",
//                        "admin", "example".toCharArray()))
//                .applyToClusterSettings(settings ->
//                        settings.hosts((singletonList(new ServerAddress("127.0.0.1", 27017)))));
//    }


}
