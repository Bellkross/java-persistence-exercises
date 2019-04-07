package ua.procamp.locking;

import ua.procamp.locking.exception.OptimisticLockingException;
import ua.procamp.locking.optimistic.OptimisticLockingDao;

import java.util.concurrent.atomic.AtomicBoolean;

public class Main {

    private static OptimisticLockingDao olDao = new OptimisticLockingDao(
            DataSourceProvider.getDataSource()
    );
    private static AtomicBoolean noLocks = new AtomicBoolean(true);

    public static void main(String[] args) throws InterruptedException {
        Program program1 = olDao.findProgramById(1L).get();
        program1.name = "empty";
        olDao.updateProgram(program1);
        while (noLocks.get()) {
            startAppendingThread();
            Thread.sleep(100);
        }
    }

    private static void startAppendingThread() {
        new Thread(() -> {
            while (noLocks.get()) {
                try {
                    appendOneToTheProgram1Name(olDao);
                } catch (OptimisticLockingException lockingException) {
                    System.out.println("Received an optimistic locking exception!");
                    noLocks.set(false);
                }
            }
        }).start();
    }

    private static void appendOneToTheProgram1Name(OptimisticLockingDao olDao) {
        Program p1 = olDao.findProgramById(1L).get();
        p1.name += "1";
        olDao.updateProgram(p1);
        int plen = p1.name.length();
        p1.name = p1.name.substring(0, plen - 1);
        olDao.updateProgram(p1);
    }
}
