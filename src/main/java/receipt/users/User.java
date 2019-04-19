package receipt.users;

import org.flowable.task.api.Task;
import receipt.Main;
import receipt.service.UserService;

import java.util.*;

import static receipt.service.UserService.LOGOUT_ACTION;

public abstract class User {

	/**
	 * Mapping action code -> action
	 */
	private Map<Object, Action> actions;
	
	/**
	 * Counter for next usable action code that isn't taken
	 */
	private int actionCodeCounter;

	/**
	 * Service with utility functions. Lazy loaded, to prevent infinite recursion.
	 */
	private UserService userService;

	protected User() {
		actions = new HashMap<>();

		registerAction(new Action("Odhlasit", scanner -> LOGOUT_ACTION));
		registerActions();
	}
	
	/**
	 * Add actions to the {@code code -> action} mapping
	 */
	protected abstract void registerActions();
	
	/**
	 * Default implementation for registering an action using counter
	 */
	protected void registerAction(Action action) {
		actions.put(actionCodeCounter++, action);
	}

	protected List<Task> getUserTasks(String taskId) {
		return getUserService().getTasks(getName(), taskId);
	}

	/**
	 * @return Name used as assignee
	 */
	public abstract String getName();
	
	/**
	 * Print action codes
	 */
	public boolean printActions() {
		Main.engine.getTaskService()
				.createTaskQuery()
				.list()
				.forEach(System.out::println);

		actions.forEach((key, action) -> System.out.println(key + ": " + action.getDescription())); 
		return true;
	}
	
	/**
	 * @return Set of valid actions
	 */
	public Set<Object> getValidActions() {
		return actions.keySet();
	}

	/**
	 * Executes specified action
	 *
	 * @return Action other than cancel was executed
	 */
	public boolean executeAction(int actionCode, Scanner scanner) {
		return actions.get(actionCode).execute(scanner);
	}

	protected UserService getUserService() {
		if (userService == null) {
			userService = UserService.getInstance();
		}

		return userService;
	}

}
