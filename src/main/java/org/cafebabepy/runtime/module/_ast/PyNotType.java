package org.cafebabepy.runtime.module._ast;

import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.DefinePyType;

/**
 * Created by yotchang4s on 2017/05/29.
 */
@DefinePyType(name = "_ast.Not", parent = {"_ast.unaryop"})
public class PyNotType extends AbstractAST {

    public PyNotType(Python runtime) {
        super(runtime);
    }
}
