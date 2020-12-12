import java.io.IOException;
import java.util.*;

import java.util.logging.*;

public class ProcessLogger {
	
	static Logger peerDataLogger;

	private static final Level InfoLevel = Level.INFO;
	
	public static Logger fetchPeerDataLogger(Integer peerId) {
		peerDataLogger = Logger.getLogger(ProcessLogger.class.getName());
		peerDataLogger.setLevel(InfoLevel);
		
		FileHandler handler = null;
		try {
			String logFile = "log_peer_" + peerId + ".log";
			handler = new FileHandler(logFile);
		}catch (Exception exception) {
			System.err.println("Exception Occurred while Handling the Log File: "+exception.getMessage());
			exception.printStackTrace();
		}
		
		handler.setFormatter(new SimpleFormatter() {
			private static final String format = "[%1$tF %1$tT] [%2$-7s] %3$s %n";

            @Override
            public synchronized String format(LogRecord record) {
                return String.format(format, new Date(record.getMillis()), record.getLevel().getLocalizedName(), record.getMessage());
            }
        });
		
		peerDataLogger.addHandler(handler);
		return peerDataLogger;
	}

}
