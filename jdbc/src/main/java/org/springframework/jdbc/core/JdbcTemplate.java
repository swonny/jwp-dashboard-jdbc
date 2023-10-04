package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    private static final int FIRST_PARAMETER_INDEX = 1;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T, P> T queryForObject(final String sql, final RowMapper<T> rowMapper, final P parameter) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ) {
            log.debug("query : {}", sql);

            preparedStatement.setObject(FIRST_PARAMETER_INDEX, parameter);

            final ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return rowMapper.mapRow(resultSet);
            }
            if (!resultSet.isLast()) {
                throw new RuntimeException("단일 데이터가 아닙니다.");
            }
            throw new RuntimeException("찾는 데이터가 존재하지 않습니다.");
        } catch (SQLException e) {
            log.error(e.getMessage(), e);

            throw new RuntimeException(e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ) {
            final ResultSet resultSet = preparedStatement.executeQuery();

            log.debug("query : {}", sql);

            final List<T> objects = new ArrayList<>();
            while (resultSet.next()) {
                final T object = rowMapper.mapRow(resultSet);
                objects.add(object);
            }
            return objects;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> int update(final String sql, final Object... parameters) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ) {
            log.debug("query : {}", sql);

            for (int parameterIndex = 0; parameterIndex < parameters.length; parameterIndex++) {
                preparedStatement.setObject(parameterIndex + 1, parameters[parameterIndex]);
            }

            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
