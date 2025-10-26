package org.kiwiproject.beta.collect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.google.common.collect.Lists;
import lombok.experimental.ExtensionMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.commons.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

@DisplayName("KiwiCollections2")
class KiwiCollections2Test {

    @Nested
    class FindFirstOfType {

        @Test
        void shouldNotAllowNullType() {
            var messages = List.of(new JsonMessage(), new TextMessage());
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiCollections2.findFirstOfType(null, messages));
        }

        @Test
        void shouldNotAllowNullCollection() {
            Collection<TextMessage> nullCollection = null;
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiCollections2.findFirstOfType(TextMessage.class, nullCollection));
        }

        @Test
        void shouldReturnEmptyOptionalWhenCollectionIsEmpty() {
            assertThat(KiwiCollections2.findFirstOfType(JsonMessage.class, List.of()))
                    .isEmpty();
        }

        @Test
        void shouldReturnEmptyOptionalWhenTypeIsNotFound() {
            var messages = List.of(
                    new JsonMessage(),
                    new TextMessage(),
                    new BytesMessage(),
                    new TextMessage(),
                    new JsonMessage()
            );

            assertThat(KiwiCollections2.findFirstOfType(MapMessage.class, messages))
                    .isEmpty();
        }

        @Test
        void shouldFindAnyObject() {
            var now = Instant.now();
            var later = now.plusSeconds(60);
            var objects = List.of(
                    new Object(),
                    later,
                    now,
                    24.0,
                    42,
                    new Object(),
                    84,
                    3.14,
                    42L,
                    "forty-two",
                    84L,
                    "42"
            );

            assertAll(
                    () -> assertThat(KiwiCollections2.findFirstOfType(Integer.class, objects))
                            .contains(42),
                    () -> assertThat(KiwiCollections2.findFirstOfType(Long.class, objects))
                            .contains(42L),
                    () -> assertThat(KiwiCollections2.findFirstOfType(Double.class, objects))
                            .contains(24.0),
                    () -> assertThat(KiwiCollections2.findFirstOfType(Instant.class, objects))
                            .contains(later),
                    () -> assertThat(KiwiCollections2.findFirstOfType(String.class, objects))
                            .contains("forty-two")
            );
        }

        @Test
        void shouldFindTheOnlyElementOfTheSpecifiedType() {
            var message = new BytesMessage();
            var messages = List.of(
                    new JsonMessage(),
                    new TextMessage(),
                    message,
                    new TextMessage(),
                    new JsonMessage()
            );

            assertThat(KiwiCollections2.findFirstOfType(BytesMessage.class, messages))
                    .contains(message);
        }

        @Test
        void shouldFindTheFirstElementOfTheSpecifiedType() {
            var message = new TextMessage();
            var messages = List.of(
                    new JsonMessage(),
                    new BytesMessage(),
                    new JsonMessage(),
                    message,
                    new TextMessage()
            );

            assertThat(KiwiCollections2.findFirstOfType(TextMessage.class, messages))
                    .contains(message);
        }

        @Test
        void shouldIgnoreNullObjects() {
            var message = new MapMessage();
            var messages = Lists.newArrayList(
                    null,
                    new TextMessage(),
                    new JsonMessage(),
                    null,
                    message,
                    null,
                    new JsonMessage()
            );

            assertThat(KiwiCollections2.findFirstOfType(MapMessage.class, messages))
                    .contains(message);
        }

        @Test
        void shouldAcceptCollectionOfExtendsTypeParameter_U_Where_U_IsNumber() {
            // Note: declare as Collection<? extends Number>
            Collection<? extends Number> nums = List.of(1, 2, 3);

            // T = Integer, U = Number
            // This requires the wildcard in the method signature
            var result = KiwiCollections2.findFirstOfType(Integer.class, nums);

            assertThat(result).contains(1);
        }

        @Test
        void shouldAcceptCollectionOfExtendsTypeParameterU_Where_U_IsCustomTypeAbstractMessages() {
            // Note: declare as Collection<? extends AbstractMessage>
            Collection<? extends AbstractMessage> messages = List.of(new TextMessage(), new JsonMessage());

            // T = TextMessage, U = AbstractMessage
            var result = KiwiCollections2.findFirstOfType(TextMessage.class, messages);

            assertThat(result).containsInstanceOf(TextMessage.class);
        }

        @Test
        void shouldWorkWhenTypeParameter_U_IsPinnedToSupertypeAndCollectionIsSubtype() {
            List<Integer> ints = List.of(10, 20);

            // Force inference: T = Integer, U = Number while passing List<Integer>
            var result = KiwiCollections2.<Integer, Number>findFirstOfType(Integer.class, ints);

            assertThat(result).contains(10);
        }
    }

    @Nested
    class FindFirstOfTypeFromIterable {

        @Test
        void shouldNotAllowNullType() {
            Iterable<AbstractMessage> messages = List.of(new JsonMessage(), new TextMessage());
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiCollections2.findFirstOfType(null, messages));
        }

        @Test
        void shouldNotAllowNullIterable() {
            Iterable<AbstractMessage> nullIterable = null;
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiCollections2.findFirstOfType(TextMessage.class, nullIterable));
        }

        @Test
        void shouldReturnEmptyOptionalWhenIterableIsEmpty() {
            Iterable<AbstractMessage> empty = List.of();
            assertThat(KiwiCollections2.findFirstOfType(JsonMessage.class, empty)).isEmpty();
        }

        @Test
        void shouldFindFirstMatchingElement_IgnoringNulls() {
            var message = new MapMessage();
            Iterable<AbstractMessage> messages = Lists.newArrayList(
                    null,
                    new TextMessage(),
                    new JsonMessage(),
                    null,
                    message,
                    null,
                    new JsonMessage()
            );

            assertThat(KiwiCollections2.findFirstOfType(MapMessage.class, messages)).contains(message);
        }
    }

    @Nested
    class FindFirstOfTypeFromStream {

        @Test
        void shouldNotAllowNullType() {
            var stream = java.util.stream.Stream.of(new JsonMessage(), new TextMessage());
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiCollections2.findFirstOfType(null, stream));
        }

        @Test
        void shouldNotAllowNullStream() {
            java.util.stream.Stream<AbstractMessage> nullStream = null;
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiCollections2.findFirstOfType(TextMessage.class, nullStream));
        }

        @Test
        void shouldReturnEmptyOptionalWhenStreamIsEmpty() {
            var empty = java.util.stream.Stream.<AbstractMessage>empty();
            assertThat(KiwiCollections2.findFirstOfType(JsonMessage.class, empty)).isEmpty();
        }

        @Test
        void shouldFindFirstMatchingElement() {
            var message = new BytesMessage();
            var stream = java.util.stream.Stream.of(
                    new JsonMessage(),
                    new TextMessage(),
                    message,
                    new TextMessage(),
                    new JsonMessage()
            );

            assertThat(KiwiCollections2.findFirstOfType(BytesMessage.class, stream)).contains(message);
        }
    }

    // Sample message class hierarchy
    static sealed class AbstractMessage
            permits BytesMessage, JsonMessage, MapMessage, TextMessage {}
    static final class BytesMessage extends AbstractMessage {}
    static final class JsonMessage extends AbstractMessage {}
    static final class MapMessage extends AbstractMessage {}
    static final class TextMessage extends AbstractMessage {}

    @Nested
    class AddIf {

        @Test
        void shouldNotAddValue_AndReturnFalse_WhenConditionNotSatisfied() {
            var numbers = new ArrayList<Integer>();
            var added = KiwiCollections2.addIf(numbers, null, Objects::nonNull);

            assertAll(
                    () -> assertThat(added).isFalse(),
                    () -> assertThat(numbers).isEmpty()
            );
        }

        @ParameterizedTest
        @ValueSource(strings = {"Red", "Green", "Blue"})
        void shouldNotAddValue_AndReturnFalse_WhenConditionIsSatisfied_ButCollectionAddIsFalse(String newColor) {
            var colors = new HashSet<String>();
            colors.add("Red");
            colors.add("Green");
            colors.add("Blue");

            var added = KiwiCollections2.addIf(colors, newColor, StringUtils::isNotBlank);

            assertAll(
                    () -> assertThat(added).isFalse(),
                    () -> assertThat(colors).isEqualTo(Set.of("Red", "Green", "Blue"))
            );
        }

        @ParameterizedTest
        @ValueSource(ints = {42, 84, 126})
        void shouldAddValue_AndReturnTrue_WhenConditionIsSatisfied(int number) {
            var numbers = new ArrayList<Integer>();
            var added = KiwiCollections2.addIf(numbers, number, Objects::nonNull);

            assertAll(
                    () -> assertThat(added).isTrue(),
                    () -> assertThat(numbers).isEqualTo(List.of(number))
            );
        }

        @Test
        void shouldThrowIllegalArgument_WhenObjectsIsNull() {
            assertThatIllegalArgumentException().isThrownBy(
                    () -> KiwiCollections2.addIf(null, 1, Objects::nonNull)
            ).withMessage("collection must not be null");
        }

        @Test
        void shouldThrowIllegalArgument_WhenConditionIsNull() {
            var list = new ArrayList<Integer>();
            assertThatIllegalArgumentException().isThrownBy(
                    () -> KiwiCollections2.addIf(list, 1, null)
            ).withMessage("condition must not be null");
        }

        @Test
        void shouldAllowAddingNull_WhenPredicateAcceptsNull() {
            var list = new ArrayList<Integer>();
            var added = KiwiCollections2.addIf(list, null, Objects::isNull);
            assertAll(
                    () -> assertThat(added).isTrue(),
                    () -> assertThat(list).containsExactly((Integer) null)
            );
        }

        @Test
        void shouldPropagateExceptionFromCollectionAdd() {
            var unmodifiable = List.of(1);
            assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(
                    () -> KiwiCollections2.addIf(unmodifiable, 2, value -> true)
            );
        }
    }

    @ExtensionMethod(KiwiCollectionExtensions.class)
    @Nested
    class AddIfAsExtensionMethod {

        @Test
        void shouldAddValue() {
            var names = new ArrayList<String>();
            Predicate<String> startsWithA = name -> name.startsWith("A");
            var aliceAdded = names.addIf("Alice", startsWithA);
            var bobAdded = names.addIf("Bob", startsWithA);

            assertAll(
                    () -> assertThat(aliceAdded).isTrue(),
                    () -> assertThat(bobAdded).isFalse(),
                    () -> assertThat(names).containsExactly("Alice")
            );
        }
    }

    @Nested
    class AddIfNonNull {

        @Test
        void shouldAddValue_WhenNonNull() {
            var numbers = new ArrayList<Integer>();
            var added = KiwiCollections2.addIfNonNull(numbers, 42);

            assertAll(
                    () -> assertThat(added).isTrue(),
                    () -> assertThat(numbers).containsExactly(42)
            );
        }

        @Test
        void shouldNotAddValue_WhenNull() {
            var numbers = new ArrayList<Integer>();
            var added = KiwiCollections2.addIfNonNull(numbers, null);

            assertAll(
                    () -> assertThat(added).isFalse(),
                    () -> assertThat(numbers).isEmpty()
            );
        }

        @Test
        void shouldReturnFalse_WhenCollectionAddReturnsFalse() {
            var set = new HashSet<Integer>();
            set.add(5);

            var added = KiwiCollections2.addIfNonNull(set, 5);

            assertAll(
                    () -> assertThat(added).isFalse(),
                    () -> assertThat(set).containsExactlyInAnyOrder(5)
            );
        }

        @Test
        void shouldThrowIllegalArgument_WhenObjectsIsNull() {
            assertThatIllegalArgumentException().isThrownBy(
                    () -> KiwiCollections2.addIfNonNull(null, 1)
            ).withMessage("collection must not be null");
        }

        @Test
        void shouldPropagateExceptionFromCollectionAdd() {
            var unmodifiable = List.of(1);
            assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(
                    () -> KiwiCollections2.addIfNonNull(unmodifiable, 2)
            );
        }
    }

    @ExtensionMethod(KiwiCollectionExtensions.class)
    @Nested
    class AddIfNonNullAsExtensionMethod {

        @Test
        void shouldAddValue_WhenNonNull() {
            var names = new ArrayList<String>();
            var added = names.addIfNonNull("Alice");

            assertAll(
                    () -> assertThat(added).isTrue(),
                    () -> assertThat(names).containsExactly("Alice")
            );
        }

        @Test
        void shouldNotAddValue_WhenNull() {
            var names = new ArrayList<String>();
            var added = names.addIfNonNull(null);

            assertAll(
                    () -> assertThat(added).isFalse(),
                    () -> assertThat(names).isEmpty()
            );
        }

        @Test
        void shouldReturnFalse_WhenCollectionAddReturnsFalse() {
            var set = new HashSet<Integer>();
            set.add(5);

            var added = set.addIfNonNull(5);

            assertAll(
                    () -> assertThat(added).isFalse(),
                    () -> assertThat(set).containsExactlyInAnyOrder(5)
            );
        }
    }
}
