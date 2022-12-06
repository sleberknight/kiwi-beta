package org.kiwiproject.beta.xml.ws.soap;

import static java.util.Collections.emptyIterator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.kiwiproject.test.junit.jupiter.ClearBoxTest;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.List;

import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.soap.SOAPFaultException;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@DisplayName("SOAPFaults")
@Slf4j
class SOAPFaultsTest {

    @Test
    void shouldNotLogWhenThrowableIsNull() {
        assertThat(SOAPFaults.logSoapFaultIfPresent("test", null, LOG)).isFalse();
    }

    @Test
    void shouldNotLogWhenThrowableDoesNotContainSOAPFaultException() {
        var ioEx = new UncheckedIOException(new IOException("An I/O error"));

        assertThat(SOAPFaults.logSoapFaultIfPresent("test", ioEx, LOG)).isFalse();
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldAcceptBlankContext(String context) {
        var exception = new SOAPFaultException(mockSoap_1_1_Fault());

        assertThat(SOAPFaults.logSoapFaultIfPresent(context, exception, LOG)).isTrue();
    }

    @Test
    void shouldLogWhenThrowableContainsSOAPFaultException() {
        var fault = mockSoap_1_1_Fault();
        var exception = new SOAPFaultException(fault);
        var wrappingException = new RuntimeException("SOAP wrapper", exception);

        assertThat(SOAPFaults.logSoapFaultIfPresent("test", wrappingException, LOG)).isTrue();
    }

    @Test
    void shouldLogWhenThrowableContainsSOAPFaultException_WithNullDetail() {
        var fault = mockSoap_1_1_Fault(null);
        var exception = new SOAPFaultException(fault);
        var wrappingException = new RuntimeException("SOAP wrapper", exception);

        assertThat(SOAPFaults.logSoapFaultIfPresent("test", wrappingException, LOG)).isTrue();
    }

    @Test
    void shouldLogWhenFaultContainsReasonTexts() {
        var fault = mockSoap_1_2_Fault();
        var exception = new SOAPFaultException(fault);
        var wrappingException = new RuntimeException("SOAP wrapper", exception);

        assertThat(SOAPFaults.logSoapFaultIfPresent("test", wrappingException, LOG)).isTrue();
    }

    @Test
    void shouldNotAllowUnexpectedExceptionsToEscape() {
        var fault = mock(SOAPFault.class);
        when(fault.getFaultActor()).thenThrow(new RuntimeException("well, this is unexpected"));

        assertThatCode(() -> SOAPFaults.logSoapFault("test", fault, LOG)).doesNotThrowAnyException();;
    }

    @ClearBoxTest
    void shouldSuppressSOAPExceptionsWhenGettingReasonTexts() throws SOAPException {
        var fault = mock(SOAPFault.class);
        when(fault.getFaultReasonTexts()).thenThrow(new SOAPException("error getting reason texts"));

        var errorText = "Error getting ReasonText items";
        assertThat(SOAPFaults.getReasonTexts(fault, errorText)).containsExactly(errorText);
    }

    @ClearBoxTest
    void shouldSuppressUnexpectedExceptionGettingDetail() {
        var fault = mock(SOAPFault.class);
        when(fault.getDetail()).thenThrow(new RuntimeException("unexpected error getting Detail"));

        var errorText = "Error getting Detail";
        assertThat(SOAPFaults.getDetailsAsStrings(fault, errorText)).containsExactly(errorText);
    }

    @SneakyThrows
    private static SOAPFault mockSoap_1_1_Fault() {
        var mockSoapDetail = mockSoapDetail();
        return mockSoap_1_1_Fault(mockSoapDetail);
    }

    @SneakyThrows
    private static SOAPFault mockSoap_1_1_Fault(Detail detail) {
        var fault = mock(SOAPFault.class);

        when(fault.getFaultCode()).thenReturn("SOAP-Env:Server");
        when(fault.getFaultString()).thenReturn("Something that we didn't want to happen, did happen. Sorry.");
        when(fault.getFaultActor()).thenReturn(null);
        when(fault.getFaultRole()).thenThrow(new UnsupportedOperationException("faultRole not supported"));
        when(fault.getFaultReasonTexts())
                .thenThrow(new UnsupportedOperationException("faultReasonTexts not supported"));
        when(fault.getFaultSubcodes()).thenThrow(new UnsupportedOperationException("faultSubcodes not supported"));
        when(fault.getDetail()).thenReturn(detail);

        return fault;
    }

    @SneakyThrows
    private static SOAPFault mockSoap_1_2_Fault() {
        var mockSoapDetail = mockSoapDetail();
        return mockSoap_1_2_Fault(mockSoapDetail);
    }

    @SneakyThrows
    private static SOAPFault mockSoap_1_2_Fault(Detail detail) {
        var fault = mock(SOAPFault.class);

        when(fault.getFaultCode()).thenReturn("SOAP-Env:Server");
        when(fault.getFaultString()).thenReturn("Something that we didn't want to happen, did happen. Sorry.");
        when(fault.getFaultActor()).thenReturn("The Role");
        when(fault.getFaultRole()).thenReturn("The Role");
        when(fault.getFaultReasonTexts()).thenReturn(List.of("reason-1", "reason-2").iterator());
        when(fault.getFaultSubcodes()).thenReturn(emptyIterator());
        when(fault.getDetail()).thenReturn(detail);

        return fault;
    }

    private static Detail mockSoapDetail() {
        var detail = mock(Detail.class);

        var detailEntryIterator = mockSoapDetailEntries();
        when(detail.getDetailEntries()).thenReturn(detailEntryIterator);

        return detail;
    }

    private static Iterator<DetailEntry> mockSoapDetailEntries() {
        var entry1 = mock(DetailEntry.class);
        when(entry1.getNodeName()).thenReturn("detail-1");
        when(entry1.getValue()).thenReturn("value of detail-1");

        var entry2 = mock(DetailEntry.class);
        when(entry2.getNodeName()).thenReturn("detail-2");
        when(entry2.getValue()).thenReturn("value of detail-2");

        return List.of(entry1, entry2).iterator();
    }
}
