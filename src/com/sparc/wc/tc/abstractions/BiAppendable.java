package com.sparc.wc.tc.abstractions;

import com.sparc.tc.exceptions.TCException;

public interface BiAppendable<T, U> {

    void add(T value, U className) throws TCException;

}
