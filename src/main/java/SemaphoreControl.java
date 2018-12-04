import java.util.concurrent.Semaphore;

public class SemaphoreControl{
    //Set maximum thread number
    private static final int MAX_AVAILABLE = 1;
    private static final Semaphore available = new Semaphore(MAX_AVAILABLE, false);

    public static void Sema_accquire() throws Exception
    {
        available.acquire();
    }
    public static void Sema_release()
    {
        available.release();
    }
}
