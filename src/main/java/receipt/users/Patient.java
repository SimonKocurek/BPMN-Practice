package receipt.users;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Scanner;

import org.flowable.task.api.Task;
import receipt.Main;
import receipt.entity.Drug;
import receipt.service.UserService;

import static receipt.service.UserService.STAY_LOGGED_IN;

public class Patient extends User {

    @Override
    public String getName() {
        return "Patient";
    }

    @Override
    protected void registerActions() {
        registerAction(new Action("Vypis recepty na zaplatenie.", this::printPendingPayments));
        registerAction(new Action("Zaplat za recept.", this::payForReceipt));

        registerAction(new Action("Vypis recepty na prevzatie.", this::getPendingConfirmations));
        registerAction(new Action("Potvrd prevzatie receptu.", this::confirmDrugReceived));
    }

    private boolean printPendingPayments(Scanner scanner) {
        List<Task> pendingPayments = getUserTasks("patient_pay");

        System.out.println("Lieky na zaplatenie:");
        for (int i = 0; i < pendingPayments.size(); i++) {
            Task task = pendingPayments.get(i);
            Drug pendingDrugPayment = (Drug) taskService.getVariable(task.getId(), "drug");
            System.out.println(i + ": " + pendingDrugPayment + " z coho pacient hradi: " + getPaidPrice(pendingDrugPayment) + "€");
        }

        return STAY_LOGGED_IN;
    }

    private boolean payForReceipt(Scanner scanner) {
        List<Task> pendingPayments = getUserTasks("patient_pay");

        System.out.print("Zadaj kod lieku na zaplatenie: ");
        int activityCode = getUserService().getActivityId(pendingPayments.size(), scanner);
        if (activityCode == UserService.INVALID_CODE) {
            System.out.println("Zadany kod " + activityCode + " nieje jedno z ID receptov na zaplatenie.");
            return STAY_LOGGED_IN;
        }

        Task task = pendingPayments.get(activityCode);
        Drug pendingDrug = (Drug) taskService.getVariable(task.getId(), "drug");

        System.out.println("Prebehla platbla " + getPaidPrice(pendingDrug) + "€");
        String paymentTaskId = pendingPayments.get(activityCode).getId();
        Main.engine.getTaskService().complete(paymentTaskId);

        return STAY_LOGGED_IN;
    }

    private boolean getPendingConfirmations(Scanner scanner) {
        List<Task> pendingConfirmations = getUserTasks("drug_received_confirm");

        System.out.println("Potvrdenie dorucenia liekov:");
        for (int i = 0; i < pendingConfirmations.size(); i++) {
            Task task = pendingConfirmations.get(i);
            Drug confirmedDrug = (Drug) taskService.getVariable(task.getId(), "drug");
            System.out.println(i + ": " + confirmedDrug);
        }

        return STAY_LOGGED_IN;
    }

    private boolean confirmDrugReceived(Scanner scanner) {
        List<Task> pendingConfirmations = getUserTasks("drug_received_confirm");

        System.out.print("Zadaj kod prevzateho lieku: ");
        int activityCode = getUserService().getActivityId(pendingConfirmations.size(), scanner);
        if (activityCode == UserService.INVALID_CODE) {
            System.out.println("Zadany kod " + activityCode + " nieje jedno z ID receptov, cakajucich na potvrdenie prevzatia.");
            return STAY_LOGGED_IN;
        }

        String confirmedId = pendingConfirmations.get(activityCode).getId();
        Main.engine.getTaskService().complete(confirmedId);

        return STAY_LOGGED_IN;
    }

    private BigDecimal getPaidPrice(Drug drug) {
        BigDecimal drugPrice = BigDecimal.valueOf(drug.getPrice());
        BigDecimal fractionPaid = BigDecimal.valueOf(3);
        return drugPrice.divide(fractionPaid, RoundingMode.HALF_UP);
    }

}
