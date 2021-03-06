package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;
import org.cafebabepy.runtime.object.iterator.PyDictKeyIteratorObject;

import static org.cafebabepy.util.ProtocolNames.__iter__;
import static org.cafebabepy.util.ProtocolNames.__next__;

/**
 * Created by yotchang4s on 2018/10/28.
 */
@DefinePyType(name = "builtins.dict_keyiterator", appear = false)
public class PyDictKeyIteratorType extends AbstractCafeBabePyType {

    public PyDictKeyIteratorType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __next__)
    public PyObject __next__(PyObject self) {
        if (!(self instanceof PyDictKeyIteratorObject)) {
            throw this.runtime.newRaiseTypeError(
                    "descriptor '__next__' requires a 'dict_keyiterator' object but received a '"
                            + self.getType().getFullName()
                            + "'");
        }

        return ((PyDictKeyIteratorObject) self).next();
    }

    @DefinePyFunction(name = __iter__)
    public PyObject __iter__(PyObject self) {
        if (!(self instanceof PyDictKeyIteratorObject)) {
            throw this.runtime.newRaiseTypeError(
                    "descriptor '__iter__' requires a 'dict_keyiterator' object but received a '"
                            + self.getType().getFullName()
                            + "'");
        }

        return this;
    }
}
