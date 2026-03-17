package com.sparc.wc.tc.abstractions;

import com.sparc.tc.exceptions.TCException;

public interface MonoAppendable<T> {

    void add(T value) throws TCException;

}
