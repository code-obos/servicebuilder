package no.obos.util.servicebuilder.exception;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import no.obos.util.servicebuilder.model.LogLevel;
import no.obos.util.servicebuilder.model.HttpProblem;
import no.obos.util.servicebuilder.util.FormatUtil;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

@Slf4j
public class ExternalResourceExceptionMapper implements ExceptionMapper<ExternalResourceException> {
    final private ExceptionUtil exceptionUtil;

    private final static ImmutableSet<String> SKIP_REQUEST_HEADERS = ImmutableSet.of();
    private final static ImmutableSet<String> SKIP_RESPONSE_HEADERS = ImmutableSet.of();
    private final static String INDENTATION = "  ";

    @Inject
    public ExternalResourceExceptionMapper(ExceptionUtil exceptionUtil) {
        this.exceptionUtil = exceptionUtil;
    }

    @Override
    public Response toResponse(ExternalResourceException exception) {
        String detail;
        ExternalResourceException.MetaData meta = exception.getMetaData();
        if (meta.gotAnswer) {
            detail = "Feil under kommunikasjon med " + meta.targetName;
        } else {
            detail = "Kan ikke nå " + meta.targetName;
        }
        final String referenceId = (meta.httpResponseMetaData != null
                && Strings.isNullOrEmpty(meta.httpResponseMetaData.incidentReferenceId))
                ? meta.httpResponseMetaData.incidentReferenceId
                : null;
        List<String> metaLines = FormatUtil.indentLines(metaDataToLogLines(meta), INDENTATION);
        return exceptionUtil.handle(exception, cfg -> cfg
                .status(INTERNAL_SERVER_ERROR.getStatusCode())
                .logLevel(LogLevel.ERROR)
                .detail(detail)
                .reference(referenceId)
                .internalMessage(Joiner.on('\n').join(metaLines))
                .logger(log)
        );
    }

    public List<String> metaDataToLogLines(ExternalResourceException.MetaData meta) {
        List<String> lines = Lists.newArrayList();
        lines.add("Target: " + meta.targetName);
        if (meta.httpRequestMetaData != null) {
            lines.add("HttpRequest:");
            lines.addAll(FormatUtil.indentLines(httpRequestMetaToLogLines(meta.httpRequestMetaData), INDENTATION));
        }
        if (meta.httpResponseMetaData != null) {
            lines.add("HttpResponse:");
            lines.addAll(FormatUtil.indentLines(httpResponseMetaToLogLines(meta.httpResponseMetaData), INDENTATION));
        }

        return lines;
    }

    public List<String> httpRequestMetaToLogLines(ExternalResourceException.HttpRequestMetaData meta) {
        List<String> lines = Lists.newArrayList();
        lines.add("Url: " + meta.url);
        List<String> filteredHeaders =
                FormatUtil.stringMapAsIndentedLines(meta.headers, SKIP_REQUEST_HEADERS, INDENTATION);
        if (!filteredHeaders.isEmpty()) {
            lines.add("Headers:");
            lines.addAll(filteredHeaders);
        }
        return lines;
    }

    public List<String> httpResponseMetaToLogLines(ExternalResourceException.HttpResponseMetaData meta) {
        List<String> lines = Lists.newArrayList();

        lines.add("Status: " + meta.status);

        if (meta.httpProblem != null) {
            lines.add("HttpProblem:");
            List<String> httpProblemLines = httpProblemToLogLines(meta.httpProblem);
            lines.addAll(FormatUtil.indentLines(httpProblemLines, INDENTATION));
        } else if (!Strings.isNullOrEmpty(meta.response)) {
            lines.add("Body:");
            List<String> bodyLines = Splitter.on("\n").splitToList(meta.response);
            lines.addAll(FormatUtil.indentLines(bodyLines, INDENTATION));
        } else {
            lines.add("empty response");
        }

        List<String> headers = FormatUtil.stringMapAsIndentedLines(meta.headers, SKIP_RESPONSE_HEADERS, INDENTATION);
        if (!headers.isEmpty()) {
            lines.add("Headers:");
            lines.addAll(headers);
        }
        return lines;
    }

    private List<String> httpProblemToLogLines(HttpProblem problem) {
        List<String> lines = Lists.newArrayList();

        if (Strings.isNullOrEmpty(problem.type) && !"about:blank".equals(problem.type)) {
            lines.add("Type: " + problem.type);
        }
        lines.add("Detail: " + problem.detail);
        lines.add("Title: " + problem.title);
        Map<String, String> context = problem.getContext();
        if (context != null && !context.isEmpty()) {
            lines.add("Context: " + problem.title);
            lines.addAll(FormatUtil.stringMapAsIndentedLines(context, Sets.newHashSet(), INDENTATION));
        }
        return lines;
    }

}
