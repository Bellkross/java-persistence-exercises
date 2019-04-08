package ua.procamp.dao;

import ua.procamp.model.Account;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.List;

public class AccountDaoImpl implements AccountDao {
    private EntityManagerFactory emf;

    public AccountDaoImpl(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public void save(Account account) {
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
        EntityManager entityManager = emf.createEntityManager();
        entityManager.getTransaction().begin();
        try {
            Account acc = entityManager.find(Account.class, account.getId());
            acc.setEmail(account.getEmail());
            acc.setCreationTime(account.getCreationTime());
            acc.setBirthday(account.getBirthday());
            acc.setBalance(account.getBalance());
            acc.setFirstName(account.getFirstName());
            acc.setGender(account.getGender());
            entityManager.persist(acc);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
        } finally {
            entityManager.close();
        }
    }

    @Override
    public void remove(Account account) {
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

