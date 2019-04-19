package receipt.users;

import java.util.List;
import java.util.Scanner;

import org.flowable.task.api.Task;
import receipt.entity.Drug;
import receipt.service.DrugService;
import receipt.Main;
import receipt.service.UserService;

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

            Main.engine
                    .getRuntimeService()
                    .createProcessInstanceBuilder()
                    .processDefinitionKey("receipt_process")
                    .variable("drug", prescribed)
                    .start();
        }

        return true;
    }

    private boolean printRejectedReceipts(Scanner scanner) {
        List<Task> rejectedReceiptTasks = getUserTasks("receipt_fix");

        System.out.println("Zamietnute lieky:");
        for (int i = 0; i < rejectedReceiptTasks.size(); i++) {
            Task task = rejectedReceiptTasks.get(i);
            Drug rejectedDrug = (Drug) task.getProcessVariables().get("drug");
            System.out.println(i + ": " + rejectedDrug);
        }

        return true;
    }

    private boolean fixRejectedReceipt(Scanner scanner) {
        List<Task> rejectedReceiptTasks = getUserTasks("receipt_fix");

        System.out.print("Zadaj kod neschvaleneho receptu: ");
        int receiptCode = getUserService().getActivityId(rejectedReceiptTasks.size(), scanner);
        if (receiptCode == UserService.INVALID_CODE) {
            System.out.println("Zadany kod " + receiptCode + " nieje jedno z ID zamietnutych receptov.");
            return true;
        }

        Task task = rejectedReceiptTasks.get(receiptCode);
        Drug rejectedDrug = (Drug) task.getProcessVariables().get("drug");
        Drug fixed = createReceipt(scanner);

        if (fixed == null) {
            Main.engine.getRuntimeService().deleteProcessInstance(task.getProcessInstanceId(), getName() + " didn't perscribe new drug.");
            System.out.println("Liecba pre zameitnuty liek " + rejectedDrug + " bola zrusena.");

        } else {
            task.getProcessVariables().put("drug", fixed);
            Main.engine.getTaskService().complete(task.getId());
            System.out.println("Liek " + rejectedDrug + " bol zmeneny na " + fixed);
        }

        return true;
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

            if ("nic".equals(drugName)) {
                return null;
            }
        }

        return drugs.findByName(drugName);
    }

}
