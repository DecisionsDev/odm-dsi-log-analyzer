package dsi;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DSIRecord implements Comparable<DSIRecord> {
        private final String threadId;
        private final String loggerName;
        private final String type;
        private String message;
        private final LocalDateTime time;
        
        public static final int TYPE_ENTRY = 1;
        public static final int TYPE_RETURN = 2;
        public static final int TYPE_INFO = 3;
        public static final int TYPE_FINEST = 4;
        public static final int TYPE_FINE = 5;
        public static final int TYPE_FINER = 6;
        public static final int TYPE_AUDIT = 7;
        public static final int TYPE_WARNING = 8;
        public static final int TYPE_ERROR = 9;
        public static final int TYPE_STDERR = 10;
        
        public DSIRecord(LocalDateTime time,
                         String threadId,
                         String loggerName,
                         String type,
                         String message) {
                this.time = time;
                this.threadId = threadId;
                this.loggerName = loggerName;
                this.type = type;
                this.message = message;
        }
        
        public String getThreadId() {
                return threadId;
        }
        
        public LocalDateTime getTime() {
                return time;
        }
        
        public String getMessage() {
                return message;
        }
        
        public static final Pattern PATTERN_ERROR_CODE = Pattern.compile("(.+): .*");
        
        public String getErrorCode() {
                Matcher m;
                
                m = PATTERN_ERROR_CODE.matcher(message);
                
                if (m.matches())                
                        return m.group(1);
                
                return null;
        }
        
        public void appendMessage(String newChunk) {
                message += "\n" + newChunk;
        }
        
        public int getType() {
                switch (type) {
                case "<":
                        return TYPE_ENTRY;
                case ">":
                        return TYPE_RETURN;
                case "I":
                        return TYPE_INFO;
                case "3":
                        return TYPE_FINEST;
                case "1":
                        return TYPE_FINE;
                case "2":
                        return TYPE_FINER;
                case "A":
                        return TYPE_AUDIT;
                case "W":
                        return TYPE_WARNING;
                case "E":
                        return TYPE_ERROR;       
                case "R":
                        return TYPE_STDERR;
                }
                
                return -1;
        };
        
        public String getTypeAsString() {
                return type;
        }

        @Override
        public int compareTo(DSIRecord r) {
                return time.compareTo(r.time);
        }
        
        @Override
        public String toString() {
                return getTypeAsString() + ":" + message;
        }
}
