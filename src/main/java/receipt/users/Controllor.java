package receipt.users;

import java.util.List;
import java.util.Scanner;

import org.flowable.task.api.Task;
import receipt.entity.Drug;
import receipt.Main;
import receipt.service.UserService;

import static receipt.service.UserService.STAY_LOGGED_IN;

public class Controllor extends User {

    @Override
    public String getName() {
        return "Controllor";
    }

    @Override
    protected void registerActions() {
        registerAction(new Action("Vypis recepty na kontrolu.", this::printCheckedReceipts));
        registerAction(new Action("Skontroluj recept.", this::checkReceipt));
    }

    private boolean printCheckedReceipts(Scanner scanner) {
        List<Task> checkedReceipts = getUserTasks("controllor_check");

        System.out.println("Lieky na kontrolu:");
        for (int i = 0; i < checkedReceipts.size(); i++) {
            Task task = checkedReceipts.get(i);
            Drug controlledDrug = (Drug) taskService.getVariable(task.getId(), "drug");
            System.out.println(i + ": " + controlledDrug);
        }

        return STAY_LOGGED_IN;
    }

    private boolean checkReceipt(Scanner scanner) {
        List<Task> checkedReceipts = getUserTasks("controllor_check");

        System.out.print("Zadaj kod receptu na schvalenie: ");
        int receiptCode = getUserService().getActivityId(checkedReceipts.size(), scanner);
        if (receiptCode == UserService.INVALID_CODE) {
            System.out.println("Zadany kod " + receiptCode + " nieje jedno z ID kontrolovanych receptov.");
            return STAY_LOGGED_IN;
        }

        Task task = checkedReceipts.get(receiptCode);
        Drug checkedDrug = (Drug) taskService.getVariable(task.getId(), "drug");

        System.out.println("Schvalit kontrolovany liek " + checkedDrug + "? ano/nie ");
        boolean answer = getUserService().getYesNoAnswer(scanner);

        taskService.setVariable(task.getId(), "approved", answer);
        taskService.complete(task.getId());

        return STAY_LOGGED_IN;
    }

}
