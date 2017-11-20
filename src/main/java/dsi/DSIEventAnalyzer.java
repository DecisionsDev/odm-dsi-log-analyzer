package dsi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class DSIEventAnalyzer extends Analyzer {

        private static final Logger LOG = Logger.getLogger(DSIEventAnalyzer.class.getName());

        @Parameter(names = { "-h", "--help" }, help = true)
        private boolean help;
        
        @Parameter(names = { "--event-id" })
        public String eventId;
        
        @Parameter(names = { "--memento-key" })
        public String mementoKey;
        
        @Parameter(names = { "--display-all" })
        public boolean displayAll = false;
        
        @Parameter(description = "root dirs of the DSI logs files")
        private List<String> dirs;

        private int process(String[] args) throws IOException, InterruptedException {
                JCommander jc;

                jc = new JCommander(this, args);

                if (help) {
                        jc.usage();
                        System.exit(0);
                }

                if (args == null || args.length == 0 || eventId == null) {
                        jc.usage();
                        System.exit(1);
                }

                return process(dirs);
        }
        
        public static void main(String[] args) throws IOException, InterruptedException {
                DSIEventAnalyzer analyzer;
                int ret;

                analyzer = new DSIEventAnalyzer();

                ret = analyzer.process(args);                               
                
                LOG.info("Exit code: " + ret);
                
                if (ret > 0) {
                        System.err.println("Exit with error code: " + ret);
                        System.exit(ret);
                }
        }

        @Override
        protected int analyze(List<File> logs) throws IOException, InterruptedException {
                final DSIEventReport report;
                
                report = new DSIEventReport(eventId, mementoKey, displayAll);
                
                for (final File log: logs)
                        analyze(log, report);

                LOG.info("Finished parsing of all logs");
                
                report.printReport();
                
                return 0;
        }                
                
        protected int analyze(File log, DSIEventReport report) throws IOException {
                BufferedReader reader;
                String line;
                final Pattern recordPattern = Pattern.compile("\\[([^\\]]*)\\]\\s+([^ ]*)\\s+([^ ]*)\\s+([^ ]*)\\s+(.)\\s+(.*)");
                Matcher m;
                DSIRecord rec, lastRec;
                int recType;
                LocalDateTime ldt;
                long n;
                
                LOG.info("Analyze file: " + log);
                
                reader = new BufferedReader(new FileReader(log));
                
                try {                        
                        line = reader.readLine();
                        if (line == null || !line.equals("********************************************************************************")) {
                                System.err.println("Wrong begining of the log trace header");
                                return 1;
                        }
                        
                        while ((line = reader.readLine()) != null) {
                                if (line.equals("********************************************************************************"))
                                        break;
                        }                        
                        if (line == null) {
                                System.err.println("Wrong ending of the log trace header");
                                return 1;
                        }
                        
                        lastRec = null;
                        n = 0;
                        while ( (line = reader.readLine()) != null) {
                                m = recordPattern.matcher(line);
                                if (m.matches()) {
                                        ldt = getDateTime(m.group(1));
                                        
                                        if (ldt == null) {
                                                System.err.println("Cannot parse date-time: " + m.group(1));
                                        } else {
                                                rec = new DSIRecord(ldt,
                                                                    m.group(2),
                                                                    m.group(4),
                                                                    m.group(5),
                                                                    m.group(6));
                                                                                
                                                recType = rec.getType();
                                        
                                                if (recType == -1) {
                                                        System.err.println("Unknown record type:" + rec.getTypeAsString()
                                                                           + " of " + m.group(0));
                                                        lastRec = null;
                                                } else {
                                                        report.add(rec);                                                        
                                                        lastRec = rec; 
                                                }
                                        }     
                                        n++;
                                } else {
                                        if (lastRec == null)
                                                System.err.println("Failed to parse: " + line);
                                        else 
                                                lastRec.appendMessage(line);
                                }
                        }
                        
                        LOG.info("Finished analyze of " + log + " number of parsed records: " + n);
                } finally {                                        
                        reader.close();
                }
                return 0;
        }
}
