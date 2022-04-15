package org.kiwiproject.beta.dao;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotBlank;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

import com.google.common.annotations.Beta;

import org.kiwiproject.collect.KiwiArrays;
import org.kiwiproject.collect.KiwiLists;
import org.kiwiproject.spring.data.KiwiSort;
import org.kiwiproject.spring.data.KiwiSort.Direction;
import org.kiwiproject.spring.data.PagingRequest;
import org.springframework.data.domain.Sort;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

// TODO Allow caller to define the 'connector', by default it is SQL 'order by'?
// TODO Overloads that accept a String query and returns a new String? Would add lots of methods...
// TODO Add "quiet" methods that are no-ops if given any disallowed sort fields?
//  i.e. don't throw exception, but don't modify query, maybe just log warning
// TODO Option to NOT have AllowedFields? Do we really want to even allow no security?
// TODO Add "parameter object with builder" to allow building what you want to sort?
//  or maybe have the whole thing be a builder, which would allow you to specify
//  either a StringBuilder or a String for the query, and then the builder would have
//  to be "smart" and branch such that the terminal method is either void or returns
//  a String depending on whether the builder received a StringBuilder or String for
//  the query.

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

    private static final String CONNECTOR_ORDER_BY = " order by ";
    private static final String CONNECTOR_COMMA = ", ";

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

    private static KiwiSort.Direction toKiwiSortDirectionOrNull(@Nullable Sort.Direction sortDirection) {
        if (isNull(sortDirection)) {
            return null;
        } else {
            return sortDirection.isAscending() ? Direction.ASC : Direction.DESC;
        }
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

        var primarySortDirection = toKiwiSortDirectionOrNull(sort.getDirection());

        addSort(query, allowedSortFields, sort.getProperty(), primarySortDirection);
    }

    /**
     * Add a sort to the query restricting by the {@link AllowedFields}
     * for the sort field and direction.
     */
    public static void addSort(StringBuilder query,
            AllowedFields allowedSortFields,
            String sortField,
            @Nullable KiwiSort.Direction sortDirection) {

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

        var primarySortDirection = toKiwiSortDirectionOrNull(primarySort.getDirection());
        var secondarySortDirection = toKiwiSortDirectionOrNull(secondarySort.getDirection());

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
     *           a warning is logged, the secondary sort is ignored, and therefore
     *           the query is not modified.
     */
    public static void addSorts(
            StringBuilder query,
            AllowedFields allowedSortFields,
            @Nullable String primarySortField,
            @Nullable KiwiSort.Direction primarySortDirection,
            @Nullable String secondarySortField,
            @Nullable KiwiSort.Direction secondarySortDirection) {

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
                    CONNECTOR_ORDER_BY,
                    primarySortField,
                    primarySortDirection);

            if (isNotBlank(secondarySortField)) {
                addSort(query,
                        allowedSortFields,
                        CONNECTOR_COMMA,
                        secondarySortField,
                        secondarySortDirection);
            }
        }
    }

    private static void logWarningIfOnlySecondarySort(String primarySortField,
            String secondarySortField,
            @Nullable KiwiSort.Direction secondarySortDirection) {

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

        // Verify all sorts are valid before proceeeding
        nonNullSorts.stream().forEach(sort -> allowedSortFields.assertAllowed(sort.getProperty()));

        var firstSort = KiwiLists.first(nonNullSorts);
        addSort(query,
                allowedSortFields,
                CONNECTOR_ORDER_BY,
                firstSort.getProperty(),
                toKiwiSortDirectionOrNull(firstSort.getDirection()));

        var remainingSorts = KiwiLists.subListExcludingFirst(nonNullSorts);
        remainingSorts.forEach(sort -> addSort(query,
                allowedSortFields,
                CONNECTOR_COMMA,
                sort.getProperty(),
                toKiwiSortDirectionOrNull(sort.getDirection())));
    }

    private static KiwiSort.Direction toKiwiSortDirectionOrNull(String sortDirection) {
        if (isBlank(sortDirection)) {
            return null;
        }

        return KiwiSort.Direction.valueOf(sortDirection.toUpperCase(Locale.US));
    }

    // TODO Is there any (good) reason not to make this public?
    // Maybe security reasons on connector?
    // Could make connector an enum containing only 'order by', comma, etc.
    private static void addSort(StringBuilder query,
            AllowedFields allowedSortFields,
            String connector,
            String sortField,
            @Nullable KiwiSort.Direction sortDirection) {

        checkQueryNotNull(query);
        checkAllowedSortFieldsNotNull(allowedSortFields);
        checkArgumentNotBlank(connector, "connector must not be blank");
        checkArgumentNotBlank(sortField, "sortField must not be blank");

        allowedSortFields.assertAllowed(sortField);

        query.append(connector)
                .append(allowedSortFields.getPrefixedFieldName(sortField))
                .append(" ")
                .append(Optional.ofNullable(sortDirection).orElse(KiwiSort.Direction.ASC));
    }

    private static void checkQueryNotNull(StringBuilder query) {
        checkArgumentNotNull(query, "query must not be null");
    }

    private static void checkAllowedSortFieldsNotNull(AllowedFields allowedSortFields) {
        checkArgumentNotNull(allowedSortFields, "allowedSortFields must not be null");
    }
}
