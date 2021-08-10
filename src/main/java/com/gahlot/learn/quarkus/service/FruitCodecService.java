package com.gahlot.learn.quarkus.service;

import com.gahlot.learn.quarkus.Fruit;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import io.quarkus.kubernetes.service.binding.runtime.ServiceBinding;
import io.quarkus.kubernetes.service.binding.runtime.ServiceBindingConfigSource;
import io.quarkus.mongodb.runtime.MongoServiceBindingConverter;
import io.quarkus.runtime.StartupEvent;

// No need to use Document anymore, as the Codec will take care of converting the Document to Fruit
@ApplicationScoped
public class FruitCodecService {
    static final String NAME = "name";
    static final String DESC = "description";

    private static final Logger LOGGER = Logger.getLogger("ListenerBean");

    @Inject
    private MongoClient mongoClient;

    private Path readDir(String name) {
        Path directory = Paths.get(name);
        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            LOGGER.info("File '" + directory + "' is not a proper directory");
            return null;
        }
        return directory;
    }

    void onStart(@Observes StartupEvent event) {
        LOGGER.info("TMP:: APP is starting...");
        String[] dirs = {"bindings", "target/classes/bindings", "classes/bindings"};

        for (String dir : dirs) {
            Path path = readDir(dir);
            if (path != null) {
                LOGGER.info("TMP: Using '" + dir + "' as the binding dir...");

                List<ServiceBinding> bindings = new ArrayList<>();
                ServiceBinding serviceBinding = new ServiceBinding(path);
                bindings.add(serviceBinding);

                LOGGER.info(String.format("name=%s, type=%s, properties=%s", serviceBinding.getName(), serviceBinding.getType(), serviceBinding.getProperties()));

                Optional<ServiceBindingConfigSource> configSource = new MongoServiceBindingConverter().convert(bindings);

                if (configSource.isPresent()) {
                    configSource.get().getProperties().entrySet().forEach(entry -> {
                        LOGGER.info(String.format("TMP:: key=%s, value=%s", entry.getKey(), entry.getValue()));
                    });
                } else {
                    LOGGER.info("TMP:: NO ConfigSource");
                }
            }
        }
//        LOGGER.info(String.format("bindings=%s", readDir("bindings"), readDir("target/classes/bindings"), readDir("classes/bindings")));


        LOGGER.info("TMP:: DONE start app...");
    }

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