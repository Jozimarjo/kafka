package br.com.pemaza.wssync.dto;

import java.io.IOException;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import lombok.Data;

@Data
public class KafkaMsgDTO {
    @Id
    @JsonIgnore
    private ObjectId _id;
    private String label;
    private Object content;
    private String source;
    @JsonIgnore
    private String topic;
    @JsonIgnore
    @Indexed
    private Date timestamp;

    public static KafkaMsgDTO fromString(String msg) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(msg, new TypeReference<KafkaMsgDTO>() {});
    }


}