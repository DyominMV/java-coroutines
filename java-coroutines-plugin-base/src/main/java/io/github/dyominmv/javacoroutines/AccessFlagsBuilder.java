package io.github.dyominmv.javacoroutines;

import java.lang.reflect.AccessFlag;

public record AccessFlagsBuilder(int mask) {

    public AccessFlagsBuilder() { this(0); }

    public AccessFlagsBuilder with(AccessFlag ...flags) {
        var newMask = mask();
        for (AccessFlag flag : flags) newMask |= flag.mask();
        return new AccessFlagsBuilder(newMask);
    }

}
