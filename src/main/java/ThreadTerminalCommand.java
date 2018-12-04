import org.apache.commons.io.FileUtils;
import java.util.logging.Logger;
import java.io.*;

public class ThreadTerminalCommand extends Thread{

    String whole_line;
    String JDime_file;
    String source_path;
    String target_path;
    Logger logger;

    public ThreadTerminalCommand(String arg_whole_line, String arg_JDime_file,
                                 String arg_source_path, String arg_target_path, Logger arg_logger)
    {
        whole_line = arg_whole_line;
        JDime_file = arg_JDime_file;
        source_path = arg_source_path;
        target_path = arg_target_path;
        logger = arg_logger;
    }


    public void run()
    {
        //Generate commands and run in terminal environment
        String[] cmds = {"/bin/sh", "-c",
                "cd /home/ppp/IdeaProjects/JDime/build/install/JDime/bin/" + " && " +
                        whole_line};

        try {
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(cmds);
            //Define a flag to determine the following operation
            boolean flag = false;
            //Read stream from terminal
            int exit = proc.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = "";
            if (exit != 0) {
                //Output error information
                BufferedReader error = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                StringBuffer sb = new StringBuffer();
                while ((line = error.readLine()) != null) {
                    sb.append(line + "\n");
                }
                System.err.println(sb.toString());
                FileWriter error_txt = new FileWriter(source_path+"/error.txt");
                error_txt.write(sb.toString());
                error_txt.close();
                //Still copy this path to dataset
                flag = true;
                logger.info("Error in merge,start copying to dataset");
            }
            else
            {
                //Check the JDime merge result whether contain unresolved conflicts
                if (check_conflict(JDime_file))
                {
                    flag = true;
                    //Found merge conflicts, select into dataset
                    //Start copying
                    logger.info("Found conflict,start copying to dataset");
                }
            }

                //Split the path and combine it
                String[] arrOfStr = source_path.split("/", 7);
                String path_partB = arrOfStr[6];
                String targetPath = target_path + "/" + path_partB;
            if (flag)
            {
                //move source to target using FileUtils Class
                try {
                    FileUtils.moveDirectory(new File(source_path), new File(targetPath));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //Finish copying
                logger.info("Finish copying to dataset");
            }
            else
            {
                //Just delete the directory
                try {
                    FileUtils.deleteDirectory(new File(source_path));
                } catch (IOException e)
                {
                    e.printStackTrace();
                }

            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        //Release semaphore
        SemaphoreControl signal = new SemaphoreControl();
        signal.Sema_release();

    }

    private static boolean check_conflict(String path) throws IOException{
        boolean conflict_flag = false;
        BufferedReader reader = new BufferedReader(new FileReader(path));
        while (true)
        {
            String str = reader.readLine();
            if (str == null)
                break;
            else
            if (str.contains("<<<<<<"))
            {
                conflict_flag = true;
                break;
            }
        }
        return conflict_flag;
    }

}
