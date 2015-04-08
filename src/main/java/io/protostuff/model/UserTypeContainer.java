package io.protostuff.model;

/**
 * @author Kostiantyn Shchepanovskyi
 */
public interface UserTypeContainer extends MessageContainer, EnumContainer {

    /**
     * Returns string prefix that is common for all children full names.
     * For root container it is an empty string if package is not set.
     *
     * @return
     */
    String getNamespacePrefix();
}
