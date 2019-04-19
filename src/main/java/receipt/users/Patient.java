package receipt.users;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Scanner;

import org.flowable.task.api.Task;
import receipt.Main;
import receipt.entity.Drug;
import receipt.service.UserService;

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
			Drug controlledDrug = (Drug) task.getProcessVariables().get("drug");
			System.out.println(i + ": " + controlledDrug + " z coho pacient hradi: " + getPaidPrice(controlledDrug));
		}

		return true;
	}
	
	private boolean payForReceipt(Scanner scanner) {
		List<Task> pendingPayments = getUserTasks("patient_pay");

		System.out.print("Zadaj kod lieku na zaplatenie: ");
		int activityCode = getUserService().getActivityId(pendingPayments.size(), scanner);
		if (activityCode == UserService.INVALID_CODE) {

		}

		System.out.println("Prebehla platbla ");
		String paymentTaskId = pendingPayments.get(activityCode).getId();
		Main.engine.getTaskService().complete(paymentTaskId);

		return true;
	}

	private boolean getPendingConfirmations(Scanner scanner) {
		List<Task> pendingPayments = getUserTasks("drug_received_confirm");

		System.out.println("Potvrdenie dorucenia liekov:");
		for (int i = 0; i < pendingPayments.size(); i++) {
			Task task = pendingPayments.get(i);
			Drug controlledDrug = (Drug) task.getProcessVariables().get("drug");
			System.out.println(i + ": " + controlledDrug);
		}

		return true;
	}

	private boolean confirmDrugReceived(Scanner scanner) {
		List<Task> pendingPayments = getUserTasks("drug_received_confirm");

		System.out.print("Zadaj kod lieku na zaplatenie: ");
		int activityCode = getUserService().getActivityId(pendingPayments.size(), scanner);
		if (activityCode == UserService.INVALID_CODE) {

		}

		System.out.println("Prebehla platbla ");
		String paymentTaskId = pendingPayments.get(activityCode).getId();
		Main.engine.getTaskService().complete(paymentTaskId);

		return true;
	}

	private BigDecimal getPaidPrice(Drug drug) {
		BigDecimal drugPrice = BigDecimal.valueOf(drug.getPrice());
		BigDecimal fractionPaid = BigDecimal.valueOf(3);
		return drugPrice.divide(fractionPaid, RoundingMode.HALF_UP);
	}

}
