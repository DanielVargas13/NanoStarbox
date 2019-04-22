package box.star.system.action;

import box.star.system.Action;

public class exit extends Action {
    /**
     * Override
     *
     * @return the command name
     */
    @Override
    public String toString() {
        return "exit";
    }

    @Override
    public void main(String[] parameters) {
        System.exit((parameters.length > 1) ? Integer.parseInt(parameters[1]) : 0);
    }

}
