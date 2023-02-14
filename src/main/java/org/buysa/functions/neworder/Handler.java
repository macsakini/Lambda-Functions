package org.buysa.functions.neworder;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// Handler value: example.Handler
public class Handler implements RequestHandler<Map<String,String>, String>{
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    @Override
    public String handleRequest(Map<String,String> event, Context context)
    {

        Properties localproperties = new Properties();
        InputStream localfile = null;
        try {
            localfile= new FileInputStream("local.properties");
            localproperties.load(localfile);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String Bootstrap_server = "13.246.19.88:9093";

        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Bootstrap_server);
        properties.setProperty(ProducerConfig.CLIENT_ID_CONFIG, "Buysa Lambda New Order");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG, "gzip");
        properties.setProperty(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, "/Users/mac/IdeaProjects/buysaeventsfunctions/private/client.truststore.jks");
        properties.setProperty(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "SAkiniolinga@123");
        properties.setProperty(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, "/Users/mac/IdeaProjects/buysaeventsfunctions/private/server.keystore.jks");
        properties.setProperty(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, "SAkiniolinga@123");
        properties.setProperty(SslConfigs.SSL_KEY_PASSWORD_CONFIG, "SAkiniolinga@123");

        KafkaProducer<String,String> producer = new KafkaProducer<String, String>(properties);

        ProducerRecord<String, String> message = new ProducerRecord<String, String>("orders", "hshsjsj", "jsjsjsj");

        producer.send(message, (metadata, exception) -> {
            Logger logger= LoggerFactory.getLogger(Main.class.getName());
            if (exception == null) {
                logger.info("Successfully received the details as: \n" +
                        "Topic:" + metadata.topic() + "\n" +
                        "Partition:" + metadata.partition() + "\n" +
                        "Offset" + metadata.offset() + "\n" +
                        "Timestamp" + metadata.timestamp());
            }

            else {
                logger.error("Can't produce,getting error",exception);
            }
            // Log the info
            logger.info("Message 1 for logger");
        });

        LambdaLogger logger = context.getLogger();
        String response = new String("200 OK");
        // log execution details
        logger.log("ENVIRONMENT VARIABLES: " + gson.toJson(System.getenv()));
        logger.log("CONTEXT: " + gson.toJson(context));
        // process event
        logger.log("EVENT: " + gson.toJson(event));
        logger.log("EVENT TYPE: " + event.getClass().toString());
//
        producer.close();
        return response;
    }
}