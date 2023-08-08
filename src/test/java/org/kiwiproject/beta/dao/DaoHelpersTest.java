package org.kiwiproject.beta.dao;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.kiwiproject.base.KiwiStrings.f;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.jupiter.cartesian.CartesianTest;
import org.junitpioneer.jupiter.cartesian.CartesianTest.Enum;
import org.kiwiproject.base.KiwiStrings;
import org.kiwiproject.spring.data.KiwiSort;
import org.kiwiproject.spring.data.PagingRequest;
import org.kiwiproject.test.junit.jupiter.params.provider.AsciiOnlyBlankStringSource;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.List;

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
        void shouldAddPrimarySortAscending() {
            pagingRequest.setPrimarySort("lastName");
            pagingRequest.setPrimaryDirection(Sort.Direction.ASC);

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

        @Test
        void shouldNotAddSortForDisallowedSecondaryField() {
            pagingRequest.setPrimarySort("lastName");
            pagingRequest.setSecondarySort("someOtherField");

            assertThatThrownBy(() -> DaoHelpers.addSorts(query, allowedSortFields, pagingRequest))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThat(query)
                    .describedAs("should not change query if field now allowed")
                    .hasToString(BASE_QUERY);
        }
    }

    @Nested
    class AddSortFromSingleKiwiSort {

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

        @ParameterizedTest
        @EnumSource(KiwiSort.Direction.class)
        void shouldAddPrimarySort(KiwiSort.Direction sortDirection) {
            DaoHelpers.addSorts(query, allowedSortFields, "lastName", sortDirection, null, null);

            assertThat(query).hasToString(BASE_QUERY + " order by lastName " + sortDirection.name());
        }

        @CartesianTest
        void shouldAddPrimaryAndSecondarySorts(
                @Enum KiwiSort.Direction primaryDirection,
                @Enum KiwiSort.Direction secondaryDirection) {

            DaoHelpers.addSorts(query, allowedSortFields,
                    "firstName", primaryDirection,
                    "lastName", secondaryDirection);

            var expectedQuery = f(BASE_QUERY + " order by firstName {}, lastName {}",
                    primaryDirection, secondaryDirection);
            assertThat(query).hasToString(expectedQuery);
        }

        @Test
        void shouldDefaultToAscendingOrder() {
            DaoHelpers.addSorts(query, allowedSortFields,
                    "lastName", null,
                    "age", null);

            assertThat(query).hasToString(BASE_QUERY + " order by lastName ASC, age ASC");
        }

        @ParameterizedTest
        @AsciiOnlyBlankStringSource
        void shouldDoNothingWhenNoSortsDefined(String blankValue) {
            DaoHelpers.addSorts(query, allowedSortFields, blankValue, null, blankValue, null);

            assertThat(query).hasToString(BASE_QUERY);
        }

        @ParameterizedTest
        @ValueSource(strings = {"salary", "ssn"})
        void shouldNotAddSortForDisallowedPrimarySortField(String primarySortField) {
            assertThatThrownBy(() -> DaoHelpers.addSorts(query, allowedSortFields,
                    primarySortField, KiwiSort.Direction.DESC,
                    "firstName", KiwiSort.Direction.ASC))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThat(query)
                    .describedAs("should not change query if field now allowed")
                    .hasToString(BASE_QUERY);
        }

        @ParameterizedTest
        @ValueSource(strings = {"salary", "nickname"})
        void shouldNotAddSortForDisallowedSecondarySortField(String secondarySortField) {
            assertThatThrownBy(() -> DaoHelpers.addSorts(query, allowedSortFields,
                    "lastName", KiwiSort.Direction.DESC,
                    secondarySortField, KiwiSort.Direction.ASC))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThat(query)
                    .describedAs("should not change query if field now allowed")
                    .hasToString(BASE_QUERY);
        }

        @Test
        void shouldDoNothingIfOnlySecondarySortSpecified() {
            DaoHelpers.addSorts(query, allowedSortFields,
                    null, null,
                    "age", KiwiSort.Direction.DESC);

            assertThat(query).hasToString(BASE_QUERY);
        }
    }

    @Nested
    class AddSortsFromKiwiSortVarargs {

        @ParameterizedTest
        @EnumSource(KiwiSort.Direction.class)
        void shouldAddSingleSort(KiwiSort.Direction sortDirection) {
            var sort = KiwiSort.of("age", sortDirection);

            DaoHelpers.addSorts(query, allowedSortFields, sort);

            assertThat(query).hasToString(BASE_QUERY + " order by age " + sortDirection);
        }

        @Test
        void shouldAddTwoSorts() {
            var sort1 = KiwiSort.of("lastName", KiwiSort.Direction.DESC);
            var sort2 = KiwiSort.of("age", KiwiSort.Direction.ASC);

            DaoHelpers.addSorts(query, allowedSortFields, sort1, sort2);

            assertThat(query).hasToString(BASE_QUERY + " order by lastName DESC, age ASC");
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "lastName, firstName",
                "lastName, age, firstName",
                "updatedAt, createdAt, lastName",
                "age, lastName, firstName, nickname",
                "lastName, firstName, age, email"
        })
        void shouldAddManySorts(String sortFieldsCsv) {
            allowedSortFields = AllowedFields.of(
                    "firstName", "lastName", "nickname", "age", "email", "createdAt", "updatedAt");

            var sortFields = KiwiStrings.splitOnCommas(sortFieldsCsv);

            var sorts = sortFields
                    .stream()
                    .map(sortField -> KiwiSort.of(sortField, KiwiSort.Direction.ASC))
                    .toList()
                    .toArray(KiwiSort[]::new);

            DaoHelpers.addSorts(query, allowedSortFields, sorts);

            var expectedOrderByClause = sortFields
                    .stream()
                    .map(sortField -> sortField + " ASC")
                    .collect(joining(", "));

            assertThat(query).hasToString(BASE_QUERY + " order by " + expectedOrderByClause);
        }

        @Test
        void shouldIgnoreAnyNullKiwiSortsInVarargs() {
            var sort1 = KiwiSort.of("age", KiwiSort.Direction.DESC);
            var sort2 = KiwiSort.of("lastName", KiwiSort.Direction.ASC);

            DaoHelpers.addSorts(query, allowedSortFields, null, sort1, null, sort2, null);

            assertThat(query).hasToString(BASE_QUERY + " order by age DESC, lastName ASC");
        }

        @Test
        void shouldIgnoreAnyNullKiwiSortsInCollection() {
            var sort1 = KiwiSort.of("age", KiwiSort.Direction.DESC);
            var sort2 = KiwiSort.of("lastName", KiwiSort.Direction.ASC);

            DaoHelpers.addSorts(query, allowedSortFields, Arrays.asList(null, sort1, null, sort2, null));

            assertThat(query).hasToString(BASE_QUERY + " order by age DESC, lastName ASC");
        }

        // salary is the illegal field
        @ParameterizedTest
        @CsvSource({
                "salary, lastName, firstName",
                "lastName, salary, age",
                "age, lastName, salary"
        })
        void shouldNotAddSortForAnyDisallowedSortFields(
                String sortField1,
                String sortField2,
                String sortField3) {

            var sort1 = KiwiSort.of(sortField1, KiwiSort.Direction.ASC);
            var sort2 = KiwiSort.of(sortField2, KiwiSort.Direction.ASC);
            var sort3 = KiwiSort.of(sortField3, KiwiSort.Direction.ASC);

            assertThatThrownBy(() -> DaoHelpers.addSorts(query, allowedSortFields, sort1, sort2, sort3))
                    .isExactlyInstanceOf(IllegalArgumentException.class)
                    .hasMessage("salary is not allowed");
        }

        @Test
        void shouldDoNothingWhenNoSortsDefinedInVarargs() {
            DaoHelpers.addSorts(query, allowedSortFields);

            assertThat(query).hasToString(BASE_QUERY);
        }

        @Test
        void shouldDoNothingWhenNoSortsDefinedInCollection() {
            DaoHelpers.addSorts(query, allowedSortFields, List.of());

            assertThat(query).hasToString(BASE_QUERY);
        }
    }
}
