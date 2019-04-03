package ua.procamp.dao;

import ua.procamp.exception.DaoOperationException;
import ua.procamp.model.Product;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

public class ProductDaoImpl implements ProductDao {

    private final String FIND_ALL_QUERY = "SELECT * FROM products;";
    private final String FIND_QUERY = "SELECT p.* FROM products p WHERE p.id = ?";
    private final String REMOVE_QUERY = "DELETE FROM products WHERE products.id = ?";
    private final String INSERT_QUERY = "INSERT INTO products (name, producer, price, expiration_date) VALUES (?, ?, ?, ?);";
    private final String UPDATE_QUERY = "UPDATE products SET (name, producer, price, expiration_date) = (?, ?, ?, ?) WHERE products.id = ?";

    private DataSource dataSource;

    public ProductDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(Product product) {
        requireNonNull(product);
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement insertStatement = prepareInsertStatement(product, connection);
            insertStatement.executeUpdate();
            Long id = fetchGeneratedId(insertStatement);
            product.setId(id);
        } catch (SQLException e) {
            throw new DaoOperationException(String.format("Error saving product: %s", product));
        }
    }

    private PreparedStatement prepareInsertStatement(final Product product, final Connection connection) {
        try {
            PreparedStatement insertStatement = connection.prepareStatement(INSERT_QUERY, PreparedStatement.RETURN_GENERATED_KEYS);
            insertStatement.setString(1, product.getName());
            insertStatement.setString(2, product.getProducer());
            insertStatement.setBigDecimal(3, product.getPrice());
            insertStatement.setDate(4, Date.valueOf(product.getExpirationDate()));
            return insertStatement;
        } catch (SQLException e) {
            throw new DaoOperationException(String.format("Cannot prepare statement for product: %s", product));
        }
    }

    private Long fetchGeneratedId(PreparedStatement insertStatement) throws SQLException {
        ResultSet generatedKeys = insertStatement.getGeneratedKeys();
        if (generatedKeys.next()) {
            return generatedKeys.getLong(1);
        } else {
            throw new DaoOperationException("Can not obtain product ID");
        }
    }

    @Override
    public List<Product> findAll() {
        try (Connection connection = dataSource.getConnection()) {
            return fetchAllProducts(connection);
        } catch (SQLException e) {
            throw new DaoOperationException("Problem while findAll operation", e);
        }
    }

    private List<Product> fetchAllProducts(Connection connection) throws SQLException {
        List<Product> products = new ArrayList<>();
        PreparedStatement statement = connection.prepareStatement(FIND_ALL_QUERY);
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
            products.add(getProductFromRow(resultSet));
        }
        return products;
    }

    private Product getProductFromRow(ResultSet resultSet) {
        try {
            long id = resultSet.getLong("id");
            String name = resultSet.getString("name");
            String producer = resultSet.getString("producer");
            BigDecimal price = resultSet.getBigDecimal("price");
            LocalDate localDate = resultSet.getDate("expiration_date").toLocalDate();
            LocalDateTime localDateTime = resultSet.getTimestamp("creation_time").toLocalDateTime();
            return new Product(id, name, producer, price, localDate, localDateTime);
        } catch (SQLException e) {
            throw new DaoOperationException("Cannot parse product from result set", e);
        }
    }

    @Override
    public Product findOne(Long id) {
        requireNonNull(id);
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement findStatement = prepareFindStatement(id, connection);
            ResultSet resultSet = findStatement.executeQuery();
            if (resultSet.next()) {
                return getProductFromRow(resultSet);
            } else {
                throw new DaoOperationException(String.format("Product with id = %d does not exist", id));
            }
        } catch (SQLException e) {
            throw new DaoOperationException(String.format("Error finding product by id = %d", id), e);
        }
    }

    private PreparedStatement prepareFindStatement(final Long id, final Connection connection) {
        try {
            PreparedStatement statement = connection.prepareStatement(FIND_QUERY);
            statement.setLong(1, id);
            return statement;
        } catch (SQLException e) {
            throw new DaoOperationException(String.format("Cannot prepare select by id statement for id = %d", id), e);
        }
    }

    @Override
    public void update(Product product) {
        checkProductForUpdate(product);
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement updateStatement = prepareUpdateStatement(product, connection);
            checkUpdateStatementResult(updateStatement.executeUpdate(), product.getId());
        } catch (SQLException e) {
            throw new DaoOperationException(String.format("Error updating product: %s", product));
        }
    }

    private PreparedStatement prepareUpdateStatement(Product product, Connection connection) {
        try {
            PreparedStatement updateStatement = connection.prepareStatement(UPDATE_QUERY);
            updateStatement.setString(1, product.getName());
            updateStatement.setString(2, product.getProducer());
            updateStatement.setBigDecimal(3, product.getPrice());
            updateStatement.setDate(4, Date.valueOf(product.getExpirationDate()));
            updateStatement.setLong(5, product.getId());
            return updateStatement;
        } catch (SQLException e) {
            throw new DaoOperationException(String.format("Cannot prepare statement for product: %s", product));
        }
    }

    @Override
    public void remove(Product product) {
        checkProductForUpdate(product);
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement removeStatement = prepareRemoveStatement(product, connection);
            checkUpdateStatementResult(removeStatement.executeUpdate(), product.getId());
        } catch (SQLException e) {
            throw new DaoOperationException(String.format("Error removing product: %s", product));
        }
    }

    private PreparedStatement prepareRemoveStatement(Product product, Connection connection) {
        try {
            PreparedStatement removeStatement = connection.prepareStatement(REMOVE_QUERY);
            removeStatement.setLong(1, product.getId());
            return removeStatement;
        } catch (SQLException e) {
            throw new DaoOperationException(String.format("Cannot prepare statement for product: %s", product));
        }
    }

    private void checkUpdateStatementResult(int rowCount, long id) {
        if (rowCount == 0) {
            throw new DaoOperationException(String.format("Product with id = %d does not exist", id));
        }
    }

    private void checkProductForUpdate(Product product) {
        requireNonNull(product);
        if (isNull(product.getId())) {
            throw new DaoOperationException("Product id cannot be null");
        }
    }

}
