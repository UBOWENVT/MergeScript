import java.io.*;
import java.util.ArrayList;
import java.util.Timer;
import java.util.concurrent.Semaphore;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import org.apache.commons.io.FileUtils;

public class MergeScript {
    //Set path
    static String project_name = "drjava";
    static String project_path = "/home/ppp/IdeaProjects/CodemergeStudy/assemblyline";
    static String dataset_path = "/home/ppp/IdeaProjects/MergeScript/Dataset/Success";
    static String JDime_problemset_path = "/home/ppp/IdeaProjects/MergeScript/Dataset/JDimeError";
    static String MergeMine_problemset_path = "/home/ppp/IdeaProjects/MergeScript/Dataset/MergeMinerError";

    //Set the longest waiting time to wait for a task to execute
    private static final int MAX_WAITINGTIME = 10*60*1000;

    public void call() throws Exception {
        //Create a logger to record all operation
        Logger logger = Logger.getLogger("MyLog");
        FileHandler fh;
        // This block configure the logger with handler and formatter
        fh = new FileHandler(project_path + "/logger.txt");
        logger.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();
        fh.setFormatter(formatter);


        //Read all project folders in the path
        File file = new File(project_path + "/" + project_name);
        File[] tmplist = file.listFiles();
        ArrayList<String> commit_folders = new ArrayList<String>();
        for (int i = 0; i < tmplist.length; i++) {
            if (tmplist[i].isDirectory()) {
                commit_folders.add(tmplist[i].toString());
            }
        }

        //In each folder, run JDime to get the output
        for (int i = 0; i < commit_folders.size(); i++) {
            String new_path = commit_folders.get(i);
            //function declartion
            //logger, new_args[]
            logger.info("Start working on commit\t" + new_path);
            file = new File(new_path);
            tmplist = file.listFiles();
            for (int j = 0; j < tmplist.length; j++) {
                if (tmplist[j].isDirectory()) {
                    //Start merging
                    logger.info("Start merging on file\t" + tmplist[j].toString());
                    String[] new_args = new String[8];
                    //Example commands
                    // "-f --mode linebased,structured --output ./*_merged.java ./*_left.java ./*_base.java ./*_right.java"
                    new_args[0] = "-f";
                    new_args[1] = "--mode";
                    new_args[2] = "linebased,structured";
                    new_args[3] = "--output";
                    //Create a new java file, overwrite if exist
                    String JDime_file = tmplist[j].toString() + "/JDime_merged.java";
                    new_args[4] = JDime_file;
                    FileWriter f_writer = new FileWriter(JDime_file);
                    f_writer.close();

                    //Now we are in folder of separate java file
                    //Get the three versions of files to be merged
                    File[] java_tmplist = tmplist[j].listFiles();
                    String[] temp;
                    //Find java file name ends with "left"
                    temp = tmplist[j].list(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {
                            return name.endsWith("_left.java");
                        }
                    });
                    if (temp.length != 1) {
                        logger.severe("Multiple left file found");
                    } else {
                        new_args[5] = tmplist[j].toString() + "/" + temp[0];
                        File f = new File(new_args[5]);
                        if (f.length() == 0)
                        {
                            //Split the path and combine it
                            String[] arrOfStr = tmplist[j].toString().split("/", 7);
                            String path_partB = arrOfStr[6];
                            String targetPath = MergeMine_problemset_path + "/" + path_partB;
                            //Move directory to MergeMiner problem set
                            FileUtils.moveDirectory(
                                    new File(tmplist[j].toString()),
                                    new File(targetPath));
                            logger.info("0 byte file detected, move to MergeMinerError");
                            continue;
                        }
                    }
                    //Find java file name ends with "base"
                    temp = tmplist[j].list(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {
                            return name.endsWith("_base.java");
                        }
                    });
                    if (temp.length != 1) {
                        logger.severe("Multiple base file found");
                    } else {
                        new_args[6] = tmplist[j].toString() + "/" + temp[0];
                        File f = new File(new_args[6]);
                        if (f.length() == 0)
                        {
                            //Split the path and combine it
                            String[] arrOfStr = tmplist[j].toString().split("/", 7);
                            String path_partB = arrOfStr[6];
                            String targetPath = MergeMine_problemset_path + "/" + path_partB;
                            //Move directory to MergeMiner problem set
                            FileUtils.moveDirectory(
                                    new File(tmplist[j].toString()),
                                    new File(targetPath));
                            logger.info("0 byte file detected, move to MergeMinerError");
                            continue;
                        }
                    }
                    //Find java file name ends with "right"
                    temp = tmplist[j].list(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {
                            return name.endsWith("_right.java");
                        }
                    });
                    if (temp.length != 1) {
                        logger.severe("Multiple right file found");
                    } else {
                        new_args[7] = tmplist[j].toString() + "/" + temp[0];
                        File f = new File(new_args[7]);
                        if (f.length() == 0)
                        {
                            //Split the path and combine it
                            String[] arrOfStr = tmplist[j].toString().split("/", 7);
                            String path_partB = arrOfStr[6];
                            String targetPath = MergeMine_problemset_path + "/" + path_partB;
                            //Move directory to MergeMiner problem set
                            FileUtils.moveDirectory(
                                    new File(tmplist[j].toString()),
                                    new File(targetPath));
                            logger.info("0 byte file detected, move to MergeMinerError");
                            continue;
                        }
                    }

                    //Everything is ready, let's go
                    String whole_line = "./JDime " + String.join(" ", new_args);
                    //Create task to be executed in terminal
                    ThreadTerminalCommand task1 = new ThreadTerminalCommand(whole_line,
                            JDime_file,tmplist[j].toString(),dataset_path,logger);
                    //Acquire semaphore before execution
                    SemaphoreControl signal = new SemaphoreControl();
                    signal.Sema_accquire();
                    //Start the task
                    task1.start();
                    //Create a timer task to monitor the execution of task
                    Timer task_monitor = new Timer();
                    task_monitor.schedule(new ThreadTimerTask(task1,
                                    tmplist[j].toString(),JDime_problemset_path,logger),
                            MAX_WAITINGTIME);
                    //Wait for semaphore released, two possible condition
                    signal.Sema_accquire();
                    if (!task1.isInterrupted())
                    {
                        //task finish normally, cancel the monitor
                        task_monitor.cancel();
                    }
                    signal.Sema_release();
                }
            }
        }
        logger.info("Finish all work of project "+ project_name);
        FileUtils.moveFileToDirectory(new File(project_path +
                       "/logger.txt"),
                new File(dataset_path + "/" + project_name), true);
    }

    public static void main(String[] args) {
        try{
            MergeScript script = new MergeScript();
            script.call();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
