package org.kiwiproject.beta.dao;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotBlank;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import com.google.common.annotations.Beta;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.kiwiproject.collect.KiwiArrays;
import org.kiwiproject.collect.KiwiLists;
import org.kiwiproject.spring.data.KiwiSort;
import org.kiwiproject.spring.data.KiwiSort.Direction;
import org.kiwiproject.spring.data.PagingRequest;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Some simple utilities useful to data access code that is building queries,
 * for example structured query languages such as SQL or HQL.
 * <p>
 * This is useful in situations where you have a relatively static query but
 * you need to add dynamically defined sorting/ordering.
 */
@Beta
@UtilityClass
@Slf4j
public class DaoHelpers {

    /**
     * Defines/restricts the values that can be used when generating the ordering clause.
     *
     * @implNote Currently this is very restrictive and will only work in certain languages
     * such as SQL or HQL.
     */
    private enum Connector {

        /**
         * Separates the "base" query from the order clause.
         */
        ORDER_BY(" order by "),

        /**
         * Separator to use between fields when there is more than one sort field.
         */
        SORT_FIELD_SEPARATOR(", "),

        /**
         * Separator to use between the sort field and sort direction.
         */
        SORT_DIRECTION_SEPARATOR(" ");

        final String value;

        Connector(String value) {
            this.value = value;
        }
    }

    /**
     * Add sorts to the query restricting by the {@link AllowedFields} for the
     * {@link PagingRequest}.
     */
    public static void addSorts(StringBuilder query,
                                AllowedFields allowedSortFields,
                                PagingRequest pagingRequest) {

        checkQueryNotNull(query);
        checkAllowedSortFieldsNotNull(allowedSortFields);
        checkArgumentNotNull(pagingRequest, "pagingRequest must not be null");

        var primarySortDirection = toKiwiSortDirectionOrNull(pagingRequest.getPrimaryDirection());
        var secondarySortDirection = toKiwiSortDirectionOrNull(pagingRequest.getSecondaryDirection());

        addSorts(query,
                allowedSortFields,
                pagingRequest.getPrimarySort(),
                primarySortDirection,
                pagingRequest.getSecondarySort(),
                secondarySortDirection);
    }

    private static KiwiSort.Direction toKiwiSortDirectionOrNull(Sort.@Nullable Direction sortDirection) {
        return isNull(sortDirection) ? null : toKiwiSortDirection(sortDirection);
    }

    private static KiwiSort.Direction toKiwiSortDirection(Sort.Direction sortDirection) {
        checkArgumentNotNull(sortDirection);

        return sortDirection.isAscending() ? Direction.ASC : Direction.DESC;
    }

    /**
     * Add a sort to the query restricting by the {@link AllowedFields} for the
     * {@link KiwiSort}.
     */
    public static void addSort(StringBuilder query,
                               AllowedFields allowedSortFields,
                               KiwiSort sort) {

        checkQueryNotNull(query);
        checkAllowedSortFieldsNotNull(allowedSortFields);
        checkArgumentNotNull(sort, "sort must not be null");

        var primarySortDirection = toKiwiSortDirection(sort);

        addSort(query, allowedSortFields, sort.getProperty(), primarySortDirection);
    }

    /**
     * Add a sort to the query restricting by the {@link AllowedFields}
     * for the sort field and direction.
     */
    public static void addSort(StringBuilder query,
                               AllowedFields allowedSortFields,
                               String sortField,
                               @Nullable Direction sortDirection) {

        addSorts(query, allowedSortFields, sortField, sortDirection, null, null);
    }

    /**
     * Add sorts to the query restricting by the {@link AllowedFields}
     * for the primary and secondary sort criteria.
     */
    public static void addSorts(StringBuilder query,
                                AllowedFields allowedSortFields,
                                KiwiSort primarySort,
                                KiwiSort secondarySort) {

        checkArgumentNotNull(primarySort, "primarySort must not be null");
        checkArgumentNotNull(secondarySort, "secondarySort must not be null");

        var primarySortDirection = toKiwiSortDirection(primarySort);
        var secondarySortDirection = toKiwiSortDirection(secondarySort);

        addSorts(query,
                allowedSortFields,
                primarySort.getProperty(),
                primarySortDirection,
                secondarySort.getProperty(),
                secondarySortDirection);
    }

    /**
     * Add sorts to the query restricting by the {@link AllowedFields}
     * for the primary and secondary sort fields and directions.
     * <p>
     * This allows for the possibility that there are no sort criteria, in
     * which case the query is not modified.
     *
     * @implNote If a secondary sort is specified but not a primary sort, then
     * a warning is logged, the secondary sort is ignored, and therefore
     * the query is not modified.
     */
    public static void addSorts(
            StringBuilder query,
            AllowedFields allowedSortFields,
            @Nullable String primarySortField,
            @Nullable Direction primarySortDirection,
            @Nullable String secondarySortField,
            @Nullable Direction secondarySortDirection) {

        checkQueryNotNull(query);
        checkAllowedSortFieldsNotNull(allowedSortFields);

        logWarningIfOnlySecondarySort(primarySortField, secondarySortField, secondarySortDirection);

        // Verify all sorts are valid before proceeeding
        if (isNotBlank(primarySortField)) {
            allowedSortFields.assertAllowed(primarySortField);
        }
        if (isNotBlank(secondarySortField)) {
            allowedSortFields.assertAllowed(secondarySortField);
        }

        if (isNotBlank(primarySortField)) {
            addSort(query,
                    allowedSortFields,
                    Connector.ORDER_BY,
                    primarySortField,
                    primarySortDirection);

            if (isNotBlank(secondarySortField)) {
                addSort(query,
                        allowedSortFields,
                        Connector.SORT_FIELD_SEPARATOR,
                        secondarySortField,
                        secondarySortDirection);
            }
        }
    }

    private static void logWarningIfOnlySecondarySort(String primarySortField,
                                                      String secondarySortField,
                                                      @Nullable Direction secondarySortDirection) {

        if (onlyContainsSecondarySort(primarySortField, secondarySortField)) {
            LOG.warn("A secondary sort ({} {}) was specified without a primary sort. Ignoring.",
                    secondarySortField, secondarySortDirection);
        }
    }

    private static boolean onlyContainsSecondarySort(
            @Nullable String primarySortField,
            @Nullable String secondarySortField) {

        return isBlank(primarySortField) && isNotBlank(secondarySortField);
    }

    /**
     * Adds sorts to the query restricting by the {@link AllowedFields}
     * for all the specified sorts.
     * <p>
     * This allows for the possibility that there are no sort criteria, in
     * which case the query is not modified.
     *
     * @implNote Any null values in the {@code sorts} array are filtered out
     */
    public static void addSorts(StringBuilder query,
                                AllowedFields allowedSortFields,
                                KiwiSort... sorts) {

        checkQueryNotNull(query);
        checkAllowedSortFieldsNotNull(allowedSortFields);
        checkArgumentNotNull(sorts, "sorts (varargs) must not be null");

        if (KiwiArrays.isNullOrEmpty(sorts)) {
            return;
        }

        addSorts(query, allowedSortFields, Arrays.asList(sorts));
    }

    /**
     * Adds sorts to the query restricting by the {@link AllowedFields}
     * for all the specified sorts.
     * <p>
     * This allows for the possibility that there are no sort criteria, in
     * which case the query is not modified.
     *
     * @implNote Any null values in the {@code sorts} list are filtered out
     */
    public static void addSorts(StringBuilder query,
                                AllowedFields allowedSortFields,
                                List<KiwiSort> sorts) {

        checkQueryNotNull(query);
        checkAllowedSortFieldsNotNull(allowedSortFields);
        checkArgumentNotNull(sorts, "sorts must not be null");

        if (KiwiLists.isNullOrEmpty(sorts)) {
            return;
        }

        var nonNullSorts = sorts.stream().filter(Objects::nonNull).collect(toList());

        // Verify all sorts are valid before proceeding
        nonNullSorts.forEach(sort -> allowedSortFields.assertAllowed(sort.getProperty()));

        var firstSort = KiwiLists.first(nonNullSorts);
        addSort(query,
                allowedSortFields,
                Connector.ORDER_BY,
                firstSort.getProperty(),
                toKiwiSortDirection(firstSort));

        var remainingSorts = KiwiLists.subListExcludingFirst(nonNullSorts);
        remainingSorts.forEach(sort -> addSort(query,
                allowedSortFields,
                Connector.SORT_FIELD_SEPARATOR,
                sort.getProperty(),
                toKiwiSortDirection(sort)));
    }

    /**
     * @implNote Eventually KiwiSort should have a method to directly obtain the Direction object.
     * See the proposed KiwiSort feature <a href="https://github.com/kiwiproject/kiwi/discussions/707">here</a>.
     * For now, we need to convert it manually from a String, and KiwiSort should never have a
     * null/blank value returned by getDirection() thus the state check below. Also, the value
     * returned from getDirection() should always be uppercase but be conservative and ensure it is.
     */
    private static KiwiSort.Direction toKiwiSortDirection(KiwiSort sort) {
        checkArgumentNotNull(sort);
        checkState(isNotBlank(sort.getDirection()), "KiwiSort has a blank direction");

        return KiwiSort.Direction.valueOf(sort.getDirection().toUpperCase(Locale.US));
    }

    private static void addSort(StringBuilder query,
                                AllowedFields allowedSortFields,
                                Connector connector,
                                String sortField,
                                KiwiSort.@Nullable Direction sortDirection) {

        checkQueryNotNull(query);
        checkAllowedSortFieldsNotNull(allowedSortFields);
        checkArgumentNotNull(connector, "connector must not be blank");
        checkArgumentNotBlank(sortField, "sortField must not be blank");

        allowedSortFields.assertAllowed(sortField);

        query.append(connector.value)
                .append(allowedSortFields.getPrefixedFieldName(sortField))
                .append(Connector.SORT_DIRECTION_SEPARATOR.value)
                .append(Optional.ofNullable(sortDirection).orElse(KiwiSort.Direction.ASC));
    }

    private static void checkQueryNotNull(StringBuilder query) {
        checkArgumentNotNull(query, "query must not be null");
    }

    private static void checkAllowedSortFieldsNotNull(AllowedFields allowedSortFields) {
        checkArgumentNotNull(allowedSortFields, "allowedSortFields must not be null");
    }
}
