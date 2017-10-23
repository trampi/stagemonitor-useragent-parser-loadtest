package org.stagemonitor.web.servlet.useragent;

import io.opentracing.Span;

public interface UAP {

	public void setUserAgentInformation(final Span span, final String userAgentHeader);

}
