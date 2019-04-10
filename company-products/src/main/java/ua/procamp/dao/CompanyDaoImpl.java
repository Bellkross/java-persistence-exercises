package ua.procamp.dao;

import ua.procamp.exception.CompanyDaoException;
import ua.procamp.model.Company;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.Objects;

public class CompanyDaoImpl implements CompanyDao {
    private EntityManagerFactory entityManagerFactory;

    public CompanyDaoImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public Company findByIdFetchProducts(Long id) {
        Objects.requireNonNull(id);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Company c;
        try {
            entityManager.getTransaction().begin();
            c = entityManager.createQuery(
                    "select c from company c left join fetch c.products where c.id = :id",
                    Company.class
            ).setParameter("id", id).getSingleResult();
        } catch (Exception e) {
            throw new CompanyDaoException(String.format("No entity found with id %d", id), e);
        } finally {
            entityManager.close();
        }
        return c;
    }
}
