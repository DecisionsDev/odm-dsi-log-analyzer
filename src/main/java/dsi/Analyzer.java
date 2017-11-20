package dsi;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class Analyzer {
        protected abstract int analyze(List<File> logs) throws IOException, InterruptedException;
        
        private static final DateTimeFormatter dtf_std = DateTimeFormatter.ofPattern("M/d/yy H:m:s:n z");
        private static final DateTimeFormatter dtf_bst = DateTimeFormatter.ofPattern("d/M/yy H:m:s:n z");
        
        protected LocalDateTime getDateTime(String str) throws IOException {
                DateTimeFormatter dtf;
                
                if (str.endsWith("BST"))
                        dtf = dtf_bst;
                else
                        dtf = dtf_std;

                return LocalDateTime.parse(str, dtf);
        }
        
        
        private void collectLogFiles(File f, List<File> logFiles) {
                if (f.isDirectory()) {
                        for (File child: f.listFiles())
                                collectLogFiles(child, logFiles);
                } else {
                        if (f.getName().matches("trace.*\\.log")
                            || f.getName().matches("messages.log")) {
                                logFiles.add(f);
                        }                                
                }
        }
        
        protected int process(List<String> strs) throws IOException, InterruptedException {
                List<File> logFiles;
                
                logFiles = new ArrayList<File>();
                
                for (String s: strs)
                        collectLogFiles(new File(s), logFiles);
                
                logFiles.sort(new Comparator<File>() {
                        @Override
                        public int compare(File o1, File o2) {
                                return o1.getAbsolutePath().compareTo(o2.getAbsolutePath());
                        }
                        
                });
                
                return analyze(logFiles);
        }
}
