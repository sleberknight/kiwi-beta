package org.kiwiproject.beta.collect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

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
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiCollections2.findFirstOfType(TextMessage.class, null));
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
        void shouldAcceptCollectionOfExtendsTypeParameter_U_1() {
            // Note: declare as Collection<? extends Number>
            Collection<? extends Number> nums = List.of(1, 2, 3);

            // T = Integer, U = Number
            // This requires the wildcard in the method signature
            var result = KiwiCollections2.findFirstOfType(Integer.class, nums);

            assertThat(result).contains(1);
        }

        @Test
        void shouldAcceptCollectionOfExtendsTypeParameterU_2() {
            // Note: declare as Collection<? extends Message>
            Collection<? extends AbstractMessage> messages = List.of(new TextMessage(), new JsonMessage());

            // T = TextMessage, U = Message
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

    // Sample message class hierarchy
    static sealed class AbstractMessage
            permits BytesMessage, JsonMessage, MapMessage, TextMessage {}
    static final class BytesMessage extends AbstractMessage {}
    static final class JsonMessage extends AbstractMessage {}
    static final class MapMessage extends AbstractMessage {}
    static final class TextMessage extends AbstractMessage {}
}
