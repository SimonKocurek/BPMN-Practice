package receipt.users;

import org.flowable.task.api.Task;
import receipt.Main;
import receipt.entity.Drug;
import receipt.service.DrugService;
import receipt.service.UserService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static receipt.service.UserService.STAY_LOGGED_IN;

public class Doctor extends User {

    @Override
    public String getName() {
        return "Doctor";
    }

    @Override
    protected void registerActions() {
        registerAction(new Action("Vypln recept pre pacienta.", this::startReceiptProcess));

        registerAction(new Action("Vypis neschvalene recepty.", this::printRejectedReceipts));
        registerAction(new Action("Oprav neschaleny recept.", this::fixRejectedReceipt));
    }

    private boolean startReceiptProcess(Scanner scanner) {
        Drug prescribed = createReceipt(scanner);

        if (prescribed != null) {
            System.out.println("Vypisany recept na liek " + prescribed);

            Main.engine.getRuntimeService()
                    .startProcessInstanceByKey("receipt_process", Collections.singletonMap("drug", prescribed));
        }

        return STAY_LOGGED_IN;
    }

    private boolean printRejectedReceipts(Scanner scanner) {
        List<Task> rejectedReceiptTasks = getUserTasks("receipt_fix");

        System.out.println("Zamietnute lieky:");
        for (int i = 0; i < rejectedReceiptTasks.size(); i++) {
            Task task = rejectedReceiptTasks.get(i);
            Drug rejectedDrug = (Drug) taskService.getVariable(task.getId(), "drug");
            System.out.println(i + ": " + rejectedDrug);
        }

        return STAY_LOGGED_IN;
    }

    private boolean fixRejectedReceipt(Scanner scanner) {
        List<Task> rejectedReceiptTasks = getUserTasks("receipt_fix");

        System.out.print("Zadaj kod neschvaleneho receptu: ");
        int receiptCode = getUserService().getActivityId(rejectedReceiptTasks.size(), scanner);
        if (receiptCode == UserService.INVALID_CODE) {
            System.out.println("Zadany kod " + receiptCode + " nieje jedno z ID zamietnutych receptov.");
            return STAY_LOGGED_IN;
        }

        Task task = rejectedReceiptTasks.get(receiptCode);
        Map<String, Object> variables = taskService.getVariables(task.getId());
        Drug rejectedDrug = (Drug) variables.get("drug");
        Drug fixed = createReceipt(scanner);

        variables.put("drug", fixed);
        taskService.complete(task.getId(), variables);
        System.out.println("Liek " + rejectedDrug + " bol zmeneny na " + fixed);

        return STAY_LOGGED_IN;
    }

    /**
     * @return Prescribed drug, or null if prescription was cancelled.
     */
    private Drug createReceipt(Scanner scanner) {
        DrugService drugs = DrugService.getInstance();

        System.out.print("Zadaj nazov lieku: ");
        String drugName = scanner.nextLine();

        while (!drugs.nameExists(drugName)) {
            System.out.print("Liek s nazvom " + drugName + "sa nenasiel. "
                    + "Prosim zadaj iny nazov, alebo 'nic', ak chcete zrusit predpis receptu: ");
            drugName = scanner.nextLine();
        }

        return drugs.findByName(drugName);
    }

}
