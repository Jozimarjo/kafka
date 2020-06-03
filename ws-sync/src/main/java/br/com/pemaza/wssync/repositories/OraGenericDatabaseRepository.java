package br.com.pemaza.wssync.repositories;

import java.sql.ResultSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;

import br.com.pemaza.wssync.dto.KafkaDbInputDTO;
import br.com.pemaza.wssync.dto.TypedColumn;

@Profile("matriz")
@Service
public class OraGenericDatabaseRepository extends GenericDatabaseRepository {

	@Override
	public List<TypedColumn> getTypedColumnsByTable(String schema, String table, Set<String> columns,
			Set<String> ignoreColumns) {
		final MapSqlParameterSource namedParameters = new MapSqlParameterSource()
				.addValue("tn", adaptFieldCasing(table)).addValue("ts", adaptFieldCasing(schema));

		String sql = "SELECT COLUMN_NAME, DATA_TYPE FROM ALL_TAB_COLUMNS WHERE TABLE_NAME = :tn AND OWNER = :ts";
		if (columns != null && columns.size() > 0) {
			String where = columns.stream().map((column) -> {
				namedParameters.addValue(adaptFieldCasing(column), adaptFieldCasing(column));
				return String.format(":%s", adaptFieldCasing(column));
			}).collect(Collectors.joining(","));
			sql += String.format(" AND COLUMN_NAME IN (%s)", where);
		}
		if (ignoreColumns != null && ignoreColumns.size() > 0) {
			String ignore = ignoreColumns.stream().map(column -> {
				namedParameters.addValue(adaptFieldCasing(column), adaptFieldCasing(column));
				return String.format(":%s", adaptFieldCasing(column));
			}).collect(Collectors.joining(","));
			sql += String.format(" AND COLUMN_NAME NOT IN (%s)", ignore);
		}
		return template.query(sql, namedParameters,
				(rs, rowNum) -> new TypedColumn(rs.getString("COLUMN_NAME"), null, rs.getString("DATA_TYPE")));

	}

	@Override
	public String adaptFieldCasing(String string) {
		return string.toUpperCase();
	}

	@Override
	protected String gambiPemazaPublic(String schema) {
		if (schema.equalsIgnoreCase("public"))
			return "pemaza";
		return schema;
	}

	@Override
	public void update(Long id) {
		final MapSqlParameterSource namedParameters = new MapSqlParameterSource().addValue("id", id);
		template.update("UPDATE PEMAZA.TABSYNC SET REG_STATUS ='E', REG_DTENVIO = current_timestamp WHERE ID_SYNC = :id", namedParameters);
	}

	@Override
	public List<KafkaDbInputDTO> getMsgToSend() {
		return template.query(
				"SELECT REG_DADOS, ID_SYNC FROM PEMAZA.TABSYNC WHERE REG_STATUS != 'E' ORDER BY ID_SYNC",
				(ResultSet rs, int rowNum) -> {
					KafkaDbInputDTO msgCustomer = new KafkaDbInputDTO();
					msgCustomer.setId(rs.getLong("ID_SYNC"));
					msgCustomer.setReg_dados(rs.getString("REG_DADOS"));
					return msgCustomer;
				});
	}

}