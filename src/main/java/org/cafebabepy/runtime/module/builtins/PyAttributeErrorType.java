package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.module.DefinePyType;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;

/**
 * Created by yotchang4s on 2017/05/31.
 */
@DefinePyType(name = "builtins.AttributeError", parent = {"builtins.Exception"})
public class PyAttributeErrorType extends AbstractCafeBabePyType {

    public PyAttributeErrorType(Python runtime) {
        super(runtime);
    }
}
