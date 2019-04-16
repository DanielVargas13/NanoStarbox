package box.star;

import box.star.opkit.Parameters;
import box.star.opkit.Parameters.*;
import org.junit.jupiter.api.Test;

class ParametersTest {

    Parameters parameters;
    ParameterParserState state;
    private ParameterData result;

    @Test
    void beginParameterParsing() {
        parameters = new Parameters();
        state = new ParameterParserState();
        parameters.beginParameterParsing(state, 0, new String[]{"w:", "G"});
        while (parseNextParameter());
    }

    boolean parseNextParameter() {
        result = new ParameterData();
        boolean v = parameters.parseNextParameter(state, result);
        state.printTrace(System.err);
        if (v) result.printTrace(System.err);
        return v;
    }

}