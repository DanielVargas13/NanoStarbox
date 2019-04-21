package box.star.system.builtins;

import box.star.system.Builtin;
import box.star.system.Environment;

import java.io.IOException;
import java.util.Arrays;
import java.util.Stack;

public class echo extends Builtin {

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
        try {
            stdout.write(String.join(" ", print).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Override
     * tries to use this builtin to resolve a complex query of any form.
     * @param command the command name specified
     * @return true if this builtin will handle this execution request.
     */
    @Override
    public boolean match(String command) {
       //if (command.equals("?:")) return true;
       return false;
    }

}
