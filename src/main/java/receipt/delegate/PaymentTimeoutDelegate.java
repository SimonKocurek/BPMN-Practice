package receipt.delegate;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import receipt.Main;
import receipt.entity.Drug;

public class PaymentTimeoutDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        String processInstanceId = execution.getProcessInstanceId();
        Drug drug = execution.getVariable("drug", Drug.class);
        Main.engine.getRuntimeService().deleteProcessInstance(processInstanceId, "Drug " + drug + " payment took longer than 30 days.");
        System.out.println("Platba za liek " + drug + " trvala dlhsie, ako 30 dni.");
    }

}
