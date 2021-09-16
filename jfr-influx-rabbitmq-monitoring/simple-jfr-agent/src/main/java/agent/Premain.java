package agent;

import agent.jfr.JfrAgentMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.Instrumentation;

public class Premain {

    private static final Logger log = LoggerFactory.getLogger(Premain.class);

    // premain is run before application's main for statically loading agent
    public static void premain(String agentArgs, Instrumentation instrumentation) {

        try {
            log.info("Starting thread to attach JFR Monitor...");
            new Thread(new JfrAgentMonitor(), "JfrAgentMonitor").start();

        } catch (Throwable t) {
            log.error("Could not attach JFR Monitor!", t);
        }
    }

    // agentmain is run to dynamically load agent into already running java process
    public static void agentmain(String agentArgs, Instrumentation instrumentation) {

        log.warn("Agentmain is no-op");
    }
}
