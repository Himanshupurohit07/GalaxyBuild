package com.sparc.tc.abstractions;

import com.sparc.tc.domain.PlaceHolder;
import com.sparc.tc.exceptions.TCExceptionRuntime;

public interface PlaceholderParser {

    PlaceHolder parse(String instruction) throws TCExceptionRuntime;

}
