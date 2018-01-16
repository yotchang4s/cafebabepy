package org.cafebabepy.runtime.module.ast;

import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyModule;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyModule;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by yotchang4s on 2017/06/04.
 */
@DefinePyModule(name = "ast")
public class PyAstModule extends AbstractCafeBabePyModule {

    public PyAstModule(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = "iter_fields")
    public PyObject iter_fields(PyObject node) {
        Map<String, PyObject> objectMap = node.getScope().gets();
        Iterator<Map.Entry<String, PyObject>> objectIter = objectMap.entrySet().iterator();

        return this.runtime.generator(stopper -> {
            if (!objectIter.hasNext()) {
                stopper.stop(this.runtime);
                // Always RaiseException

                return this.runtime.None();
            }

            Map.Entry<String, PyObject> e = objectIter.next();

            return this.runtime.list(this.runtime.str(e.getKey()), e.getValue());
        });
    }
}
