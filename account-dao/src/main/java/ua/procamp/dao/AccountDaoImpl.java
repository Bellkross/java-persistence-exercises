package ua.procamp.dao;

import ua.procamp.exception.AccountDaoException;
import ua.procamp.model.Account;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.isNull;

public class AccountDaoImpl implements AccountDao {
    private EntityManagerFactory emf;

    public AccountDaoImpl(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public void save(Account account) {
        Objects.requireNonNull(account);
        requireCorrectAccount(account);
        EntityManager entityManager = emf.createEntityManager();
        entityManager.getTransaction().begin();
        try {
            entityManager.persist(account);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
        } finally {
            entityManager.close();
        }
    }

    @Override
    public Account findById(Long id) {
        Objects.requireNonNull(id);
        EntityManager entityManager = emf.createEntityManager();
        entityManager.getTransaction().begin();
        Account result = null;
        try {
            result = entityManager
                    .createQuery("select a from Account a where a.id = :id", Account.class)
                    .setParameter("id", id)
                    .getSingleResult();
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
        } finally {
            entityManager.close();
        }
        return result;
    }

    @Override
    public Account findByEmail(String email) {
        Objects.requireNonNull(email);
        EntityManager entityManager = emf.createEntityManager();
        entityManager.getTransaction().begin();
        Account result = null;
        try {
            result = entityManager
                    .createQuery("select a from Account a where a.email = :email", Account.class)
                    .setParameter("email", email)
                    .getSingleResult();
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
        } finally {
            entityManager.close();
        }
        return result;
    }

    @Override
    public List<Account> findAll() {
        EntityManager entityManager = emf.createEntityManager();
        entityManager.getTransaction().begin();
        List<Account> result = new ArrayList<>();
        try {
            result = entityManager
                    .createQuery("select a from Account a", Account.class)
                    .getResultList();
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
        } finally {
            entityManager.close();
        }
        return result;
    }

    @Override
    public void update(Account account) {
        Objects.requireNonNull(account);
        Objects.requireNonNull(account.getId());
        requireCorrectAccount(account);
        EntityManager entityManager = emf.createEntityManager();
        entityManager.getTransaction().begin();
        try {
            Account acc = entityManager.merge(account);
            entityManager.persist(acc);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
        } finally {
            entityManager.close();
        }
    }

    private void requireCorrectAccount(Account account) {
        if (isNull(account.getFirstName()) || isNull(account.getLastName()) ||
                isNull(account.getEmail()) || isNull(account.getBirthday()) ||
                isNull(account.getGender()) || isNull(account.getCreationTime())) {
            throw new AccountDaoException("Attempt to incorrect updating", new IllegalArgumentException());
        }
    }

    @Override
    public void remove(Account account) {
        Objects.requireNonNull(account);
        EntityManager entityManager = emf.createEntityManager();
        entityManager.getTransaction().begin();
        try {
            Account acc = entityManager.find(Account.class, account.getId());
            entityManager.remove(acc);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
        } finally {
            entityManager.close();
        }
    }
}

