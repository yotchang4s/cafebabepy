package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.annotation.DefineCafeBabePyFunction;
import org.cafebabepy.annotation.DefineCafeBabePyType;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.object.PyTupleIteratorObject;
import org.cafebabepy.runtime.object.PyTupleObject;

import static org.cafebabepy.util.ProtocolNames.__iter__;
import static org.cafebabepy.util.ProtocolNames.__next__;

/**
 * Created by yotchang4s on 2017/06/14.
 */
@DefineCafeBabePyType(name = "builtins.tuple_iterator", appear = false)
public class PyTupleIteratorType extends AbstractCafeBabePyType {

    public PyTupleIteratorType(Python runtime) {
        super(runtime);
    }

    @DefineCafeBabePyFunction(name = __next__)
    public PyObject __next__(PyObject self) {
        if (!(self instanceof PyTupleIteratorObject)) {
            throw this.runtime.newRaiseTypeError(
                    "descriptor '__next__' requires a 'tuple_iterator' object but received a '"
                            + self.getType().getFullName()
                            + "'");
        }

        return ((PyTupleIteratorObject) self).next();
    }

    @DefineCafeBabePyFunction(name = __iter__)
    public PyObject __iter__(PyObject self) {
        if (!(self instanceof PyTupleIteratorObject)) {
            throw this.runtime.newRaiseTypeError(
                    "descriptor '__iter__' requires a 'tuple_iterator' object but received a '"
                            + self.getType().getFullName()
                            + "'");
        }

        return this;
    }
}
