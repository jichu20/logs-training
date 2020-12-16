package com.jichu20.commons.interceptor;

import java.io.IOException;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

public class RequestResponseLoggingInterceptor implements ClientHttpRequestInterceptor {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
		logRequest(request, body);
		ClientHttpResponse response = execution.execute(request, body);
		logResponse(response);
		return response;
	}

	private void logRequest(HttpRequest request, byte[] body) throws IOException {
		if (log.isInfoEnabled()) {
			StringBuffer sb = new StringBuffer(
					String.format("===========================request begin================================================%n"));
			sb.append(String.format("URI         : %s%n", request.getURI()));
			sb.append(String.format("Method      : %s%n", request.getMethod()));
			sb.append(String.format("Headers     : %s%n", request.getHeaders()));

			MediaType contentType = request.getHeaders().getContentType();
			if ((MediaType.TEXT_PLAIN.equals(contentType) || MediaType.APPLICATION_JSON.equals(contentType)) && body != null
					&& body.length > 0) {
				sb.append(String.format("Request body: %s%n", new String(body, "UTF-8")));
			}
			sb.append("==========================request end================================================");
			log.info(sb.toString());
		}
	}

	private void logResponse(ClientHttpResponse response) throws IOException {
		if (log.isInfoEnabled()) {
			StringBuffer sb = new StringBuffer(
					String.format("============================response begin==========================================%n"));
			sb.append(String.format("Status code  : %s%n", response.getStatusCode()));
			sb.append(String.format("Status text  : %s%n", response.getStatusText()));
			sb.append(String.format("Headers      : %s%n", response.getHeaders()));

			MediaType contentType = response.getHeaders().getContentType();
			boolean hasContentDisposition = response.getHeaders().containsKey(HttpHeaders.CONTENT_DISPOSITION);
			if ((MediaType.TEXT_PLAIN.equals(contentType) || MediaType.APPLICATION_JSON.equals(contentType)) && response.getBody() != null
					&& !hasContentDisposition) {
				sb.append(String.format("Response body: %s", StreamUtils.copyToString(response.getBody(), Charset.defaultCharset())));
			}
			sb.append("=======================response end=================================================");
			log.info(sb.toString());
		}
	}
}
