package org.kiwiproject.beta.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.kiwiproject.spring.data.KiwiSort;
import org.kiwiproject.spring.data.PagingRequest;
import org.springframework.data.domain.Sort;

@DisplayName("DaoHelpers")
class DaoHelpersTest {

    private static final String BASE_QUERY = "select * from users";

    private StringBuilder query;
    private AllowedFields allowedSortFields;

    @BeforeEach
    void setUp() {
        query = new StringBuilder(BASE_QUERY);
        allowedSortFields = AllowedFields.of("firstName", "lastName", "age");
    }

    @Nested
    class AddSortsFromPagingRequest {

        private PagingRequest pagingRequest;

        @BeforeEach
        void setUp() {
            pagingRequest = new PagingRequest();
        }

        // TODO test the arg checks?

        @Test
        void shouldDoNothingWhenNoSortsDefined() {
            DaoHelpers.addSorts(query, allowedSortFields, pagingRequest);

            assertThat(query).hasToString(BASE_QUERY);
        }

        @Test
        void shouldAddPrimarySort() {
            pagingRequest.setPrimarySort("lastName");

            DaoHelpers.addSorts(query, allowedSortFields, pagingRequest);

            assertThat(query).hasToString(BASE_QUERY + " order by lastName ASC");
        }

        @Test
        void shouldAddPrimarySortDescending() {
            pagingRequest.setPrimarySort("lastName");
            pagingRequest.setPrimaryDirection(Sort.Direction.DESC);

            DaoHelpers.addSorts(query, allowedSortFields, pagingRequest);

            assertThat(query).hasToString(BASE_QUERY + " order by lastName DESC");
        }

        @Test
        void shouldAddPrimaryAndSecondarySorts() {
            pagingRequest.setPrimarySort("lastName");
            pagingRequest.setPrimaryDirection(Sort.Direction.DESC);

            pagingRequest.setSecondarySort("firstName");
            pagingRequest.setSecondaryDirection(Sort.Direction.DESC);

            DaoHelpers.addSorts(query, allowedSortFields, pagingRequest);

            assertThat(query).hasToString(BASE_QUERY + " order by lastName DESC, firstName DESC");
        }

        @Test
        void shouldDoNothingIfOnlySecondarySortSpecified() {
            pagingRequest.setSecondarySort("lastName");

            DaoHelpers.addSorts(query, allowedSortFields, pagingRequest);

            assertThat(query).hasToString(BASE_QUERY);
        }

        @Test
        void shouldNotAddSortForDisallowedField() {
            pagingRequest.setPrimarySort("someOtherField");

            assertThatThrownBy(() -> DaoHelpers.addSorts(query, allowedSortFields, pagingRequest))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThat(query)
                    .describedAs("should not change query if field now allowed")
                    .hasToString(BASE_QUERY);
        }

        // TODO Add test for disallowed secondary field: should throw, should not change query
    }

    @Nested
    class AddSortFromSingleKiwiSort {

        // TODO test the arg checks?

        @ParameterizedTest
        @EnumSource(KiwiSort.Direction.class)
        void shouldAddSort(KiwiSort.Direction sortDirection) {
            var sort = KiwiSort.of("firstName", sortDirection);

            DaoHelpers.addSort(query, allowedSortFields, sort);

            assertThat(query).hasToString(BASE_QUERY + " order by firstName " + sortDirection);
        }

        @Test
        void shouldNotAddSortForDisallowedField() {
            var sort = KiwiSort.of("salary", KiwiSort.Direction.DESC);

            assertThatThrownBy(() -> DaoHelpers.addSort(query, allowedSortFields, sort))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThat(query)
                    .describedAs("should not change query if field now allowed")
                    .hasToString(BASE_QUERY);
        }
    }

    @Nested
    class AddSortFromSortFieldAndDirection {

        // TODO test the arg checks?

        @ParameterizedTest
        @EnumSource(KiwiSort.Direction.class)
        void shouldAddSort(KiwiSort.Direction sortDirection) {
            DaoHelpers.addSort(query, allowedSortFields, "age", sortDirection);

            assertThat(query).hasToString(BASE_QUERY + " order by age " + sortDirection);
        }

        @Test
        void shouldNotAddSortForDisallowedField() {
            assertThatThrownBy(() -> DaoHelpers.addSort(query, allowedSortFields, "salary", KiwiSort.Direction.DESC))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThat(query)
                    .describedAs("should not change query if field now allowed")
                    .hasToString(BASE_QUERY);
        }
    }

    @Nested
    class AddSortsFromPrimaryAndSecondaryKiwiSort {

        // TODO test the arg checks?

        @Test
        void shouldAddSorts() {
            var primarySort = KiwiSort.of("lastName", KiwiSort.Direction.DESC);
            var secondarySort = KiwiSort.of("age", KiwiSort.Direction.ASC);

            DaoHelpers.addSorts(query, allowedSortFields, primarySort, secondarySort);

            assertThat(query).hasToString(BASE_QUERY + " order by lastName DESC, age ASC");
        }

        @Test
        void shouldNotAddSortForDisallowedPrimarySortField() {
            var primarySort = KiwiSort.of("salary", KiwiSort.Direction.DESC);
            var secondarySort = KiwiSort.of("lastName", KiwiSort.Direction.ASC);

            assertThatThrownBy(() -> DaoHelpers.addSorts(query, allowedSortFields, primarySort, secondarySort))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThat(query)
                    .describedAs("should not change query if field now allowed")
                    .hasToString(BASE_QUERY);
        }

        @Test
        void shouldNotAddSortForDisallowedSecondarySortField() {
            var primarySort = KiwiSort.of("lastName", KiwiSort.Direction.DESC);
            var secondarySort = KiwiSort.of("salary", KiwiSort.Direction.DESC);

            assertThatThrownBy(() -> DaoHelpers.addSorts(query, allowedSortFields, primarySort, secondarySort))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThat(query)
                    .describedAs("should not change query if field now allowed")
                    .hasToString(BASE_QUERY);
        }

    }

    @Nested
    class AddSortsFromPrimaryAndSecondaryFields {

        // TODO
    }

    @Nested
    class AddSortsFromKiwiSortVarags {

        // TODO
    }
}
