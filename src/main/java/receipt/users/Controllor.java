package receipt.users;

import java.util.List;
import java.util.Scanner;

import org.flowable.task.api.Task;
import receipt.entity.Drug;
import receipt.Main;
import receipt.service.UserService;

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
            Drug controlledDrug = (Drug) task.getProcessVariables().get("drug");
            System.out.println(i + ": " + controlledDrug);
        }

        return true;
    }

    private boolean checkReceipt(Scanner scanner) {
        List<Task> checkedReceipts = getUserTasks("controllor_check");

        System.out.print("Zadaj kod receptu na schvalenie: ");
        int receiptCode = getUserService().getActivityId(checkedReceipts.size(), scanner);
        if (receiptCode == UserService.INVALID_CODE) {
            System.out.println("Zadany kod " + receiptCode + " nieje jedno z ID kontrolovanych receptov.");
            return true;
        }

        Task task = checkedReceipts.get(receiptCode);
        Drug checkedDrug = (Drug) task.getProcessVariables().get("drug");

        System.out.println("Zamietnut kontrolovany liek " + checkedDrug + "? ano/nie ");
        boolean answer = getUserService().getYesNoAnswer(scanner);

        task.getProcessVariables().put("check", answer);
        Main.engine.getTaskService().complete(task.getId());
        System.out.println("Liek " + checkedDrug + " je schvaleny: " + answer);

        return true;
    }

}
