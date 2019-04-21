package receipt;

import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngineConfiguration;
import receipt.service.UserService;
import receipt.users.User;

import java.util.Scanner;

import static receipt.service.UserService.LOGOUT_ACTION;

public class Main {

    public static ProcessEngine engine;

    public static void main(String[] args) {
        ProcessEngineConfiguration configuration = ProcessEngineConfiguration
                .createStandaloneInMemProcessEngineConfiguration();

        try {
            engine = configuration
                    .setAsyncExecutorActivate(true) // For timer events
                    .buildProcessEngine();

            engine.getRepositoryService()
                    .createDeployment()
                    .addClasspathResource("ReceiptProcess.bpmn20.xml")
                    .key("receipt_process")
                    .deploy();

            mainLoop();

        } finally {
            if (engine != null) {
                engine.close();
            }
        }
    }

    private static void mainLoop() {
        UserService users = UserService.getInstance();

        User user = null;

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                if (user == null) {
                    user = users.loginUser(scanner);
                }

                if (users.executeUserAction(user, scanner) == LOGOUT_ACTION) {
                    user = null;
                }
            }
        }
    }

}
