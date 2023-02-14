package org.buysa.functions.newcustomer;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

// Handler value: example.Handler
public class Handler implements RequestHandler<Map<String,String>, String>{
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    //    private static Logger logger = Logger.getLogger(Main.class.getName());
//    static Slack slack = new Slack();x
    @Override
    public String handleRequest(Map<String,String> event, Context context)
    {
        LambdaLogger logger = context.getLogger();

        logger.log("Logger Initialized");

        InputStream propertiesfile = null;

        Properties localproperties = new Properties();

        try {
            propertiesfile= new FileInputStream("local.properties");
            localproperties.load(propertiesfile);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "13.246.19.88:9093");
        properties.setProperty(ProducerConfig.CLIENT_ID_CONFIG, "Buysa Lambda New Order");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
//        properties.setProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG, "gzip");
//        properties.setProperty(SslConfigs.SSL_ENABLED_PROTOCOLS_CONFIG, "SSL");
        properties.setProperty(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, localproperties.getProperty("SSL_TRUSTSTORE_FILE"));
        properties.setProperty(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, localproperties.getProperty("SSL_PASSWORD"));
        properties.setProperty(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, localproperties.getProperty("SSL_KEYSTORE_FILE"));
        properties.setProperty(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG,localproperties.getProperty("SSL_PASSWORD"));
        properties.setProperty(SslConfigs.SSL_KEY_PASSWORD_CONFIG, localproperties.getProperty("SSL_PASSWORD"));

        KafkaProducer<String,String> producer = new KafkaProducer<String, String>(properties);

        ProducerRecord<String, String> message = new ProducerRecord<String, String>(
                "items",
                "New Key from Lambda",
                "I Worked"
        );

        try {
            producer.send(message, (metadata, exception) -> {
                if (exception == null) {
                    logger.log("Successfully received the details as: \n" +
                            "Topic:" + metadata.topic() + "\n" +
                            "Partition:" + metadata.partition() + "\n" +
                            "Offset" + metadata.offset() + "\n" +
                            "Timestamp" + metadata.timestamp()
                    );
                }
                else {
                    logger.log("Can't produce,getting error" + exception);
                }
            });
            producer.flush();
//            slack.slackSend("Template Message");
        } catch(Exception e){
            e.printStackTrace();
        }

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