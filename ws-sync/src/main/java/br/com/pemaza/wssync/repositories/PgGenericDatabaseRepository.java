package br.com.pemaza.wssync.repositories;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;

import br.com.pemaza.wssync.dto.KafkaDbInputDTO;
import br.com.pemaza.wssync.dto.TypedColumn;

@Profile("!matriz")
@Service
public class PgGenericDatabaseRepository extends GenericDatabaseRepository {

	@Override
	public List<TypedColumn> getTypedColumnsByTable(String schema, String table, Set<String> columns,
			Set<String> ignoreColumns) {
		final MapSqlParameterSource namedParameters = new MapSqlParameterSource()
				.addValue("tn", adaptFieldCasing(table)).addValue("ts", adaptFieldCasing(schema));

		String sql = "SELECT column_name, data_type from information_schema.columns WHERE table_name = :tn AND table_schema = :ts";
		if (columns != null && columns.size() > 0) {
			String where = columns.stream().map((column) -> {
				namedParameters.addValue(adaptFieldCasing(column), adaptFieldCasing(column));
				return String.format(":%s", adaptFieldCasing(column));
			}).collect(Collectors.joining(","));
			sql += String.format(" AND column_name IN (%s)", where);
		}
		if (ignoreColumns != null && ignoreColumns.size() > 0) {
			String ignore = ignoreColumns.stream().map(column -> {
				namedParameters.addValue(adaptFieldCasing(column), adaptFieldCasing(column));
				return String.format(":%s", adaptFieldCasing(column));
			}).collect(Collectors.joining(","));
			sql += String.format(" AND column_name NOT IN (%s)", ignore);
		}
		return template.query(sql, namedParameters,
				(rs, rowNum) -> new TypedColumn(rs.getString("column_name"), null, rs.getString("data_type")));
	}

	@Override
	public String adaptFieldCasing(String string) {
		return string.toLowerCase();
	}

	@Override
	protected String gambiPemazaPublic(String schema) {
		if (schema.equalsIgnoreCase("pemaza"))
			return "public";
		return schema;
	}

	// UPDATE "public"."tabsync" SET "reg_status" = 'A', "reg_dtenvio" = '2019-12-17
	// 12:13:31.784000' WHERE "id_sync" = 65

	@Override
	public void update(Long id) {
		final MapSqlParameterSource namedParameters = new MapSqlParameterSource().addValue("id", id);
		template.update("update public.tabsync set reg_status ='E', reg_dtenvio = current_timestamp where id_sync = :id",
				namedParameters);
	}

	@Override
	public List<KafkaDbInputDTO> getMsgToSend() {
		return template.query("SELECT reg_dados, id_sync FROM tabsync WHERE reg_status != 'E' order by id_sync",
				(ResultSet rs, int rowNum) -> {
					KafkaDbInputDTO msgCustomer = new KafkaDbInputDTO();
					msgCustomer.setId(rs.getLong("id_sync"));
					msgCustomer.setReg_dados(rs.getString("reg_dados"));
					return msgCustomer;
				});
	}

}
