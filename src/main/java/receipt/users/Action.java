package receipt.users;

import java.util.Scanner;
import java.util.function.Function;
import java.util.function.Supplier;

class Action {

    private String description;

    /**
     * Functionality using scanner for reading the user input and producing a result code, if user needs to be logged out
     */
    private Function<Scanner, Boolean> functionality;

    Action(String description, Function<Scanner, Boolean> functionality) {
        this.description = description;
        this.functionality = functionality;
    }

    String getDescription() {
        return description;
    }

    void setDescription(String description) {
        this.description = description;
    }

    boolean execute(Scanner scanner) {
        return functionality.apply(scanner);
    }

}
