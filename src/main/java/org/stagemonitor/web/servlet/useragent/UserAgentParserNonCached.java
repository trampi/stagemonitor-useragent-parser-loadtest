package org.stagemonitor.web.servlet.useragent;

import net.sf.uadetector.ReadableUserAgent;
import net.sf.uadetector.UserAgentStringParser;
import net.sf.uadetector.service.UADetectorServiceFactory;

import java.util.LinkedHashMap;
import java.util.Map;

import io.opentracing.Span;

/**
 * The uadetector library is discontinued as the underlying database is now commercial.
 * <p>
 * Consider using the Elasticsearch ingest user agent plugin: https://www.elastic.co/guide/en/elasticsearch/plugins/master/ingest-user-agent.html
 */
@Deprecated
public class UserAgentParserNonCached implements UAP {

	// prevents reDOS attacks like described in https://github.com/before/uadetector/issues/130
	private static final int MAX_USERAGENT_LENGTH = 256;
	private final UserAgentStringParser parser;

	public UserAgentParserNonCached() {
		this(UADetectorServiceFactory.getResourceModuleParser());
	}

	public UserAgentParserNonCached(UserAgentStringParser parser) {
		this.parser = parser;
	}

	public void setUserAgentInformation(final Span span, final String userAgentHeader) {
		if (userAgentHeader != null && userAgentHeader.length() < MAX_USERAGENT_LENGTH) {
			final ReadableUserAgent userAgent = parser.parse(userAgentHeader);
			span.setTag("user_agent.type", userAgent.getTypeName());
			span.setTag("user_agent.device", userAgent.getDeviceCategory().getName());
			span.setTag("user_agent.os", userAgent.getOperatingSystem().getName());
			span.setTag("user_agent.os_family", userAgent.getOperatingSystem().getFamilyName());
			span.setTag("user_agent.os_version", userAgent.getOperatingSystem().getVersionNumber().toVersionString());
			span.setTag("user_agent.browser", userAgent.getName());
			span.setTag("user_agent.browser_version", userAgent.getVersionNumber().toVersionString());
		}
	}

}
