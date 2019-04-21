package receipt.service;

import org.flowable.task.api.Task;
import receipt.Main;
import receipt.users.Controllor;
import receipt.users.Doctor;
import receipt.users.Patient;
import receipt.users.User;

import java.util.*;

public class UserService {

    private static volatile UserService instance = null;

    /**
     * Got invalid code/ id of activity
     */
    public static final int INVALID_CODE = -1;

    /**
     * Flag indicating user needs to be logged out
     */
    public static final boolean LOGOUT_ACTION = false;

    public static final boolean STAY_LOGGED_IN = true;

    private Map<String, User> users;

    private UserService() {
        users = new HashMap<>();

        Doctor doctor = new Doctor();
        users.put(doctor.getName(), doctor);

        Controllor controllor = new Controllor();
        users.put(controllor.getName(), controllor);

        Patient patient = new Patient();
        users.put(patient.getName(), patient);
    }

    public static UserService getInstance() {
        if (instance == null) {
            synchronized (UserService.class) {
                if (instance == null) {
                    instance = new UserService();
                }
            }
        }

        return instance;
    }

    public User loginUser(Scanner scanner) {
        System.out.print("Zadaj login: ");
        String login = scanner.nextLine().trim();
        System.out.println();

        while (!users.containsKey(login)) {
            System.out.print("Pouzivatel s menom " + login + " neexistuje. Prosim zadaj novy login: ");
            login = scanner.nextLine().trim();
            System.out.println();
        }

        return users.get(login);
    }

    public boolean executeUserAction(User user, Scanner scanner) {
        System.out.println("Mozne akcie: ");
        user.printActions();
        System.out.println();

        Set<Object> validActions = user.getValidActions();
        int action = readUntilIntFound(scanner);

        while (!validActions.contains(action)) {
            System.out.print("Akcia " + action + " nieje validna akcia. Prosim zadajte akciu zo zoznamu: ");
            action = readUntilIntFound(scanner);
        }

        // Read empty newline
        scanner.nextLine();
        return user.executeAction(action, scanner);
    }

    /**
     * @return First found int value, while discarding all other input
     */
    private int readUntilIntFound(Scanner scanner) {
        while (!scanner.hasNextInt()) {
            scanner.next();
        }

        return scanner.nextInt();
    }

    /**
     * @param upperBound Upper bound of the accepted interval of IDs [0, upperBound)
     * @param scanner    Scanner for reading the user input
     * @return Checked code, or {@code INVALID_CODE} if code is invalid
     */
    public int getActivityId(int upperBound, Scanner scanner) {
        while (!scanner.hasNextInt()) {
            System.out.println("Ocakava sa nezaporne cislo");
            scanner.next();
        }

        int receiptCode = scanner.nextInt();

        if (receiptCode < 0 || receiptCode >= upperBound) {
            return INVALID_CODE;
        }

//         Clear scanner input
//        while (scanner.hasNext()) {
//            scanner.next();
//        }

        return receiptCode;
    }

    /**
     * Loads user input, until {@code ano}, or {@code nie} is read.
     *
     * @param scanner Scanner for reading the user input
     * @return {@code true} if user answered {@code ano}, {@code false} if user answered {@code nie}
     */
    public boolean getYesNoAnswer(Scanner scanner) {
        String answer = scanner.next();

        while (!"ano".equals(answer) && !"nie".equals(answer)) {
            System.out.println("Prosim odpovedajte len ano, alebo nie.");
            answer = scanner.next();
        }

        return "ano".equals(answer);
    }

    /**
     * Get all tasks for certain user with specified ID
     *
     * @param username User to get tasks for
     * @param taskId   Id of task to get
     * @return List of all tasks found
     */
    public List<Task> getTasks(String username, String taskId) {
        return Main.engine.getTaskService()
                .createTaskQuery()
                .taskCandidateOrAssigned(username)
                .taskDefinitionKey(taskId)
                .list();
    }

}
