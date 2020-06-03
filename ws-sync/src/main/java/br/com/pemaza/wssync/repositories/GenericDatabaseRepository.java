package br.com.pemaza.wssync.repositories;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import br.com.pemaza.wssync.dto.KafkaDbInputDTO;
import br.com.pemaza.wssync.dto.TypedColumn;

public abstract class GenericDatabaseRepository {

    @Autowired
    protected NamedParameterJdbcTemplate template;

    /**
     * Retorna uma string com a clausa de 'where' para que seja usada em uma named
     * parameter query do JDBC, com os campos passados por parâmetro. A função
     * recebe também um biconsumer (callback) que será executado passando os campos
     * da coluna (nome e valor).
     * 
     * @param columns colunas que farão parte da clausa 'where'
     * @param paramCb callback a ser executado com nome e valor de cada coluna
     * @return clausa 'where' com as colunas passadas. Ex.: id_pessoa = :id_pessoa
     *         AND id_produto = :id_produto
     */
    protected String buildPrimaryKeyWhere(Map<String, Object> columns, BiConsumer<String, Object> paramCb) {
        return columns.entrySet().stream().map((entry) -> {
            paramCb.accept(adaptFieldCasing(entry.getKey()), entry.getValue());
            return String.format("\"%s\" = :%s", adaptFieldCasing(entry.getKey()), adaptFieldCasing(entry.getKey()));
        }).collect(Collectors.joining(" AND "));
    }

    /**
     * Retorna uma lista de 'colunas tipadas'. As colunas tipadas possuem nome da
     * coluna e tipo, neste momento.
     * 
     * @param schema  schema das colunas a serem buscadas
     * @param table   tabela das colunas a serem buscadas
     * @param columns colunas a serem buscadas
     * @return lista de colunas tipadas
     */
    public abstract List<TypedColumn> getTypedColumnsByTable(String schema, String table, Set<String> columns,
            Set<String> ignoreColumns);

    /**
     * Retorna um campo adaptado para a query do banco sendo utilizado. Oracle sendo
     * maiúsculo e postgres sendo minúsculo
     * 
     * @param string campo a ser adaptado
     * @return campo adaptado
     */
    protected abstract String adaptFieldCasing(String string);

    /**
     * Retorna o schema do banco adequado de acordo com o banco instanciado. Caso
     * seja oracle, o schema é 'pemaza'. Caso postgres, o schema é 'public'.
     * 
     * @param schema schema a ser analisado, geralmente vem da mensagem do kafka
     * @return schema adaptado para o banco usado
     */
    protected abstract String gambiPemazaPublic(String schema);

    /**
     * Faz um 'cast' de colunas do tipo date ou timestamp para que o JDBC possa
     * fazer corretamente a operação no banco.
     * Colunas do tipo timestamp são convertidas em character varying, conforme solicitado e conversado com sr, Carlos, Daniel, Marcelo.
     * 
     * @param column coluna a ser verificada
     * @return coluna com valores date ou timestamp devidamente tipados
     */
    private Object castValueType(TypedColumn column) {
    	if(column.getType().toLowerCase().contains("timestamp")) {
    		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
    		return format.format(new Date((long) column.getValue())); 	
    	}
        if (column.getType().equalsIgnoreCase("date")) {
            return new Date((long) column.getValue());
        }
        return column.getValue();
    }

    /**
     * Executa um insert no banco com JDBC
     * 
     * @param schema  schema onde será feito o insert
     * @param table   tabela onde será feito o insert
     * @param columns colunas a serem inseridas
     * @return número de registros afetados
     */
    private int execInsert(String schema, String table, List<TypedColumn> columns) {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        final StringBuilder columnNames = new StringBuilder("(");
        final StringBuilder columnValues = new StringBuilder("(");

        columns.stream().forEach((column) -> {
            columnNames.append(String.format("\"%s\",", adaptFieldCasing(column.getName())));
            columnValues.append(String.format(":%s,", adaptFieldCasing(column.getName())));
            params.addValue(adaptFieldCasing(column.getName()), castValueType(column));
        });
        columnNames.deleteCharAt(columnNames.length() - 1);
        columnValues.deleteCharAt(columnValues.length() - 1);
        columnNames.append(")");
        columnValues.append(")");

        String sql = String.format("INSERT INTO %s.%s %s VALUES %s", gambiPemazaPublic(schema), table,
                columnNames.toString(), columnValues.toString());
        return template.update(sql, params);
    }

    /**
     * Executa um update no banco com JDBC
     * 
     * @param schema      schema a ser feito update
     * @param table       tabela a ser feito update
     * @param primaryKeys chaves para idenficicar unicamente uma tupla
     * @param columns     colunas a serem atualizadas
     * @return número de registros afetados
     */
    private int execUpdate(String schema, String table, Map<String, Object> primaryKeys, List<TypedColumn> columns) {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        String setColumns = columns.stream().map((column) -> {
            params.addValue(adaptFieldCasing(column.getName()), castValueType(column));
            return String.format("\"%s\" = :%s", adaptFieldCasing(column.getName()),adaptFieldCasing(column.getName()));
        }).collect(Collectors.joining(","));

        String where = buildPrimaryKeyWhere(primaryKeys, (name, value) -> params.addValue(name, value));

        String sql = String.format("UPDATE %s.%s SET %s WHERE 1 = 1 AND %s", gambiPemazaPublic(schema), table,
                setColumns, where);
        return template.update(sql, params);
    }

    /**
     * Busca os valores das colunas passadas por parâmetro e retorna uma nova lista
     * com as colunas tipadas e seus valores atribuídos.
     * 
     * @param schema      schema das colunas
     * @param table       tabela das colunas
     * @param primaryKeys chaves para identificar unicamente uma tupla
     * @param columns     colunas a serem buscadas
     * @return lista de colunas tipadas e com valores atribuídos
     */
    public List<TypedColumn> getAllColumnsUsingPk(String schema, String table, Map<String, Object> primaryKeys,
            List<TypedColumn> columns) {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        String where = buildPrimaryKeyWhere(primaryKeys, (name, value) -> params.addValue(name, value));

        StringBuilder query = new StringBuilder(
                String.format("SELECT * FROM %s.%s WHERE 1 = 1 AND %s", gambiPemazaPublic(schema), table, where));

        return template.query(query.toString(), params, (rs, rowNum) -> columns.stream().map((column) -> {
            try {
                return new TypedColumn(column.getName(), rs.getObject(column.getName()), column.getType());
            } catch (Exception e) {
                return null;
            }
        }).collect(Collectors.toList())).get(0);
    }

    /**
     * Verifica se existe no banco um registro com as chaves passadas por parâmetro
     * e executa a operação adequada, insert ou update.
     * 
     * @param schema      schema do registro
     * @param table       tabela do registro
     * @param primaryKeys chaves para identificar unicamente o registro
     * @param columns     colunas a usar na operação do registro
     * @return número de registros afetados
     */
    public int upsertRow(String schema, String table, Map<String, Object> primaryKeys, List<TypedColumn> columns) {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        String where = buildPrimaryKeyWhere(primaryKeys, (name, value) -> params.addValue(name, value));

        String sql = String.format("SELECT COUNT(*) FROM %s.%s WHERE 1 = 1 AND %s", gambiPemazaPublic(schema), table,
                where);
        return template.queryForObject(sql, params, Integer.class) > 0 ? execUpdate(schema, table, primaryKeys, columns)
                : execInsert(schema, table, columns);
    }

    /**
     * Deleta um registro no banco
     * 
     * @param schema      schema do registro
     * @param table       tabela do registro
     * @param primaryKeys chaves para identificar unicamente o registro
     * @return número de registros afetados
     */
    public int deleteRow(String schema, String table, Map<String, Object> primaryKeys) {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        String where = buildPrimaryKeyWhere(primaryKeys, (name, value) -> params.addValue(name, value));

        String sql = String.format("DELETE FROM %s.%s WHERE 1 = 1 AND %s", gambiPemazaPublic(schema), table, where);
        return template.update(sql, params);
    }
    
    /**
     * Busca os registros que não possuem status 'E'
     * 
     * @return  lista de registros com a mensagem e id;
     */
	public abstract List<KafkaDbInputDTO> getMsgToSend();
	
    /**
     * Executa um update no banco com JDBC
     * 
     * @param id        id do registro
     * 
     * @return void
     */
	public abstract void update(Long id);

}