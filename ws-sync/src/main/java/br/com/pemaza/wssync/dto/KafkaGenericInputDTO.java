package br.com.pemaza.wssync.dto;

import java.util.List;
import java.util.Map;
import java.util.Set;

import br.com.pemaza.wssync.utils.DbOperationEnum;
import lombok.Data;

@Data
public class KafkaGenericInputDTO {
    private DbOperationEnum operation;
    private String table;
    private String schema;
    private Map<String, Object> primaryKeys;
    private List<String> topics;
    private Set<String> syncColumns;
    private Set<String> ignoreColumns;
}