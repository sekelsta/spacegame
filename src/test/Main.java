package sekelsta.test;

import java.io.PrintWriter;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import sekelsta.engine.network.NetworkManager;
import sekelsta.engine.network.MessageContext;

public class Main {
    public static void main(String[] args) {
        // Set up for all tests
        NetworkManager.context = new MessageContext();

        System.out.println("Starting tests");

        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
            .selectors(
                selectPackage("sekelsta.test")
            )
            .build();

        SummaryGeneratingListener listener = new SummaryGeneratingListener();

        try (LauncherSession session = LauncherFactory.openSession()) {
            Launcher launcher = session.getLauncher();
            launcher.registerTestExecutionListeners(listener);
            launcher.execute(request);
        }

        PrintWriter writer = new PrintWriter(System.out);
        TestExecutionSummary summary = listener.getSummary();
        summary.printTo(writer);
        summary.printFailuresTo(writer);
    }
}

