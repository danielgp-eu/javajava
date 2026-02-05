package javajava;

import tools.jackson.core.ObjectWriteContext;

/**
 * Singleton class for ObjectWriteContext
 */
public final class WriteContextSingletonClass {

    private WriteContextSingletonClass() { }

    /**
     * Getter for ObjectWriteContext singleton instance
     * @return ObjectWriteContext
     */
    public static ObjectWriteContext get() {
        return Holder.INSTANCE;
    }

    /**
     * Holder class for lazy-loaded singleton instance
     */
    private static final class Holder {
        /**
         * Singleton instance of ObjectWriteContext
         */
        /* default */ static final ObjectWriteContext INSTANCE = ObjectWriteContext.empty();
    }

}
