import java.util.TimerTask;
import org.apache.commons.io.FileUtils;
import java.util.logging.Logger;
import java.io.*;

public class ThreadTimerTask extends TimerTask {
    ThreadTerminalCommand task;
    String source_path;
    String target_path;
    Logger logger;

    public ThreadTimerTask(ThreadTerminalCommand arg_task, String arg_source_path,
                                 String arg_target_path, Logger arg_logger)
    {
        task = arg_task;
        source_path = arg_source_path;
        target_path = arg_target_path;
        logger = arg_logger;
    }
    public void run()
    {
        task.interrupt();
        //Complete the following task
        //Copy and paste the folder
        //Split the path and combine it
        String[] arrOfStr = source_path.split("/", 7);
        String path_partB = arrOfStr[6];
        String targetPath = target_path + "/" + path_partB;
        //move source to target using FileUtils Class
        try {
            FileUtils.moveDirectory(new File(source_path), new File(targetPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Finish copying
        logger.info("JDime failed to execute, copying folder to special case in dataset");

        //Release semaphore
        SemaphoreControl signal = new SemaphoreControl();
        signal.Sema_release();
    }
}
