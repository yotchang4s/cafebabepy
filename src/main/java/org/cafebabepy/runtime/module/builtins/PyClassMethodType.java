package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;
import org.cafebabepy.runtime.object.proxy.PyLexicalScopeProxyObject;

import static org.cafebabepy.util.ProtocolNames.__get__;
import static org.cafebabepy.util.ProtocolNames.__init__;

/**
 * Created by yotchang4s on 2018/07/01.
 */
@DefinePyType(name = "builtins.classmethod")
public class PyClassMethodType extends AbstractCafeBabePyType {

    public PyClassMethodType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __init__)
    public void __init__(PyObject self, PyObject f) {
        self.getFrame().getNotAppearLocals().put("f", f);
    }

    @DefinePyFunction(name = __get__)
    public PyObject __get__(PyObject self, PyObject obj, PyObject klass) {
        PyObject f = self.getFrame().getNotAppearLocals().get("f");
        if (f == null) {
            throw this.runtime.newRaiseException("builtins.RuntimeError", "uninitialized staticmethod object");
        }

        if (klass.isNone()) {
            klass = obj.getType();
        }

        PyObject scope = new PyLexicalScopeProxyObject(self);

        this.runtime.setattr(scope, "f", f);
        this.runtime.setattr(scope, "klass", klass);

        return this.runtime.eval(scope, "<classmethod>", ""
                + "def newfunc(*args):\n"
                + "  return f(klass, *args)\n"
                + "newfunc");
    }
}
