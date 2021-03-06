package org.cafebabepy.runtime.object.iterator;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.object.java.PyDictKeysObject;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by yotchang4s on 2018/10/17.
 */
public class PyDictKeyIteratorObject extends AbstractPyIteratorObject {

    public PyDictKeyIteratorObject(Python runtime, PyDictKeysObject dictKeys) {
        super(runtime, new DictKeyIterator(runtime, dictKeys.getValue()));
    }

    @Override
    public PyObject getType() {
        return this.runtime.typeOrThrow("builtins.dict_keyiterator", false);
    }

    static class DictKeyIterator implements Iterator<PyObject> {

        private final Python runtime;

        private Iterator<PyObject> iterator;

        DictKeyIterator(Python runtime, Set<PyObject> view) {
            this.runtime = runtime;
            this.iterator = view.iterator();
        }

        @Override
        public boolean hasNext() {
            try {
                return this.iterator.hasNext();

            } catch (ConcurrentModificationException e) {
                throw this.runtime.newRaiseException("RuntimeError", "dictionary changed size during iteration");
            }
        }

        @Override
        public PyObject next() {
            if (!hasNext()) {
                throw this.runtime.newRaiseException("builtins.StopIteration");
            }

            try {
                return this.iterator.next();

            } catch (ConcurrentModificationException e) {
                throw this.runtime.newRaiseException("RuntimeError", "dictionary changed size during iteration");
            }
        }
    }
}
