package ua.procamp.locking.optimistic;

import ua.procamp.locking.Program;
import ua.procamp.locking.exception.OptimisticLockingException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

import static ua.procamp.locking.ProgramQueries.SELECT_PROGRAM_QUERY;
import static ua.procamp.locking.ProgramQueries.UPDATE_PROGRAM_QUERY;

public class OptimisticLockingDao {

    private final DataSource dataSource;

    public OptimisticLockingDao(final DataSource dataSource) {
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
        PreparedStatement statement = connection.prepareStatement(SELECT_PROGRAM_QUERY);
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
            Optional<Program> programOptional = findProgramById(updatedProgram.id);
            Program updatableProgram = programOptional.orElseThrow(
                    () -> new SQLException(String.format("Program with id = %d doesn't exists", updatedProgram.id))
            );
            updatableProgram.name = updatedProgram.name;
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            requireConsistencyWithDatabase(updatableProgram);
            int updatedFieldsCount = updateProgram(connection, updatableProgram);
            connection.commit();
            return updatedFieldsCount;
        } catch (SQLException e) {
            // ignore
        }
        return 0;
    }

    private void requireConsistencyWithDatabase(Program program) throws OptimisticLockingException {
        Optional<Program> programOptional = findProgramById(program.id);
        Program programForCheck = programOptional.orElseThrow(
                () -> new OptimisticLockingException(
                        String.format("Transaction failed because %s entity was removed", program)
                )
        );
        if (!programForCheck.version.equals(program.version)) {
            throw new OptimisticLockingException(
                    String.format("Transaction failed because %s entity was modified", program)
            );
        }
    }

    private int updateProgram(Connection connection, Program program) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(UPDATE_PROGRAM_QUERY);
        statement.setString(1, program.name);
        statement.setLong(2, ++program.version);
        statement.setLong(3, program.id);
        return statement.executeUpdate();
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

}
