package br.com.pemaza.wssync.dto;

import java.util.AbstractMap.SimpleEntry;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import br.com.pemaza.wssync.utils.DbOperationEnum;
import lombok.Data;

@Data
public class KafkaGenericOutputDTO {
    @Id
    @JsonIgnore
    private ObjectId _id;
    private DbOperationEnum operation;
    private Map<String, Object> primaryKeys;
    private String schema;
    private String table;
    private Set<String> syncColumns;
    private String topic;
    private String source;
    private List<TypedColumn> columns;
    @JsonIgnore
    @Indexed
    private Date timestamp;

    public static KafkaGenericOutputDTO fromInput(KafkaGenericInputDTO inputDTO) {
        KafkaGenericOutputDTO outputDTO = new KafkaGenericOutputDTO();
        outputDTO.setOperation(inputDTO.getOperation());
        outputDTO.setPrimaryKeys(inputDTO.getPrimaryKeys());
        outputDTO.setSchema(inputDTO.getSchema());
        outputDTO.setSyncColumns(inputDTO.getSyncColumns());
        outputDTO.setTable(inputDTO.getTable());
        return outputDTO;
    }

    public void setTable(String table) {
        this.table = table.toLowerCase();
    }

    public void setPrimaryKeys(Map<String, Object> primaryKeys) {
        this.primaryKeys = primaryKeys.entrySet().stream()
                .map(entry -> new SimpleEntry<>(entry.getKey().toLowerCase(), entry.getValue()))
                .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof KafkaGenericOutputDTO)) {
            return false;
        }
        KafkaGenericOutputDTO kafkaGenericOutputDTO = (KafkaGenericOutputDTO) o;
        return Objects.equals(table.toLowerCase(), kafkaGenericOutputDTO.table.toLowerCase())
                && Objects.equals(primaryKeys, kafkaGenericOutputDTO.getPrimaryKeys());
    }

    @Override
    public int hashCode() {
        return Objects.hash(primaryKeys, table);
    }

    private List<TypedColumn> logDataLogHora(List<TypedColumn> columns) {
        return columns.stream()
                .filter(column -> column.getName().toLowerCase().equals("log_data")
                        || column.getName().toLowerCase().equals("log_hora"))
                .sorted((c1, c2) -> c1.getName().toLowerCase().compareTo(c2.getName().toLowerCase()))
                .collect(Collectors.toList());
    }



}