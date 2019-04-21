package box.star.system.action;

import box.star.system.Action;

import java.io.IOException;
import java.util.Arrays;
import java.util.Stack;

public class echo extends Action {

    /**
     * Override
     * @return the command name
     */
    @Override
    public String toString() {
        return "echo";
    }

    @Override
    public void main(String[] parameters) {
        Stack<String>print = new Stack<>();
        print.addAll(Arrays.asList(parameters));
        print.remove(0);
        boolean lineMode = true;
        while (print.get(0).startsWith("-")){
            if (print.get(0).equals("-n")){
                lineMode = false;
                print.remove(0);
                continue;
            }
            break;
        }
        try {
            String out = String.join(" ", print) + ((lineMode)?environment.getSystemLineTerminator():"");
            stdout.write(out.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Override
     * tries to use this action to resolve a complex query of any form.
     * @param command the command name specified
     * @return true if this action will handle this execution request.
     */
    @Override
    public boolean match(String command) {
       //if (command.equals("?:")) return true;
       return false;
    }

}
