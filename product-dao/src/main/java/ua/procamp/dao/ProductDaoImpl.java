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

public class ProductDaoImpl implements ProductDao {
    private final String SQL_FIND_PRODUCT = "SELECT p.name, p.producer, p.price, p.expiration_date, p.creation_time " +
            "FROM products p " +
            "WHERE p.id = ?";
    private final String SQL_FIND_ALL_PRODUCTS = "SELECT * FROM products;";
    private final String SQL_SAVE_PRODUCT = "INSERT INTO products (id, name, producer, price, expiration_date, creation_time) " +
            "VALUES (?, ?, ?, ?, ?, ?);";
    private final String SQL_UPDATE_PRODUCT = "UPDATE products SET (name, producer, price, expiration_date, creation_time) = (?, ?, ?, ?, ?)" +
            "WHERE products.id = ?";
    private final String SQL_REMOVE_PRODUCT = "DELETE FROM products WHERE products.id = ?";
    private final String ERROR_SAVING_PRODUCT_STRING = "Error saving product: %s";
    private final String ERROR_UPDATING_PRODUCT_STRING = "Error updating product: %s";
    private final String ERROR_EXISTENCE_PRODUCT_STRING = "Product with id = %d does not exist";
    private DataSource dataSource;

    public ProductDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private void checkSaveRestrictions(Product product) {
        if (isNull(product) ||
                isNull(product.getName()) ||
                isNull(product.getProducer()) ||
                isNull(product.getPrice()) ||
                isNull(product.getExpirationDate()) ||
                isNull(product.getCreationTime())) {
            throw new DaoOperationException(String.format(ERROR_SAVING_PRODUCT_STRING, product));
        }
    }
    private void checkUpdateRestrictions(Product product) {
        if (isNull(product)) {
            throw new DaoOperationException(String.format(ERROR_SAVING_PRODUCT_STRING, product));
        }
        checkId(product.getId());
    }
    private void checkId(Long id) {
        if (isNull(id)) {
            throw new DaoOperationException("Product id cannot be null");
        }
    }

    @Override
    public void save(Product product) {
        checkSaveRestrictions(product);
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(SQL_SAVE_PRODUCT);
            statement.setLong(1, product.getId());
            statement.setString(2, product.getName());
            statement.setString(3, product.getProducer());
            statement.setBigDecimal(4, product.getPrice());
            Date expirationDate = Date.valueOf(product.getExpirationDate());
            statement.setDate(5, expirationDate);
            Timestamp timestamp = Timestamp.valueOf(product.getCreationTime());
            statement.setTimestamp(6, timestamp);
            connection.commit();
        } catch (SQLException e) {
            throw new DaoOperationException(String.format(ERROR_SAVING_PRODUCT_STRING, product));
        }
    }

    @Override
    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(SQL_FIND_ALL_PRODUCTS);
            while (resultSet.next()) {
                long id = resultSet.getLong(1);
                String name = resultSet.getString(2);
                String producer = resultSet.getString(3);
                BigDecimal price = resultSet.getBigDecimal(4);
                LocalDate localDate = resultSet.getDate(5).toLocalDate();
                LocalDateTime localDateTime = resultSet.getTimestamp(6).toLocalDateTime();
                products.add(new Product(id, name, producer, price, localDate, localDateTime));
            }
        } catch (SQLException e) {
            throw new DaoOperationException("Problem while findAll operation", e);
        }
        return products;
    }

    @Override
    public Product findOne(Long id) {
        checkId(id);
        Product product = null;
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(SQL_FIND_PRODUCT);
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery(SQL_FIND_PRODUCT);
            if (resultSet.next()) {
                String name = resultSet.getString(1);
                String producer = resultSet.getString(2);
                BigDecimal price = resultSet.getBigDecimal(3);
                LocalDate localDate = resultSet.getDate(4).toLocalDate();
                LocalDateTime localDateTime = resultSet.getTimestamp(5).toLocalDateTime();
                product = new Product(id, name, producer, price, localDate, localDateTime);
            }
        } catch (SQLException e) {
            throw new DaoOperationException(String.format(ERROR_EXISTENCE_PRODUCT_STRING, id), e);
        }
        return product;
    }

    @Override
    public void update(Product product) {
        checkUpdateRestrictions(product);
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_PRODUCT);
            statement.setString(1, product.getName());
            statement.setString(2, product.getProducer());
            statement.setBigDecimal(3, product.getPrice());
            Date expirationDate = Date.valueOf(product.getExpirationDate());
            statement.setDate(4, expirationDate);
            Timestamp timestamp = Timestamp.valueOf(product.getCreationTime());
            statement.setTimestamp(5, timestamp);
            statement.setLong(6, product.getId());
        } catch (SQLException e) {
            throw new DaoOperationException(String.format(ERROR_UPDATING_PRODUCT_STRING, product));
        }
    }

    @Override
    public void remove(Product product) {
        checkUpdateRestrictions(product);
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(SQL_REMOVE_PRODUCT);
            statement.setLong(1, product.getId());
            var result = statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoOperationException("Problem while findOne operation", e);
        }
    }

}
