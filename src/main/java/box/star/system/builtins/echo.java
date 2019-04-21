package box.star.system.builtins;

import box.star.system.Builtin;
import box.star.system.Environment;

import java.io.IOException;
import java.util.Arrays;
import java.util.Stack;

public class echo extends Builtin {

    @Override
    public void main(String[] parameters) {
        Stack<String>print = new Stack<>();
        print.addAll(Arrays.asList(parameters));
        print.remove(0);
        try {
            stdout.write(String.join(" ", print).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }
    }

}
