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

import static java.util.Objects.*;

public class ProductDaoImpl implements ProductDao {

    private final String FIND_ALL_QUERY = "SELECT p.* FROM products p WHERE p.id = ?";
    private final String SQL_FIND_ALL_PRODUCTS = "SELECT * FROM products;";
    private final String INSERT_QUERY = "INSERT INTO products (name, producer, price, expiration_date) VALUES (?, ?, ?, ?);";
    private final String SQL_UPDATE_PRODUCT =
            "UPDATE products SET (name, producer, price, expiration_date, creation_time) = (?, ?, ?, ?, ?)" +
            "WHERE products.id = ?";
    private final String SQL_REMOVE_PRODUCT =
            "DELETE FROM products WHERE products.id = ?";

    private DataSource dataSource;

    public ProductDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(Product product) {
        requireNonNull(product);
        try(Connection connection = dataSource.getConnection()) {
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
        List<Product> products = new ArrayList<>();
        return products;
    }

    @Override
    public Product findOne(Long id) {
        Product product = null;
        return product;
    }

    @Override
    public void update(Product product) {
    }

    @Override
    public void remove(Product product) {
    }

}
