package org.cafebabepy.runtime.object.literal;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.object.AbstractPyObjectObject;

/**
 * Created by yotchang4s on 2017/06/08.
 */
public class PyEllipsisObject extends AbstractPyObjectObject {

    public PyEllipsisObject(Python runtime) {
        super(runtime);
    }

    @Override
    public PyObject getType() {
        return this.runtime.typeOrThrow("builtins.Ellipsis");
    }

    @Override
    public boolean isEllipsis() {
        return true;
    }
}
