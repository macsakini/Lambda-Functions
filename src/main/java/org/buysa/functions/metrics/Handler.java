package org.buysa.functions.metrics;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;


import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Properties;


import org.json.JSONException;
import org.json.JSONObject;


public class Handler implements RequestHandler<LinkedHashMap, String>{
    @Override
    public String handleRequest(LinkedHashMap event, Context context)
    {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

        LambdaLogger logger = context.getLogger();

        Properties localproperties = new Properties();
        try (InputStream propertiesfile = new FileInputStream("lib/local.properties");) {
            localproperties.load(propertiesfile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "13.246.19.88:9093");
        properties.setProperty(ProducerConfig.CLIENT_ID_CONFIG, "metrics");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG, "gzip");
        properties.setProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
        if(localproperties.getProperty("PRODUCTION").equals("true")){
            properties.setProperty(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, localproperties.getProperty("SSL_TRUSTSTORE_PROD_LOCATION"));
            properties.setProperty(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, localproperties.getProperty("SSL_KEYSTORE_PROD_LOCATION"));
            logger.log("PROD");
        }else{
            properties.setProperty(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, localproperties.getProperty("SSL_TRUSTSTORE_DEV_LOCATION"));
            properties.setProperty(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, localproperties.getProperty("SSL_KEYSTORE_DEV_LOCATION"));
            logger.log("DEV");
        }
        properties.setProperty(SslConfigs.SSL_PROTOCOL_CONFIG, "TLS");
        properties.setProperty(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, localproperties.getProperty("SSL_PASSWORD"));
        properties.setProperty(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG,localproperties.getProperty("SSL_PASSWORD"));
        properties.setProperty(SslConfigs.SSL_KEY_PASSWORD_CONFIG, localproperties.getProperty("SSL_PASSWORD"));
        properties.setProperty(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "");

        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        JSONObject eventjson = null;
        JSONObject request = null;
        JSONObject body = null;

        try{
            eventjson = new JSONObject(event);
            body = eventjson.getJSONObject("body");
        } catch (JSONException e){
            eventjson = new JSONObject(event);
            request = new JSONObject(eventjson.getString("body"));
            body = request.getJSONObject("body");
        }

        ProducerRecord<String, String> message = new ProducerRecord<String, String>(
                "metrics",
                body.getString("order_key"),
                eventjson.toString()
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
//         log execution details
        logger.log("ENVIRONMENT VARIABLES: " + event);

        logger.log("EVENT TYPE: " + event.getClass());

        producer.close();
        return "Success";
    }
}