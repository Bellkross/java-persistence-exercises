package ua.procamp.locking;

import ua.procamp.locking.exception.OptimisticLockingException;
import ua.procamp.locking.optimistic.OptimisticLockingDao;
import ua.procamp.locking.pessimistic.PessimisticLockingDao;

import java.util.concurrent.atomic.AtomicBoolean;

public class Main {

    private static OptimisticLockingDao olDao = new OptimisticLockingDao(
            DataSourceProvider.getDataSource()
    );
    private static PessimisticLockingDao plDao = new PessimisticLockingDao(
            DataSourceProvider.getDataSource()
    );
    private static AtomicBoolean noLocks = new AtomicBoolean(true);

    public static void main(String[] args) throws InterruptedException {
        testOptimisticLocking();

        // testPessimisticLocking();
    }

    private static void testPessimisticLocking() throws InterruptedException {
        Program program1 = olDao.findProgramById(1L).get();
        program1.name = "empty";
        plDao.updateProgram(program1);
        for(int i = 0; i < 2; ++i) {
            startAppendingPessimisticThread();
        }
    }

    private static void startAppendingPessimisticThread() {
        new Thread(() -> {
            for(int i = 0; i < 10; ++i) {
                appendPessimisticOneToTheProgram1Name();
            }
        }).start();
    }

    private static void appendPessimisticOneToTheProgram1Name() {
        Program p1 = plDao.findProgramById(1L).get();
        p1.name += "1";
        plDao.updateProgram(p1);
        int plen = p1.name.length();
        p1.name = p1.name.substring(0, plen - 1);
        plDao.updateProgram(p1);
    }

    private static void testOptimisticLocking() throws InterruptedException {
        Program program1 = olDao.findProgramById(1L).get();
        program1.name = "empty";
        olDao.updateProgram(program1);
        while (noLocks.get()) {
            startAppendingOptimisticThread();
            Thread.sleep(100);
        }
    }

    private static void startAppendingOptimisticThread() {
        new Thread(() -> {
            while (noLocks.get()) {
                try {
                    appendOptimisticOneToTheProgram1Name();
                } catch (OptimisticLockingException lockingException) {
                    System.out.println("Received an optimistic locking exception!");
                    noLocks.set(false);
                }
            }
        }).start();
    }

    private static void appendOptimisticOneToTheProgram1Name() {
        Program p1 = olDao.findProgramById(1L).get();
        p1.name += "1";
        olDao.updateProgram(p1);
        int plen = p1.name.length();
        p1.name = p1.name.substring(0, plen - 1);
        olDao.updateProgram(p1);
    }
}
