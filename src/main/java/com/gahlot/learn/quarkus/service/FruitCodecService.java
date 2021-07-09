package com.gahlot.learn.quarkus.service;

import com.gahlot.learn.quarkus.Fruit;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

// No need to use Document anymore, as the Codec will take care of converting the Document to Fruit
@ApplicationScoped
public class FruitCodecService {
    static final String NAME = "name";
    static final String DESC = "description";

    @Inject
    private MongoClient mongoClient;

    public List<Fruit> getFruits() {
        ArrayList<Fruit> fruits = new ArrayList<>();
        MongoCursor<Fruit> cursor = getCollection().find().iterator();

        try {
            while (cursor.hasNext()) {
                fruits.add(cursor.next());
            }
        } finally {
            cursor.close();
        }

        return fruits;
    }

    public void add(Fruit fruit) {
        getCollection().insertOne(fruit);
    }

    private MongoCollection<Fruit> getCollection() {
        return mongoClient.getDatabase("fruit").getCollection("fruit", Fruit.class);
    }

}