package com.sparc.tc.abstractions;

import com.sparc.tc.domain.AttributeValue;
import com.sparc.tc.domain.PlaceHolder;

public interface ValueSupplier {

    AttributeValue getValue(PlaceHolder placeHolder);

}
