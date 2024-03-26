package br.com.suporte.moovinAudit.jdbc;

import org.springframework.boot.jdbc.HikariCheckpointRestoreLifecycle;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

public class JdbcTemplate {
    private static final String BASEURL = "jdbc:postgresql://186.209.139.30:5432/ocean";
    private static final String USERNAME = "ocean";
    private static final String PASSWORD = "adcaf2d135a75ad9336a0af3155e10b5de83ddb0dbd89ca5809703a7cee0650b";

    public static List<String> jdbcTemplateConsultaComQueryNativaPostgresListaResultadoOneRow(String searchPath,String query){
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(BASEURL);
        config.setUsername(USERNAME);
        config.setPassword(PASSWORD);
        config.addDataSourceProperty("currentSchema", searchPath);

        DataSource dataSource = new HikariDataSource(config);
        org.springframework.jdbc.core.JdbcTemplate jdbc = new org.springframework.jdbc.core.JdbcTemplate(dataSource);

        List<String> result = jdbc.queryForList(query,String.class);
        return result;
    }
    public static List<ResultQuery> jdbcTemplateConsultaComQueryNativaPostgresListaResultadoTwoRow(String searchPath,
                                                                                        String query,
                                                                                        String column1,
                                                                                        String column2) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(BASEURL);
        config.setUsername(USERNAME);
        config.setPassword(PASSWORD);
        config.addDataSourceProperty("currentSchema", searchPath);

        DataSource dataSource = new HikariDataSource(config);
        org.springframework.jdbc.core.JdbcTemplate jdbc = new org.springframework.jdbc.core.JdbcTemplate(dataSource);

        List<ResultQuery> result = jdbc.query(query, (rs, rowNum) -> {
            ResultQuery resultQuery = new ResultQuery();
            resultQuery.setColumn1(rs.getString(column1));
            resultQuery.setColumn2(rs.getString(column2));
            return resultQuery;
        });
        return result;
    }
    public static void jdbcInsert(String sql){
        try(Connection conn = DriverManager.getConnection(BASEURL,USERNAME,PASSWORD)){
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            int linhasAfecctec = preparedStatement.executeUpdate();
            System.out.println("Linhas afetadas: " + linhasAfecctec);
        } catch (SQLException e) {
            System.err.println("Erro ao inserir dados: " + e.getMessage());
        }
    }
    public static class ResultQuery {
        private String column1;
        private String column2;
        private String column3;
        private String column4;

        public ResultQuery() {
        }

        public String getColumn1() {
            return column1;
        }

        public void setColumn1(String column1) {
            this.column1 = column1;
        }

        public String getColumn2() {
            return column2;
        }

        public void setColumn2(String column2) {
            this.column2 = column2;
        }

        public String getColumn3() {
            return column3;
        }

        public void setColumn3(String column3) {
            this.column3 = column3;
        }

        public String getColumn4() {
            return column4;
        }

        public void setColumn4(String column4) {
            this.column4 = column4;
        }
    }
}


