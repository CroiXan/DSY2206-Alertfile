package com.duoc.AlertFile.receiver;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.duoc.AlertFile.model.VitalSignAlert;
import com.duoc.AlertFile.service.S3Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class AlertReceiver {

    private final S3Service s3Service;

    public AlertReceiver( S3Service s3Service ){
        this.s3Service = s3Service;
    }

    @RabbitListener(queues = "Alerta_List")
    public void receiveMessage(String message) {
        System.out.println("Mensaje recibido: " + message);
        boolean isValidAlertJson = isValidJson(message,VitalSignAlert.class);

        if (isValidAlertJson) {
            
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
                String timestamp = LocalDateTime.now().format(formatter);

                File tempFile = File.createTempFile("${file.upload-dir}/alerta_"+timestamp+"_", ".json");
                FileWriter writer = new FileWriter(tempFile);
                writer.write(message);
                writer.close();
                System.out.println("Ruta: " + tempFile.getAbsolutePath());

                //s3Service.uploadFile(tempFile.getAbsolutePath(), "uploads/alerta_"+timestamp+".json");

                //tempFile.delete();
            } catch (Exception e) { 
                e.printStackTrace();
            }

            System.out.println("Archivo JSON guardado");
        }else{
            System.out.println("Error al generar archivo");
        }
    }

    public static <T> boolean isValidJson(String json, Class<T> clazz) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.readValue(json, clazz);
            return true; 
        } catch (JsonProcessingException e) {
            return false; 
        }
    }

}
