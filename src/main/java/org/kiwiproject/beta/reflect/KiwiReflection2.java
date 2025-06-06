package org.kiwiproject.beta.reflect;

import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import com.google.common.annotations.Beta;
import lombok.experimental.UtilityClass;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

/**
 * Utilities related to reflection.
 * <p>
 * These utilities can be considered for inclusion into kiwi's {@link org.kiwiproject.reflect.KiwiReflection} class.
 */
@Beta
@UtilityClass
public class KiwiReflection2 {

    /**
     * Describes the access modifiers allowed on Java members, e.g., classes, fields, methods, and constructors.
     * <p>
     * Note that {@link JavaAccessModifier#PACKAGE_PRIVATE} means there is no explicit modifier keyword as
     * described in section <a href="https://docs.oracle.com/javase/specs/jls/se19/html/jls-6.html#jls-6.6">6.6 Access Control</a>
     * in the <a href="https://docs.oracle.com/javase/specs/">Java Language Specification</a>.
     */
    public enum JavaAccessModifier {
        PUBLIC,
        PROTECTED,
        PRIVATE,
        PACKAGE_PRIVATE
    }

    /**
     * Check whether a {@link Member} has a given access modifier.
     *
     * @param member the Java member to check
     * @param modifier the modified to check against
     * @return true if the member has the given modifier, otherwise false
     */
    public static boolean hasAccessModifier(Member member, JavaAccessModifier modifier) {
        return switch (modifier) {
            case PUBLIC -> isPublic(member);
            case PROTECTED -> isProtected(member);
            case PRIVATE -> isPrivate(member);
            case PACKAGE_PRIVATE -> isPackagePrivate(member);
        };
    }

    /**
     * Check whether a {@link Class} has a given access modifier.
     *
     * @param clazz the Java class to check
     * @param modifier the modified to check against
     * @return true if the class has the given modifier, otherwise false
     */
    public static boolean hasAccessModifier(Class<?> clazz, JavaAccessModifier modifier) {
        return switch (modifier) {
            case PUBLIC -> isPublic(clazz);
            case PROTECTED -> isProtected(clazz);
            case PRIVATE -> isPrivate(clazz);
            case PACKAGE_PRIVATE -> isPackagePrivate(clazz);
        };
    }

    /**
     * Check if the member has the {@code public} modifier.
     *
     * @param member the member to check
     * @return true if the member is public, otherwise false
     */
    public static boolean isPublic(Member member) {
        return Modifier.isPublic(member.getModifiers());
    }

    /**
     * Check if the class has the {@code public} modifier.
     *
     * @param clazz the class to check
     * @return true if the class is public, otherwise false
     */
    public static boolean isPublic(Class<?> clazz) {
        return Modifier.isPublic(clazz.getModifiers());
    }

    /**
     * Check if the member has the {@code protected} modifier.
     *
     * @param member the member to check
     * @return true if the member is protected, otherwise false
     */
    public static boolean isProtected(Member member) {
        return Modifier.isProtected(member.getModifiers());
    }

    /**
     * Check if the class has the {@code protected} modifier.
     *
     * @param clazz the class to check
     * @return true if the class is protected, otherwise false
     */
    public static boolean isProtected(Class<?> clazz) {
        return Modifier.isProtected(clazz.getModifiers());
    }

    /**
     * Check if the member has the {@code private} modifier.
     *
     * @param member the member to check
     * @return true if the member is private, otherwise false
     */
    public static boolean isPrivate(Member member) {
        return Modifier.isPrivate(member.getModifiers());
    }

    /**
     * Check if the class has the {@code private} modifier.
     *
     * @param clazz the class to check
     * @return true if the class is private, otherwise false
     */
    public static boolean isPrivate(Class<?> clazz) {
        return Modifier.isPrivate(clazz.getModifiers());
    }

    /**
     * Check if the member has no explicit modifier, i.e., is accessible only by other members of the same package.
     *
     * @param member the member to check
     * @return true, if the member has no explicit modifier, otherwise false
     */
    public static boolean isPackagePrivate(Member member) {
        return !isPublic(member) && !isProtected(member) && !isPrivate(member);
    }

    /**
     * Check if the class has no explicit modifier,
     * i.e., is accessible only by other classes and members in the same package.
     *
     * @param clazz the class to check
     * @return true if the class has no explicit modifier, otherwise false
     */
    public static boolean isPackagePrivate(Class<?> clazz) {
        return !isPublic(clazz) && !isProtected(clazz) && !isPrivate(clazz);
    }

    /**
     * Get the type information for the given {@link Field}.
     *
     * @param field the Field to check
     * @return the type information
     */
    public static TypeInfo typeInformationOf(@NonNull Field field) {
        checkArgumentNotNull(field, "field to inspect must not be null");
        var genericType = field.getGenericType();
        return typeInformationOf(genericType);
    }

    /**
     * Get the type information for the given {@link Type}.
     * <p>
     * This is a convenience method that delegates to {@link TypeInfo#ofType(Type)}.
     *
     * @param type the type to check
     * @return the type information
     */
    public static TypeInfo typeInformationOf(@NonNull Type type) {
        return TypeInfo.ofType(type);
    }

}
