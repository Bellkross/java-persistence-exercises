package ua.procamp.dao;

import org.hibernate.Session;
import ua.procamp.model.Photo;
import ua.procamp.model.PhotoComment;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Please note that you should not use auto-commit mode for your implementation.
 */
public class PhotoDaoImpl implements PhotoDao {
    private EntityManagerFactory entityManagerFactory;

    public PhotoDaoImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public void save(Photo photo) {
        consumeWithTx(em -> em.persist(photo));
    }

    @Override
    public Photo findById(long id) {
        return readWithTx(em ->
                em.createQuery("select p from photo p where p.id = :id", Photo.class)
                        .setParameter("id", id).getSingleResult()
        );
    }

    @Override
    public List<Photo> findAll() {
        return readWithTx(em -> em.createQuery("select p from photo p", Photo.class).getResultList());
    }

    @Override
    public void remove(Photo photo) {
        Objects.requireNonNull(photo);
        Objects.requireNonNull(photo.getId());
        consumeWithTx(em -> {
            Photo ph = em.find(Photo.class, photo.getId());
            em.remove(ph);
        });
    }

    @Override
    public void addComment(long photoId, String comment) {
        Objects.requireNonNull(comment);
        consumeWithTx(em -> {
            Photo p = em.find(Photo.class, photoId);
            PhotoComment pc = new PhotoComment();
            pc.setText(comment);
            pc.setPhoto(p);
            em.persist(pc);
            p.addComment(pc);
        });
    }

    private void consumeWithTx(Consumer<EntityManager> entityManagerTFunction) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        try {
            entityManagerTFunction.accept(entityManager);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw new RuntimeException("Read transaction was failed", e);
        } finally {
            entityManager.close();
        }
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
            throw new RuntimeException("Read transaction was failed", e);
        } finally {
            entityManager.close();
        }
    }

    private void makeReadOnly(EntityManager em) {
        em.unwrap(Session.class).setDefaultReadOnly(true);
    }
}
