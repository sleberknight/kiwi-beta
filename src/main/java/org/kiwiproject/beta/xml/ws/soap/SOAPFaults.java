package org.kiwiproject.beta.xml.ws.soap;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;
import static org.kiwiproject.collect.KiwiMaps.newLinkedHashMap;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Streams;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.soap.SOAPFaultException;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Utilities related to {@link SOAPFault} and {@link SOAPFaultException}.
 */
@UtilityClass
@Slf4j
public class SOAPFaults {

    /**
     * Indicates an unsupported SOAP feature.
     */
    public static final String UNSUPPORTED = "UNSUPPORTED";

    /**
     * If the given throwable contains a {@link SOAPFaultException}, log detailed information about it.
     *
     * @param context a description that will be included in the log message, for easier traceability
     * @param throwable the {@link Throwable} to check
     * @param logger the SLF4J {@link Logger} to use for logging
     * @return true if the throwable contained a {@link SOAPFaultException}, false otherwise
     */
    public static boolean logSoapFaultIfPresent(String context, @Nullable Throwable throwable, Logger logger) {
        var contextOrUnspecified = contextOrUnspecified(context);
        try {
            var index = ExceptionUtils.indexOfType(throwable, SOAPFaultException.class);
            if (index == -1) {
                return false;
            }

            var throwables = ExceptionUtils.getThrowables(throwable);
            var soapFaultException = (SOAPFaultException) throwables[index];

            logSoapFault(contextOrUnspecified, soapFaultException, logger);
        } catch (Exception e) {
            LOG.error("[{}] Error logging information about SOAPFault", contextOrUnspecified, e);
        }

        return true;
    }

    public static void logSoapFault(String context, SOAPFaultException exception, Logger logger) {
        checkArgumentNotNull(exception);
        logSoapFault(context, exception.getFault(), logger);
    }

    public static void logSoapFault(String context, SOAPFault fault, Logger logger) {
        checkArgumentNotNull(fault);
        checkArgumentNotNull(logger);

        var contextOrUnspecified = contextOrUnspecified(context);
        try {
            var faultInfo = soapFaultAsMap(fault);
            logger.error("[{}] SOAPFault: {}", contextOrUnspecified, faultInfo);
        } catch (Exception e) {
            LOG.error("[{}] Error logging information about SOAPFault", contextOrUnspecified, e);
        }
    }

    private static String contextOrUnspecified(String context) {
        return isBlank(context) ? "unspecified" : context;
    }

    public static Map<String, Object> soapFaultAsMap(SOAPFault fault) {
        checkArgumentNotNull(fault);

        var faultCode = fault.getFaultCode();
        var faultString = fault.getFaultString();
        var faultActor = fault.getFaultActor();
        var faultRole = getFaultRole(fault);
        var faultReasonTexts = getReasonTexts(fault, "[Unable to get faultReasonTexts]");
        var faultSubcodes = getFaultSubcodes(fault);
        var details = getDetailsAsStrings(fault, "[Unable to get fault Detail information]");

        return newLinkedHashMap(
                "faultCode", faultCode,
                "faultString", faultString,
                "faultActor", faultActor,
                "faultRole", faultRole,
                "faultReasonTexts", faultReasonTexts,
                "faultSubcodes", faultSubcodes,
                "details", details
        );
    }

    private static String getFaultRole(SOAPFault fault) {
        try {
            return fault.getFaultRole();
        } catch (UnsupportedOperationException unsupportedEx) {
            LOG.debug("faultRole is not supported");
            LOG.trace("faultRole unsupported stack trace:", unsupportedEx);
            return UNSUPPORTED;
        }
    }

    @VisibleForTesting
    static List<String> getReasonTexts(SOAPFault fault, String errorText) {
        try {
            Iterator<String> faultReasonTexts = fault.getFaultReasonTexts();

            return Streams.stream(faultReasonTexts).collect(toList());
        } catch (UnsupportedOperationException unsupportedEx) {
            LOG.debug("faultReasonTexts is not supported");
            LOG.trace("faultReasonTexts unsupported stack trace:", unsupportedEx);
            return List.of(UNSUPPORTED);
        } catch (Exception e) {
            LOG.error("Error getting faultReasonTexts", e);
            return List.of(errorText);
        }
    }

    @VisibleForTesting
    static List<String> getFaultSubcodes(SOAPFault fault) {
        try {
            return Streams.stream(fault.getFaultSubcodes())
                    .map(QName::toString)
                    .collect(toList());
        } catch (UnsupportedOperationException unsupportedEx) {
            LOG.debug("faultSubcodes is not supported");
            LOG.trace("faultSubcodes unsupported stack trace:", unsupportedEx);
            return List.of(UNSUPPORTED);
        }
    }

    @VisibleForTesting
    static List<String> getDetailsAsStrings(SOAPFault fault, String errorText) {
        try {
            var detail = fault.getDetail();
            if (isNull(detail)) {
                return List.of();
            }

            Iterator<DetailEntry> detailEntries = detail.getDetailEntries();

            return Streams.stream(detailEntries)
                    .map(detailEntry -> {
                        var nodeName = detailEntry.getNodeName();
                        var nodeValue = detailEntry.getNodeValue();
                        return nodeName + " = " + nodeValue;
                    })
                    .collect(toList());

        } catch (Exception e) {
            LOG.warn("Error getting fault Detail information", e);
            return List.of(errorText);
        }
    }
}
