package br.com.pemaza.wssync.dto;

import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypedColumn {
    private String name;
    private Object value;
    private String type;
    
    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof TypedColumn)) {
            return false;
        }
        TypedColumn typedColumn = (TypedColumn) o;
        return Objects.equals(name.toLowerCase(), typedColumn.name.toLowerCase()) && Objects.equals(value, typedColumn.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name.toLowerCase(), value);
    }
        
}