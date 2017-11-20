package dsi;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DSIEventReport {
        private final String eventId;
        private final String mementoKey;
        private final List<String> threads = new ArrayList<>();
        private final List<DSIRecord> records = new ArrayList<>();
        private final boolean displayAll;
        
        public DSIEventReport(String eventId, String mementoKey, boolean displayAll) {
                this.eventId = eventId;
                this.mementoKey = mementoKey;
                this.displayAll = displayAll;
        }
        
        private static String format(LocalDateTime t) {
                final DateTimeFormatter df = DateTimeFormatter.ofPattern("YYYY/MM/dd HH:mm:ss.nnnn");
                
                return df.format(t);
        }
        
        private void print(DSIRecord rec) {
                String msg;
                Matcher m;
                final Pattern saveMementoPattern = Pattern.compile(".*saveMemento\\(EngineMemento\\) MementoKey id=([^ ]+):\\s+(.*)");
                final Pattern getMementoPattern = Pattern.compile(".*getMemento MementoKey id=([^ ]+):\\s+(.*)");
                final Pattern takePattern = Pattern.compile(".*take\\(\\): key=.*uuid=([^\\]]+)\\].*");
                final Pattern removingPattern = Pattern.compile(".*remove\\(\\): key.*uuid=([^\\]]+)\\].*");
                final Pattern removedPattern = Pattern.compile(".*remove\\(\\): performed.*uuid=([^\\]]+)\\].*");
                
                msg = rec.getMessage();
                                
                if (msg.contains("Starting Event Processor")) {
                        msg = "Start Event Processor " + eventId;
                        System.out.println(format(rec.getTime()) + " " + rec.getThreadId() + " " + msg);
                        return ;
                }
                
                if (msg.contains("Completed Event Processor")) {
                        msg = "Completed Event Processor " + eventId;
                        System.out.println(format(rec.getTime()) + " " + rec.getThreadId() + " " + msg);
                        return ;
                } 
                
                m = saveMementoPattern.matcher(msg);
                if (m.matches()) {
                        if (mementoKey == null || m.group(1).contains(mementoKey)) {
                                msg = "Save " + m.group(1) + "\n\t\t" + m.group(2);
                                System.out.println(format(rec.getTime()) + " " + rec.getThreadId() + " " + msg);
                                return ;
                        }
                }
                         
                m = getMementoPattern.matcher(msg);
                if (m.matches()) {
                        if (mementoKey == null || m.group(1).contains(mementoKey)) {
                                msg = "Get " + m.group(1) + "\n\t\t" + m.group(2);
                                System.out.println(format(rec.getTime()) + " " + rec.getThreadId() + " " + msg);
                                return ;
                        }       
                }
                
                m = takePattern.matcher(msg);
                if (m.matches() && m.group(1).equals(eventId)) {
                        System.out.println(format(rec.getTime()) + " " + rec.getThreadId() + " Take " + m.group(1));
                        return ;
                }

                m = removingPattern.matcher(msg);
                if (m.matches()) {
                        System.out.println(format(rec.getTime()) + " " + rec.getThreadId() + " Removing " + m.group(1));
                        return ;
                }

                
                m = removedPattern.matcher(msg);
                if (m.matches()) {
                        System.out.println(format(rec.getTime()) + " " + rec.getThreadId() + " Removed " + m.group(1));
                        return ;
                }

                if (displayAll) {                
                        msg = msg.replace("\n", "\n\t");
                        System.out.println(format(rec.getTime()) + " " + rec.getThreadId() + " " + msg);
                }                                               
        }
        
        private boolean contains(String str, List<String> l) {
                for (String s: l)
                        if (s.trim().equals(str.trim()))
                                return true;
                
                return false;
        }
        
        public void add(DSIRecord rec) {
                String msg;

                /* Keep only records of the threads which are processing the events */
                msg = rec.getMessage();
                if (msg.contains(eventId)) {
                        if (msg.contains("Starting Event Processor")) {
                                threads.add(rec.getThreadId());
                                records.add(rec);
                        } else if (msg.contains("Completed Event Processor")) {
                                threads.remove(rec.getThreadId());
                                records.add(rec);
                        } else {
                                records.add(rec);
                        }
                } else if (contains(rec.getThreadId(), threads)) {
                        records.add(rec);
                }
        }
        
        public void printReport() {
                for (DSIRecord rec: records) {
                        print(rec);
                }
        }
}
