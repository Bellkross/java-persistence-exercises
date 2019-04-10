package ua.procamp.dao;

import org.hibernate.Session;
import ua.procamp.exception.CompanyDaoException;
import ua.procamp.model.Company;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.Objects;
import java.util.function.Function;

public class CompanyDaoImpl implements CompanyDao {
    private EntityManagerFactory entityManagerFactory;

    public CompanyDaoImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public Company findByIdFetchProducts(Long id) {
        Objects.requireNonNull(id);
        return readWithTx(em -> em.createQuery(
                "select c from company c left join fetch c.products where c.id = :id",
                Company.class
        ).setParameter("id", id).getSingleResult());
    }

    private <T> T readWithTx(Function<EntityManager, T> entityManagerTFunction) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        makeReadOnly(entityManager);
        try {
            T result = entityManagerTFunction.apply(entityManager);
            entityManager.getTransaction().commit();
            return result;
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw new CompanyDaoException("Read transaction was failed", e);
        } finally {
            entityManager.close();
        }
    }

    private void makeReadOnly(EntityManager em) {
        em.unwrap(Session.class).setDefaultReadOnly(true);
    }
}
