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

import static ua.procamp.locking.ProgramQueries.*;

public class OptimisticLockingDao {

    private final DataSource dataSource;

    public OptimisticLockingDao(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Optional<Program> findProgramById(final Long programId) {
        Objects.requireNonNull(programId);
        Program result = null;
        try (Connection connection = getConnection()) {
            PreparedStatement statement = prepareSelectByIdProgramStatement(connection, programId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                result = fetchProgramFromRow(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(result);
    }

    public Optional<Program> findSuchProgram(final Program program) {
        Objects.requireNonNull(program);
        Objects.requireNonNull(program.id);
        Program result = null;
        try (Connection connection = getConnection()) {
            PreparedStatement statement = prepareSelectByIdAndVerProgramStatement(connection, program.id, program.version);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                result = fetchProgramFromRow(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(result);
    }

    private PreparedStatement prepareSelectByIdProgramStatement(Connection connection, Long programId) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SELECT_PROGRAM_BY_ID_QUERY);
        statement.setLong(1, programId);
        return statement;
    }

    private PreparedStatement prepareSelectByIdAndVerProgramStatement(Connection connection, Long programId, Long programVersion) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SELECT_PROGRAM_BY_ID_AND_VER_QUERY);
        statement.setLong(1, programId);
        statement.setLong(2, programVersion);
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
        int updatedFieldsCount = 0;
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try {
                Optional<Program> programOptional = findSuchProgram(updatedProgram);
                Program updatableProgram = programOptional.orElseThrow(
                        () -> new OptimisticLockingException(
                                String.format("Unsuccessful update of program with id %d and version = %d",
                                        updatedProgram.id,
                                        updatedProgram.version)
                        )
                );
                updatableProgram.name = updatedProgram.name;
                updatedFieldsCount = updateProgram(connection, updatableProgram);
                if (updatedFieldsCount == 0) {
                    throw new OptimisticLockingException(
                            String.format("Unsuccessful update of program with id %d and version = %d",
                                    updatedProgram.id,
                                    updatedProgram.version)
                    );
                }
            } catch (OptimisticLockingException ole) {
                connection.rollback();
                throw ole;
            }
            connection.commit();
            return updatedFieldsCount;
        } catch (SQLException e) {
            // ignore
        }
        return updatedFieldsCount;
    }

    private int updateProgram(Connection connection, Program program) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(UPDATE_PROGRAM_QUERY);
        statement.setString(1, program.name);
        statement.setLong(4, program.version);
        statement.setLong(2, ++program.version);
        statement.setLong(3, program.id);
        return statement.executeUpdate();
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

}
