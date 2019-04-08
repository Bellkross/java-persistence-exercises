package ua.procamp.locking.pessimistic;

import ua.procamp.locking.Program;
import ua.procamp.locking.exception.OptimisticLockingException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

import static ua.procamp.locking.ProgramQueries.SELECT_PROGRAM_BY_ID_AND_VER_BLOCKING_QUERY;
import static ua.procamp.locking.ProgramQueries.UPDATE_PROGRAM_QUERY;

public class PessimisticLockingDao {

    private final DataSource dataSource;

    public PessimisticLockingDao(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Optional<Program> findProgramById(final Long programId) {
        Objects.requireNonNull(programId);
        Program program = null;
        try (Connection connection = getConnection()) {
            PreparedStatement statement = prepareSelectProgramStatement(connection, programId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                program = fetchProgramFromRow(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(program);
    }

    private PreparedStatement prepareSelectProgramStatement(Connection connection, Long programId) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SELECT_PROGRAM_BY_ID_AND_VER_BLOCKING_QUERY);
        statement.setLong(1, programId);
        return statement;
    }

    private Program fetchProgramFromRow(ResultSet resultSet) throws SQLException {
        return Program.builder()
                .id(resultSet.getLong("id"))
                .name(resultSet.getString("name"))
                .version(resultSet.getLong("version")).build();
    }

    public int updateProgram(Program updatedProgram) throws OptimisticLockingException {
        Objects.requireNonNull(updatedProgram);
        Objects.requireNonNull(updatedProgram.id);
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            Optional<Program> programOptional = findProgramById(updatedProgram.id);
            Program updatableProgram = null;
            if (programOptional.isEmpty()) {
                connection.rollback();
                throw new SQLException(String.format("Program with id = %d doesn't exists", updatedProgram.id));
            } else {
                updatableProgram = programOptional.get();
            }
            updatableProgram.name = updatedProgram.name;
            int updatedFieldsCount = updateProgram(connection, updatableProgram);
            connection.commit();
            return updatedFieldsCount;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int updateProgram(Connection connection, Program program) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(UPDATE_PROGRAM_QUERY);
        statement.setString(1, program.name);
        statement.setLong(2, program.version); // no version changing
        statement.setLong(3, program.id);
        return statement.executeUpdate();
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

}
